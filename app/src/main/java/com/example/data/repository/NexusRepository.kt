package com.example.data.repository

import android.content.Context
import com.example.data.local.NexusDatabase
import com.example.data.local.SeedData
import com.example.data.model.Attempt
import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import com.example.data.model.DocumentNote
import com.example.data.model.Experiment
import com.example.data.model.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

class NexusRepository(context: Context) {
    private val db = NexusDatabase.getDatabase(context)
    private val conceptDao = db.conceptDao()
    private val questionDao = db.questionDao()
    private val attemptDao = db.attemptDao()
    private val documentNoteDao = db.documentNoteDao()
    private val experimentDao = db.experimentDao()

    init {
        // Initialize with rich scientific seed dataset on first launch
        CoroutineScope(Dispatchers.IO).launch {
            val existing = conceptDao.getAllConcepts().first()
            if (existing.isEmpty()) {
                seedInitialData()
            }
        }
    }

    suspend fun seedInitialData() {
        conceptDao.insertConcepts(SeedData.initialConcepts)
        conceptDao.insertRelationships(SeedData.initialRelationships)
        questionDao.insertQuestions(SeedData.initialQuestions)
        SeedData.sampleDocuments.forEach { documentNoteDao.insertDocument(it) }
    }

    // --- Concepts & Graph ---
    val allConcepts: Flow<List<Concept>> = conceptDao.getAllConcepts()
    val allRelationships: Flow<List<ConceptRelationship>> = conceptDao.getAllRelationships()
    val weakConcepts: Flow<List<Concept>> = conceptDao.getWeakConcepts()
    val totalConceptsCount: Flow<Int> = conceptDao.getTotalConceptsCount()
    val masteredCount: Flow<Int> = conceptDao.getMasteredCount()
    val learningCount: Flow<Int> = conceptDao.getLearningCount()
    val weakCount: Flow<Int> = conceptDao.getWeakCount()

    suspend fun getConceptById(id: String): Concept? = conceptDao.getConceptById(id)
    suspend fun getConceptByName(name: String): Concept? = conceptDao.getConceptByName(name)

    suspend fun insertConcept(concept: Concept) {
        conceptDao.insertConcept(concept)
    }

    suspend fun updateConcept(concept: Concept) {
        conceptDao.updateConcept(concept)
    }

    suspend fun addRelationships(relationships: List<ConceptRelationship>) {
        conceptDao.insertRelationships(relationships)
    }

    fun searchConcepts(query: String): Flow<List<Concept>> = conceptDao.searchConcepts(query)

    // --- Questions & Practice ---
    val allQuestions: Flow<List<Question>> = questionDao.getAllQuestions()
    fun getQuestionsForConcept(conceptId: String): Flow<List<Question>> = questionDao.getQuestionsForConcept(conceptId)
    fun getQuestionsBySubject(subject: String): Flow<List<Question>> = questionDao.getQuestionsBySubject(subject)

    suspend fun insertQuestion(question: Question) {
        questionDao.insertQuestion(question)
    }

    suspend fun insertQuestions(questions: List<Question>) {
        questionDao.insertQuestions(questions)
    }

    // --- Attempts & Adaptive Learning Engine ---
    val allAttempts: Flow<List<Attempt>> = attemptDao.getAllAttempts()
    val totalAttemptsCount: Flow<Int> = attemptDao.getTotalAttemptsCount()
    val correctAttemptsCount: Flow<Int> = attemptDao.getCorrectAttemptsCount()

    suspend fun recordAttempt(
        questionId: String,
        conceptId: String,
        conceptName: String,
        userAnswer: String,
        isCorrect: Boolean,
        mistakeCategory: String = "None",
        remedialTip: String = ""
    ): Attempt {
        val attempt = Attempt(
            questionId = questionId,
            conceptId = conceptId,
            conceptName = conceptName,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            mistakeCategory = mistakeCategory,
            remedialTip = remedialTip
        )
        attemptDao.insertAttempt(attempt)

        // Update Concept Mastery Score and State
        val existingConcept = conceptDao.getConceptById(conceptId)
        if (existingConcept != null) {
            val newAttempted = existingConcept.questionsAttempted + 1
            val newCorrect = if (isCorrect) existingConcept.correctCount + 1 else existingConcept.correctCount
            val newMistakes = if (!isCorrect) existingConcept.mistakesCount + 1 else existingConcept.mistakesCount

            // Estimated Application Mastery score (0..100)
            val accuracy = (newCorrect.toDouble() / newAttempted.toDouble()) * 100.0
            val penalty = (newMistakes * 8).coerceAtMost(40)
            val updatedScore = (accuracy - penalty).toInt().coerceIn(5, 100)

            val updatedLevel = when {
                updatedScore >= 80 && newAttempted >= 3 -> "Mastered"
                updatedScore < 45 && newMistakes >= 2 -> "Weak"
                else -> "Learning"
            }

            conceptDao.updateConcept(
                existingConcept.copy(
                    questionsAttempted = newAttempted,
                    correctCount = newCorrect,
                    mistakesCount = newMistakes,
                    masteryScore = updatedScore,
                    masteryLevel = updatedLevel,
                    lastReviewedDate = System.currentTimeMillis()
                )
            )
        }

        return attempt
    }

    /**
     * Identifies potential prerequisite gaps when a user struggles with a concept.
     * Returns list of prerequisite concepts that need attention before attempting difficult topics.
     */
    suspend fun identifyPrerequisiteGaps(conceptId: String): List<Concept> {
        val target = conceptDao.getConceptById(conceptId) ?: return emptyList()
        val prereqs = mutableListOf<Concept>()

        try {
            val array = JSONArray(target.prerequisitesJson)
            for (i in 0 until array.length()) {
                val prereqNameOrId = array.optString(i)
                val prereq = conceptDao.getConceptByName(prereqNameOrId)
                    ?: conceptDao.getConceptById(prereqNameOrId)
                if (prereq != null) {
                    prereqs.add(prereq)
                }
            }
        } catch (_: Exception) {}

        // If target concept is Weak, find which prerequisites are not yet Mastered
        return prereqs.filter { it.masteryScore < 80 }
    }

    // --- Documents & Notes ---
    val allDocuments: Flow<List<DocumentNote>> = documentNoteDao.getAllDocuments()

    suspend fun insertDocument(doc: DocumentNote) {
        documentNoteDao.insertDocument(doc)
    }

    suspend fun deleteDocument(id: String) {
        documentNoteDao.deleteDocumentById(id)
    }

    // --- Experiments ---
    val allExperiments: Flow<List<Experiment>> = experimentDao.getAllExperiments()

    suspend fun insertExperiment(experiment: Experiment) {
        experimentDao.insertExperiment(experiment)
    }

    suspend fun deleteExperiment(id: String) {
        experimentDao.deleteExperimentById(id)
    }

    suspend fun resetDatabase() {
        val concepts = conceptDao.getAllConcepts().first()
        concepts.forEach { conceptDao.deleteConceptById(it.id) }
        seedInitialData()
    }
}
