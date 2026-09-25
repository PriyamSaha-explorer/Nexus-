package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Concept in the NEXUS Personal Knowledge Graph.
 */
@Entity(tableName = "concepts")
data class Concept(
    @PrimaryKey
    val id: String,
    val name: String,
    val subject: String, // "Physics", "Space", "Chemistry", "Biology", "Mathematics"
    val difficulty: String, // "Beginner", "Intermediate", "Advanced", "Olympiad"
    val description: String,
    val prerequisitesJson: String = "[]", // List of concept names or IDs
    val relatedConceptsJson: String = "[]", // List of related concept names
    val masteryLevel: String = "Unexplored", // "Mastered", "Learning", "Weak", "Unexplored"
    val masteryScore: Int = 0, // 0 to 100 estimated mastery score
    val mistakesCount: Int = 0,
    val questionsAttempted: Int = 0,
    val correctCount: Int = 0,
    val lastReviewedDate: Long = System.currentTimeMillis(),
    val graphX: Float = 0f,
    val graphY: Float = 0f,
    val isUserCreated: Boolean = false
)

/**
 * Relationship link between concepts in the graph.
 */
@Entity(tableName = "concept_relationships", primaryKeys = ["sourceId", "targetId", "relationType"])
data class ConceptRelationship(
    val sourceId: String,
    val targetId: String,
    val relationType: String // "PREREQUISITE", "RELATED", "EXTENDS"
)

/**
 * Practice question for adaptive testing.
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey
    val id: String,
    val conceptId: String,
    val conceptName: String,
    val subject: String,
    val questionText: String,
    val questionType: String, // "MCQ", "NUMERICAL", "CONCEPTUAL"
    val optionsJson: String = "[]", // JSON array of options for MCQ
    val correctAnswer: String, // Correct option index ("0", "1", etc.) or text
    val explanation: String,
    val misconceptionAnalysis: String,
    val testedConcept: String,
    val difficulty: String = "Medium" // "Easy", "Medium", "Hard", "Olympiad", "Challenge"
)

/**
 * Record of user attempt for learning analytics and "Why Am I Wrong?".
 */
@Entity(tableName = "attempts")
data class Attempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: String,
    val conceptId: String,
    val conceptName: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val mistakeCategory: String = "None", // "None", "Conceptual", "Calculation", "Unit", "FormulaSelection", "Reading", "PrerequisiteGap"
    val remedialTip: String = ""
)

/**
 * Document Intelligence & Notes study records.
 */
@Entity(tableName = "documents")
data class DocumentNote(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val content: String,
    val summary: String,
    val keyFormulasJson: String = "[]",
    val extractedConceptsJson: String = "[]",
    val flashcardsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI Experiment recorded in the NEXUS Simulation Laboratory.
 */
@Entity(tableName = "experiments")
data class Experiment(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val question: String,
    val hypothesis: String,
    val variablesJson: String,
    val assumptionsJson: String,
    val simulationType: String, // "PROJECTILE", "ORBITAL", "PENDULUM", "CIRCUIT", "POPULATION"
    val observations: String,
    val conclusion: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Chat history message in the NEXUS AI central assistant.
 */
data class NexusChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val structuredData: ConceptAnalysisResult? = null,
    val imageBase64: String? = null
)

/**
 * Structured output data model returned or parsed from Gemini.
 */
data class ConceptAnalysisResult(
    val mainConcept: String,
    val subject: String = "General Science",
    val difficulty: String = "Intermediate",
    val keyIdeas: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val relatedConcepts: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val practiceQuestion: Question? = null,
    val suggestedNextTopics: List<String> = emptyList(),
    val simulationType: String? = null,
    val explanationMode: String = "Detailed"
)

/**
 * "Teach Me Like I Learn" explanation modes.
 */
enum class ExplanationMode(val label: String, val description: String) {
    SIMPLE("Simple", "Intuitive analogies, plain language, no dense jargon"),
    SCHOOL("School", "Curriculum aligned, standard definitions and foundational diagrams"),
    DETAILED("Detailed", "In-depth mechanics, intermediate rigor, step-by-step logic"),
    ADVANCED("Advanced", "First-principles derivations, mathematical proofs, edge cases"),
    OLYMPIAD("Olympiad", "High-order problem solving, unconventional constraints, competition tricks"),
    JEE_FOUNDATION("JEE / Foundation", "Exam-oriented numerical rigor, speed formulas, trap identification"),
    UNIVERSITY("University", "Rigorous academic formalism, differential forms, tensor or state-space logic")
}
