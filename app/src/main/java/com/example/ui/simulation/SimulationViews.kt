package com.example.ui.simulation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// --- 1. PROJECTILE MOTION VIEW ---
@Composable
fun ProjectileSimulationView(
    modifier: Modifier = Modifier
) {
    var v0 by remember { mutableFloatStateOf(30f) }
    var angle by remember { mutableFloatStateOf(45f) }
    var gravity by remember { mutableFloatStateOf(9.8f) }
    var airDrag by remember { mutableFloatStateOf(0.04f) }
    var isRunning by remember { mutableStateOf(false) }
    var currentT by remember { mutableFloatStateOf(0f) }

    val state = remember(v0, angle, gravity, airDrag) {
        ProjectileState(
            velocity0 = v0,
            angleDeg = angle,
            gravity = gravity,
            airResistanceCoeff = airDrag
        )
    }

    val trajectory = remember(state) { state.calculateTrajectory(120) }
    val maxDist = (trajectory.lastOrNull()?.first ?: 10f).coerceAtLeast(10f)
    val maxH = (trajectory.maxOfOrNull { it.second } ?: 10f).coerceAtLeast(10f)

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val totalTime = state.idealTimeOfFlight * 1.2f
            while (isRunning) {
                delay(30)
                currentT += 0.05f
                if (currentT >= totalTime) {
                    currentT = 0f
                }
            }
        }
    }

    Column(modifier = modifier) {
        // Live Telemetry Readout Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Range",
                value = String.format("%.1f m", trajectory.lastOrNull()?.first ?: 0f),
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Apex Height",
                value = String.format("%.1f m", maxH),
                color = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Flight Time",
                value = String.format("%.2f s", state.idealTimeOfFlight),
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulation Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D1424))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(230.dp).padding(16.dp)) {
                val w = size.width
                val h = size.height
                val groundY = h - 20f

                // Draw Ground
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, groundY),
                    end = Offset(w, groundY),
                    strokeWidth = 3f
                )

                // Draw Grid Marks
                for (i in 0..4) {
                    val gx = (w * i) / 4f
                    drawLine(
                        color = Color(0x2294A3B8),
                        start = Offset(gx, 0f),
                        end = Offset(gx, groundY),
                        strokeWidth = 1f
                    )
                }

                // Scale factors
                val scaleX = (w - 40f) / (maxDist * 1.15f)
                val scaleY = (groundY - 30f) / (maxH * 1.25f)

                // Draw Trajectory Curve
                if (trajectory.size > 1) {
                    val path = Path()
                    val startPt = trajectory[0]
                    path.moveTo(startPt.first * scaleX, groundY - (startPt.second * scaleY))
                    for (i in 1 until trajectory.size) {
                        val pt = trajectory[i]
                        path.lineTo(pt.first * scaleX, groundY - (pt.second * scaleY))
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                }

                // Draw Current Ball Position
                val (currX, currY) = state.getPositionAt(currentT)
                val ballPx = currX * scaleX
                val ballPy = groundY - (currY * scaleY)

                // Ball Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8), Color.Transparent),
                        center = Offset(ballPx, ballPy),
                        radius = 24f
                    ),
                    radius = 24f,
                    center = Offset(ballPx, ballPy)
                )
                // Ball Core
                drawCircle(
                    color = Color(0xFFFFFFFF),
                    radius = 7f,
                    center = Offset(ballPx, ballPy)
                )

                // Velocity vector arrow from ball
                val angleR = state.angleRad
                val arrowLen = 35f
                drawLine(
                    color = Color(0xFFFBBF24),
                    start = Offset(ballPx, ballPy),
                    end = Offset(ballPx + cos(angleR) * arrowLen, ballPy - sin(angleR) * arrowLen),
                    strokeWidth = 2f
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Play / Pause Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else Color(0xFF0EA5E9)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Launch"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "Pause" else "Launch Projectile")
            }

            IconButton(onClick = {
                currentT = 0f
                isRunning = false
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF94A3B8))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Variable Sliders
        SimulationSlider(
            label = "Launch Velocity (v0)",
            value = v0,
            range = 10f..60f,
            unit = "m/s",
            onValueChange = { v0 = it; currentT = 0f }
        )
        SimulationSlider(
            label = "Launch Angle (θ)",
            value = angle,
            range = 10f..85f,
            unit = "°",
            onValueChange = { angle = it; currentT = 0f }
        )
        SimulationSlider(
            label = "Gravity (g)",
            value = gravity,
            range = 1.6f..24.8f,
            unit = "m/s²",
            onValueChange = { gravity = it; currentT = 0f }
        )
        SimulationSlider(
            label = "Air Resistance (k)",
            value = airDrag,
            range = 0.0f..0.15f,
            unit = "drag coeff",
            onValueChange = { airDrag = it; currentT = 0f }
        )
    }
}

// --- 2. ORBITAL MECHANICS VIEW ("What happens if Jupiter disappears?") ---
@Composable
fun OrbitalSimulationView(
    modifier: Modifier = Modifier
) {
    var jupiterPresent by remember { mutableStateOf(true) }
    var jupiterMassMult by remember { mutableFloatStateOf(1.0f) }
    var simSpeed by remember { mutableFloatStateOf(1f) }
    var isRunning by remember { mutableStateOf(true) }

    var earthAngle by remember { mutableFloatStateOf(0f) }
    var jupiterAngle by remember { mutableFloatStateOf(1.2f) }

    // Asteroids list
    val asteroidAngles = remember {
        mutableStateListOf(0.2f, 0.7f, 1.1f, 1.8f, 2.4f, 3.1f, 3.6f, 4.2f, 4.8f, 5.3f, 5.9f)
    }
    val asteroidRadii = remember {
        mutableStateListOf(140f, 148f, 155f, 142f, 150f, 158f, 145f, 152f, 143f, 156f, 147f)
    }
    var threateningEarthCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning, jupiterPresent, jupiterMassMult) {
        while (isRunning) {
            delay(25)
            earthAngle += 0.035f * simSpeed
            if (jupiterPresent) {
                jupiterAngle += 0.010f * simSpeed
            }

            var threat = 0
            for (i in asteroidAngles.indices) {
                // Orbital speed Keplerian
                val baseSpeed = 0.020f * simSpeed
                asteroidAngles[i] = (asteroidAngles[i] + baseSpeed) % (2 * PI.toFloat())

                if (!jupiterPresent) {
                    // Gravitational destabilization without Jupiter's resonant locking
                    asteroidRadii[i] += (sin(asteroidAngles[i] * 3) * 0.4f)
                    if (asteroidRadii[i] < 95f) {
                        threat++
                    }
                } else {
                    // Jupiter keeps asteroid belt safely locked outside Earth's orbit
                    val targetR = 145f + (i % 5) * 3f
                    asteroidRadii[i] += (targetR - asteroidRadii[i]) * 0.05f
                }
            }
            threateningEarthCount = threat
        }
    }

    Column(modifier = modifier) {
        // Status Alert Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (jupiterPresent) Color(0xFF0F1E2E) else Color(0xFF2E1018)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (jupiterPresent) Color(0xFF0EA5E9) else Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (jupiterPresent) "Jupiter Present (Gravitational Shield Active)" else "Jupiter Removed (Inner System Destabilizing!)",
                        fontWeight = FontWeight.Bold,
                        color = if (jupiterPresent) Color(0xFF38BDF8) else Color(0xFFF87171),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (jupiterPresent) "Kirkwood resonance intact; Earth is protected." else "Asteroid orbits perturbed; $threateningEarthCount crossing inner Earth orbit!",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = jupiterPresent,
                    onCheckedChange = { jupiterPresent = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF38BDF8),
                        checkedTrackColor = Color(0xFF0369A1)
                    )
                )
            }
        }

        // Space Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF080C16))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Sun
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFBBF24), Color(0xFFEA580C), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = 28f
                    ),
                    radius = 28f,
                    center = Offset(cx, cy)
                )
                drawCircle(color = Color(0xFFFFFBEB), radius = 10f, center = Offset(cx, cy))

                // Earth Orbit ring
                val earthR = 75f
                drawCircle(
                    color = Color(0x3338BDF8),
                    radius = earthR,
                    center = Offset(cx, cy),
                    style = Stroke(1.5f)
                )

                // Asteroid Belt Zone (Faint Ring)
                drawCircle(
                    color = Color(0x1594A3B8),
                    radius = 145f,
                    center = Offset(cx, cy),
                    style = Stroke(25f)
                )

                // Jupiter Orbit ring (if present)
                val jupiterR = 210f
                if (jupiterPresent) {
                    drawCircle(
                        color = Color(0x33C084FC),
                        radius = jupiterR,
                        center = Offset(cx, cy),
                        style = Stroke(1.5f)
                    )
                }

                // Earth Position
                val ex = cx + earthR * cos(earthAngle)
                val ey = cy + earthR * sin(earthAngle)
                drawCircle(color = Color(0xFF0284C7), radius = 6.5f, center = Offset(ex, ey))
                drawCircle(color = Color(0xFF67E8F9), radius = 3f, center = Offset(ex, ey))

                // Jupiter Position (if present)
                if (jupiterPresent) {
                    val jx = cx + jupiterR * cos(jupiterAngle)
                    val jy = cy + jupiterR * sin(jupiterAngle)
                    // Jupiter Gravity Well Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x55C084FC), Color.Transparent),
                            center = Offset(jx, jy),
                            radius = 35f
                        ),
                        radius = 35f,
                        center = Offset(jx, jy)
                    )
                    // Jupiter Body
                    drawCircle(color = Color(0xFFE879F9), radius = 13f, center = Offset(jx, jy))
                    drawCircle(color = Color(0xFFFFFFFF), radius = 4f, center = Offset(jx, jy))
                }

                // Asteroids
                for (i in asteroidAngles.indices) {
                    val aR = asteroidRadii[i]
                    val ax = cx + aR * cos(asteroidAngles[i])
                    val ay = cy + aR * sin(asteroidAngles[i])
                    val isDanger = !jupiterPresent && aR < 100f
                    drawCircle(
                        color = if (isDanger) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        radius = if (isDanger) 3.5f else 2.5f,
                        center = Offset(ax, ay)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders
        SimulationSlider(
            label = "Jupiter Mass Multiplier",
            value = jupiterMassMult,
            range = 0.2f..3.0f,
            unit = "x M_jup",
            onValueChange = { jupiterMassMult = it }
        )
        SimulationSlider(
            label = "Orbital Simulation Speed",
            value = simSpeed,
            range = 0.5f..3.0f,
            unit = "x",
            onValueChange = { simSpeed = it }
        )
    }
}

// --- 3. HARMONIC & DAMPED PENDULUM VIEW ---
@Composable
fun PendulumSimulationView(
    modifier: Modifier = Modifier
) {
    var length by remember { mutableFloatStateOf(1.8f) }
    var damping by remember { mutableFloatStateOf(0.12f) }
    var gravity by remember { mutableFloatStateOf(9.8f) }
    var isRunning by remember { mutableStateOf(true) }

    var theta by remember { mutableFloatStateOf(0.75f) }
    var omega by remember { mutableFloatStateOf(0f) }

    val period = (2 * PI * sqrt(length / gravity)).toFloat()

    LaunchedEffect(isRunning, length, damping, gravity) {
        val dt = 0.025f
        while (isRunning) {
            delay(25)
            val alpha = -(gravity / length) * sin(theta) - damping * omega
            omega += alpha * dt
            theta += omega * dt
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Angle (θ)",
                value = String.format("%.1f°", (theta * 180f / PI).toFloat()),
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Ang. Speed (ω)",
                value = String.format("%.2f rad/s", omega),
                color = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Period (T)",
                value = String.format("%.2f s", period),
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D1424))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                val pivotX = size.width / 2f
                val pivotY = 25f

                // Pivot Base
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(pivotX - 35f, pivotY),
                    end = Offset(pivotX + 35f, pivotY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )

                // Scaling length to canvas
                val rodPixels = (length * 75f).coerceIn(60f, 175f)
                val bobX = pivotX + rodPixels * sin(theta)
                val bobY = pivotY + rodPixels * cos(theta)

                // Faint equilibrium dashed line
                drawLine(
                    color = Color(0x3394A3B8),
                    start = Offset(pivotX, pivotY),
                    end = Offset(pivotX, pivotY + rodPixels + 15f),
                    strokeWidth = 1.5f
                )

                // Pendulum Rod
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(pivotX, pivotY),
                    end = Offset(bobX, bobY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Bob Glow & Circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8), Color.Transparent),
                        center = Offset(bobX, bobY),
                        radius = 24f
                    ),
                    radius = 24f,
                    center = Offset(bobX, bobY)
                )
                drawCircle(color = Color(0xFF0284C7), radius = 12f, center = Offset(bobX, bobY))
                drawCircle(color = Color(0xFFFFFFFF), radius = 4f, center = Offset(bobX, bobY))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else Color(0xFF0EA5E9)
                )
            ) {
                Text(if (isRunning) "Pause" else "Resume")
            }
            OutlinedButton(onClick = {
                theta = 0.8f
                omega = 0f
            }) {
                Text("Reset Angle to 45°")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SimulationSlider(
            label = "Rod Length (L)",
            value = length,
            range = 0.5f..3.0f,
            unit = "m",
            onValueChange = { length = it }
        )
        SimulationSlider(
            label = "Air Damping (b)",
            value = damping,
            range = 0.0f..0.4f,
            unit = "coeff",
            onValueChange = { damping = it }
        )
        SimulationSlider(
            label = "Gravity (g)",
            value = gravity,
            range = 1.6f..24.8f,
            unit = "m/s²",
            onValueChange = { gravity = it }
        )
    }
}

// --- 4. RLC CIRCUIT & RESONANCE VIEW ---
@Composable
fun CircuitSimulationView(
    modifier: Modifier = Modifier
) {
    var resistance by remember { mutableFloatStateOf(10f) }
    var inductanceMilliH by remember { mutableFloatStateOf(50f) } // mH
    var capacitanceMicroF by remember { mutableFloatStateOf(100f) } // uF
    var frequency by remember { mutableFloatStateOf(60f) } // Hz

    val lHenries = inductanceMilliH / 1000f
    val cFarads = capacitanceMicroF / 1000000f
    val state = remember(resistance, lHenries, cFarads, frequency) {
        CircuitState(
            resistance = resistance,
            inductance = lHenries,
            capacitance = cFarads,
            frequency = frequency
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "circuit_electrons")
    val electronOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((1000 / (frequency / 10).coerceAtLeast(1f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "electrons"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Resonant Freq (f₀)",
                value = String.format("%.1f Hz", state.resonantFrequency),
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Impedance (Z)",
                value = String.format("%.1f Ω", state.impedance),
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Current (I_peak)",
                value = String.format("%.2f A", state.currentAmplitude),
                color = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Circuit Diagram & Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D1424))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(230.dp).padding(16.dp)) {
                val w = size.width
                val h = size.height

                // Draw AC Waveform (Voltage vs Current)
                val midY = h / 2f
                drawLine(
                    color = Color(0x3394A3B8),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1f
                )

                val vPath = Path()
                val iPath = Path()
                val vAmp = 45f
                val iAmp = (state.currentAmplitude * 25f).coerceIn(8f, 65f)
                val phase = state.phaseShiftRad

                for (px in 0..w.toInt() step 3) {
                    val xVal = px.toFloat()
                    val waveAngle = (xVal / w) * (4 * PI.toFloat())
                    val vy = midY - vAmp * sin(waveAngle)
                    val iy = midY - iAmp * sin(waveAngle - phase)

                    if (px == 0) {
                        vPath.moveTo(xVal, vy)
                        iPath.moveTo(xVal, iy)
                    } else {
                        vPath.lineTo(xVal, vy)
                        iPath.lineTo(xVal, iy)
                    }
                }

                // Voltage Wave (Cyan)
                drawPath(
                    path = vPath,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 2.5f)
                )

                // Current Wave (Violet / Phase shifted)
                drawPath(
                    path = iPath,
                    color = Color(0xFFC084FC),
                    style = Stroke(width = 2.5f)
                )

                // Flowing Electron Particle Indicator
                val electronX = electronOffset * w
                val electronY = midY - iAmp * sin((electronX / w) * (4 * PI.toFloat()) - phase)
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 5f,
                    center = Offset(electronX, electronY)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SimulationSlider(
            label = "Driving Frequency (f)",
            value = frequency,
            range = 10f..200f,
            unit = "Hz",
            onValueChange = { frequency = it }
        )
        SimulationSlider(
            label = "Resistance (R)",
            value = resistance,
            range = 1f..50f,
            unit = "Ω",
            onValueChange = { resistance = it }
        )
        SimulationSlider(
            label = "Inductance (L)",
            value = inductanceMilliH,
            range = 10f..200f,
            unit = "mH",
            onValueChange = { inductanceMilliH = it }
        )
        SimulationSlider(
            label = "Capacitance (C)",
            value = capacitanceMicroF,
            range = 20f..500f,
            unit = "μF",
            onValueChange = { capacitanceMicroF = it }
        )
    }
}

// --- 5. LOTKA-VOLTERRA POPULATION DYNAMICS VIEW ---
@Composable
fun PopulationSimulationView(
    modifier: Modifier = Modifier
) {
    var alpha by remember { mutableFloatStateOf(0.8f) }
    var beta by remember { mutableFloatStateOf(0.04f) }
    var gamma by remember { mutableFloatStateOf(0.6f) }
    var delta by remember { mutableFloatStateOf(0.02f) }

    var preyCount by remember { mutableFloatStateOf(35f) }
    var predCount by remember { mutableFloatStateOf(14f) }
    val preyHistory = remember { mutableStateListOf<Float>() }
    val predHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(alpha, beta, gamma, delta) {
        while (true) {
            delay(40)
            val dt = 0.04f
            val dPrey = (alpha * preyCount - beta * preyCount * predCount) * dt
            val dPred = (delta * preyCount * predCount - gamma * predCount) * dt
            preyCount = (preyCount + dPrey).coerceAtLeast(1f)
            predCount = (predCount + dPred).coerceAtLeast(1f)

            preyHistory.add(preyCount)
            predHistory.add(predCount)
            if (preyHistory.size > 80) preyHistory.removeAt(0)
            if (predHistory.size > 80) predHistory.removeAt(0)
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Prey Population",
                value = String.format("%.0f", preyCount),
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Predator Count",
                value = String.format("%.0f", predCount),
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Cycle Phase",
                value = if (preyCount > predCount * 2) "Abundance" else "Predation",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D1424))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(230.dp).padding(16.dp)) {
                val w = size.width
                val h = size.height

                // Draw Dual Line Curves
                if (preyHistory.size > 2) {
                    val preyPath = Path()
                    val predPath = Path()
                    val dx = w / 80f
                    val scaleY = (h - 20f) / 70f

                    for (i in preyHistory.indices) {
                        val px = i * dx
                        val pyPrey = h - (preyHistory[i] * scaleY).coerceIn(5f, h - 5f)
                        val pyPred = h - (predHistory[i] * scaleY).coerceIn(5f, h - 5f)

                        if (i == 0) {
                            preyPath.moveTo(px, pyPrey)
                            predPath.moveTo(px, pyPred)
                        } else {
                            preyPath.lineTo(px, pyPrey)
                            predPath.lineTo(px, pyPred)
                        }
                    }

                    // Prey Curve (Emerald)
                    drawPath(
                        path = preyPath,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                    // Predator Curve (Rose/Red)
                    drawPath(
                        path = predPath,
                        color = Color(0xFFF43F5E),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SimulationSlider(
            label = "Prey Birth Rate (α)",
            value = alpha,
            range = 0.2f..1.5f,
            unit = "rate",
            onValueChange = { alpha = it }
        )
        SimulationSlider(
            label = "Predation Rate (β)",
            value = beta,
            range = 0.01f..0.08f,
            unit = "encounters",
            onValueChange = { beta = it }
        )
        SimulationSlider(
            label = "Predator Mortality (γ)",
            value = gamma,
            range = 0.2f..1.2f,
            unit = "death rate",
            onValueChange = { gamma = it }
        )
    }
}

// --- HELPER COMPONENTS ---
@Composable
fun TelemetryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2F)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SimulationSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
            Text(
                text = String.format("%.2f %s", value, unit),
                fontSize = 12.sp,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF0284C7),
                inactiveTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
