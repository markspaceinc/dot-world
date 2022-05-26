package com.example.dotworld

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.*
import com.example.dotworld.ui.theme.DotWorldTheme
import kotlin.math.abs
import kotlin.math.pow

// Dot handling support
class Dot(
    var x: Float, var y: Float,
    var radius : Float,
    var selected: Boolean = false,
    var color : Int = 0
) {
    fun isEqual(otherDot: Dot): Boolean {
        var dotIsEqual = false

        if ( x == otherDot.x && y == otherDot.y && radius == otherDot.radius) {
            dotIsEqual = true
        }

        return dotIsEqual
    }
}

class DotManager() {
    fun findDotAt(dots : MutableList<Dot>, x : Float, y : Float): Dot? {
        var hitDot : Dot? = null

        for (currentDot in dots.asReversed()) {
            if ((x - currentDot.x).toDouble().pow(2.0)
                + (y - currentDot.y).toDouble().pow(2.0)
                < currentDot.radius.toDouble().pow(2.0)) {
                hitDot = currentDot
                break
            }
        }

        return hitDot
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DotWorldTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DotWorldUI()
                }
            }
        }
    }
}

@Composable
fun DotWorldUI() {
    Box(
        modifier = Modifier
            .background(color = Color.Gray)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        DrawDots()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawDots() {

    val dots = remember { mutableStateListOf<Dot>() }
    var selectedDot = remember { Dot(0.0F, 0.0F, 0.0F) }
    val dotManager = remember { DotManager() }
    var penDown by remember { mutableStateOf( false ) }
    var lastPenX by remember { mutableStateOf( 0F ) }
    var lastPenY by remember { mutableStateOf( 0F ) }

    var currentPointerIcon = remember { mutableStateOf( PointerIcon(0)) }

    var logText : String = ""

    // Make modifier
    val constructedModifier = Modifier
        .fillMaxSize()
        .pointerHoverIcon(currentPointerIcon.value, true)
        .pointerInteropFilter {
            val motionEvent = it
            var toolType = motionEvent.getToolType(0)
            var buttonState = motionEvent.buttonState

            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    var newSelectedDot = dotManager.findDotAt(dots, motionEvent.x, motionEvent.y)
                    if (newSelectedDot != null) {
                        if (!selectedDot.isEqual(newSelectedDot)) {
                            selectedDot.selected = false
                            selectedDot = newSelectedDot
                            selectedDot.selected = true

                            logText = "TAP SELECT at ${motionEvent.x}, ${motionEvent.y}"
                        }
                    } else {
                        selectedDot.selected = false
                        selectedDot = Dot(motionEvent.x, motionEvent.y, 50.0F)
                        selectedDot.selected = true
                        dots.add(selectedDot)

                        logText = "TAP ADD at ${motionEvent.x}, ${motionEvent.y}"
                    }

                    if (selectedDot != null) {
                        if (buttonState != 0) {
                            selectedDot.color = abs(selectedDot.color - 1)
                        }
                    }

                    penDown = true
                    lastPenX = motionEvent.x
                    lastPenY = motionEvent.y
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                }
                MotionEvent.ACTION_UP -> {
                    logText = "UP at ${motionEvent.x}, ${motionEvent.y}"
                    penDown = false
                }
                MotionEvent.ACTION_POINTER_UP -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    if (penDown && selectedDot != null) {
                        var deltaX = motionEvent.x - lastPenX
                        var deltaY = motionEvent.y - lastPenY
                        selectedDot.x += deltaX
                        selectedDot.y += deltaY
                        lastPenX = motionEvent.x
                        lastPenY = motionEvent.y

                        logText = "MOVE to ${motionEvent.x}, ${motionEvent.y}"
                    }
                }
                MotionEvent.ACTION_HOVER_ENTER -> {
                    currentPointerIcon.value = PointerIcon(4)

                    logText = "HOVER_ENTER"
                }
                MotionEvent.ACTION_HOVER_MOVE -> {
                    currentPointerIcon.value = PointerIcon(4)

                    logText = "HOVER_MOVE"
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    currentPointerIcon.value = PointerIcon(0)
                    logText = "HOVER_EXIT"
                }
            }
            true
        }

    // End make modifier
    Canvas(constructedModifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val dotColor1 = Color(1f, 1f, 0f, 1f)
        val dotColor2 = Color(0.5f, 1f, 0f, 1f)
        val selectedDotOutlineColor = Color(1f, 0.5f, 0f, 1f)
        val xyColor = Color(0f, 0f, 0f, 1f)

        val textPaint = Paint()
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 48f
        textPaint.color = 0xff000000.toInt()
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        for (dot in dots.asReversed()) {

            drawCircle(
                color = if (dot.color == 0) dotColor1 else dotColor2,
                center = Offset(x = dot.x, y = dot.y),
                radius = 50.0.toFloat()
            )

            if (dot == selectedDot) {
                drawCircle(
                    color = selectedDotOutlineColor,
                    center = Offset(x = dot.x, y = dot.y),
                    radius = 50.0.toFloat(),
                    style = Stroke(10f)
                )
            }
        }


        drawCircle(
            color = xyColor,
            center = Offset(x = lastPenX, y = lastPenY),
            radius = 2.0.toFloat()
        )
        // Log text
        drawIntoCanvas {
            it.nativeCanvas.drawText(logText, 25f, canvasHeight - 50f, textPaint)
        }
    }
}

