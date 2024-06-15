package data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class BulbStringConfig(
    val initialTouchPosition: Offset = Offset(100f, 100f),
    val bulbCenterX: Float = 100f,
    val initialYOffset: Float = 100f,
    val waveSequence: List<Pair<Float, Int>> = listOf(
        60f to 1,
        40f to -1,
        20f to 1,
        10f to -1
    ),
    val lengthSequence: List<Float> = listOf(
        60f,
        100f,
        80f,
        100f,
        80f,
        100f,
    ),
    val touchThreshold: Float = 50f,
    val strokeWidth: Float = 3f,
    val circleRadius: Float = 4f,
    val lineColor: Color = Color.Black,
    val circleColor: Color = Color.Black
)