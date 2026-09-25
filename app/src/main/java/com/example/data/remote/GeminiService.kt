package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.ConceptAnalysisResult
import com.example.data.model.ExplanationMode
import com.example.data.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    var userCustomApiKey: String = ""

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(): String {
        return when {
            userCustomApiKey.isNotBlank() -> userCustomApiKey.trim()
            try { BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" } catch (_: Throwable) { false } -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }
    }

    suspend fun queryGemini(
        prompt: String,
        systemInstruction: String? = null,
        imageBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            throw IllegalStateException("API_KEY_NOT_CONFIGURED")
        }

        val jsonRequest = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()

        // Text Part
        val textPart = JSONObject()
        textPart.put("text", prompt)
        parts.put(textPart)

        // Image Part if present
        if (imageBitmap != null) {
            val imagePart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", bitmapToBase64(imageBitmap))
            imagePart.put("inlineData", inlineData)
            parts.put(imagePart)
        }

        contentObj.put("parts", parts)
        contents.put(contentObj)
        jsonRequest.put("contents", contents)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.3)
        genConfig.put("topP", 0.95)
        jsonRequest.put("generationConfig", genConfig)

        // System Instruction if provided
        if (!systemInstruction.isNullOrBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            val sysTextPart = JSONObject()
            sysTextPart.put("text", systemInstruction)
            sysParts.put(sysTextPart)
            sysObj.put("parts", sysParts)
            jsonRequest.put("systemInstruction", sysObj)
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "HTTP ${response.code}"
            throw RuntimeException("Gemini API Error (${response.code}): $errorBody")
        }

        val responseString = response.body?.string() ?: ""
        val responseJson = JSONObject(responseString)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return@withContext "No response candidate generated."
        }

        val firstCandidate = candidates.getJSONObject(0)
        val candContent = firstCandidate.optJSONObject("content")
        val candParts = candContent?.optJSONArray("parts")
        if (candParts == null || candParts.length() == 0) {
            return@withContext "Empty content received."
        }

        candParts.getJSONObject(0).optString("text", "")
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}

class NexusAiService {

    suspend fun explainAndAnalyze(
        userPrompt: String,
        mode: ExplanationMode = ExplanationMode.DETAILED,
        imageBitmap: Bitmap? = null
    ): Pair<String, ConceptAnalysisResult> = withContext(Dispatchers.Default) {
        val systemPrompt = """
            You are NEXUS, an AI-powered personal knowledge and simulation engine.
            Goal: Understand → Connect → Explain → Simulate → Practice → Remember.
            Explanation Level requested: ${mode.label} (${mode.description}).
            
            You must provide two parts in your response:
            1. An engaging, deep, and structured explanation tailored specifically to the ${mode.label} level.
            2. At the very end of your response, output a strict JSON block delimited by ```json and ``` with the following schema:
            {
              "mainConcept": "string (name of the core scientific/mathematical concept)",
              "subject": "Physics" | "Space" | "Chemistry" | "Biology" | "Mathematics" | "Circuits",
              "difficulty": "Beginner" | "Intermediate" | "Advanced" | "Olympiad",
              "keyIdeas": ["3-4 key bullet points"],
              "prerequisites": ["2-3 prerequisite concepts required to understand this"],
              "relatedConcepts": ["2-3 related or downstream concepts"],
              "examples": ["1-2 concrete real-world or laboratory examples"],
              "commonMistakes": ["1-2 common misconceptions or calculation traps"],
              "practiceQuestion": {
                "questionText": "A challenging conceptual or numerical question",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "correctAnswerIndex": 0,
                "explanation": "Why this option is correct",
                "misconceptionAnalysis": "Why the wrong options are tempting and where students fail"
              },
              "suggestedNextTopics": ["2 topics to learn next"],
              "simulationType": "PROJECTILE" | "ORBITAL" | "PENDULUM" | "CIRCUIT" | "POPULATION" | null
            }
        """.trimIndent()

        try {
            val rawOutput = GeminiClient.queryGemini(
                prompt = userPrompt,
                systemInstruction = systemPrompt,
                imageBitmap = imageBitmap
            )

            val parsedJson = extractJsonFromOutput(rawOutput)
            val analysis = if (parsedJson != null) {
                parseStructuredJson(parsedJson, mode)
            } else {
                generateHeuristicAnalysis(userPrompt, mode)
            }

            val cleanedExplanation = cleanMarkdownExplanation(rawOutput)
            Pair(cleanedExplanation, analysis)
        } catch (e: Exception) {
            // Intelligent fallback synthesis engine
            val fallback = generateHeuristicAnalysis(userPrompt, mode, imageBitmap != null)
            val fallbackExplanation = generateFallbackExplanation(userPrompt, mode, fallback)
            Pair(fallbackExplanation, fallback)
        }
    }

    private fun extractJsonFromOutput(output: String): JSONObject? {
        val jsonStart = output.indexOf("```json")
        val jsonEnd = output.lastIndexOf("```")
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            val jsonContent = output.substring(jsonStart + 7, jsonEnd).trim()
            return try {
                JSONObject(jsonContent)
            } catch (_: Exception) {
                null
            }
        }
        // Try finding raw braces
        val firstBrace = output.indexOf('{')
        val lastBrace = output.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return try {
                JSONObject(output.substring(firstBrace, lastBrace + 1))
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun cleanMarkdownExplanation(raw: String): String {
        val jsonIdx = raw.indexOf("```json")
        return if (jsonIdx != -1) {
            raw.substring(0, jsonIdx).trim()
        } else {
            raw.trim()
        }
    }

    private fun parseStructuredJson(json: JSONObject, mode: ExplanationMode): ConceptAnalysisResult {
        val mainConcept = json.optString("mainConcept", "Core Concept")
        val subject = json.optString("subject", "Physics")
        val difficulty = json.optString("difficulty", "Intermediate")

        val keyIdeas = jsonArrayToList(json.optJSONArray("keyIdeas"))
        val prereqs = jsonArrayToList(json.optJSONArray("prerequisites"))
        val related = jsonArrayToList(json.optJSONArray("relatedConcepts"))
        val examples = jsonArrayToList(json.optJSONArray("examples"))
        val mistakes = jsonArrayToList(json.optJSONArray("commonMistakes"))
        val nextTopics = jsonArrayToList(json.optJSONArray("suggestedNextTopics"))
        val simType = json.optString("simulationType").takeIf { it.isNotBlank() && it != "null" }

        val qObj = json.optJSONObject("practiceQuestion")
        val question = if (qObj != null) {
            val qText = qObj.optString("questionText", "Check your understanding of $mainConcept")
            val opts = jsonArrayToList(qObj.optJSONArray("options"))
            val correctIdx = qObj.optInt("correctAnswerIndex", 0).toString()
            val exp = qObj.optString("explanation", "")
            val mis = qObj.optString("misconceptionAnalysis", "")
            Question(
                id = UUID.randomUUID().toString(),
                conceptId = mainConcept.lowercase().replace(" ", "_"),
                conceptName = mainConcept,
                subject = subject,
                questionText = qText,
                questionType = "MCQ",
                optionsJson = JSONArray(opts).toString(),
                correctAnswer = correctIdx,
                explanation = exp,
                misconceptionAnalysis = mis,
                testedConcept = mainConcept,
                difficulty = difficulty
            )
        } else null

        return ConceptAnalysisResult(
            mainConcept = mainConcept,
            subject = subject,
            difficulty = difficulty,
            keyIdeas = keyIdeas,
            prerequisites = prereqs,
            relatedConcepts = related,
            examples = examples,
            commonMistakes = mistakes,
            practiceQuestion = question,
            suggestedNextTopics = nextTopics,
            simulationType = simType,
            explanationMode = mode.label
        )
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.optString(i))
        }
        return list
    }

    /**
     * Resilient offline scientific analysis engine when API key is missing or offline.
     */
    fun generateHeuristicAnalysis(
        prompt: String,
        mode: ExplanationMode,
        isImage: Boolean = false
    ): ConceptAnalysisResult {
        val lower = prompt.lowercase()
        return when {
            lower.contains("jupiter") || lower.contains("orbit") || lower.contains("planet") || lower.contains("gravity") || lower.contains("solar") -> {
                ConceptAnalysisResult(
                    mainConcept = "Jupiter Gravitational Shield & Orbital Perturbations",
                    subject = "Space",
                    difficulty = "Advanced",
                    keyIdeas = listOf(
                        "Jupiter has 318 times the mass of Earth, dominating solar system gravitational perturbations.",
                        "Maintains orbital resonance gaps (Kirkwood gaps) in the Asteroid Belt.",
                        "Acts as a cosmic vacuum cleaner by deflecting long-period comets into interstellar space or absorbing them."
                    ),
                    prerequisites = listOf("Orbital Mechanics", "Universal Gravitation", "Newton's Second Law"),
                    relatedConcepts = listOf("Kepler's Laws", "Three-Body Problem", "Escape Velocity"),
                    examples = listOf("Comet Shoemaker-Levy 9 collision (1994)", "Kirkwood resonance gaps in the asteroid belt"),
                    commonMistakes = listOf("Assuming Jupiter's magnetic field repels comets (it is gravitational mass deflection)"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "space_jupiter",
                        conceptName = "Jupiter Gravitational Shield",
                        subject = "Space",
                        questionText = "If Jupiter were suddenly removed from our solar system, what would be the primary long-term impact on the inner planets?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf(
                            "Earth would instantly lose its magnetic shielding",
                            "Inner solar system would face destabilized asteroid belt orbits and increased collision rates",
                            "The Sun's nuclear fusion would slow down by 10%",
                            "The Earth would drift away into deep interstellar space"
                        )).toString(),
                        correctAnswer = "1",
                        explanation = "Without Jupiter's massive stabilizing gravitational well, asteroid belt orbital resonances would destabilize over geological timescales, scattering bodies inwards toward Mars and Earth.",
                        misconceptionAnalysis = "Conflating gravitational planetary mechanics with solar fusion or immediate ejection.",
                        testedConcept = "Gravitational n-body perturbation and orbital stability"
                    ),
                    suggestedNextTopics = listOf("Three-Body Lagrange Points", "Tidal Forces and Roche Limit"),
                    simulationType = "ORBITAL",
                    explanationMode = mode.label
                )
            }
            lower.contains("projectile") || lower.contains("velocity") || lower.contains("launch") || lower.contains("trajectory") -> {
                ConceptAnalysisResult(
                    mainConcept = "Projectile Motion & Parabolic Trajectories",
                    subject = "Physics",
                    difficulty = "Intermediate",
                    keyIdeas = listOf(
                        "Motion is decoupled into uniform horizontal motion (ax = 0) and uniformly accelerated vertical motion (ay = -g).",
                        "Maximum range occurs at 45° angle on flat ground without air resistance.",
                        "Air resistance introduces quadratic drag F_d = (1/2)ρv²C_d A, truncating the apex and descending angle."
                    ),
                    prerequisites = listOf("Newton's Second Law", "Velocity & Speed", "Vectors & Scalars"),
                    relatedConcepts = listOf("Kinetic Energy", "Aerodynamic Drag", "Escape Velocity"),
                    examples = listOf("Satellite orbital insertion trajectory", "Artillery ballistic curve"),
                    commonMistakes = listOf("Thinking the velocity at the apex is zero (horizontal component vx remains non-zero)"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "phys_projectile",
                        conceptName = "Projectile Motion",
                        subject = "Physics",
                        questionText = "At the peak apex of a projectile's parabolic trajectory (ignoring air drag), what is its instantaneous acceleration?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf(
                            "0 m/s²",
                            "g (9.8 m/s²) directed strictly downward",
                            "vx / t directed forward",
                            "g / 2 directed along the tangent"
                        )).toString(),
                        correctAnswer = "1",
                        explanation = "Gravitational acceleration is constant everywhere throughout the flight: always equal to 9.8 m/s² pointing downward.",
                        misconceptionAnalysis = "Confusing zero vertical velocity with zero vertical acceleration.",
                        testedConcept = "Independence of velocity and gravitational acceleration in projectile flight"
                    ),
                    suggestedNextTopics = listOf("Drag Forces & Terminal Velocity", "Non-flat Earth Trajectories"),
                    simulationType = "PROJECTILE",
                    explanationMode = mode.label
                )
            }
            lower.contains("pendulum") || lower.contains("oscillation") || lower.contains("harmonic") || lower.contains("spring") -> {
                ConceptAnalysisResult(
                    mainConcept = "Harmonic Oscillator & Simple Pendulum",
                    subject = "Physics",
                    difficulty = "Intermediate",
                    keyIdeas = listOf(
                        "Governing equation: d²θ/dt² + (g/L) sin(θ) = 0.",
                        "Small-angle approximation sin(θ) ≈ θ yields simple harmonic motion with period T = 2π√(L/g).",
                        "Period is independent of the bob's mass m (Galileo's isochronism)."
                    ),
                    prerequisites = listOf("Newton's Second Law", "Trigonometry & Small Angles", "Inertial Mass"),
                    relatedConcepts = listOf("Damped Oscillations", "Resonance", "RLC Circuits"),
                    examples = listOf("Grandfather clock escapement", "Seismic pendulum dampers in skyscrapers"),
                    commonMistakes = listOf("Assuming heavier bobs swing faster (period is independent of mass)"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "phys_pendulum",
                        conceptName = "Harmonic Oscillator & Pendulum",
                        subject = "Physics",
                        questionText = "If you quadruple the length L of a simple pendulum, what happens to its period of oscillation T?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf(
                            "Period doubles (2T)",
                            "Period quadruples (4T)",
                            "Period is halved (T/2)",
                            "Period remains unchanged"
                        )).toString(),
                        correctAnswer = "0",
                        explanation = "Since T = 2π√(L/g), if L becomes 4L, √(4L) = 2√L, so the period doubles.",
                        misconceptionAnalysis = "Forgetting the square root dependence in the period equation.",
                        testedConcept = "Period scaling in simple harmonic motion"
                    ),
                    suggestedNextTopics = listOf("Damped Harmonic Motion", "Coupled Pendulums & Chaos"),
                    simulationType = "PENDULUM",
                    explanationMode = mode.label
                )
            }
            lower.contains("circuit") || lower.contains("ohm") || lower.contains("resistor") || lower.contains("capacitor") || lower.contains("current") -> {
                ConceptAnalysisResult(
                    mainConcept = "RLC Circuit Resonance & Ohm's Law",
                    subject = "Circuits",
                    difficulty = "Intermediate",
                    keyIdeas = listOf(
                        "Ohm's Law: V = I · R defines linear resistive dissipation.",
                        "Inductors oppose rate of change of current (v = L di/dt); capacitors oppose rate of change of voltage (i = C dv/dt).",
                        "Resonant angular frequency: ω₀ = 1/√(LC), where impedance is purely resistive."
                    ),
                    prerequisites = listOf("Ohm's Law & Resistance", "Differential Equations Basics"),
                    relatedConcepts = listOf("Harmonic Oscillator & Pendulum", "Electromagnetic Resonance"),
                    examples = listOf("Radio tuning filters", "Wireless power transmission coils"),
                    commonMistakes = listOf("Confusing capacitive reactance 1/(ωC) with inductive reactance ωL"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "circ_rlc",
                        conceptName = "RLC Circuit & Resonance",
                        subject = "Circuits",
                        questionText = "What happens to the inductive reactance XL = ωL when the AC driving frequency doubles?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf(
                            "It doubles (2XL)",
                            "It is cut in half (XL / 2)",
                            "It stays constant",
                            "It quadruples"
                        )).toString(),
                        correctAnswer = "0",
                        explanation = "Inductive reactance is directly proportional to frequency: XL = 2πfL. Doubling f doubles XL.",
                        misconceptionAnalysis = "Confusing inductor behavior with capacitor inverse frequency relation.",
                        testedConcept = "AC frequency dependence of reactive impedance"
                    ),
                    suggestedNextTopics = listOf("Quality Factor Q & Bandwidth", "Impedance Matching"),
                    simulationType = "CIRCUIT",
                    explanationMode = mode.label
                )
            }
            lower.contains("population") || lower.contains("predator") || lower.contains("prey") || lower.contains("ecosystem") || lower.contains("biology") -> {
                ConceptAnalysisResult(
                    mainConcept = "Lotka-Volterra Predator-Prey Dynamics",
                    subject = "Biology",
                    difficulty = "Advanced",
                    keyIdeas = listOf(
                        "Nonlinear coupled differential equations: dx/dt = αx - βxy, dy/dt = δxy - γy.",
                        "Prey abundance drives predator proliferation; excessive predation collapses prey, leading to predator famine.",
                        "Produces perpetual closed phase space orbits around a center equilibrium."
                    ),
                    prerequisites = listOf("Exponential & Logistic Growth", "Differential Equations Basics"),
                    relatedConcepts = listOf("Carrying Capacity K", "Ecological Niche", "Trophic Cascades"),
                    examples = listOf("Canadian Lynx and Snowshoe Hare 10-year historical cycle", "Phytoplankton and zooplankton ocean blooms"),
                    commonMistakes = listOf("Assuming predator and prey peaks align in time (predator peaks lag behind prey)"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "bio_predator_prey",
                        conceptName = "Lotka-Volterra Dynamics",
                        subject = "Biology",
                        questionText = "In the Lotka-Volterra equations, what term represents the rate at which prey are captured and consumed?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf(
                            "αx (intrinsic growth)",
                            "βxy (encounter predation rate)",
                            "γy (predator natural mortality)",
                            "δxy (predator reproduction efficiency)"
                        )).toString(),
                        correctAnswer = "1",
                        explanation = "The bilinear interaction term β·x·y represents the frequency of encounters between prey x and predator y resulting in predation.",
                        misconceptionAnalysis = "Confusing consumption rate with the predator's conversion into offspring (δxy).",
                        testedConcept = "Mathematical formulation of predation encounter kinetics"
                    ),
                    suggestedNextTopics = listOf("Logistic Predator-Prey Models", "Bifurcations and Chaos"),
                    simulationType = "POPULATION",
                    explanationMode = mode.label
                )
            }
            else -> {
                val conceptName = if (prompt.length < 35 && !prompt.contains("?")) prompt else "Newtonian Dynamics & Force Interactions"
                ConceptAnalysisResult(
                    mainConcept = conceptName,
                    subject = "Physics",
                    difficulty = "Intermediate",
                    keyIdeas = listOf(
                        "Understanding core interactions through first-principles conservation and rate equations.",
                        "Identifying prerequisites ensures foundational clarity before addressing complex applications.",
                        "Connecting theoretical formalisms with interactive physical simulations."
                    ),
                    prerequisites = listOf("Physical Units & Dimensions", "Inertial Mass", "Force & Vectors"),
                    relatedConcepts = listOf("Kinetic Energy", "Linear Momentum", "Differential Equations Basics"),
                    examples = listOf("Spacecraft orbital maneuvers", "High-speed rail propulsion dynamics"),
                    commonMistakes = listOf("Confusing velocity with acceleration during directional changes"),
                    practiceQuestion = Question(
                        id = UUID.randomUUID().toString(),
                        conceptId = "phys_newton2",
                        conceptName = "Newton's Second Law",
                        subject = "Physics",
                        questionText = "A net force of 20 N acts on an object of mass 4 kg. What is the acceleration produced?",
                        questionType = "MCQ",
                        optionsJson = JSONArray(listOf("0.2 m/s²", "5.0 m/s²", "16.0 m/s²", "80.0 m/s²")).toString(),
                        correctAnswer = "1",
                        explanation = "By Newton's Second Law: a = F / m = 20 N / 4 kg = 5.0 m/s².",
                        misconceptionAnalysis = "Multiplying instead of dividing (80 m/s²) or subtracting (16 m/s²).",
                        testedConcept = "Direct application of a = F/m"
                    ),
                    suggestedNextTopics = listOf("Impulse & Momentum Theorem", "Work-Energy Theorem"),
                    simulationType = "PROJECTILE",
                    explanationMode = mode.label
                )
            }
        }
    }

    private fun generateFallbackExplanation(
        prompt: String,
        mode: ExplanationMode,
        analysis: ConceptAnalysisResult
    ): String {
        return buildString {
            append("### ${analysis.mainConcept}\n\n")
            append("**Mode: ${mode.label}** — *${mode.description}*\n\n")
            append("#### Core Understanding\n")
            analysis.keyIdeas.forEach { idea ->
                append("• $idea\n")
            }
            append("\n#### Conceptual Breakdown & First Principles\n")
            when (mode) {
                ExplanationMode.SIMPLE -> {
                    append("Think of this concept like everyday motion you can touch and see. Rather than complicated formulas, imagine what happens when you push an object: it resists until enough force builds up. Once moving, it stays moving unless an obstacle or friction slows it down.\n\n")
                }
                ExplanationMode.ADVANCED, ExplanationMode.UNIVERSITY -> {
                    append("From a formal analytical perspective, the dynamical behavior is governed by state-space differentials. We formulate the equation of motion in canonical generalized coordinates q_i and conjugate momenta p_i via Hamilton's variational principle delta integral L dt = 0.\n\n")
                }
                ExplanationMode.OLYMPIAD, ExplanationMode.JEE_FOUNDATION -> {
                    append("For rapid problem solving, identify the conservation invariant immediately before writing Newton's equations. Check whether external torque or non-conservative work vanishes. Watch out for sign conventions and non-inertial reference frame fictitious forces (Coriolis and centrifugal terms).\n\n")
                }
                else -> {
                    append("Physical systems obey strict causal relationships governed by conservation laws. To understand this concept deeply, trace the interaction step-by-step from its underlying prerequisites through to observable consequences.\n\n")
                }
            }
            append("#### Concrete Physical Examples\n")
            analysis.examples.forEach { eg ->
                append("• $eg\n")
            }
            append("\n#### Misconceptions & Traps to Avoid\n")
            analysis.commonMistakes.forEach { trap ->
                append("⚠️ **Common Pitfall**: $trap\n")
            }
            append("\n*Explore the interactive simulation or practice question below to test your understanding!*")
        }
    }
}
