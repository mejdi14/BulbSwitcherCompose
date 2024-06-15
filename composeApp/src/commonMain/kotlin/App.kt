import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bulbswitchercompose.composeapp.generated.resources.Res
import bulbswitchercompose.composeapp.generated.resources.bulb_switcher
import bulbswitchercompose.composeapp.generated.resources.compose_multiplatform
import bulbswitchercompose.composeapp.generated.resources.tim

import data.BulbStringConfig
import data.BulbSwitcherActionListener
import kotlinx.coroutines.launch
import theme.AppTheme
import viewmodel.ThemeViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
@Preview
fun App() {
    val viewModel : ThemeViewModel = ThemeViewModel()
    val isDarkTheme = viewModel.isDarkTheme.collectAsState().value
    val isDark = remember { mutableStateOf(false) }

    AppTheme(darkTheme = isDark.value) {
        BulbSwitcher(listener = object : BulbSwitcherActionListener{
            override fun onPull(position: Offset) {
                // when you start pulling the string
            }

            override fun onRelease(position: Offset) {
                // when you release the string
                isDark.value = !isDark.value
            }

            override fun onEndRelease() {
            }

        }, modifier = Modifier.background(color = MaterialTheme.colors.background))
    }
}

@Composable
private fun BulbSwitcher(config: BulbStringConfig = BulbStringConfig(),
                         listener: BulbSwitcherActionListener,
                         modifier: Modifier = Modifier) {
    var touchPosition by remember { mutableStateOf(config.initialTouchPosition) }
    var isTouching by remember { mutableStateOf(false) }
    val bulbCenterX = remember { config.bulbCenterX }
    val endPoint = remember { mutableStateOf(Offset(bulbCenterX, config.initialYOffset)) }
    val waveAmplitude = remember { Animatable(0f) }
    val yOffset = remember { Animatable(config.initialYOffset) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isTouching) {
        if (!isTouching) {
            val waveSequence = config.waveSequence
            val lengthSequence = config.lengthSequence


            coroutineScope.launch {
                val lengthAnimations = launch {
                    lengthSequence.forEach { length ->
                        yOffset.animateTo(
                            targetValue = length,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                val waveAnimations = launch {
                    waveSequence.forEach { (amplitude, direction) ->
                        waveAmplitude.animateTo(
                            targetValue = amplitude * direction,  // Positive for right, negative for left
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                        waveAmplitude.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                lengthAnimations.join()
                waveAnimations.join()
                listener.onEndRelease()
                waveAmplitude.snapTo(0f)
            }
        }
    }

    MaterialTheme
    Box {
        Column(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.height(20.dp))
            Image(
                painterResource(Res.drawable.tim),
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    rotationZ = 180f
                    translationX = -30f
                    translationY = 4f
                }
            )
            Canvas(modifier = Modifier.weight(1f).size(width = 100.dp, height = 50.dp)
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (abs(down.position.x - endPoint.value.x) <= config.touchThreshold && abs(down.position.y - endPoint.value.y) <= config.touchThreshold) {
                                touchPosition = down.position
                                isTouching = true
                                do {
                                    val event = awaitPointerEvent()
                                    touchPosition = event.changes.first().position
                                } while (event.changes.any { it.pressed })
                                    listener.onRelease(touchPosition)
                                isTouching = false
                            }
                        }
                    }
                }) {
                val path = Path().apply {
                    moveTo(bulbCenterX, 0f)
                    if (isTouching) {
                        lineTo(touchPosition.x, touchPosition.y)
                    } else {
                        var x = bulbCenterX
                        var y = 0f
                        for (i in 0..yOffset.value.toInt() step 5) {
                            y = i.toFloat()
                            val phase = 2 * (yOffset.value - y) / yOffset.value * PI
                            val dx = waveAmplitude.value * sin(phase).toFloat()
                            lineTo(x + dx, y)
                        }
                    }
                }
                drawPath(
                    path = path,
                    color = config.lineColor,
                    style = Stroke(width = config.strokeWidth.dp.toPx())
                )
                drawCircle(
                    color = config.circleColor,
                    radius = config.circleRadius.dp.toPx(),
                    center = if (isTouching) touchPosition else Offset(bulbCenterX, yOffset.value)
                )
            }
        }
        Column {
            Box(modifier = Modifier.width(200.dp).height(90.dp),
                )
        }
    }
}
