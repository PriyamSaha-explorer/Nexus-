package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.learning.AdaptiveLearningEngine
import com.example.data.model.AdaptiveLearningPath
import com.example.data.model.Attempt
import com.example.data.model.Concept
import com.example.data.model.ConceptAnalysisResult
import com.example.data.model.ConceptRelationship
import com.example.data.model.DocumentNote
import com.example.data.model.Experiment
import com.example.data.model.ExplanationMode
import com.example.data.model.NexusChatMessage
import com.example.data.model.Question
import com.example.data.remote.GeminiClient
import com.example.data.remote.NexusAiService
import com.example.data.repository.GeminiAiRepository
import com.example.data.repository.NexusRepository
import com.example.ui.simulation.SimulationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

enum class AppScreen {
    DASHBOARD,
    LEARNING_PATH,
    AI_TUTOR,
    KNOWLEDGE_GRAPH,
    SIMULATOR,
    EXPERIMENTS,
    PRACTICE,
    IMAGE_ANALYSIS,
    DOCUMENTS,
    SEARCH,
    SETTINGS
}

data class DiagnosticResult(
    val isCorrect: Boolean,
    val mistakeCategory: String, // "None", "Conceptual", "Calculation", "Unit", "FormulaSelection", "Reading", "PrerequisiteGap"
    val explanation: String,
    val misconceptionAnalysis: String,
    val recommendedPrerequisites: List<Concept> = emptyList(),
    val remedialTip: String
)

class NexusViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NexusRepository(application)
    private val aiService = NexusAiService()
    val geminiAiRepository = GeminiAiRepository()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen stack for BackHandler navigation
    private val screenStack = mutableListOf(AppScreen.DASHBOARD)

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // Explanation Mode ("Teach Me Like I Learn")
    private val _explanationMode = MutableStateFlow(ExplanationMode.DETAILED)
    val explanationMode: StateFlow<ExplanationMode> = _explanationMode.asStateFlow()

    fun setExplanationMode(mode: ExplanationMode) {
        _explanationMode.value = mode
    }

    // Database Flows
    val allConcepts: StateFlow<List<Concept>> = repository.allConcepts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRelationships: StateFlow<List<ConceptRelationship>> = repository.allRelationships
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weakConcepts: StateFlow<List<Concept>> = repository.weakConcepts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<Attempt>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentNote>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExperiments: StateFlow<List<Experiment>> = repository.allExperiments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically generated Adaptive Learning Path prioritizing prerequisite gaps
    val adaptiveLearningPath: StateFlow<AdaptiveLearningPath> = combine(allConcepts, allRelationships) { concepts, rels ->
        AdaptiveLearningEngine.generateAdaptivePath(concepts, rels)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdaptiveLearningEngine.generateAdaptivePath(emptyList(), emptyList())
    )

    // Selected Concept in Graph or Inspector
    private val _selectedConcept = MutableStateFlow<Concept?>(null)
    val selectedConcept: StateFlow<Concept?> = _selectedConcept.asStateFlow()

    fun selectConcept(concept: Concept?) {
        _selectedConcept.value = concept
    }

    // Active Simulation
    private val _activeSimulation = MutableStateFlow(SimulationType.PROJECTILE)
    val activeSimulation: StateFlow<SimulationType> = _activeSimulation.asStateFlow()

    fun setSimulation(sim: SimulationType) {
        _activeSimulation.value = sim
    }

    // --- AI TUTOR CONVERSATION ---
    private val _chatMessages = MutableStateFlow<List<NexusChatMessage>>(
        listOf(
            NexusChatMessage(
                text = "Welcome to NEXUS. I am your personal knowledge navigator. I don't just answer questions—I map ideas to the Knowledge Graph, uncover prerequisites, detect gaps, and generate interactive simulations. What would you like to understand today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<NexusChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendAiPrompt(prompt: String, image: Bitmap? = null) {
        if (prompt.isBlank() && image == null) return

        val userMsg = NexusChatMessage(text = prompt, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val (explanation, analysis) = aiService.explainAndAnalyze(
                    userPrompt = prompt,
                    mode = _explanationMode.value,
                    imageBitmap = image
                )

                val aiMsg = NexusChatMessage(
                    text = explanation,
                    isUser = false,
                    structuredData = analysis
                )
                _chatMessages.value = _chatMessages.value + aiMsg

                // Add or link the concept to the Knowledge Graph
                val conceptId = analysis.mainConcept.lowercase().replace(" ", "_")
                val existing = repository.getConceptById(conceptId)
                if (existing == null) {
                    val newConcept = Concept(
                        id = conceptId,
                        name = analysis.mainConcept,
                        subject = analysis.subject,
                        difficulty = analysis.difficulty,
                        description = analysis.keyIdeas.firstOrNull() ?: "Extracted via NEXUS AI",
                        prerequisitesJson = JSONArray(analysis.prerequisites).toString(),
                        relatedConceptsJson = JSONArray(analysis.relatedConcepts).toString(),
                        masteryLevel = "Learning",
                        masteryScore = 50,
                        graphX = (300..800).random().toFloat(),
                        graphY = (200..700).random().toFloat(),
                        isUserCreated = true
                    )
                    repository.insertConcept(newConcept)
                }

                // If practice question was generated, add to repository
                if (analysis.practiceQuestion != null) {
                    repository.insertQuestion(analysis.practiceQuestion)
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + NexusChatMessage(
                    text = "I encountered an issue analyzing that request: ${e.message}. Please try again.",
                    isUser = false
                )
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // --- ADAPTIVE PRACTICE ENGINE & "WHY AM I WRONG?" ---
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption.asStateFlow()

    private val _diagnosticResult = MutableStateFlow<DiagnosticResult?>(null)
    val diagnosticResult: StateFlow<DiagnosticResult?> = _diagnosticResult.asStateFlow()

    fun selectOption(optionIndex: String) {
        _selectedOption.value = optionIndex
    }

    fun submitPracticeAnswer(question: Question) {
        val userAns = _selectedOption.value ?: return
        val isCorrect = userAns.trim() == question.correctAnswer.trim()

        viewModelScope.launch {
            // Determine mistake category and targeted remediation if wrong
            val mistakeCat = if (isCorrect) "None" else determineMistakeCategory(question, userAns)
            val remedialTip = if (isCorrect) {
                "Great job! You demonstrated mastery of ${question.testedConcept}."
            } else {
                generateRemedialTip(question, mistakeCat)
            }

            // Check for prerequisite gaps
            val prereqGaps = if (!isCorrect) {
                repository.identifyPrerequisiteGaps(question.conceptId)
            } else emptyList()

            // Record in repository & update concept mastery
            repository.recordAttempt(
                questionId = question.id,
                conceptId = question.conceptId,
                conceptName = question.conceptName,
                userAnswer = userAns,
                isCorrect = isCorrect,
                mistakeCategory = mistakeCat,
                remedialTip = remedialTip
            )

            _diagnosticResult.value = DiagnosticResult(
                isCorrect = isCorrect,
                mistakeCategory = mistakeCat,
                explanation = question.explanation,
                misconceptionAnalysis = question.misconceptionAnalysis,
                recommendedPrerequisites = prereqGaps,
                remedialTip = remedialTip
            )
        }
    }

    private fun determineMistakeCategory(question: Question, answer: String): String {
        val lower = question.questionText.lowercase()
        return when {
            lower.contains("vector") || lower.contains("superposition") || lower.contains("direction") -> "Conceptual"
            lower.contains("calculate") || lower.contains("magnitude") || lower.contains("magnitude of") -> "Calculation"
            lower.contains("unit") || lower.contains("dimension") -> "Unit"
            lower.contains("formula") || lower.contains("law") -> "FormulaSelection"
            else -> "Conceptual"
        }
    }

    private fun generateRemedialTip(question: Question, mistakeCat: String): String {
        return when (mistakeCat) {
            "Conceptual" -> "Likely Misconception: Revisit the core definitions and physical interactions of ${question.testedConcept}. Try running the interactive simulation first!"
            "Calculation" -> "Arithmetic or Vector Error: Double check your component breakdown and algebraic steps."
            "Unit" -> "Dimensional inconsistency: Ensure all quantities are converted to base SI units (kg, m, s, A) before solving."
            "FormulaSelection" -> "Formula Mismatch: Verify whether the problem conditions satisfy the assumptions (e.g. constant acceleration or no drag) before picking an equation."
            else -> "Review the prerequisite concepts before re-attempting."
        }
    }

    fun nextQuestion(totalQuestions: Int) {
        _selectedOption.value = null
        _diagnosticResult.value = null
        _currentQuestionIndex.value = (_currentQuestionIndex.value + 1) % totalQuestions.coerceAtLeast(1)
    }

    // --- SEARCH ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- DOCUMENT INTELLIGENCE ---
    fun addDocument(title: String, subject: String, content: String) {
        viewModelScope.launch {
            val doc = DocumentNote(
                id = UUID.randomUUID().toString(),
                title = title,
                subject = subject,
                content = content,
                summary = "User document on $subject exploring core laws and definitions.",
                keyFormulasJson = "[\"F = ma\", \"E = mc²\"]",
                extractedConceptsJson = "[\"$title\"]",
                flashcardsJson = "[{\"front\":\"Key topic in $title\",\"back\":\"Foundational concepts analyzed by NEXUS.\"}]"
            )
            repository.insertDocument(doc)
        }
    }

    // --- EXPERIMENTS ---
    fun saveExperiment(experiment: Experiment) {
        viewModelScope.launch {
            repository.insertExperiment(experiment)
        }
    }

    // --- SETTINGS ---
    fun setCustomApiKey(key: String) {
        GeminiClient.userCustomApiKey = key
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}
