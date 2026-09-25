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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.DocumentNote
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun DocumentIntelligenceScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsState()
    var selectedDoc by remember { mutableStateOf<DocumentNote?>(null) }
    var activeTab by remember { mutableStateOf("Summary") } // "Summary", "Formulas", "Flashcards", "Concept Graph"
    var flashcardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val active = selectedDoc ?: documents.firstOrNull()

    val formulas = remember(active) {
        if (active != null) {
            try {
                val arr = JSONArray(active.keyFormulasJson)
                (0 until arr.length()).map { arr.optString(it) }
            } catch (_: Exception) { emptyList() }
        } else emptyList()
    }

    val extractedConcepts = remember(active) {
        if (active != null) {
            try {
                val arr = JSONArray(active.extractedConceptsJson)
                (0 until arr.length()).map { arr.optString(it) }
            } catch (_: Exception) { emptyList() }
        } else emptyList()
    }

    val flashcards = remember(active) {
        if (active != null) {
            try {
                val arr = JSONArray(active.flashcardsJson)
                (0 until arr.length()).map {
                    val obj = arr.getJSONObject(it)
                    Pair(obj.optString("front"), obj.optString("back"))
                }
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
        // --- 1. HEADER ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Document Intelligence",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Structured extraction: Summaries, formulas, flashcards & concept graph",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // --- 2. DOCUMENT SELECTOR PILLS ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(documents) { doc ->
                    val isSelected = doc.id == active?.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF831843) else Color(0xFF131D33))
                            .border(1.dp, if (isSelected) Color(0xFFF472B6) else Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .clickable {
                                selectedDoc = doc
                                flashcardIndex = 0
                                isFlipped = false
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Column {
                            Text(
                                text = doc.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                            Text(
                                text = doc.subject,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFFCE7F3) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. SECTION TABS ---
        if (active != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Summary", "Formulas", "Flashcards", "Concept Graph").forEach { tab ->
                        val isSelected = tab == activeTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (activeTab) {
                "Summary" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("document_summary_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Executive Summary",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = active.summary,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Extracted Raw Content:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = active.content,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                "Formulas" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("document_formulas_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Extracted Governing Formulas & Invariants",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                formulas.forEach { f ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141F36))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = f,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "Flashcards" -> {
                    item {
                        if (flashcards.isNotEmpty()) {
                            val card = flashcards[flashcardIndex % flashcards.size]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable { isFlipped = !isFlipped }
                                    .testTag("interactive_flashcard"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFlipped) Color(0xFF1E1B4B) else Color(0xFF0C192E)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFlipped) Color(0xFFA855F7) else Color(0xFF38BDF8)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0x33FFFFFF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isFlipped) "ANSWER (Tap to flip back)" else "QUESTION (Tap to flip)",
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (isFlipped) card.second else card.first,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            lineHeight = 22.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Card ${flashcardIndex + 1} of ${flashcards.size}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Button(
                                    onClick = {
                                        flashcardIndex = (flashcardIndex + 1) % flashcards.size
                                        isFlipped = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next Card", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                "Concept Graph" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Concepts Connected to Graph",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                extractedConcepts.forEach { c ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141F36))
                                            .clickable {
                                                viewModel.navigateTo(AppScreen.KNOWLEDGE_GRAPH)
                                            }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(c, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text("Inspect →", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
