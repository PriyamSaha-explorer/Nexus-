package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.simulation.SimulationType

@Composable
fun SearchScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val concepts by viewModel.allConcepts.collectAsState()
    val questions by viewModel.allQuestions.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()

    val filteredConcepts = if (query.isBlank()) emptyList() else concepts.filter {
        it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true)
    }

    val filteredQuestions = if (query.isBlank()) emptyList() else questions.filter {
        it.questionText.contains(query, ignoreCase = true) || it.conceptName.contains(query, ignoreCase = true)
    }

    val filteredDocuments = if (query.isBlank()) emptyList() else documents.filter {
        it.title.contains(query, ignoreCase = true) || it.summary.contains(query, ignoreCase = true)
    }

    val matchingSimulations = if (query.isBlank()) emptyList() else SimulationType.values().filter {
        it.displayName.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search concepts, questions, simulations, notes...", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                modifier = Modifier.fillMaxWidth().testTag("global_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0D1424),
                    unfocusedContainerColor = Color(0xFF0D1424),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        if (query.isBlank()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type a term to search across concepts, questions, simulations, and documents.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Concepts Group
            if (filteredConcepts.isNotEmpty()) {
                item {
                    Text("Concepts (${filteredConcepts.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
                items(filteredConcepts) { c ->
                    SearchResultCard(
                        title = c.name,
                        subtitle = "${c.subject} • ${c.difficulty} • ${c.masteryLevel} (${c.masteryScore}%)",
                        icon = Icons.Default.Hub,
                        iconColor = Color(0xFF38BDF8),
                        onClick = {
                            viewModel.selectConcept(c)
                            viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                        }
                    )
                }
            }

            // Simulations Group
            if (matchingSimulations.isNotEmpty()) {
                item {
                    Text("Simulations (${matchingSimulations.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                }
                items(matchingSimulations) { sim ->
                    SearchResultCard(
                        title = sim.displayName,
                        subtitle = "${sim.subject} Simulation Laboratory",
                        icon = Icons.Default.Tune,
                        iconColor = Color(0xFFA855F7),
                        onClick = {
                            viewModel.setSimulation(sim)
                            viewModel.navigateTo(AppScreen.SIMULATOR)
                        }
                    )
                }
            }

            // Questions Group
            if (filteredQuestions.isNotEmpty()) {
                item {
                    Text("Practice Questions (${filteredQuestions.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                items(filteredQuestions) { q ->
                    SearchResultCard(
                        title = q.conceptName,
                        subtitle = q.questionText.take(90) + "…",
                        icon = Icons.Default.Calculate,
                        iconColor = Color(0xFF10B981),
                        onClick = {
                            viewModel.navigateTo(AppScreen.PRACTICE)
                        }
                    )
                }
            }

            // Documents Group
            if (filteredDocuments.isNotEmpty()) {
                item {
                    Text("Study Documents (${filteredDocuments.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                }
                items(filteredDocuments) { doc ->
                    SearchResultCard(
                        title = doc.title,
                        subtitle = doc.summary.take(90) + "…",
                        icon = Icons.Default.Description,
                        iconColor = Color(0xFFEC4899),
                        onClick = {
                            viewModel.navigateTo(AppScreen.DOCUMENTS)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 11.5.sp, color = Color(0xFF94A3B8), maxLines = 2)
            }
        }
    }
}
