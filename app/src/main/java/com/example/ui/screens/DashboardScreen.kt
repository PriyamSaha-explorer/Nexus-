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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Concept
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.graph.InteractiveKnowledgeGraph

@Composable
fun DashboardScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val concepts by viewModel.allConcepts.collectAsState()
    val relationships by viewModel.allRelationships.collectAsState()
    val weakConcepts by viewModel.weakConcepts.collectAsState()
    val attempts by viewModel.allAttempts.collectAsState()
    val learningPath by viewModel.adaptiveLearningPath.collectAsState()

    val masteredCount = concepts.count { it.masteryLevel == "Mastered" }
    val learningCount = concepts.count { it.masteryLevel == "Learning" }
    val weakCount = concepts.count { it.masteryLevel == "Weak" }
    val correctAttempts = attempts.count { it.isCorrect }
    val totalAttempts = attempts.size
    val accuracy = if (totalAttempts > 0) (correctAttempts * 100) / totalAttempts else 85

    var queryText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. HERO BRANDING & SEARCH BAR ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "NEXUS",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NEXUS",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF8FAFC),
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = "Understand anything. Connect everything.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Normal
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SEARCH) },
                    modifier = Modifier.testTag("dashboard_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central AI prompt launcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("central_ai_input_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F293D))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "What do you want to understand?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("e.g. Why do planets orbit in ellipses?", color = Color(0xFF64748B), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_ai_query_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0B101D),
                            unfocusedContainerColor = Color(0xFF0B101D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            Button(
                                onClick = {
                                    if (queryText.isNotBlank()) {
                                        viewModel.sendAiPrompt(queryText)
                                        queryText = ""
                                        viewModel.navigateTo(AppScreen.AI_TUTOR)
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("home_ask_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                            ) {
                                Text("Ask", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }

        // --- 2. QUICK ACTION LAUNCHPADS ---
        item {
            Text(
                text = "Core Modules",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    ActionLaunchCard(
                        title = "Learning Path",
                        subtitle = "${learningPath.steps.size} Sequenced Topics",
                        icon = Icons.Default.Route,
                        accentColor = Color(0xFF00E5FF),
                        onClick = { viewModel.navigateTo(AppScreen.LEARNING_PATH) },
                        tag = "nav_learning_path"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "Knowledge Graph",
                        subtitle = "${concepts.size} Connected Nodes",
                        icon = Icons.Default.Hub,
                        accentColor = Color(0xFF38BDF8),
                        onClick = { viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH) },
                        tag = "nav_knowledge_graph"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "NEXUS Simulator",
                        subtitle = "5 Deterministic Physics Labs",
                        icon = Icons.Default.Tune,
                        accentColor = Color(0xFFA855F7),
                        onClick = { viewModel.navigateTo(AppScreen.SIMULATOR) },
                        tag = "nav_simulator"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "Adaptive Practice",
                        subtitle = "With 'Why Am I Wrong?'",
                        icon = Icons.Default.Calculate,
                        accentColor = Color(0xFF10B981),
                        onClick = { viewModel.navigateTo(AppScreen.PRACTICE) },
                        tag = "nav_practice"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "Image Scanner",
                        subtitle = "Analyze Scientific Diagrams",
                        icon = Icons.Default.CameraAlt,
                        accentColor = Color(0xFFF59E0B),
                        onClick = { viewModel.navigateTo(AppScreen.IMAGE_ANALYSIS) },
                        tag = "nav_image_scanner"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "Document Intel",
                        subtitle = "Summaries & Flashcards",
                        icon = Icons.Default.Description,
                        accentColor = Color(0xFFEC4899),
                        onClick = { viewModel.navigateTo(AppScreen.DOCUMENTS) },
                        tag = "nav_documents"
                    )
                }
                item {
                    ActionLaunchCard(
                        title = "AI Experiments",
                        subtitle = "Formulate & Test Hypotheses",
                        icon = Icons.Default.Science,
                        accentColor = Color(0xFF06B6D4),
                        onClick = { viewModel.navigateTo(AppScreen.EXPERIMENTS) },
                        tag = "nav_experiments"
                    )
                }
            }
        }

        // --- 3. TODAY'S LEARNING & MASTERY METRICS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1526)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2E4D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Learning Engine Telemetry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "Estimated Mastery",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill(label = "Mastered", count = masteredCount, color = Color(0xFF10B981))
                        MetricPill(label = "Learning", count = learningCount, color = Color(0xFF0EA5E9))
                        MetricPill(label = "Weak Gaps", count = weakCount, color = Color(0xFFEF4444))
                        MetricPill(label = "Accuracy", count = accuracy, unit = "%", color = Color(0xFFA855F7))
                    }
                }
            }
        }

        // --- 4. ADAPTIVE LEARNING PATH SECTION ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.LEARNING_PATH) }
                    .testTag("dashboard_learning_path_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1427)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adaptive Learning Path",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "View All →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = learningPath.summary,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview of Top Sequenced Steps
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        learningPath.steps.take(3).forEach { step ->
                            val badgeColor = Color(step.stepType.badgeColorHex)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF131D36))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${step.stepNumber}",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = step.conceptName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = step.stepType.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }

                                Text(
                                    text = "~${step.estimatedMinutes}m",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.LEARNING_PATH) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("open_full_learning_path_button")
                    ) {
                        Text("Open Complete Learning Path (${learningPath.totalSteps} Milestones)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. PREREQUISITE GAP ALERT & TARGETED REMEDIATION ---
        item {
            val weak = weakConcepts.firstOrNull() ?: concepts.firstOrNull { it.id == "phys_newton2" }
            if (weak != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231018)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Gap Detected",
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prerequisite Gap Alert",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB7185)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Repeated struggle detected with: ${weak.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Instead of another difficult question, NEXUS recommends strengthening foundational prerequisites first:",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Prerequisite Chain Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChainChip(text = "Units", isResolved = true)
                            Text("→", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            ChainChip(text = "Mass", isResolved = true)
                            Text("→", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            ChainChip(text = "Acceleration", isResolved = false)
                            Text("→", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            ChainChip(text = "Force", isResolved = false)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.selectConcept(concepts.firstOrNull { it.id == "phys_accel" } ?: weak)
                                viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("review_prerequisite_button")
                        ) {
                            Text("Study Recommended Prerequisite: Acceleration", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 5. MINI KNOWLEDGE GRAPH PREVIEW ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH) }
                    .testTag("mini_graph_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personal Knowledge Graph",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Tap to Explore →",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        InteractiveKnowledgeGraph(
                            concepts = concepts,
                            relationships = relationships,
                            selectedConcept = null,
                            onConceptSelected = {
                                viewModel.selectConcept(it)
                                viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                            }
                        )
                    }
                }
            }
        }

        // --- 6. RECOMMENDED NEXT TOPICS ---
        item {
            Text(
                text = "Recommended Next Topics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RecommendedTopicCard(
                    title = "Jupiter Gravitational Shield",
                    subject = "Space Science",
                    reason = "Unexplored cluster linked to Orbital Mechanics",
                    onClick = {
                        viewModel.selectConcept(concepts.firstOrNull { it.id == "space_jupiter" })
                        viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                    }
                )
                RecommendedTopicCard(
                    title = "Projectile Motion Simulation",
                    subject = "Physics",
                    reason = "Connect 2D vectors with real-time trajectory canvas",
                    onClick = {
                        viewModel.setSimulation(com.example.ui.simulation.SimulationType.PROJECTILE)
                        viewModel.navigateTo(AppScreen.SIMULATOR)
                    }
                )
                RecommendedTopicCard(
                    title = "RLC Circuit Resonance",
                    subject = "Circuits",
                    reason = "Compare mechanical pendulum harmonic resonance with electrical AC phase",
                    onClick = {
                        viewModel.setSimulation(com.example.ui.simulation.SimulationType.CIRCUIT)
                        viewModel.navigateTo(AppScreen.SIMULATOR)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActionLaunchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(125.dp)
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F5F9),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    count: Int,
    color: Color,
    unit: String = ""
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count$unit",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun ChainChip(text: String, isResolved: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isResolved) Color(0xFF064E3B) else Color(0xFF4C1D24))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isResolved) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
        )
    }
}

@Composable
fun RecommendedTopicCard(
    title: String,
    subject: String,
    reason: String,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(subject, fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(reason, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        }
    }
}
