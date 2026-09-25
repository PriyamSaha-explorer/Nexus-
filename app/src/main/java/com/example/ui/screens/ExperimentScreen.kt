package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Experiment
import com.example.ui.NexusViewModel
import com.example.ui.simulation.OrbitalSimulationView
import com.example.ui.simulation.ProjectileSimulationView
import java.util.UUID

@Composable
fun ExperimentScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val experiments by viewModel.allExperiments.collectAsState()

    var question by remember { mutableStateOf("What happens to orbital stability and terrestrial collision rates if Jupiter's mass is altered or removed?") }
    var hypothesis by remember { mutableStateOf("If Jupiter's gravitational field is removed, Kirkwood orbital resonances in the asteroid belt will destabilize, dramatically increasing asteroid crossings into Earth's orbit.") }
    var observations by remember { mutableStateOf("Toggling Jupiter off causes outer asteroids to disperse inward within several simulation cycles; crossing count increases from 0 to 4.") }
    var conclusion by remember { mutableStateOf("Jupiter acts as a critical gravitational shield and perturbation barrier for the inner solar system, verifying the three-body resonance stabilization principle.") }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI Scientific Experiment Laboratory",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Question → Hypothesis → Variables → Live Simulation → Conclusion",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // --- 2. SCIENTIFIC METHOD WORKFLOW CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("experiment_workflow_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Question
                    Text("1. Scientific Question", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hypothesis
                    Text("2. Hypothesis", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = hypothesis,
                        onValueChange = { hypothesis = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Variables Matrix
                    Text("3. Experimental Variables", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VariableCard(label = "Independent", value = "Jupiter Presence / Mass", color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                        VariableCard(label = "Dependent", value = "Asteroid Crossing Count", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        VariableCard(label = "Controlled", value = "Solar Mass & Earth Orbit", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 3. LIVE INTERACTIVE SIMULATION LAB ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("4. Live Simulation Verification", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Manipulate independent variables and observe live gravitational perturbations:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(10.dp))
                    OrbitalSimulationView()
                }
            }
        }

        // --- 4. OBSERVATIONS & CONCLUSION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("5. Experimental Observations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = observations,
                        onValueChange = { observations = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("6. Scientific Conclusion", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = conclusion,
                        onValueChange = { conclusion = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val exp = Experiment(
                                id = UUID.randomUUID().toString(),
                                title = "Celestial Perturbation & Jupiter Gravitational Shield",
                                subject = "Space Science",
                                question = question,
                                hypothesis = hypothesis,
                                variablesJson = "[{\"type\":\"Independent\",\"name\":\"Jupiter Mass\"}]",
                                assumptionsJson = "[\"Coplanar orbits\",\"Point masses\"]",
                                simulationType = "ORBITAL",
                                observations = observations,
                                conclusion = conclusion
                            )
                            viewModel.saveExperiment(exp)
                            isSaved = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSaved) Color(0xFF059669) else Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_experiment_button")
                    ) {
                        Icon(if (isSaved) Icons.Default.Check else Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSaved) "Experiment Saved to Laboratory Log" else "Record & Save Scientific Experiment", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. PAST EXPERIMENT LOG ---
        if (experiments.isNotEmpty()) {
            item {
                Text("Saved Laboratory Log (${experiments.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            }

            items(experiments) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(exp.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Question: ${exp.question}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Conclusion: ${exp.conclusion}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

@Composable
fun VariableCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141F36))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 11.sp, color = Color.White, maxLines = 2)
        }
    }
}
