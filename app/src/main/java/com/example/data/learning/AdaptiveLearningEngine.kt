package com.example.data.learning

import com.example.data.model.AdaptiveLearningPath
import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import com.example.data.model.LearningPathStep
import com.example.data.model.PathStepType
import org.json.JSONArray

object AdaptiveLearningEngine {

    fun generateAdaptivePath(
        concepts: List<Concept>,
        relationships: List<ConceptRelationship>
    ): AdaptiveLearningPath {
        if (concepts.isEmpty()) {
            return AdaptiveLearningPath(
                title = "Adaptive Path Initializing",
                summary = "Exploring your knowledge graph...",
                focusArea = "General Science",
                totalSteps = 0,
                completedSteps = 0,
                steps = emptyList()
            )
        }

        val conceptMap = concepts.associateBy { it.id }
        val nameMap = concepts.associateBy { it.name.lowercase() }
        val steps = mutableListOf<LearningPathStep>()
        val includedConceptIds = mutableSetOf<String>()
        var stepCounter = 1

        // 1. Identify Weak Concepts
        val weakConcepts = concepts.filter { it.masteryLevel == "Weak" || (it.masteryScore < 45 && it.questionsAttempted > 0) }
            .sortedBy { it.masteryScore }

        for (weak in weakConcepts) {
            // Find its prerequisite gaps
            val prereqNames = try {
                val arr = JSONArray(weak.prerequisitesJson)
                (0 until arr.length()).map { arr.optString(it) }
            } catch (_: Exception) { emptyList() }

            val prereqGaps = prereqNames.mapNotNull { name ->
                nameMap[name.lowercase()] ?: conceptMap[name]
            }.filter { it.masteryScore < 75 }

            // Prioritize PREREQUISITES first!
            for (gap in prereqGaps) {
                if (gap.id !in includedConceptIds) {
                    includedConceptIds.add(gap.id)
                    steps.add(
                        LearningPathStep(
                            stepNumber = stepCounter++,
                            conceptId = gap.id,
                            conceptName = gap.name,
                            subject = gap.subject,
                            difficulty = gap.difficulty,
                            stepType = PathStepType.PREREQUISITE_REMEDIATION,
                            reason = "Foundational gap detected before studying ${weak.name}. Reinforcing this eliminates repeated mistakes.",
                            estimatedMinutes = 12,
                            isCompleted = gap.masteryScore >= 80,
                            isCurrentFocus = steps.none { !it.isCompleted },
                            simulationType = determineSimType(gap.name, gap.subject)
                        )
                    )
                }
            }

            // Then add the weak concept itself as CORE_STRENGTHENING
            if (weak.id !in includedConceptIds) {
                includedConceptIds.add(weak.id)
                steps.add(
                    LearningPathStep(
                        stepNumber = stepCounter++,
                        conceptId = weak.id,
                        conceptName = weak.name,
                        subject = weak.subject,
                        difficulty = weak.difficulty,
                        stepType = PathStepType.CORE_STRENGTHENING,
                        reason = "Primary remediation target (${weak.mistakesCount} mistakes recorded). Conquer with focused diagnostics and simulations.",
                        estimatedMinutes = 18,
                        isCompleted = false,
                        isCurrentFocus = steps.none { !it.isCompleted },
                        simulationType = determineSimType(weak.name, weak.subject)
                    )
                )
            }
        }

        // 2. Add in-progress "Learning" concepts that naturally follow
        val learningConcepts = concepts.filter { it.masteryLevel == "Learning" && it.id !in includedConceptIds }
            .sortedByDescending { it.masteryScore }

        for (learn in learningConcepts.take(2)) {
            includedConceptIds.add(learn.id)
            steps.add(
                LearningPathStep(
                    stepNumber = stepCounter++,
                    conceptId = learn.id,
                    conceptName = learn.name,
                    subject = learn.subject,
                    difficulty = learn.difficulty,
                    stepType = PathStepType.ADVANCED_EXPANSION,
                    reason = "Natural stepping stone (${learn.masteryScore}% current mastery). Ready for high-order application and practice.",
                    estimatedMinutes = 15,
                    isCompleted = false,
                    isCurrentFocus = steps.none { !it.isCompleted },
                    simulationType = determineSimType(learn.name, learn.subject)
                )
            )
        }

        // 3. Add Unexplored frontier concepts
        val unexplored = concepts.filter { it.masteryLevel == "Unexplored" && it.id !in includedConceptIds }
        for (unexp in unexplored.take(2)) {
            includedConceptIds.add(unexp.id)
            steps.add(
                LearningPathStep(
                    stepNumber = stepCounter++,
                    conceptId = unexp.id,
                    conceptName = unexp.name,
                    subject = unexp.subject,
                    difficulty = unexp.difficulty,
                    stepType = PathStepType.UNEXPLORED_FRONTIER,
                    reason = "Broaden multidisciplinary scope once foundational mechanics are secured.",
                    estimatedMinutes = 20,
                    isCompleted = false,
                    isCurrentFocus = steps.none { !it.isCompleted },
                    simulationType = determineSimType(unexp.name, unexp.subject)
                )
            )
        }

        val primarySubject = steps.groupBy { it.subject }.maxByOrNull { it.value.size }?.key ?: "Physics & Celestial Mechanics"
        val completedCount = steps.count { it.isCompleted }

        return AdaptiveLearningPath(
            title = "Personalized Scientific Trajectory",
            summary = if (weakConcepts.isNotEmpty()) {
                "Remediating prerequisite gaps in ${weakConcepts.first().name} before advancing into ${steps.lastOrNull()?.subject ?: "higher physics"}."
            } else {
                "Accelerating mastery across ${steps.size} interconnected concepts."
            },
            focusArea = primarySubject,
            totalSteps = steps.size,
            completedSteps = completedCount,
            steps = steps
        )
    }

    private fun determineSimType(name: String, subject: String): String? {
        val lower = name.lowercase()
        return when {
            lower.contains("jupiter") || lower.contains("orbit") || subject.equals("Space", ignoreCase = true) -> "ORBITAL"
            lower.contains("pendulum") || lower.contains("harmonic") -> "PENDULUM"
            lower.contains("circuit") || lower.contains("ohm") || lower.contains("rlc") -> "CIRCUIT"
            lower.contains("population") || lower.contains("predator") || subject.equals("Biology", ignoreCase = true) -> "POPULATION"
            lower.contains("projectile") || lower.contains("second law") || lower.contains("accel") -> "PROJECTILE"
            else -> null
        }
    }
}
