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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Concept
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.graph.InteractiveKnowledgeGraph
import com.example.ui.simulation.SimulationType
import org.json.JSONArray

@Composable
fun KnowledgeGraphScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val concepts by viewModel.allConcepts.collectAsState()
    val relationships by viewModel.allRelationships.collectAsState()
    val selectedConcept by viewModel.selectedConcept.collectAsState()

    var subjectFilter by remember { mutableStateOf("All") }
    val subjects = listOf("All", "Physics", "Space", "Circuits", "Biology", "Mathematics")

    val filteredConcepts = remember(concepts, subjectFilter) {
        if (subjectFilter == "All") concepts
        else concepts.filter { it.subject.equals(subjectFilter, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // --- 1. FULLSCREEN INTERACTIVE GRAPH ---
        InteractiveKnowledgeGraph(
            concepts = filteredConcepts,
            relationships = relationships,
            selectedConcept = selectedConcept,
            onConceptSelected = { viewModel.selectConcept(it) },
            modifier = Modifier.fillMaxSize()
        )

        // --- 2. TOP CONTROLS & FILTER BAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            // Header with Node Count and Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE60D1424))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Knowledge Constellation (${filteredConcepts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Legend Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(color = Color(0xFF10B981), label = "Mastered")
                    LegendDot(color = Color(0xFF0EA5E9), label = "Learning")
                    LegendDot(color = Color(0xFFEF4444), label = "Weak")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(subjects) { subj ->
                    val isSelected = subj == subjectFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF0284C7) else Color(0xCC0F172A))
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .clickable { subjectFilter = subj }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = subj,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // --- 3. BOTTOM INSPECTION PANEL FOR SELECTED CONCEPT ---
        if (selectedConcept != null) {
            val concept = selectedConcept!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.BottomCenter)
                    .testTag("concept_inspector_sheet"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = concept.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${concept.subject} • ${concept.difficulty}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.selectConcept(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = concept.description,
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Estimated Mastery Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Mastery (App Score)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${concept.masteryScore}% • ${concept.masteryLevel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (concept.masteryLevel) {
                                "Mastered" -> Color(0xFF10B981)
                                "Weak" -> Color(0xFFEF4444)
                                else -> Color(0xFF0EA5E9)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (concept.masteryScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (concept.masteryLevel) {
                            "Mastered" -> Color(0xFF10B981)
                            "Weak" -> Color(0xFFEF4444)
                            else -> Color(0xFF0EA5E9)
                        },
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prerequisites List
                    val prereqs = remember(concept.prerequisitesJson) {
                        try {
                            val arr = JSONArray(concept.prerequisitesJson)
                            (0 until arr.length()).map { arr.optString(it) }
                        } catch (_: Exception) { emptyList() }
                    }
                    if (prereqs.isNotEmpty()) {
                        Text("Prerequisites:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(prereqs) { pName ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable {
                                            val pConcept = concepts.firstOrNull { it.name.equals(pName, ignoreCase = true) }
                                            if (pConcept != null) viewModel.selectConcept(pConcept)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(pName, fontSize = 11.sp, color = Color(0xFF38BDF8))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.PRACTICE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            modifier = Modifier.weight(1f).testTag("practice_concept_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val simType = when {
                                    concept.name.contains("Jupiter", ignoreCase = true) || concept.subject.equals("Space", ignoreCase = true) -> SimulationType.ORBITAL
                                    concept.name.contains("Pendulum", ignoreCase = true) -> SimulationType.PENDULUM
                                    concept.name.contains("Circuit", ignoreCase = true) || concept.subject.equals("Circuits", ignoreCase = true) -> SimulationType.CIRCUIT
                                    concept.subject.equals("Biology", ignoreCase = true) -> SimulationType.POPULATION
                                    else -> SimulationType.PROJECTILE
                                }
                                viewModel.setSimulation(simType)
                                viewModel.navigateTo(AppScreen.SIMULATOR)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.weight(1f).testTag("simulate_concept_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.sendAiPrompt("Explain ${concept.name} deeply and uncover all its underlying physical mechanisms.")
                                viewModel.navigateTo(AppScreen.AI_TUTOR)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask AI", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}
