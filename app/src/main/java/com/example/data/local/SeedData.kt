package com.example.data.local

import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import com.example.data.model.DocumentNote
import com.example.data.model.Question

object SeedData {
    val initialConcepts: List<Concept> = listOf(
        // --- PHYSICS: Newtonian Mechanics Cluster ---
        Concept(
            id = "phys_units",
            name = "Physical Units & Dimensions",
            subject = "Physics",
            difficulty = "Beginner",
            description = "Fundamental SI dimensions (mass [kg], length [m], time [s]) and dimensional consistency in physical laws.",
            prerequisitesJson = "[]",
            relatedConceptsJson = "[\"Kinematics Basics\", \"Force\"]",
            masteryLevel = "Mastered",
            masteryScore = 95,
            mistakesCount = 0,
            questionsAttempted = 4,
            correctCount = 4,
            graphX = 200f,
            graphY = 150f
        ),
        Concept(
            id = "phys_velocity",
            name = "Velocity & Speed",
            subject = "Physics",
            difficulty = "Beginner",
            description = "Rate of change of displacement with respect to time (v = dx/dt). Vector quantity with both magnitude and direction.",
            prerequisitesJson = "[\"Physical Units & Dimensions\"]",
            relatedConceptsJson = "[\"Acceleration\", \"Kinematics Basics\"]",
            masteryLevel = "Mastered",
            masteryScore = 90,
            mistakesCount = 1,
            questionsAttempted = 5,
            correctCount = 4,
            graphX = 350f,
            graphY = 180f
        ),
        Concept(
            id = "phys_accel",
            name = "Acceleration",
            subject = "Physics",
            difficulty = "Beginner",
            description = "Rate of change of velocity (a = dv/dt = d²x/dt²). Central to all classical dynamics.",
            prerequisitesJson = "[\"Velocity & Speed\"]",
            relatedConceptsJson = "[\"Force\", \"Newton's Second Law\"]",
            masteryLevel = "Learning",
            masteryScore = 65,
            mistakesCount = 2,
            questionsAttempted = 6,
            correctCount = 4,
            graphX = 520f,
            graphY = 180f
        ),
        Concept(
            id = "phys_mass",
            name = "Inertial Mass",
            subject = "Physics",
            difficulty = "Beginner",
            description = "A quantitative measure of an object's resistance to acceleration when a net force is applied.",
            prerequisitesJson = "[\"Physical Units & Dimensions\"]",
            relatedConceptsJson = "[\"Force\", \"Newton's Second Law\"]",
            masteryLevel = "Mastered",
            masteryScore = 88,
            mistakesCount = 0,
            questionsAttempted = 3,
            correctCount = 3,
            graphX = 380f,
            graphY = 320f
        ),
        Concept(
            id = "phys_force",
            name = "Force & Vectors",
            subject = "Physics",
            difficulty = "Intermediate",
            description = "Any interaction that, when unopposed, changes the motion of an object. Vector addition of concurrent forces.",
            prerequisitesJson = "[\"Physical Units & Dimensions\", \"Vectors & Scalars\"]",
            relatedConceptsJson = "[\"Newton's Second Law\", \"Newton's Third Law\"]",
            masteryLevel = "Learning",
            masteryScore = 55,
            mistakesCount = 3,
            questionsAttempted = 7,
            correctCount = 4,
            graphX = 540f,
            graphY = 320f
        ),
        Concept(
            id = "phys_newton2",
            name = "Newton's Second Law",
            subject = "Physics",
            difficulty = "Intermediate",
            description = "The acceleration of an object is directly proportional to the net force acting upon it and inversely proportional to its mass: ΣF = m · a (or F = dp/dt).",
            prerequisitesJson = "[\"Force & Vectors\", \"Inertial Mass\", \"Acceleration\"]",
            relatedConceptsJson = "[\"Linear Momentum\", \"Harmonic Oscillator\", \"Projectile Motion\"]",
            masteryLevel = "Weak",
            masteryScore = 32,
            mistakesCount = 6,
            questionsAttempted = 9,
            correctCount = 3,
            graphX = 720f,
            graphY = 260f
        ),
        Concept(
            id = "phys_projectile",
            name = "Projectile Motion",
            subject = "Physics",
            difficulty = "Intermediate",
            description = "Two-dimensional motion under constant gravitational acceleration g, decomposing independent horizontal (uniform) and vertical (free fall) components.",
            prerequisitesJson = "[\"Newton's Second Law\", \"Velocity & Speed\"]",
            relatedConceptsJson = "[\"Kinetic Energy\", \"Orbital Mechanics\"]",
            masteryLevel = "Learning",
            masteryScore = 70,
            mistakesCount = 2,
            questionsAttempted = 6,
            correctCount = 4,
            graphX = 920f,
            graphY = 220f
        ),
        Concept(
            id = "phys_pendulum",
            name = "Harmonic Oscillator & Pendulum",
            subject = "Physics",
            difficulty = "Advanced",
            description = "Periodic motion governed by restoring force proportional to displacement: d²θ/dt² + (g/L)sin(θ) = 0. Small-angle approximation T = 2π√(L/g).",
            prerequisitesJson = "[\"Newton's Second Law\", \"Trigonometry & Small Angles\"]",
            relatedConceptsJson = "[\"Damped Oscillations\", \"Conservation of Energy\"]",
            masteryLevel = "Learning",
            masteryScore = 60,
            mistakesCount = 2,
            questionsAttempted = 4,
            correctCount = 2,
            graphX = 890f,
            graphY = 390f
        ),

        // --- SPACE SCIENCE: Celestial Mechanics Cluster ---
        Concept(
            id = "space_gravitation",
            name = "Universal Gravitation",
            subject = "Space",
            difficulty = "Intermediate",
            description = "Every particle attracts every other with a force proportional to product of masses and inversely proportional to square of separation: F = G(m1·m2)/r².",
            prerequisitesJson = "[\"Newton's Second Law\", \"Force & Vectors\"]",
            relatedConceptsJson = "[\"Orbital Mechanics\", \"Kepler's Laws\"]",
            masteryLevel = "Learning",
            masteryScore = 75,
            mistakesCount = 1,
            questionsAttempted = 5,
            correctCount = 4,
            graphX = 700f,
            graphY = 520f
        ),
        Concept(
            id = "space_orbital",
            name = "Orbital Mechanics",
            subject = "Space",
            difficulty = "Advanced",
            description = "Motion of celestial bodies under central gravitational force. Orbital velocity v = √(GM/r) and escape velocity v_esc = √(2GM/r).",
            prerequisitesJson = "[\"Universal Gravitation\", \"Centripetal Acceleration\"]",
            relatedConceptsJson = "[\"Kepler's Laws\", \"Jupiter Gravitational Shield\"]",
            masteryLevel = "Learning",
            masteryScore = 62,
            mistakesCount = 2,
            questionsAttempted = 5,
            correctCount = 3,
            graphX = 880f,
            graphY = 560f
        ),
        Concept(
            id = "space_jupiter",
            name = "Jupiter Gravitational Shield",
            subject = "Space",
            difficulty = "Advanced",
            description = "How Jupiter's massive gravitational field perturbs asteroid belt trajectories, either deflecting comets away from inner solar system or clearing resonance gaps.",
            prerequisitesJson = "[\"Orbital Mechanics\", \"Three-Body Perturbation\"]",
            relatedConceptsJson = "[\"Solar System Stability\", \"Asteroid Dynamics\"]",
            masteryLevel = "Unexplored",
            masteryScore = 15,
            mistakesCount = 0,
            questionsAttempted = 0,
            correctCount = 0,
            graphX = 1060f,
            graphY = 590f
        ),

        // --- CIRCUITS & ELECTRONICS Cluster ---
        Concept(
            id = "circ_ohms_law",
            name = "Ohm's Law & Resistance",
            subject = "Circuits",
            difficulty = "Beginner",
            description = "Linear relationship between potential difference and current through a conductor: V = I · R. Microscopic drift velocity and resistivity.",
            prerequisitesJson = "[\"Electric Potential\", \"Electric Current\"]",
            relatedConceptsJson = "[\"Kirchhoff's Laws\", \"Capacitance & Inductance\"]",
            masteryLevel = "Mastered",
            masteryScore = 92,
            mistakesCount = 1,
            questionsAttempted = 8,
            correctCount = 7,
            graphX = 250f,
            graphY = 520f
        ),
        Concept(
            id = "circ_rlc",
            name = "RLC Circuit & Resonance",
            subject = "Circuits",
            difficulty = "Advanced",
            description = "Second-order differential circuit combining resistor, inductor, and capacitor. Resonant frequency ω0 = 1/√(LC) and impedance Z = √(R² + (ωL - 1/ωC)²).",
            prerequisitesJson = "[\"Ohm's Law & Resistance\", \"Harmonic Oscillator & Pendulum\"]",
            relatedConceptsJson = "[\"Electromagnetic Waves\", \"Damped Oscillations\"]",
            masteryLevel = "Learning",
            masteryScore = 58,
            mistakesCount = 3,
            questionsAttempted = 6,
            correctCount = 3,
            graphX = 420f,
            graphY = 640f
        ),

        // --- BIOLOGY: Systems & Ecosystem Dynamics ---
        Concept(
            id = "bio_exp_growth",
            name = "Exponential & Logistic Growth",
            subject = "Biology",
            difficulty = "Intermediate",
            description = "Population dynamics model: dN/dt = rN(1 - N/K), transitioning from geometric acceleration to carrying capacity saturation K.",
            prerequisitesJson = "[\"Differential Equations Basics\"]",
            relatedConceptsJson = "[\"Lotka-Volterra Predator-Prey\", \"Ecological Carrying Capacity\"]",
            masteryLevel = "Mastered",
            masteryScore = 85,
            mistakesCount = 1,
            questionsAttempted = 5,
            correctCount = 4,
            graphX = 160f,
            graphY = 750f
        ),
        Concept(
            id = "bio_predator_prey",
            name = "Lotka-Volterra Dynamics",
            subject = "Biology",
            difficulty = "Advanced",
            description = "Nonlinear coupled differential equations modeling predator (y) and prey (x) interactions: dx/dt = αx - βxy, dy/dt = δxy - γy. Phase space cycle stability.",
            prerequisitesJson = "[\"Exponential & Logistic Growth\"]",
            relatedConceptsJson = "[\"Ecosystem Equilibrium\", \"Phase Space Stability\"]",
            masteryLevel = "Unexplored",
            masteryScore = 20,
            mistakesCount = 0,
            questionsAttempted = 1,
            correctCount = 0,
            graphX = 350f,
            graphY = 820f
        ),

        // --- MATHEMATICS: Foundations Cluster ---
        Concept(
            id = "math_vectors",
            name = "Vectors & Scalars",
            subject = "Mathematics",
            difficulty = "Beginner",
            description = "Geometric and algebraic representations of quantities having both magnitude and direction, dot products, cross products, and unit components.",
            prerequisitesJson = "[\"Trigonometry & Small Angles\"]",
            relatedConceptsJson = "[\"Force & Vectors\", \"Velocity & Speed\"]",
            masteryLevel = "Mastered",
            masteryScore = 96,
            mistakesCount = 0,
            questionsAttempted = 6,
            correctCount = 6,
            graphX = 320f,
            graphY = 50f
        ),
        Concept(
            id = "math_diff",
            name = "Differential Equations Basics",
            subject = "Mathematics",
            difficulty = "Intermediate",
            description = "Equations involving functions and their derivatives, representing rates of physical change across physics, biology, and circuits.",
            prerequisitesJson = "[\"Physical Units & Dimensions\"]",
            relatedConceptsJson = "[\"Newton's Second Law\", \"Harmonic Oscillator & Pendulum\", \"RLC Circuit & Resonance\"]",
            masteryLevel = "Learning",
            masteryScore = 68,
            mistakesCount = 2,
            questionsAttempted = 5,
            correctCount = 3,
            graphX = 650f,
            graphY = 70f
        )
    )

    val initialRelationships: List<ConceptRelationship> = listOf(
        ConceptRelationship("phys_units", "phys_velocity", "PREREQUISITE"),
        ConceptRelationship("phys_velocity", "phys_accel", "PREREQUISITE"),
        ConceptRelationship("phys_units", "phys_mass", "PREREQUISITE"),
        ConceptRelationship("phys_units", "phys_force", "PREREQUISITE"),
        ConceptRelationship("math_vectors", "phys_force", "PREREQUISITE"),
        ConceptRelationship("phys_accel", "phys_newton2", "PREREQUISITE"),
        ConceptRelationship("phys_mass", "phys_newton2", "PREREQUISITE"),
        ConceptRelationship("phys_force", "phys_newton2", "PREREQUISITE"),
        ConceptRelationship("math_diff", "phys_newton2", "RELATED"),
        ConceptRelationship("phys_newton2", "phys_projectile", "PREREQUISITE"),
        ConceptRelationship("phys_newton2", "phys_pendulum", "PREREQUISITE"),
        ConceptRelationship("phys_newton2", "space_gravitation", "PREREQUISITE"),
        ConceptRelationship("space_gravitation", "space_orbital", "PREREQUISITE"),
        ConceptRelationship("space_orbital", "space_jupiter", "PREREQUISITE"),
        ConceptRelationship("circ_ohms_law", "circ_rlc", "PREREQUISITE"),
        ConceptRelationship("phys_pendulum", "circ_rlc", "RELATED"),
        ConceptRelationship("math_diff", "bio_exp_growth", "PREREQUISITE"),
        ConceptRelationship("bio_exp_growth", "bio_predator_prey", "PREREQUISITE")
    )

    val initialQuestions: List<Question> = listOf(
        Question(
            id = "q_newton2_1",
            conceptId = "phys_newton2",
            conceptName = "Newton's Second Law",
            subject = "Physics",
            questionText = "A 5 kg probe in deep space has two thrusters firing perpendicularly: Thruster A delivers 12 N forward, while Thruster B delivers 9 N sideways. What is the magnitude of the probe's acceleration?",
            questionType = "MCQ",
            optionsJson = "[\"2.4 m/s²\", \"3.0 m/s²\", \"4.2 m/s²\", \"15.0 m/s²\"]",
            correctAnswer = "1",
            explanation = "Net Force is the vector hypotenuse: F_net = √(12² + 9²) = √(144 + 81) = √225 = 15 N. By Newton's Second Law: a = F_net / m = 15 N / 5 kg = 3.0 m/s².",
            misconceptionAnalysis = "Many learners either add the magnitudes scalar-wise (12 + 9 = 21 N -> 4.2 m/s²) forgetting vector orthogonality, or forget to divide net force by mass (15.0 m/s²).",
            testedConcept = "Vector superposition of perpendicular forces and Newton's Second Law a = ΣF / m",
            difficulty = "Medium"
        ),
        Question(
            id = "q_newton2_2",
            conceptId = "phys_newton2",
            conceptName = "Newton's Second Law",
            subject = "Physics",
            questionText = "If a constant net force F acts on mass m, producing acceleration a. What happens to acceleration if the mass is doubled and the net force is halved?",
            questionType = "MCQ",
            optionsJson = "[\"Acceleration quadruples (4a)\", \"Acceleration doubles (2a)\", \"Acceleration is halved (a/2)\", \"Acceleration drops to one-quarter (a/4)\"]",
            correctAnswer = "3",
            explanation = "Using a = F / m: new acceleration a' = (F / 2) / (2m) = (1/4) · (F / m) = a / 4.",
            misconceptionAnalysis = "Common mistake is mixing inverse and direct proportionality, mistakenly believing halving force cancels doubling mass.",
            testedConcept = "Proportional reasoning with F = ma",
            difficulty = "Easy"
        ),
        Question(
            id = "q_projectile_1",
            conceptId = "phys_projectile",
            conceptName = "Projectile Motion",
            subject = "Physics",
            questionText = "In the absence of air resistance, at what launch angle θ above the horizontal is the maximum horizontal range achieved on flat ground?",
            questionType = "MCQ",
            optionsJson = "[\"30°\", \"45°\", \"60°\", \"90°\"]",
            correctAnswer = "1",
            explanation = "Range formula R = (v₀² sin(2θ)) / g. The sine function reaches its maximum of 1 when 2θ = 90°, which occurs when launch angle θ = 45°.",
            misconceptionAnalysis = "Assuming higher launch angles (like 60°) always provide greater travel because of larger flight time.",
            testedConcept = "Optimization of projectile range equation",
            difficulty = "Easy"
        ),
        Question(
            id = "q_space_jupiter_1",
            conceptId = "space_jupiter",
            conceptName = "Jupiter Gravitational Shield",
            subject = "Space",
            questionText = "What physical mechanism allows Jupiter to protect inner terrestrial planets like Earth from frequent outer solar system comet impacts?",
            questionType = "MCQ",
            optionsJson = "[\"Jupiter's magnetic field repels all non-ionized icy comets\", \"Jupiter's enormous mass provides gravitational slingshots that eject long-period comets into interstellar space or absorb them\", \"Jupiter collides directly with 99% of all inbound celestial bodies\", \"Jupiter slows down solar wind pressure creating an asteroid stagnation zone\"]",
            correctAnswer = "1",
            explanation = "Jupiter acts as a gravitational vacuum cleaner and slingshot: its 318 Earth-mass gravitational field deflects long-period comets entering from the Oort Cloud either into hyperbolic escape trajectories out of the solar system or captures them into Jovian impacts (like Comet Shoemaker-Levy 9).",
            misconceptionAnalysis = "Believing that magnetic fields or direct physical blocking are responsible rather than gravitational n-body deflection.",
            testedConcept = "Gravitational perturbation and three-body gravitational deflection",
            difficulty = "Olympiad"
        ),
        Question(
            id = "q_circ_rlc_1",
            conceptId = "circ_rlc",
            conceptName = "RLC Circuit & Resonance",
            subject = "Circuits",
            questionText = "In a series RLC circuit with R = 10 Ω, L = 20 mH, and C = 5 μF, what happens to the total circuit impedance Z at the resonant angular frequency ω₀?",
            questionType = "MCQ",
            optionsJson = "[\"Impedance becomes zero\", \"Impedance equals pure resistance R (10 Ω)\", \"Impedance approaches infinity\", \"Impedance is dominated by capacitive reactance 1/(ωC)\"]",
            correctAnswer = "1",
            explanation = "At resonance, the inductive reactance equals capacitive reactance: ω₀L = 1/(ω₀C). The imaginary terms cancel out: Z = √(R² + (ωL - 1/ωC)²) = √(R² + 0) = R. The circuit impedance is purely resistive and at its absolute minimum.",
            misconceptionAnalysis = "Confusing series resonance (minimum impedance Z = R) with parallel anti-resonance (maximum impedance).",
            testedConcept = "Resonance condition and impedance minimization in series RLC",
            difficulty = "Hard"
        ),
        Question(
            id = "q_bio_predator_1",
            conceptId = "bio_predator_prey",
            conceptName = "Lotka-Volterra Dynamics",
            subject = "Biology",
            questionText = "In the classical Lotka-Volterra predator-prey model, why does the predator population peak lag behind the prey population peak?",
            questionType = "MCQ",
            optionsJson = "[\"Predators reproduce faster than prey organisms\", \"Predator population reproduction requires prior prey abundance to feed gestating mothers, creating a biological phase delay\", \"Prey immediately emigrate when predator counts reach half capacity\", \"Carrying capacity K shrinks before predation starts\"]",
            correctAnswer = "1",
            explanation = "The rate of predator increase dy/dt = δxy - γy depends on the product xy (encounters with prey). When prey is abundant, predators feed well and produce offspring over their gestation period, causing the predator peak to systematically lag approximately a quarter-phase behind the prey cycle.",
            misconceptionAnalysis = "Assuming populations change simultaneously without biological gestation or encounter latency.",
            testedConcept = "Phase-lag and coupled oscillations in non-linear biological systems",
            difficulty = "Intermediate"
        )
    )

    val sampleDocuments: List<DocumentNote> = listOf(
        DocumentNote(
            id = "doc_principles_mechanics",
            title = "Foundations of Classical Mechanics",
            subject = "Physics",
            content = """
                Classical mechanics describes the motion of macroscopic objects under forces. 
                Fundamental postulates:
                1. Galilean Relativity: The laws of physics are invariant across all inertial frames.
                2. Newton's 1st Law: An object remains at constant velocity unless acted upon by a net external force.
                3. Newton's 2nd Law: The rate of change of momentum is proportional to the applied force: F = dp/dt. If mass is constant, F = m(dv/dt) = ma.
                4. Newton's 3rd Law: For every action force, there exists an equal and opposite reaction force: F_ab = -F_ba.
                Conservation principles:
                - Conservation of Linear Momentum occurs when net external force is zero.
                - Conservation of Angular Momentum occurs when net external torque is zero.
                - Conservation of Mechanical Energy holds in conservative force fields: E = T + V = constant.
            """.trimIndent(),
            summary = "Core principles of Newtonian mechanics covering inertial reference frames, the three laws of motion, and fundamental conservation laws for momentum, angular momentum, and mechanical energy.",
            keyFormulasJson = "[\"F = m · a\", \"p = m · v\", \"E = (1/2)m·v² + V(r)\", \"L = r × p\"]",
            extractedConceptsJson = "[\"Newton's Second Law\", \"Inertial Mass\", \"Velocity & Speed\", \"Acceleration\"]",
            flashcardsJson = "[{\"front\":\"What is an inertial reference frame?\",\"back\":\"A frame of reference in which a body with zero net force moves with constant velocity.\"},{\"front\":\"State Newton's Second Law in momentum form.\",\"back\":\"F_net = dp/dt, where p is linear momentum m·v.\"}]"
        ),
        DocumentNote(
            id = "doc_celestial_gravitation",
            title = "Keplerian Orbits & Jupiter's Gravitational Influence",
            subject = "Space",
            content = """
                Celestial mechanics governs planetary and satellite trajectories under central gravitational forces.
                Kepler's Laws of Planetary Motion:
                1. The orbit of a planet is an ellipse with the Sun at one of the two foci.
                2. A line segment joining a planet and the Sun sweeps out equal areas during equal intervals of time (conservation of angular momentum).
                3. The square of the orbital period T is directly proportional to the cube of the semi-major axis a: T² ∝ a³.
                
                Jupiter's Unique Role in the Solar System:
                With 2.5 times the mass of all other planets combined (317.8 Earth masses), Jupiter dominates the orbital mechanics of the outer and inner solar system.
                - It maintains the Kirkwood gaps in the asteroid belt via orbital resonances (e.g. 3:1, 2:1 resonance).
                - It acts as a gravitational shield or deflector for terrestrial planets, altering comet trajectories from parabolic plunge courses into safe outer ejections.
            """.trimIndent(),
            summary = "Overview of Kepler's three laws of planetary motion and the critical stabilizing gravitational influence of Jupiter on solar system orbital integrity and asteroid belt dynamics.",
            keyFormulasJson = "[\"T² = (4π²/GM) · a³\", \"v_orbit = √(GM/r)\", \"v_escape = √(2GM/r)\"]",
            extractedConceptsJson = "[\"Universal Gravitation\", \"Orbital Mechanics\", \"Jupiter Gravitational Shield\"]",
            flashcardsJson = "[{\"front\":\"What is Kepler's Third Law?\",\"back\":\"T² ∝ a³ — the square of orbital period is proportional to the cube of the orbital semi-major axis.\"},{\"front\":\"Why does Jupiter affect the asteroid belt?\",\"back\":\"Through gravitational resonances, clearing Kirkwood gaps and stabilizing asteroid orbits.\"}]"
        )
    )
}
