package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningPathStep
import com.example.data.model.PathStepType
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.simulation.SimulationType

@Composable
fun LearningPathScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val path by viewModel.adaptiveLearningPath.collectAsState()
    val allConcepts by viewModel.allConcepts.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER & TRAJECTORY SUMMARY ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("learning_path_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adaptive Learning Path",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = path.focusArea,
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = path.summary,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Path Completion",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${path.completedSteps} / ${path.totalSteps} Milestones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val progressFraction = if (path.totalSteps > 0) path.completedSteps.toFloat() / path.totalSteps else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // --- 2. STEP TIMELINE ---
        item {
            Text(
                text = "Sequenced Curriculum (Prerequisites Prioritized):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )
        }

        items(path.steps) { step ->
            LearningPathStepCard(
                step = step,
                onStudyInGraph = {
                    val target = allConcepts.firstOrNull { it.id == step.conceptId }
                    if (target != null) {
                        viewModel.selectConcept(target)
                        viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                    }
                },
                onLaunchSimulation = { simTypeStr ->
                    val type = when (simTypeStr.uppercase()) {
                        "ORBITAL" -> SimulationType.ORBITAL
                        "PENDULUM" -> SimulationType.PENDULUM
                        "CIRCUIT" -> SimulationType.CIRCUIT
                        "POPULATION" -> SimulationType.POPULATION
                        else -> SimulationType.PROJECTILE
                    }
                    viewModel.setSimulation(type)
                    viewModel.navigateTo(AppScreen.SIMULATOR)
                },
                onPractice = {
                    viewModel.navigateTo(AppScreen.PRACTICE)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun LearningPathStepCard(
    step: LearningPathStep,
    onStudyInGraph: () -> Unit,
    onLaunchSimulation: (String) -> Unit,
    onPractice: () -> Unit
) {
    val badgeColor = Color(step.stepType.badgeColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("learning_path_step_${step.stepNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (step.stepType == PathStepType.PREREQUISITE_REMEDIATION) Color(0xFF1A101A) else Color(0xFF0F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (step.stepType == PathStepType.PREREQUISITE_REMEDIATION) badgeColor.copy(alpha = 0.6f) else Color(0xFF1E293B)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = step.conceptName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${step.subject} • ~${step.estimatedMinutes} mins",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = step.stepType.label,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rationale
            Text(
                text = step.reason,
                fontSize = 12.5.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStudyInGraph,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Graph", fontSize = 11.5.sp)
                }

                if (step.simulationType != null) {
                    Button(
                        onClick = { onLaunchSimulation(step.simulationType) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onPractice,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
