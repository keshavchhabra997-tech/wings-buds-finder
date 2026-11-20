package com.wingsbuds.finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeScreen() }
    }
}

private val Cyan = Color(0xFF7FDBFF)
private val Ice = Color(0xFFCFD8DC)

@Composable
fun LiquidBackground() {
    val t = rememberInfiniteTransition(label = "bg")
    val p by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p"
    )
    Canvas(Modifier.fillMaxSize().background(Color(0xFF050914))) {
        val w = size.width
        val h = size.height
        fun blob(c: Color, x: Float, y: Float, r: Float) {
            drawCircle(
                Brush.radialGradient(listOf(c, Color.Transparent), Offset(x, y), r),
                r, Offset(x, y)
            )
        }
        blob(Color(0x6600E5FF), w * (0.1f + 0.6f * p), h * (0.12f + 0.10f * p), w * 0.75f)
        blob(Color(0x553D5AFE), w * (0.9f - 0.6f * p), h * (0.50f - 0.12f * p), w * 0.85f)
        blob(Color(0x447C4DFF), w * (0.3f + 0.4f * p), h * (0.88f - 0.10f * p), w * 0.80f)
    }
}

@Composable
fun Glass(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))))
            .border(1.dp, Brush.linearGradient(listOf(Color(0x77FFFFFF), Color(0x1A7FDBFF))), shape)
            .padding(20.dp),
        content = content
    )
}

@Composable
fun GlassButton(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (active) Color(0x6600E5FF) else Color(0x22FFFFFF), label = "btn"
    )
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun DrawScope.bud(x: Float, y: Float, s: Float, tilt: Float, a: Float) {
    rotate(tilt, Offset(x, y)) {
        drawRoundRect(
            Ice.copy(alpha = a), Offset(x - s * 0.15f, y),
            Size(s * 0.30f, s * 1.2f), CornerRadius(s * 0.15f)
        )
        drawCircle(Color(0xFF263238).copy(alpha = a), s * 0.5f, Offset(x, y))
        drawCircle(Cyan.copy(alpha = a), s * 0.5f, Offset(x, y), style = Stroke(3f))
    }
}

@Composable
fun CaseScene(open: Boolean, leftOut: Boolean, rightOut: Boolean) {
    val lid by animateFloatAsState(if (open) 1f else 0f, spring(0.6f, 60f), label = "lid")
    val lo by animateFloatAsState(if (leftOut) 1f else 0f, spring(0.55f, 70f), label = "lo")
    val ro by animateFloatAsState(if (rightOut) 1f else 0f, spring(0.55f, 70f), label = "ro")
    val t = rememberInfiniteTransition(label = "float")
    val f by t.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f"
    )
    Canvas(Modifier.fillMaxWidth().height(280.dp)) {
        val cx = size.width / 2f
        val cw = size.width * 0.66f
        val ch = size.height * 0.30f
        val x0 = cx - cw / 2f
        val y0 = size.height * 0.60f
        val s = size.width * 0.12f
        val corner = CornerRadius(34f)

        drawCircle(
            Brush.radialGradient(listOf(Color(0x5500E5FF), Color.Transparent), Offset(cx, y0), size.width * 0.5f),
            size.width * 0.5f, Offset(cx, y0)
        )

        bud(cx - cw * 0.2f, y0 - s * 0.5f - lo * size.height * 0.32f + lo * f * 7f, s, -10f * lo, max(lid, lo))
        bud(cx + cw * 0.2f, y0 - s * 0.5f - ro * size.height * 0.32f + ro * f * 7f, s, 10f * ro, max(lid, ro))

        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF1B2A3A), Color(0xFF0B1220)), startY = y0, endY = y0 + ch),
            Offset(x0, y0), Size(cw, ch), corner
        )
        drawRoundRect(Color(0x667FDBFF), Offset(x0, y0), Size(cw, ch), corner, style = Stroke(2f))
        drawCircle(Cyan.copy(alpha = 0.6f + 0.4f * f), 6f, Offset(cx, y0 + ch * 0.5f))

        val lh = ch * 0.55f
        rotate(-lid * 105f, Offset(x0, y0)) {
            drawRoundRect(
                Brush.verticalGradient(listOf(Color(0xFF2A3F55), Color(0xFF16222F)), startY = y0 - lh, endY = y0),
                Offset(x0, y0 - lh), Size(cw, lh), corner
            )
            drawRoundRect(Color(0x887FDBFF), Offset(x0, y0 - lh), Size(cw, lh), corner, style = Stroke(2f))
        }
    }
}

@Composable
fun Radar() {
    val t = rememberInfiniteTransition(label = "radar")
    val p by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "p"
    )
    Canvas(Modifier.fillMaxWidth().height(200.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.height * 0.48f
        for (i in 0..2) {
            val q = (p + i / 3f) % 1f
            drawCircle(Cyan.copy(alpha = (1f - q) * 0.7f), maxR * q, c, style = Stroke(3f))
        }
        drawCircle(Cyan, 9f, c)
    }
}

@Composable
fun Stat(label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xAAFFFFFF), fontSize = 12.sp)
        Text("Unavailable", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HomeScreen() {
    var open by remember { mutableStateOf(false) }
    var leftOut by remember { mutableStateOf(false) }
    var rightOut by remember { mutableStateOf(false) }
    val t = rememberInfiniteTransition(label = "dot")
    val dot by t.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "dot"
    )

    Box(Modifier.fillMaxSize()) {
        LiquidBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("WINGS BUDS FINDER", color = Cyan, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
            Text("Your earbuds. Your control.", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)

            Glass(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFB74D).copy(alpha = dot)))
                    Spacer(Modifier.width(10.dp))
                    Text("Not connected", color = Color.White, fontSize = 14.sp)
                }
                CaseScene(open, leftOut, rightOut)
                Text(
                    "DEMO MODE: manual animation, not hardware data",
                    color = Color(0xFFFFB74D), fontSize = 11.sp, letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(if (open) "Close lid" else "Open lid", open, Modifier.weight(1f)) { open = !open }
                    GlassButton("Left", leftOut, Modifier.weight(1f)) { leftOut = !leftOut; if (leftOut) open = true }
                    GlassButton("Right", rightOut, Modifier.weight(1f)) { rightOut = !rightOut; if (rightOut) open = true }
                }
            }

            Glass(Modifier.fillMaxWidth()) {
                Text("Battery", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row {
                    Stat("Left", Modifier.weight(1f))
                    Stat("Case", Modifier.weight(1f))
                    Stat("Right", Modifier.weight(1f))
                }
            }

            Glass(Modifier.fillMaxWidth()) {
                Text("Find My Buds", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Radar()
                Text("Signal: Unavailable. Bluetooth scanner comes in the next step.", color = Color(0xAAFFFFFF), fontSize = 12.sp)
            }
        }
    }
}
