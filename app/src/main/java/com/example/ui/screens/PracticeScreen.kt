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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import org.json.JSONArray

@Composable
fun PracticeScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.allQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val diagnosticResult by viewModel.diagnosticResult.collectAsState()
    val concepts by viewModel.allConcepts.collectAsState()

    val currentQuestion: Question? = if (questions.isNotEmpty()) {
        questions[currentIndex % questions.size]
    } else null

    val optionsList = remember(currentQuestion) {
        if (currentQuestion != null) {
            try {
                val arr = JSONArray(currentQuestion.optionsJson)
                (0 until arr.length()).map { arr.optString(it) }
            } catch (_: Exception) { emptyList() }
        } else emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HEADER & ADAPTIVE STATS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Adaptive Practice Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Continuous misconception diagnosis & prerequisite routing",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (currentQuestion != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${questions.size}",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 2. QUESTION CARD ---
        if (currentQuestion != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("practice_question_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${currentQuestion.subject} • ${currentQuestion.conceptName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (currentQuestion.difficulty) {
                                            "Olympiad", "Challenge" -> Color(0xFF831843)
                                            "Hard" -> Color(0xFF7F1D1D)
                                            else -> Color(0xFF14532D)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = currentQuestion.difficulty,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 14.5.sp,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options
                        optionsList.forEachIndexed { index, option ->
                            val isSelected = selectedOption == index.toString()
                            val isAnswered = diagnosticResult != null
                            val isCorrectAnswer = currentQuestion.correctAnswer == index.toString()

                            val optionBorderColor = when {
                                isAnswered && isCorrectAnswer -> Color(0xFF10B981)
                                isAnswered && isSelected && !diagnosticResult!!.isCorrect -> Color(0xFFEF4444)
                                isSelected -> Color(0xFF00E5FF)
                                else -> Color(0xFF1E293B)
                            }

                            val optionBg = when {
                                isAnswered && isCorrectAnswer -> Color(0xFF064E3B).copy(alpha = 0.4f)
                                isAnswered && isSelected && !diagnosticResult!!.isCorrect -> Color(0xFF7F1D1D).copy(alpha = 0.4f)
                                isSelected -> Color(0xFF0C4A6E).copy(alpha = 0.4f)
                                else -> Color(0xFF0B1120)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(optionBg)
                                    .border(1.dp, optionBorderColor, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isAnswered) {
                                        viewModel.selectOption(index.toString())
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("practice_option_$index")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { if (!isAnswered) viewModel.selectOption(index.toString()) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF00E5FF),
                                            unselectedColor = Color(0xFF64748B)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = option,
                                        fontSize = 13.sp,
                                        color = if (isSelected || (isAnswered && isCorrectAnswer)) Color.White else Color(0xFFCBD5E1),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button (only when not answered)
                        if (diagnosticResult == null) {
                            Button(
                                onClick = { viewModel.submitPracticeAnswer(currentQuestion) },
                                enabled = selectedOption != null,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_practice_answer_button")
                            ) {
                                Text("Submit & Diagnose Answer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 3. "WHY AM I WRONG?" DIAGNOSTIC BREAKDOWN ---
            if (diagnosticResult != null) {
                val diag = diagnosticResult!!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagnostic_breakdown_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (diag.isCorrect) Color(0xFF06281E) else Color(0xFF280E14)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (diag.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Status Banner
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (diag.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (diag.isCorrect) Color(0xFF34D399) else Color(0xFFF87171),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (diag.isCorrect) "Correct Mastery Demonstrated!" else "Why Am I Wrong? (Cognitive Diagnosis)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diag.isCorrect) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mistake Category Badge (if incorrect)
                            if (!diag.isCorrect) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Likely Error Source: ",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = diag.mistakeCategory,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Remedial tip
                                Text(
                                    text = diag.remedialTip,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFFECDD3),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Full scientific explanation
                            Text(
                                text = "Scientific Logic:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = diag.explanation,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 19.sp
                            )

                            // Misconception breakdown
                            if (diag.misconceptionAnalysis.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Misconception Trap:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = diag.misconceptionAnalysis,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 18.sp
                                )
                            }

                            // Prerequisite Gap Recommendation if any
                            if (diag.recommendedPrerequisites.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Prerequisite Gap Detected",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFB7185)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Strengthen these concepts before attempting this again:",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                        diag.recommendedPrerequisites.forEach { prereq ->
                                            Text(
                                                text = "• ${prereq.name} (${prereq.masteryScore}% mastery)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF38BDF8),
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.selectConcept(prereq)
                                                        viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                                                    }
                                                    .padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Next Question Button
                            Button(
                                onClick = { viewModel.nextQuestion(questions.size) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("next_practice_question_button")
                            ) {
                                Text("Next Practice Problem", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No practice questions found.", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
