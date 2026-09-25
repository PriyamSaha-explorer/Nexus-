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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExplanationMode
import com.example.data.model.NexusChatMessage
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.simulation.SimulationType

@Composable
fun AiTutorScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val currentMode by viewModel.explanationMode.collectAsState()
    val allConcepts by viewModel.allConcepts.collectAsState()

    var textInput by remember { mutableStateOf("") }

    val suggestedPrompts = listOf(
        "Why do planets orbit in ellipses?",
        "What happens if Jupiter disappears?",
        "Explain Newton's Second Law simply",
        "How does an RLC circuit achieve resonance?",
        "Why do predator and prey populations oscillate?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // --- 1. "TEACH ME LIKE I LEARN" MODE SELECTOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1424))
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Teach Me Like I Learn:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentMode.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation Mode Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ExplanationMode.values()) { mode ->
                    val isSelected = mode == currentMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setExplanationMode(mode) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // --- 2. CONVERSATION MESSAGES ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    onInspectConcept = { name ->
                        val target = allConcepts.firstOrNull { it.name.equals(name, ignoreCase = true) }
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
                    }
                )
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NEXUS is synthesizing first-principles & mapping knowledge graph...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // --- 3. SUGGESTED PROMPTS ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B101D))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.sendAiPrompt(prompt)
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(prompt, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
            }
        }

        // --- 4. INPUT BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1424))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.IMAGE_ANALYSIS) },
                modifier = Modifier.testTag("ai_tutor_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Attach Diagram",
                    tint = Color(0xFF38BDF8)
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask a scientific question...", color = Color(0xFF64748B), fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_tutor_text_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF070B14),
                    unfocusedContainerColor = Color(0xFF070B14),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        val prompt = textInput
                        textInput = ""
                        viewModel.sendAiPrompt(prompt)
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0EA5E9))
                    .testTag("ai_tutor_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: NexusChatMessage,
    onInspectConcept: (String) -> Unit,
    onLaunchSimulation: (String) -> Unit
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp))
                    .background(Color(0xFF0369A1))
                    .padding(14.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        // AI Structured Response Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXUS AI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 1.sp
                        )
                    }

                    if (message.structuredData != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${message.structuredData.subject} • ${message.structuredData.difficulty}",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Text body
                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 20.sp
                )

                // Structured Analysis Elements
                val data = message.structuredData
                if (data != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Prerequisites Section
                    if (data.prerequisites.isNotEmpty()) {
                        Text(
                            text = "Prerequisites to master first:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(data.prerequisites) { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { onInspectConcept(p) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(p, fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Simulation Launcher if available
                    if (!data.simulationType.isNullOrBlank()) {
                        Button(
                            onClick = { onLaunchSimulation(data.simulationType) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("launch_simulation_from_ai_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Interactive ${data.simulationType} Simulation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
