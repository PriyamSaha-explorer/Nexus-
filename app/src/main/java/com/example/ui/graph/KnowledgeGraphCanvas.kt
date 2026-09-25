package com.example.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InteractiveKnowledgeGraph(
    concepts: List<Concept>,
    relationships: List<ConceptRelationship>,
    selectedConcept: Concept?,
    onConceptSelected: (Concept) -> Unit,
    modifier: Modifier = Modifier
) {
    var panOffsetX by remember { mutableFloatStateOf(-50f) }
    var panOffsetY by remember { mutableFloatStateOf(40f) }
    var zoomScale by remember { mutableFloatStateOf(0.72f) }

    // Map for fast lookup
    val conceptMap = remember(concepts) { concepts.associateBy { it.id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090D1A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.35f, 2.2f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .pointerInput(concepts, zoomScale, panOffsetX, panOffsetY) {
                detectTapGestures { tapOffset ->
                    // Transform screen tap to graph coordinate space
                    val graphX = (tapOffset.x - panOffsetX) / zoomScale
                    val graphY = (tapOffset.y - panOffsetY) / zoomScale

                    // Check which node was tapped (hit radius 35 in graph coords)
                    val clicked = concepts.firstOrNull { c ->
                        val dx = c.graphX - graphX
                        val dy = c.graphY - graphY
                        sqrt(dx * dx + dy * dy) <= 45f
                    }
                    if (clicked != null) {
                        onConceptSelected(clicked)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw subtle background coordinate constellation grid
            val gridStep = 80f * zoomScale
            val startX = (panOffsetX % gridStep)
            val startY = (panOffsetY % gridStep)

            var gx = startX
            while (gx < canvasW) {
                drawLine(
                    color = Color(0x1038BDF8),
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = startY
            while (gy < canvasH) {
                drawLine(
                    color = Color(0x1038BDF8),
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Draw Relationship Connection Links
            relationships.forEach { rel ->
                val source = conceptMap[rel.sourceId]
                val target = conceptMap[rel.targetId]

                if (source != null && target != null) {
                    val sx = source.graphX * zoomScale + panOffsetX
                    val sy = source.graphY * zoomScale + panOffsetY
                    val tx = target.graphX * zoomScale + panOffsetX
                    val ty = target.graphY * zoomScale + panOffsetY

                    val isHighlighted = selectedConcept?.id == source.id || selectedConcept?.id == target.id
                    val isPrereq = rel.relationType == "PREREQUISITE"

                    val lineColor = when {
                        isHighlighted && isPrereq -> Color(0xFF38BDF8)
                        isHighlighted -> Color(0xFFA855F7)
                        isPrereq -> Color(0x6638BDF8)
                        else -> Color(0x4094A3B8)
                    }

                    val strokeW = if (isHighlighted) 3.5f else 1.8f

                    // Draw Line
                    drawLine(
                        color = lineColor,
                        start = Offset(sx, sy),
                        end = Offset(tx, ty),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Draw Directional Arrowhead toward target
                    val angle = atan2((ty - sy).toDouble(), (tx - sx).toDouble()).toFloat()
                    val arrowDistFromTarget = 24f * zoomScale
                    val arrowX = tx - arrowDistFromTarget * cos(angle)
                    val arrowY = ty - arrowDistFromTarget * sin(angle)
                    val arrowHeadSize = 10f * zoomScale

                    val arrowPath = Path().apply {
                        moveTo(arrowX, arrowY)
                        lineTo(
                            arrowX - arrowHeadSize * cos(angle - PI.toFloat() / 6),
                            arrowY - arrowHeadSize * sin(angle - PI.toFloat() / 6)
                        )
                        lineTo(
                            arrowX - arrowHeadSize * cos(angle + PI.toFloat() / 6),
                            arrowY - arrowHeadSize * sin(angle + PI.toFloat() / 6)
                        )
                        close()
                    }
                    drawPath(path = arrowPath, color = lineColor)
                }
            }

            // 3. Draw Nodes
            concepts.forEach { concept ->
                val nx = concept.graphX * zoomScale + panOffsetX
                val ny = concept.graphY * zoomScale + panOffsetY

                val isSelected = selectedConcept?.id == concept.id

                val (nodeColor, haloColor) = when (concept.masteryLevel) {
                    "Mastered" -> Pair(Color(0xFF10B981), Color(0x4410B981))
                    "Learning" -> Pair(Color(0xFF0EA5E9), Color(0x440EA5E9))
                    "Weak" -> Pair(Color(0xFFEF4444), Color(0x55EF4444))
                    else -> Pair(Color(0xFF64748B), Color(0x2264748B))
                }

                val nodeRadius = (if (isSelected) 22f else 16f) * zoomScale

                // Halo glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(haloColor, Color.Transparent),
                        center = Offset(nx, ny),
                        radius = nodeRadius * 2.4f
                    ),
                    radius = nodeRadius * 2.4f,
                    center = Offset(nx, ny)
                )

                // Selection ring
                if (isSelected) {
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = nodeRadius + 5f,
                        center = Offset(nx, ny),
                        style = Stroke(2.5f)
                    )
                }

                // Core Node Body
                drawCircle(
                    color = Color(0xFF0B1329),
                    radius = nodeRadius,
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = nodeColor,
                    radius = nodeRadius * 0.72f,
                    center = Offset(nx, ny)
                )
                // Center pinpoint
                drawCircle(
                    color = Color(0xFFFFFFFF),
                    radius = nodeRadius * 0.28f,
                    center = Offset(nx, ny)
                )

                // Text label below node
                if (zoomScale > 0.45f) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.rgb(203, 213, 225)
                            textSize = (12f * zoomScale).coerceIn(10f, 16f)
                            isFakeBoldText = isSelected
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        // Shorten label if long
                        val shortLabel = if (concept.name.length > 18) {
                            concept.name.take(16) + "…"
                        } else concept.name

                        drawText(
                            shortLabel,
                            nx,
                            ny + nodeRadius + (14f * zoomScale),
                            paint
                        )

                        // Mastery score badge if zoomed in
                        if (zoomScale > 0.65f) {
                            val scorePaint = android.graphics.Paint().apply {
                                color = when (concept.masteryLevel) {
                                    "Mastered" -> android.graphics.Color.rgb(52, 211, 153)
                                    "Weak" -> android.graphics.Color.rgb(248, 113, 113)
                                    else -> android.graphics.Color.rgb(56, 189, 248)
                                }
                                textSize = (9.5f * zoomScale).coerceIn(8f, 13f)
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(
                                "${concept.masteryScore}%",
                                nx,
                                ny + nodeRadius + (26f * zoomScale),
                                scorePaint
                            )
                        }
                    }
                }
            }
        }
    }
}
