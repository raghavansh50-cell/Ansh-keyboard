package com.example.ui.editor

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreset
import com.example.keyboard.AudioSynth
import java.io.File

// Hexagon Shape
val HexagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.23f)
    lineTo(w, h * 0.77f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.77f)
    lineTo(0f, h * 0.23f)
    close()
}

// Skewed Brutalist Shape
val SkewedShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 4.dp,
    bottomStart = 4.dp,
    bottomEnd = 16.dp
)

fun getShapeFromType(style: String): Shape {
    return when (style) {
        "SQUARE" -> RoundedCornerShape(4.dp)
        "PILL" -> RoundedCornerShape(24.dp)
        "SKEWED" -> SkewedShape
        "HEXAGONAL" -> HexagonShape
        else -> RoundedCornerShape(10.dp) // ROUNDED (standard)
    }
}

fun getFontFamily(path: String?): FontFamily {
    if (path.isNullOrEmpty()) return FontFamily.Default
    val file = File(path)
    return if (file.exists()) {
        try {
            FontFamily(Font(file))
        } catch (e: Exception) {
            e.printStackTrace()
            FontFamily.Default
        }
    } else {
        FontFamily.Default
    }
}

@Composable
fun KeyboardPreview(
    preset: KeyboardPreset,
    modifier: Modifier = Modifier,
    onKeyClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val customFont = remember(preset.customFontPath) { getFontFamily(preset.customFontPath) }
    
    // Loaded Custom Image Bitmap (if applicable)
    val customBitmap = remember(preset.customBackgroundImagePath, preset.backgroundType) {
        if (preset.backgroundType == "CUSTOM" && !preset.customBackgroundImagePath.isNullOrEmpty()) {
            val file = File(preset.customBackgroundImagePath)
            if (file.exists()) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else null
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(preset.backgroundColor))
    ) {
        // Draw backgrounds programmatically to avoid heavy resources
        when (preset.backgroundType) {
            "STOCK_SPACE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Deep galactic background
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF040212), Color(0xFF13092A), Color(0xFF0D0518))
                        )
                    )
                    // Gas Nebulae (Glow spheres)
                    drawCircle(
                        color = Color(0x2EAD33FF),
                        radius = size.width * 0.4f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color(0x2B00E1D9),
                        radius = size.width * 0.3f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.8f)
                    )
                    // Stars
                    val randOffsets = listOf(
                        0.15f to 0.25f, 0.45f to 0.12f, 0.75f to 0.35f, 0.88f to 0.15f,
                        0.22f to 0.75f, 0.52f to 0.65f, 0.61f to 0.85f, 0.92f to 0.72f,
                        0.35f to 0.45f, 0.11f to 0.90f
                    )
                    randOffsets.forEach { (xRatio, yRatio) ->
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(size.width * xRatio, size.height * yRatio)
                        )
                    }
                    // Draws a planet
                    drawCircle(
                        color = Color(0xFFCD5C5C),
                        radius = 28.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.45f)
                    )
                    drawArc(
                        color = Color(0x8DFFD700),
                        startAngle = -20f,
                        sweepAngle = 220f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.65f - 40.dp.toPx(), size.height * 0.45f - 15.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(80.dp.toPx(), 30.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
            "STOCK_NATURE" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Sunset Forest ambient gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE27D60), Color(0xFF85CDCA), Color(0xFF41B3A3))
                        )
                    )
                    // Drawing some mountain ridges at bottom
                    val path1 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(size.width * 0.25f, size.height * 0.75f)
                        lineTo(size.width * 0.5f, size.height * 0.88f)
                        lineTo(size.width * 0.75f, size.height * 0.68f)
                        lineTo(size.width, size.height * 0.85f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path1, color = Color(0x4D0E3029))

                    val path2 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(size.width * 0.15f, size.height * 0.82f)
                        lineTo(size.width * 0.42f, size.height * 0.90f)
                        lineTo(size.width * 0.68f, size.height * 0.78f)
                        lineTo(size.width, size.height * 0.92f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path2, color = Color(0x660B1F1C))
                }
            }
            "STOCK_ABSTRACT" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Dark retro/brutalist geometry mesh
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                        )
                    )
                    // Modern grid intersections
                    for (i in 0..10) {
                        drawLine(
                            color = Color(0x1300FFCC),
                            start = androidx.compose.ui.geometry.Offset(0f, (size.height / 10f) * i),
                            end = androidx.compose.ui.geometry.Offset(size.width, (size.height / 10f) * i),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color(0x13FF007F),
                            start = androidx.compose.ui.geometry.Offset((size.width / 10f) * i, 0f),
                            end = androidx.compose.ui.geometry.Offset((size.width / 10f) * i, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    // Glowing curves
                    val wavePath = Path().apply {
                        moveTo(0f, size.height * 0.5f)
                        quadraticTo(size.width * 0.3f, size.height * 0.1f, size.width * 0.6f, size.height * 0.8f)
                        quadraticTo(size.width * 0.85f, size.height * 0.4f, size.width, size.height * 0.7f)
                    }
                    drawPath(
                        path = wavePath,
                        color = Color(0x8D00FFCC),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            "STOCK_ANIME" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Soft anime pastel sky
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFB7B2), Color(0xFFFFDAC1), Color(0xFFE2F0CB))
                        )
                    )
                    // Clouds
                    val cloudBrush = Brush.radialGradient(
                        colors = listOf(Color(0xE6FFFFFF), Color(0x00FFFFFF))
                    )
                    drawCircle(
                        brush = cloudBrush,
                        radius = 65.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.4f)
                    )
                    drawCircle(
                        brush = cloudBrush,
                        radius = 85.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.5f)
                    )
                    // Sparkles (4 point star shape)
                    val sparkles = listOf(
                        size.width * 0.15f to size.height * 0.2f,
                        size.width * 0.85f to size.height * 0.25f,
                        size.width * 0.55f to size.height * 0.15f
                    )
                    sparkles.forEach { (sx, sy) ->
                        val starPath = Path().apply {
                            moveTo(sx, sy - 12.dp.toPx())
                            quadraticTo(sx, sy, sx + 12.dp.toPx(), sy)
                            quadraticTo(sx, sy, sx, sy + 12.dp.toPx())
                            quadraticTo(sx, sy, sx - 12.dp.toPx(), sy)
                            quadraticTo(sx, sy, sx, sy - 12.dp.toPx())
                            close()
                        }
                        drawPath(starPath, color = Color(0xCCFFB7B2))
                        drawPath(starPath, color = Color.White)
                    }
                }
            }
            "CUSTOM" -> {
                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap,
                        contentDescription = "User Keyboard Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback Solid Box
                    Box(modifier = Modifier.fillMaxSize().background(Color(preset.backgroundColor)))
                }
            }
            else -> {
                // Color preset directly
                Box(modifier = Modifier.fillMaxSize().background(Color(preset.backgroundColor)))
            }
        }

        // Keyboard structure containing rows
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val keyShape = remember(preset.keycapShape) { getShapeFromType(preset.keycapShape) }
            val capColor = Color(preset.keycapColor)
            val textColor = Color(preset.keycapTextColor)
            val activeColor = Color(preset.keycapActiveColor)

            when (preset.layoutType) {
                "AZERTY" -> {
                    // AZERTY Rows
                    val row1 = listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P")
                    val row2 = listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M")
                    val row3 = listOf("Shift", "W", "X", "C", "V", "B", "N", "Delete")
                    val row4 = listOf("123", "Custom", "Space", ".", "Enter")

                    KeyboardRow(row = row1, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row2, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row3, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row4, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                }
                "QWERTZ" -> {
                    // QWERTZ Rows
                    val row1 = listOf("Q", "W", "E", "R", "T", "Z", "U", "I", "O", "P")
                    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
                    val row3 = listOf("Shift", "Y", "X", "C", "V", "B", "N", "M", "Delete")
                    val row4 = listOf("123", "Custom", "Space", ".", "Enter")

                    KeyboardRow(row = row1, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row2, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row3, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row4, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                }
                "NUMERIC" -> {
                    // Numeric / Symbol Rows
                    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    val row2 = listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\"")
                    val row3 = listOf("Symbols", ".", ",", "?", "!", "'", "Delete")
                    val row4 = listOf("ABC", "Custom", "Space", "_", "Enter")

                    KeyboardRow(row = row1, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row2, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row3, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row4, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                }
                else -> {
                    // QWERTY Layout rows
                    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
                    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
                    val row3 = listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Delete")
                    val row4 = listOf("123", "Custom", "Space", ".", "Enter")

                    KeyboardRow(row = row1, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row2, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row3, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                    KeyboardRow(row = row4, keyShape, capColor, textColor, activeColor, preset, customFont, onKeyClick)
                }
            }
        }
    }
}

@Composable
fun KeyboardRow(
    row: List<String>,
    shape: Shape,
    capColor: Color,
    textColor: Color,
    activeColor: Color,
    preset: KeyboardPreset,
    customFont: FontFamily,
    onKeyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.forEach { char ->
            // Dynamic weight based on standard vs action keys
            val isSpace = char == "Space"
            val isEnter = char == "Enter"
            val isDelete = char == "Delete"
            val isShift = char == "Shift"
            val isABC = char == "ABC"
            val is123 = char == "123"
            val isSymbols = char == "Symbols"

            val keyWeight = when {
                isSpace -> 4.5f
                isEnter -> 1.8f
                isDelete -> 1.5f
                isShift -> 1.3f
                isABC || is123 || isSymbols -> 1.3f
                else -> 1f
            }

            Box(
                modifier = Modifier
                    .weight(keyWeight)
                    .fillMaxHeight()
                    .padding(vertical = 3.dp)
                    .clip(shape)
                    .background(if (isEnter || isShift) activeColor else capColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.foundation.interaction.rememberRipple(
                            color = if (isEnter || isShift) Color.White else activeColor
                        ),
                        onClick = {
                            // Play synthesized tone instantly on a background thread
                            AudioSynth.playSound(preset.keySoundType, preset.keySoundPitch)
                            // Triggers key logic
                            onKeyClick(char)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isSpace -> {
                        Icon(
                            imageVector = Icons.Default.SpaceBar,
                            contentDescription = "Space",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    isDelete -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = if (isEnter || isShift) Color.White else textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    isEnter -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Enter",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    isShift -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Shift",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = char,
                            fontSize = preset.fontSize.sp,
                            fontFamily = customFont,
                            color = if (isEnter || isShift) Color.White else textColor,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
