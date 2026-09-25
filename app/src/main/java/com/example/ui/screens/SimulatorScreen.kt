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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import com.example.ui.simulation.CircuitSimulationView
import com.example.ui.simulation.OrbitalSimulationView
import com.example.ui.simulation.PendulumSimulationView
import com.example.ui.simulation.PopulationSimulationView
import com.example.ui.simulation.ProjectileSimulationView
import com.example.ui.simulation.SimulationType

@Composable
fun SimulatorScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val activeSim by viewModel.activeSimulation.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER & SIMULATION TABS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NEXUS SIMULATOR",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Deterministic physics & biological dynamic solvers",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation selector pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SimulationType.values()) { sim ->
                    val isSelected = sim == activeSim
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF131D33))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setSimulation(sim) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Column {
                            Text(
                                text = sim.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                            Text(
                                text = sim.subject,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. LIVE INTERACTIVE CANVAS SIMULATION ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_simulation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B101D)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = activeSim.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time interactive canvas with live numerical integration",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (activeSim) {
                        SimulationType.PROJECTILE -> ProjectileSimulationView()
                        SimulationType.ORBITAL -> OrbitalSimulationView()
                        SimulationType.PENDULUM -> PendulumSimulationView()
                        SimulationType.CIRCUIT -> CircuitSimulationView()
                        SimulationType.POPULATION -> PopulationSimulationView()
                    }
                }
            }
        }

        // --- 3. SCIENTIFIC ASSUMPTIONS & EQUATIONS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assumptions, Equations & Limitations",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (activeSim) {
                        SimulationType.PROJECTILE -> {
                            EquationRow("x(t) = v₀ cos(θ) · t", "Horizontal uniform motion")
                            EquationRow("y(t) = v₀ sin(θ) · t - (1/2)gt²", "Vertical constant acceleration")
                            EquationRow("F_drag = -k · |v| · v", "Quadratic aerodynamic resistance")
                            AssumptionText("Assumes flat local terrain, constant gravitational field g = 9.8 m/s², and laminar drag coefficient.")
                        }
                        SimulationType.ORBITAL -> {
                            EquationRow("F_g = G(M · m) / r²", "Newton's law of gravitation")
                            EquationRow("T² = (4π² / GM) · a³", "Kepler's third law of orbital periods")
                            EquationRow("Δv_perturb ∝ (G · M_jup) / r_min²", "Jupiter gravitational deflection impulse")
                            AssumptionText("Assumes coplanar planetary orbits, point masses, and unperturbed solar mass dominating heliocentric potential.")
                        }
                        SimulationType.PENDULUM -> {
                            EquationRow("d²θ/dt² + (b/m)(dθ/dt) + (g/L)sin(θ) = 0", "Nonlinear damped harmonic equation")
                            EquationRow("T ≈ 2π√(L/g)", "Small-angle period approximation (θ < 15°)")
                            EquationRow("E = (1/2)m(Lω)² + mgL(1 - cos θ)", "Mechanical energy balance (Kinetic + Potential)")
                            AssumptionText("Assumes rigid massless rod, point bob mass, and linear viscous damping proportional to velocity.")
                        }
                        SimulationType.CIRCUIT -> {
                            EquationRow("L(d²q/dt²) + R(dq/dt) + (1/C)q = V(t)", "Second-order RLC differential law")
                            EquationRow("ω₀ = 1 / √(L · C)", "Undamped resonant angular frequency")
                            EquationRow("Z = √(R² + (ωL - 1/ωC)²)", "Total complex circuit impedance")
                            AssumptionText("Assumes ideal linear lumped components, sinusoidal AC excitation, and negligible parasitic capacitance.")
                        }
                        SimulationType.POPULATION -> {
                            EquationRow("dx/dt = αx - βxy", "Prey rate equation (birth - predation)")
                            EquationRow("dy/dt = δxy - γy", "Predator rate equation (reproduction - natural death)")
                            AssumptionText("Assumes infinite environmental carrying capacity for prey, continuous time derivatives, and homogeneous mixing.")
                        }
                    }
                }
            }
        }

        // --- 4. EXPERIMENT LAB LAUNCHER ---
        item {
            Button(
                onClick = { viewModel.navigateTo(AppScreen.EXPERIMENTS) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_experiment_mode_button")
            ) {
                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Formulate Scientific Experiment with this Simulation", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun EquationRow(formula: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formula,
            fontSize = 12.sp,
            color = Color(0xFF38BDF8),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun AssumptionText(text: String) {
    Text(
        text = "Assumption & Scope: $text",
        fontSize = 11.5.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(top = 6.dp)
    )
}
