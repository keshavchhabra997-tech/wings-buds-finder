package com.wingsbuds.finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HomeScreen() }
    }
}

private val Cyan = Color(0xFF7FDBFF)

class BgInfo(val wPx: Int, val hPx: Int, val p: State<Float>)

val LocalBg = compositionLocalOf<BgInfo?> { null }

@Composable
fun Scene(modifier: Modifier, p: State<Float>) {
    Canvas(modifier.background(Color(0xFF040813))) {
        val w = size.width
        val h = size.height
        val t = p.value
        fun blob(c: Color, x: Float, y: Float, r: Float) {
            drawCircle(
                Brush.radialGradient(listOf(c, Color.Transparent), Offset(x, y), r),
                r, Offset(x, y)
            )
        }
        blob(Color(0xAA00B8FF), w * (0.15f + 0.6f * t), h * (0.10f + 0.12f * t), w * 0.8f)
        blob(Color(0x993D5AFE), w * (0.95f - 0.7f * t), h * (0.45f - 0.10f * t), w * 0.9f)
        blob(Color(0x887C4DFF), w * (0.2f + 0.5f * t), h * (0.85f - 0.12f * t), w * 0.85f)
        blob(Color(0x6600E5FF), w * (0.8f - 0.4f * t), h * (0.75f + 0.05f * t), w * 0.6f)
        for (i in 0 until 7) {
            val a = (t + i / 7f) * 2f * PI.toFloat()
            val cx = w * (0.15f + 0.7f * ((i * 0.37f) % 1f)) + sin(a) * w * 0.06f
            val cy = h * (0.08f + 0.84f * ((i * 0.23f + 0.1f) % 1f)) + cos(a) * h * 0.02f
            val r = w * (0.035f + 0.02f * (i % 3))
            drawCircle(
                Brush.linearGradient(
                    listOf(Color(0xCC7FDBFF), Color(0x558C9EFF)),
                    Offset(cx - r, cy - r), Offset(cx + r, cy + r)
                ),
                r, Offset(cx, cy)
            )
            drawCircle(Color(0x66FFFFFF), r * 1.9f, Offset(cx, cy), style = Stroke(2f))
        }
    }
}

@Composable
fun Glass(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    pad: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = LocalBg.current
    var pos by remember { mutableStateOf(Offset.Zero) }
    Box(modifier.onGloballyPositioned { pos = it.positionInRoot() }.clip(shape)) {
        if (bg != null) {
            Box(
                Modifier.matchParentSize().layout { m, c ->
                    val pl = m.measure(Constraints.fixed(bg.wPx, bg.hPx))
                    layout(c.maxWidth, c.maxHeight) {
                        pl.place(-pos.x.roundToInt(), -pos.y.roundToInt())
                    }
                }
            ) {
                Scene(Modifier.fillMaxSize().blur(28.dp), bg.p)
            }
        }
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(
                    listOf(Color(0x40FFFFFF), Color(0x0FFFFFFF), Color(0x1A7FDBFF))
                )
            )
        )
        Box(
            Modifier.matchParentSize().border(
                1.2.dp,
                Brush.linearGradient(
                    listOf(Color(0xCCFFFFFF), Color(0x1AFFFFFF), Color(0x667FDBFF))
                ),
                shape
            )
        )
        Column(Modifier.padding(pad), content = content)
    }
}

@Composable
fun GlassButton(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(0.5f, 500f), label = "sc")
    val bg by animateColorAsState(
        if (active) Color(0x7700E5FF) else Color(0x26FFFFFF), label = "bg"
    )
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(50))
            .clickable(interactionSource = src, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TabBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val names = listOf("Home", "Find", "Battery", "Info")
    val haptic = LocalHapticFeedback.current
    Glass(modifier, RoundedCornerShape(50), 6.dp) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val seg = maxWidth / names.size
            val x by animateDpAsState(seg * selected, spring(0.7f, 300f), label = "x")
            Box(
                Modifier
                    .offset(x = x)
                    .width(seg)
                    .height(46.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x2200E5FF)))
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(50))
            )
            Row {
                names.forEachIndexed { i, n ->
                    val src = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clickable(interactionSource = src, indication = null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(i)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            n,
                            color = if (i == selected) Color.White else Color(0xAAFFFFFF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBud(
    cx: Float, cy: Float, r: Float, stem: Float, tilt: Float, a: Float, glow: Float
) {
    if (a <= 0.01f) return
    rotate(tilt, Offset(cx, cy)) {
        if (glow > 0.01f) {
            drawCircle(
                Brush.radialGradient(
                    listOf(Cyan.copy(alpha = 0.45f * glow * a), Color.Transparent),
                    Offset(cx, cy), r * 3.2f
                ),
                r * 3.2f, Offset(cx, cy)
            )
        }
        drawRoundRect(
            Brush.horizontalGradient(
                listOf(Color(0xFF9FB1BE), Color(0xFFF2F6F8), Color(0xFF7D8E9B)),
                cx - r * 0.3f, cx + r * 0.3f
            ),
            Offset(cx - r * 0.3f, cy), Size(r * 0.6f, stem), CornerRadius(r * 0.3f),
            alpha = a
        )
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF455A6B), Color(0xFF0F1923)),
                Offset(cx - r * 0.3f, cy - r * 0.35f), r * 1.3f
            ),
            r, Offset(cx, cy), alpha = a
        )
        drawCircle(Color.White.copy(alpha = 0.45f * a), r, Offset(cx, cy), style = Stroke(r * 0.05f))
        drawCircle(Color.White.copy(alpha = 0.35f * a), r * 0.22f, Offset(cx - r * 0.38f, cy - r * 0.4f))
        drawCircle(Cyan.copy(alpha = a), r * 0.12f, Offset(cx + r * 0.35f, cy + r * 0.25f))
    }
}

@Composable
fun CaseScene(open: Boolean, leftOut: Boolean, rightOut: Boolean) {
    val lid by animateFloatAsState(if (open) 1f else 0f, spring(0.55f, 45f), label = "lid")
    val lo by animateFloatAsState(if (leftOut) 1f else 0f, spring(0.5f, 60f), label = "lo")
    val ro by animateFloatAsState(if (rightOut) 1f else 0f, spring(0.5f, 60f), label = "ro")
    val tr = rememberInfiniteTransition(label = "float")
    val f by tr.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f"
    )
    Canvas(Modifier.fillMaxWidth().height(320.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cw = w * 0.74f
        val bodyH = h * 0.30f
        val tf = h * 0.16f
        val yT = h * 0.68f
        val x0 = cx - cw / 2f
        val hinge = yT - tf
        val led = (f + 1f) / 2f

        drawCircle(
            Brush.radialGradient(
                listOf(Color(0x6600E5FF), Color.Transparent),
                Offset(cx, yT + bodyH * 0.5f), w * 0.55f
            ),
            w * 0.55f, Offset(cx, yT + bodyH * 0.5f)
        )
        drawOval(Color(0x77000000), Offset(x0 + cw * 0.05f, yT + bodyH - 8f), Size(cw * 0.9f, 30f))

        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF2C3E52), Color(0xFF0E1622)), yT, yT + bodyH),
            Offset(x0, yT), Size(cw, bodyH), CornerRadius(40f)
        )
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color.Transparent), yT, yT + bodyH * 0.4f),
            Offset(x0, yT), Size(cw, bodyH * 0.4f), CornerRadius(40f)
        )
        drawRoundRect(Color(0x667FDBFF), Offset(x0, yT), Size(cw, bodyH), CornerRadius(40f), style = Stroke(2f))
        drawCircle(Cyan.copy(alpha = 0.5f + 0.5f * led), 7f, Offset(cx, yT + bodyH * 0.55f))

        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF05080F), Color(0xFF1A2636)), hinge, yT),
            Offset(x0, hinge), Size(cw, tf), CornerRadius(30f)
        )
        val wr = cw * 0.12f
        val wh = tf * 0.34f
        val wy = hinge + tf * 0.5f
        val lx = cx - cw * 0.2f
        val rx = cx + cw * 0.2f
        drawOval(Color(0xAA000000), Offset(lx - wr, wy - wh), Size(wr * 2f, wh * 2f))
        drawOval(Color(0xAA000000), Offset(rx - wr, wy - wh), Size(wr * 2f, wh * 2f))
        drawOval(Color(0x447FDBFF), Offset(lx - wr, wy - wh), Size(wr * 2f, wh * 2f), style = Stroke(1.5f))
        drawOval(Color(0x447FDBFF), Offset(rx - wr, wy - wh), Size(wr * 2f, wh * 2f), style = Stroke(1.5f))

        val th = lid * 112f * PI.toFloat() / 180f
        val rise = h * 0.30f
        val farY = hinge + tf * cos(th) - rise * sin(th)
        val hingeW = cw * 0.94f
        val farW = cw * (1f - 0.10f * sin(th))
        val lidPath = Path().apply {
            moveTo(cx - hingeW / 2f, hinge)
            lineTo(cx + hingeW / 2f, hinge)
            lineTo(cx + farW / 2f, farY)
            lineTo(cx - farW / 2f, farY)
            close()
        }
        val top = min(hinge, farY)
        val bot = max(hinge, farY) + 1f
        drawPath(
            lidPath,
            Brush.verticalGradient(listOf(Color(0xFF3A5068), Color(0xFF1B2836)), top, bot)
        )
        drawPath(lidPath, Color(0x887FDBFF), style = Stroke(2.5f, join = StrokeJoin.Round))

        val r = w * 0.05f
        val baseY = wy - r * 0.15f
        val lift = h * 0.44f
        drawBud(
            lx, baseY - lo * lift + lo * f * 10f, r, r * (1.1f + 1.5f * lo),
            -14f * lo, max(lid * lid, lo), lo
        )
        drawBud(
            rx, baseY - ro * lift - ro * f * 10f, r, r * (1.1f + 1.5f * ro),
            14f * ro, max(lid * lid, ro), ro
        )
    }
}

@Composable
fun RadarBig() {
    val tr = rememberInfiniteTransition(label = "radar")
    val sweep by tr.animateFloat(
        0f, 360f, infiniteRepeatable(tween(3600, easing = LinearEasing)), label = "s"
    )
    val ping by tr.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "p"
    )
    Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
        val c = center
        val rr = size.minDimension / 2f - 6f
        for (i in 1..4) {
            drawCircle(Color(0x33FFFFFF), rr * i / 4f, c, style = Stroke(1.5f))
        }
        drawLine(Color(0x22FFFFFF), Offset(c.x - rr, c.y), Offset(c.x + rr, c.y), 1.5f)
        drawLine(Color(0x22FFFFFF), Offset(c.x, c.y - rr), Offset(c.x, c.y + rr), 1.5f)
        rotate(sweep, c) {
            drawCircle(
                Brush.sweepGradient(listOf(Color.Transparent, Color(0x8800E5FF)), c),
                rr, c
            )
        }
        for (i in 0..1) {
            val q = (ping + i * 0.5f) % 1f
            drawCircle(Cyan.copy(alpha = (1f - q) * 0.6f), rr * q, c, style = Stroke(3f))
        }
        drawCircle(Cyan, 10f, c)
    }
}

@Composable
fun Ring(label: String, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "ring")
    val rot by tr.animateFloat(
        0f, 360f, infiniteRepeatable(tween(20000, easing = LinearEasing)), label = "rot"
    )
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val st = Stroke(
                    width = 8f, cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 22f))
                )
                rotate(rot) {
                    drawCircle(Color(0x77FFFFFF), size.minDimension / 2f - 8f, style = st)
                }
            }
            Text("N/A", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color(0xAAFFFFFF), fontSize = 12.sp)
    }
}

@Composable
fun Header(small: String, big: String) {
    Column {
        Text(small, color = Cyan, fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
        Text(big, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
fun HomePage(
    open: Boolean, leftOut: Boolean, rightOut: Boolean,
    onLid: () -> Unit, onLeft: () -> Unit, onRight: () -> Unit, onReset: () -> Unit
) {
    val tr = rememberInfiniteTransition(label = "dot")
    val dot by tr.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "dot"
    )
    Page {
        Header("WINGS BUDS FINDER", "Your earbuds.\nYour control.")
        Glass(Modifier.fillMaxWidth(), RoundedCornerShape(50), 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFB74D).copy(alpha = dot)))
                Spacer(Modifier.width(10.dp))
                Text("Not connected", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("DEMO MODE", color = Color(0xFFFFB74D), fontSize = 11.sp, letterSpacing = 1.sp)
            }
        }
        Glass(Modifier.fillMaxWidth()) {
            CaseScene(open, leftOut, rightOut)
            Text(
                "Manual animation. Not hardware data.",
                color = Color(0xFFFFB74D), fontSize = 11.sp, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton(if (open) "Close lid" else "Open lid", open, Modifier.weight(1f), onLid)
                GlassButton("Left bud", leftOut, Modifier.weight(1f), onLeft)
                GlassButton("Right bud", rightOut, Modifier.weight(1f), onRight)
            }
            Spacer(Modifier.height(8.dp))
            GlassButton("Reset", false, Modifier.fillMaxWidth(), onReset)
        }
    }
}

@Composable
fun FindPage() {
    Page {
        Header("FIND MY BUDS", "Signal radar")
        Glass(Modifier.fillMaxWidth()) {
            RadarBig()
            Spacer(Modifier.height(10.dp))
            Text("Signal: Unavailable", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                "Live RSSI tracking arrives with the Bluetooth scanner in the next step.",
                color = Color(0xAAFFFFFF), fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BatteryPage() {
    Page {
        Header("BATTERY", "Power")
        Glass(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Ring("Left")
                Ring("Case")
                Ring("Right")
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Not supported yet. The earbuds' battery protocol is not decoded, so no numbers are shown.",
                color = Color(0xAAFFFFFF), fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InfoPage() {
    Page {
        Header("DEVICE", "Information")
        Glass(Modifier.fillMaxWidth()) {
            val rows = listOf(
                "App" to "Wings Buds Finder 0.2.0",
                "Device" to "None selected",
                "Hardware link" to "Not connected",
                "Case state" to "Unknown",
                "Mode" to "Demo animations only"
            )
            rows.forEachIndexed { i, (k, v) ->
                if (i > 0) Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(k, color = Color(0xAAFFFFFF), fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(v, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var tab by remember { mutableStateOf(0) }
    var open by remember { mutableStateOf(false) }
    var leftOut by remember { mutableStateOf(false) }
    var rightOut by remember { mutableStateOf(false) }

    val tr = rememberInfiniteTransition(label = "bg")
    val p = tr.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p"
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val info = remember(constraints.maxWidth, constraints.maxHeight) {
            BgInfo(constraints.maxWidth, constraints.maxHeight, p)
        }
        CompositionLocalProvider(LocalBg provides info) {
            Scene(Modifier.fillMaxSize(), p)
            Crossfade(targetState = tab, animationSpec = tween(350), label = "page") { t ->
                when (t) {
                    0 -> HomePage(
                        open, leftOut, rightOut,
                        onLid = {
                            open = !open
                            if (!open) { leftOut = false; rightOut = false }
                        },
                        onLeft = { leftOut = !leftOut; if (leftOut) open = true },
                        onRight = { rightOut = !rightOut; if (rightOut) open = true },
                        onReset = { open = false; leftOut = false; rightOut = false }
                    )
                    1 -> FindPage()
                    2 -> BatteryPage()
                    else -> InfoPage()
                }
            }
            TabBar(
                tab, { tab = it },
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            )
        }
    }
}
