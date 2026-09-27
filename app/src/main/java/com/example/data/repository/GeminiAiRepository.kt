package com.example.data.repository

import android.graphics.Bitmap
import com.example.BuildConfig
import com.example.data.model.Concept
import com.example.data.model.ConceptAnalysisResult
import com.example.data.model.ExplanationMode
import com.example.data.model.Question
import com.example.data.remote.GeminiClient
import com.example.data.remote.NexusAiService
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * AI Repository using the official Google AI SDK (GenerativeModel)
 * to interact with the Gemini API and handle structured JSON concept extractions.
 */
class GeminiAiRepository(
    private val modelName: String = "gemini-3.5-flash"
) {
    private val fallbackService = NexusAiService()

    private fun getEffectiveApiKey(): String {
        return when {
            GeminiClient.userCustomApiKey.isNotBlank() -> GeminiClient.userCustomApiKey.trim()
            try { BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" } catch (_: Throwable) { false } -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }
    }

    /**
     * Initializes a GenerativeModel configured for strict structured JSON output.
     */
    private fun createGenerativeModel(systemInstructionText: String? = null): GenerativeModel {
        val apiKey = getEffectiveApiKey()
        val config = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.2f
            topP = 0.95f
        }

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config,
            systemInstruction = systemInstructionText?.let { content { text(it) } }
        )
    }

    /**
     * Extracts structured scientific concepts and relationships from natural language queries.
     * Guarantees a parsed and validated ConceptAnalysisResult with prerequisites, key ideas, and practice questions.
     */
    suspend fun extractConceptStructured(
        userPrompt: String,
        mode: ExplanationMode = ExplanationMode.DETAILED
    ): ConceptAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            // Graceful offline knowledge synthesis
            return@withContext fallbackService.generateHeuristicAnalysis(userPrompt, mode)
        }

        val systemInstruction = """
            You are NEXUS, an AI-powered personal knowledge and simulation engine.
            Your role is: Understand → Connect → Explain → Simulate → Practice → Remember.
            Explanation Level: ${mode.label} (${mode.description}).
            
            Extract the core concept from the user's input into strict JSON adhering to this exact schema:
            {
              "mainConcept": "string (name of core scientific/mathematical concept)",
              "subject": "Physics" | "Space" | "Circuits" | "Biology" | "Mathematics" | "General Science",
              "difficulty": "Beginner" | "Intermediate" | "Advanced" | "Olympiad",
              "keyIdeas": ["3-4 key bullet points"],
              "prerequisites": ["2-3 prerequisite concepts required to understand this"],
              "relatedConcepts": ["2-3 related concepts"],
              "examples": ["1-2 concrete real-world or laboratory examples"],
              "commonMistakes": ["1-2 common misconceptions or calculation traps"],
              "practiceQuestion": {
                "questionText": "A challenging conceptual question testing this concept",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "correctAnswerIndex": 0,
                "explanation": "Why this option is correct",
                "misconceptionAnalysis": "Why the wrong options are tempting"
              },
              "suggestedNextTopics": ["2 topics to learn next"],
              "simulationType": "PROJECTILE" | "ORBITAL" | "PENDULUM" | "CIRCUIT" | "POPULATION" | null
            }
        """.trimIndent()

        try {
            val model = createGenerativeModel(systemInstruction)
            val response = model.generateContent(
                content {
                    text("Analyze and extract the concept from this query: $userPrompt")
                }
            )

            val rawJson = response.text ?: ""
            parseStructuredJson(rawJson, mode)
        } catch (e: Exception) {
            // Resilient fallback to local synthesis
            fallbackService.generateHeuristicAnalysis(userPrompt, mode)
        }
    }

    /**
     * Multimodal analysis of scientific diagrams and textbook captures using the Google AI SDK.
     */
    suspend fun analyzeMultimodalConcept(
        userPrompt: String,
        imageBitmap: Bitmap,
        mode: ExplanationMode = ExplanationMode.DETAILED
    ): ConceptAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext fallbackService.generateHeuristicAnalysis(userPrompt, mode, isImage = true)
        }

        val systemInstruction = """
            You are NEXUS Vision Engine. Identify the scientific diagram or formula and extract its structural components,
            prerequisites, and underlying physical laws.
            Return strict JSON matching:
            {
              "mainConcept": "string",
              "subject": "Physics" | "Space" | "Circuits" | "Biology" | "Mathematics",
              "difficulty": "Beginner" | "Intermediate" | "Advanced" | "Olympiad",
              "keyIdeas": ["Identified diagram components and relationships"],
              "prerequisites": ["Prerequisites identified from diagram"],
              "relatedConcepts": ["Downstream topics"],
              "examples": ["Real-world application"],
              "commonMistakes": ["Visual reading error or formula trap"],
              "suggestedNextTopics": ["Next topic"],
              "simulationType": "PROJECTILE" | "ORBITAL" | "PENDULUM" | "CIRCUIT" | "POPULATION" | null
            }
        """.trimIndent()

        try {
            val model = createGenerativeModel(systemInstruction)
            val response = model.generateContent(
                content {
                    image(imageBitmap)
                    text(userPrompt)
                }
            )

            val rawJson = response.text ?: ""
            parseStructuredJson(rawJson, mode)
        } catch (e: Exception) {
            fallbackService.generateHeuristicAnalysis(userPrompt, mode, isImage = true)
        }
    }

    /**
     * Parses and validates raw model JSON into a safe ConceptAnalysisResult.
     */
    private fun parseStructuredJson(rawText: String, mode: ExplanationMode): ConceptAnalysisResult {
        val cleanJsonString = extractJsonSubstring(rawText)
        val json = JSONObject(cleanJsonString)

        val mainConcept = json.optString("mainConcept", "Scientific Concept")
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

    private fun extractJsonSubstring(text: String): String {
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        return if (firstBrace != -1 && lastBrace > firstBrace) {
            text.substring(firstBrace, lastBrace + 1)
        } else {
            text
        }
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.optString(i))
        }
        return list
    }
}
