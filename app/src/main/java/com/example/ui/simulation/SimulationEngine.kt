package com.example.ui.simulation

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SimulationType(val displayName: String, val subject: String) {
    PROJECTILE("Projectile Motion", "Physics"),
    ORBITAL("Jupiter Gravitational Shield", "Space"),
    PENDULUM("Harmonic & Damped Pendulum", "Physics"),
    CIRCUIT("RLC Circuit & Resonance", "Circuits"),
    POPULATION("Lotka-Volterra Predator-Prey", "Biology")
}

// --- 1. Projectile Motion State ---
data class ProjectileState(
    val velocity0: Float = 25f, // m/s
    val angleDeg: Float = 45f, // degrees
    val gravity: Float = 9.8f, // m/s²
    val airResistanceCoeff: Float = 0.05f, // drag
    val mass: Float = 1.0f, // kg
    val currentTime: Float = 0f,
    val isRunning: Boolean = false,
    val speedMultiplier: Float = 1f
) {
    val angleRad: Float get() = (angleDeg * PI / 180.0).toFloat()
    val initialVx: Float get() = velocity0 * cos(angleRad)
    val initialVy: Float get() = velocity0 * sin(angleRad)

    // Ideal theoretical metrics (zero drag comparison)
    val idealTimeOfFlight: Float get() = (2 * initialVy / gravity).coerceAtLeast(0.01f)
    val idealMaxHeight: Float get() = (initialVy * initialVy) / (2 * gravity)
    val idealRange: Float get() = (velocity0 * velocity0 * sin(2 * angleRad)) / gravity

    // Numerical trajectory points
    fun calculateTrajectory(steps: Int = 100): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        var x = 0f
        var y = 0f
        var vx = initialVx
        var vy = initialVy
        val dt = 0.03f

        points.add(Pair(x, y))
        var t = 0f
        while (y >= 0f && t < 15f && points.size < steps) {
            val speed = sqrt(vx * vx + vy * vy)
            val dragX = -airResistanceCoeff * speed * vx / mass
            val dragY = -airResistanceCoeff * speed * vy / mass - gravity

            vx += dragX * dt
            vy += dragY * dt
            x += vx * dt
            y += vy * dt
            t += dt

            if (y >= 0f) {
                points.add(Pair(x, y))
            }
        }
        return points
    }

    // Position at given time
    fun getPositionAt(t: Float): Pair<Float, Float> {
        val traj = calculateTrajectory(200)
        val dt = 0.03f
        val index = (t / dt).toInt().coerceIn(0, traj.size - 1)
        return traj[index]
    }
}

// --- 2. Orbital Mechanics State ("What happens if Jupiter disappears?") ---
data class CelestialBody(
    val name: String,
    val orbitRadius: Float, // AU scale
    var angleRad: Float,
    val orbitalSpeed: Float, // rad/s
    val sizeRadius: Float,
    val colorHex: Long,
    val massRatio: Float = 1f,
    var isDestroyed: Boolean = false,
    val trail: MutableList<Pair<Float, Float>> = mutableListOf()
)

data class OrbitalSimState(
    val jupiterPresent: Boolean = true,
    val jupiterMassMultiplier: Float = 1.0f,
    val asteroidCount: Int = 16,
    val timeStep: Float = 0.02f,
    val isRunning: Boolean = true,
    val asteroidsCollidedWithJupiter: Int = 3,
    val asteroidsThreateningEarth: Int = 0
)

// --- 3. Pendulum State ---
data class PendulumState(
    val length: Float = 2.0f, // meters
    val mass: Float = 1.0f, // kg
    val damping: Float = 0.15f, // damping coefficient
    val gravity: Float = 9.8f, // m/s²
    var theta: Float = 0.8f, // radians (~45 deg)
    var omega: Float = 0f, // angular velocity rad/s
    var time: Float = 0f,
    val isRunning: Boolean = false,
    val speedMultiplier: Float = 1f
) {
    val naturalPeriod: Float get() = (2 * PI * sqrt(length / gravity)).toFloat()
    val kineticEnergy: Float get() = (0.5f * mass * (length * omega) * (length * omega))
    val potentialEnergy: Float get() = (mass * gravity * length * (1f - cos(theta)))
    val totalEnergy: Float get() = kineticEnergy + potentialEnergy

    // Step physics using Runge-Kutta 2nd order (Verlet / Euler-Cromer)
    fun step(dt: Float): PendulumState {
        val alpha = -(gravity / length) * sin(theta) - (damping / mass) * omega
        val newOmega = omega + alpha * dt
        val newTheta = theta + newOmega * dt
        return copy(
            theta = newTheta,
            omega = newOmega,
            time = time + dt
        )
    }
}

// --- 4. RLC Circuit State ---
data class CircuitState(
    val voltageAmplitude: Float = 12f, // Volts
    val resistance: Float = 10f, // Ohms
    val inductance: Float = 0.05f, // Henries (50 mH)
    val capacitance: Float = 0.0001f, // Farads (100 uF)
    val frequency: Float = 60f, // Hz
    var phaseAngleTime: Float = 0f,
    val isRunning: Boolean = true
) {
    val angularFrequency: Float get() = (2 * PI * frequency).toFloat()
    val inductiveReactance: Float get() = angularFrequency * inductance
    val capacitiveReactance: Float get() = 1f / (angularFrequency * capacitance).coerceAtLeast(0.0001f)
    val impedance: Float get() = sqrt(resistance * resistance + (inductiveReactance - capacitiveReactance) * (inductiveReactance - capacitiveReactance))
    val currentAmplitude: Float get() = voltageAmplitude / impedance.coerceAtLeast(0.01f)
    val resonantFrequency: Float get() = (1f / (2 * PI * sqrt(inductance * capacitance))).toFloat()
    val phaseShiftRad: Float get() = kotlin.math.atan2(inductiveReactance - capacitiveReactance, resistance)
}

// --- 5. Lotka-Volterra Population State ---
data class PopulationState(
    val alpha: Float = 0.8f, // Prey natural birth rate
    val beta: Float = 0.04f, // Predation rate
    val gamma: Float = 0.6f, // Predator death rate
    val delta: Float = 0.02f, // Predator reproduction per eaten prey
    var preyCount: Float = 40f,
    var predatorCount: Float = 15f,
    var time: Float = 0f,
    val isRunning: Boolean = true,
    val historyPrey: MutableList<Pair<Float, Float>> = mutableListOf(),
    val historyPredator: MutableList<Pair<Float, Float>> = mutableListOf()
) {
    fun step(dt: Float): PopulationState {
        val dPrey = (alpha * preyCount - beta * preyCount * predatorCount) * dt
        val dPred = (delta * preyCount * predatorCount - gamma * predatorCount) * dt
        val newPrey = (preyCount + dPrey).coerceAtLeast(1f)
        val newPred = (predatorCount + dPred).coerceAtLeast(1f)
        val newTime = time + dt

        val newHistPrey = historyPrey.toMutableList()
        val newHistPred = historyPredator.toMutableList()
        if (newHistPrey.size > 80) newHistPrey.removeAt(0)
        if (newHistPred.size > 80) newHistPred.removeAt(0)
        newHistPrey.add(Pair(newTime, newPrey))
        newHistPred.add(Pair(newTime, newPred))

        return copy(
            preyCount = newPrey,
            predatorCount = newPred,
            time = newTime,
            historyPrey = newHistPrey,
            historyPredator = newHistPred
        )
    }
}
