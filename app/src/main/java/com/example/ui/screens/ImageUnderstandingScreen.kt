package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.NexusAiService
import com.example.ui.AppScreen
import com.example.ui.NexusViewModel
import kotlinx.coroutines.launch

@Composable
fun ImageUnderstandingScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { NexusAiService() }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var diagramTitle by remember { mutableStateOf("Projectile Motion Parabola Diagram") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var componentsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var detectedPrereqs by remember { mutableStateOf<List<String>>(emptyList()) }

    // Android Photo Picker (zero-permission, compliant with Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                diagramTitle = "User Uploaded Diagram"
                analysisResult = null
            } catch (_: Exception) {}
        }
    }

    // Helper to generate vector diagram bitmap on demand
    fun createSampleDiagram(type: String): Bitmap {
        val bmp = Bitmap.createBitmap(400, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(android.graphics.Color.rgb(15, 23, 42))

        val paint = Paint().apply {
            color = android.graphics.Color.rgb(56, 189, 248)
            strokeWidth = 4f
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isAntiAlias = true
        }

        when (type) {
            "PROJECTILE" -> {
                canvas.drawText("y(x) = x tan(θ) - gx²/(2v₀²cos²θ)", 30f, 40f, textPaint)
                val path = android.graphics.Path()
                path.moveTo(40f, 200f)
                path.quadTo(200f, 40f, 360f, 200f)
                canvas.drawPath(path, paint)
                paint.color = android.graphics.Color.rgb(245, 158, 11)
                canvas.drawCircle(200f, 80f, 8f, paint)
            }
            "CIRCUIT" -> {
                canvas.drawText("Series RLC AC Circuit Diagram", 30f, 40f, textPaint)
                canvas.drawRect(60f, 80f, 340f, 180f, paint)
                paint.color = android.graphics.Color.rgb(168, 85, 247)
                canvas.drawText("R=10Ω  L=50mH  C=100μF", 80f, 140f, textPaint)
            }
            "ORBIT" -> {
                canvas.drawText("Keplerian Elliptical Orbit & Focus", 30f, 40f, textPaint)
                paint.color = android.graphics.Color.rgb(251, 191, 36)
                canvas.drawCircle(140f, 130f, 16f, paint) // Sun focus
                paint.color = android.graphics.Color.rgb(56, 189, 248)
                canvas.drawOval(40f, 80f, 360f, 180f, paint)
            }
        }
        return bmp
    }

    // Default sample bitmap on first load
    if (selectedBitmap == null) {
        selectedBitmap = remember { createSampleDiagram("PROJECTILE") }
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
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Camera & Diagram Understanding",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Multimodal scientific diagram identification & concept extraction",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // --- 2. PRE-LOADED SAMPLES / PHOTO PICKER BAR ---
        item {
            Column {
                Text(
                    text = "Select Scientific Diagram Sample or Upload:",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_custom_diagram_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick from Gallery", fontSize = 11.5.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = createSampleDiagram("PROJECTILE")
                                diagramTitle = "Projectile Parabola Trajectory"
                                analysisResult = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Projectile Parabola", fontSize = 11.5.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = createSampleDiagram("CIRCUIT")
                                diagramTitle = "Series RLC Circuit Schematic"
                                analysisResult = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RLC Circuit", fontSize = 11.5.sp)
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                selectedBitmap = createSampleDiagram("ORBIT")
                                diagramTitle = "Keplerian Gravitational Orbit"
                                analysisResult = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Planetary Orbit", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // --- 3. DIAGRAM PREVIEW CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = diagramTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Multimodal Input", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    selectedBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Scientific Diagram",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Trigger
                    Button(
                        onClick = {
                            isAnalyzing = true
                            coroutineScope.launch {
                                val (exp, analysis) = aiService.explainAndAnalyze(
                                    userPrompt = "Analyze this scientific diagram: $diagramTitle. Identify its physical components, governing equations, prerequisites, and verify relations.",
                                    imageBitmap = selectedBitmap
                                )
                                analysisResult = exp
                                componentsList = analysis.keyIdeas
                                detectedPrereqs = analysis.prerequisites
                                isAnalyzing = false
                            }
                        },
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("analyze_diagram_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Diagram with Gemini...", color = Color.Black, fontSize = 12.5.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Diagram with Gemini Vision", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 4. MULTIMODAL ANALYSIS RESULTS ---
        if (analysisResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("diagram_analysis_results_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Identified Scientific Components & Relationships",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Components
                        componentsList.forEach { comp ->
                            Text(
                                text = "• $comp",
                                fontSize = 12.5.sp,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Prerequisites
                        if (detectedPrereqs.isNotEmpty()) {
                            Text(
                                text = "Prerequisites Extracted to Knowledge Graph:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(detectedPrereqs) { p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0F263E))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(p, fontSize = 11.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full explanation
                        Text(
                            text = analysisResult ?: "",
                            fontSize = 12.5.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "*Note: AI visual identification is an assistive interpretation; verified scientific calculations should always be cross-referenced.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
