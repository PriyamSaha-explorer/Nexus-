package com.example.data.model

data class LearningPathStep(
    val stepNumber: Int,
    val conceptId: String,
    val conceptName: String,
    val subject: String,
    val difficulty: String,
    val stepType: PathStepType, // PREREQUISITE_REMEDIATION, CORE_STRENGTHENING, ADVANCED_EXPANSION, UNEXPLORED_FRONTIER
    val reason: String,
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false,
    val isCurrentFocus: Boolean = false,
    val simulationType: String? = null
)

enum class PathStepType(val label: String, val badgeColorHex: Long) {
    PREREQUISITE_REMEDIATION("Prerequisite Gap", 0xFFEF4444),
    CORE_STRENGTHENING("Core Target", 0xFFF59E0B),
    ADVANCED_EXPANSION("Next Milestone", 0xFF0EA5E9),
    UNEXPLORED_FRONTIER("Frontier Exploration", 0xFFA855F7)
}

data class AdaptiveLearningPath(
    val title: String,
    val summary: String,
    val focusArea: String,
    val totalSteps: Int,
    val completedSteps: Int,
    val steps: List<LearningPathStep>
)
