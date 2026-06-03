package com.example.ui.editor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreset
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initialPreset: KeyboardPreset,
    onSavePreset: (KeyboardPreset) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPreset by remember { mutableStateOf(initialPreset) }
    
    // Sandbox Typing state
    var typedValue by remember { mutableStateOf("") }
    var isUppercase by remember { mutableStateOf(false) }

    // File pickers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedPath = copyUriToInternalFile(context, it, "user_bg_${currentPreset.id}.jpg")
            if (copiedPath != null) {
                currentPreset = currentPreset.copy(
                    backgroundType = "CUSTOM",
                    customBackgroundImagePath = copiedPath
                )
            }
        }
    }

    val fontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedPath = copyUriToInternalFile(context, it, "user_font_${currentPreset.id}.ttf")
            if (copiedPath != null) {
                currentPreset = currentPreset.copy(
                    customFontPath = copiedPath
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(currentPreset.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { onSavePreset(currentPreset) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Layout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // SANDBOX DISPLAY PANEL (STUCK TO TOP)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (typedValue.isEmpty()) "Tap keycaps below to test output..." else typedValue,
                            fontSize = 16.sp,
                            color = if (typedValue.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                            minLines = 1,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (typedValue.isNotEmpty()) {
                        IconButton(
                            onClick = { typedValue = "" },
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors()
                        ) {
                            Text("×", fontSize = 14.sp)
                        }
                    }
                }
            }

            // KEYBOARD PREVIEW AREA (LIVE IN REAL TIME)
            KeyboardPreview(
                preset = currentPreset,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onKeyClick = { key ->
                    when (key) {
                        "Space" -> typedValue += " "
                        "Delete" -> if (typedValue.isNotEmpty()) typedValue = typedValue.dropLast(1)
                        "Enter" -> typedValue += "\n"
                        "Shift" -> isUppercase = !isUppercase
                        "Custom", "123", "ABC", "Symbols" -> {
                            // Secondary actions or layout swaps - preview layout swaps logic
                            val targetLayout = if (currentPreset.layoutType == "NUMERIC") "QWERTY" else "NUMERIC"
                            currentPreset = currentPreset.copy(layoutType = targetLayout)
                        }
                        else -> {
                            typedValue += if (isUppercase) key.uppercase() else key.lowercase()
                        }
                    }
                }
            )

            // CONTROLS LIST (SCROLLABLE UNDERNEATH)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Preset Title edit
                    OutlinedTextField(
                        value = currentPreset.name,
                        onValueChange = { currentPreset = currentPreset.copy(name = it) },
                        label = { Text("Layout Design Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 1. CHOOSE SYSTEM LAYOUT
                    OptionTitle(icon = Icons.Default.Keyboard, title = "Keyboard Base Layout")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val layouts = listOf("QWERTY", "AZERTY", "QWERTZ")
                        layouts.forEach { layout ->
                            val isSelected = currentPreset.layoutType == layout
                            FilterChip(
                                selected = isSelected,
                                onClick = { currentPreset = currentPreset.copy(layoutType = layout) },
                                label = { Text(layout) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 2. STOCK DESIGNS & THEME CATEGORIES
                    OptionTitle(icon = Icons.Default.Layers, title = "Theme & Graphics Background")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val bgCategories = listOf(
                            "STOCK_SPACE" to "Space",
                            "STOCK_NATURE" to "Nature",
                            "STOCK_ABSTRACT" to "Abstract",
                            "STOCK_ANIME" to "Anime",
                            "COLOR" to "Minimal"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Render Row 1
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                bgCategories.take(3).forEach { (value, label) ->
                                    val isSelected = currentPreset.backgroundType == value
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { currentPreset = currentPreset.copy(backgroundType = value) },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }
                            // Render Row 2
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                bgCategories.drop(3).forEach { (value, label) ->
                                        val isSelected = currentPreset.backgroundType == value
                                        ElevatedFilterChip(
                                            selected = isSelected,
                                            onClick = { currentPreset = currentPreset.copy(backgroundType = value) },
                                            label = { Text(label, fontSize = 12.sp) }
                                        )
                                    }
                                
                                // Import custom button
                                ElevatedFilterChip(
                                    selected = currentPreset.backgroundType == "CUSTOM",
                                    onClick = { imagePicker.launch("image/*") },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Text("Own Image...", fontSize = 12.sp)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Background color hex selector (if Minimal Color or overall backup tint)
                    if (currentPreset.backgroundType == "COLOR") {
                        Text(
                            text = "Primary Background Color",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presetBgColors = listOf(
                                0xFF121212.toInt(), // Charcoal
                                0xFF0D1225.toInt(), // Classic Navy
                                0xFF1E281E.toInt(), // Earth moss
                                0xFFF2EFE9.toInt(), // Vintage Cream
                                0xFFFFEFF3.toInt(), // Pastel Pink
                                0xFF2C2F3F.toInt()  // Space slate
                            )
                            presetBgColors.forEach { colorVal ->
                                val isSelected = currentPreset.backgroundColor == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal))
                                        .clickable {
                                            currentPreset = currentPreset.copy(backgroundColor = colorVal)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = "Selected",
                                            tint = if (colorVal == 0xFFF2EFE9.toInt() || colorVal == 0xFFFFEFF3.toInt()) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 3. KEYCAP SHAPES
                    OptionTitle(icon = Icons.Default.Palette, title = "Keycap Outer Shape")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val shapeTypes = listOf(
                            "ROUNDED" to "Rounded",
                            "SQUARE" to "Square",
                            "PILL" to "Pill",
                            "SKEWED" to "Skewed",
                            "HEXAGONAL" to "Hexagon"
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Multi column scrolling row to fit beautifully without breaking bounds
                            BoxWithConstraints {
                                val chunkWidth = (maxWidth - 16.dp) / 4f
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    shapeTypes.forEach { (value, label) ->
                                        val isSelected = currentPreset.keycapShape == value
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    currentPreset = currentPreset.copy(keycapShape = value)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Keycap Background Color Selector
                    Text(
                        text = "Keycap Background Paint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val keycapColors = listOf(
                            0x25FFFFFF.toInt(), // Soft glass (White transparent)
                            0xFF242424.toInt(), // Deep Dark
                            0xFFE0DBD3.toInt(), // Vintage Cream Gray
                            0x2800FFCC.toInt(), // Glowing wave cyan (transparent)
                            0x66FFFFFF.toInt(), // Translucent white (strong)
                            0xFF1C2D37.toInt()  // Slate Navy
                        )
                        keycapColors.forEach { colorVal ->
                            val isSelected = currentPreset.keycapColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .clickable {
                                        currentPreset = currentPreset.copy(keycapColor = colorVal)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Selected",
                                        tint = if (colorVal == 0x25FFFFFF || colorVal == 0x2800FFCC) MaterialTheme.colorScheme.primary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Keycap Text Label Color Selector
                    Text(
                        text = "Keycap Text Label Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val keycapTextColors = listOf(
                            0xFFFFFFFF.toInt(), // Bright White
                            0xFFECEFF4.toInt(), // Cold White
                            0xFF2B2A27.toInt(), // Cold Black
                            0xFF00FFCC.toInt(), // Neon Mint
                            0xFFE84A5F.toInt(), // Anime Salmon Red
                            0xFFFFD700.toInt()  // Gold
                        )
                        keycapTextColors.forEach { colorVal ->
                            val isSelected = currentPreset.keycapTextColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .clickable {
                                        currentPreset = currentPreset.copy(keycapTextColor = colorVal)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Selected",
                                        tint = if (colorVal == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 4. KEY SOUNDS ENGINE & PITCH
                    OptionTitle(icon = Icons.Default.VolumeUp, title = "Key Sound Synthesis")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val sounds = listOf(
                            "STANDARD" to "Classic Clack",
                            "MECHANICAL" to "Mech POP",
                            "TYPEWRITER" to "Typewriter",
                            "SYNTH" to "Synth Beep",
                            "SILENT" to "Silent"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                sounds.take(3).forEach { (value, label) ->
                                    val isSelected = currentPreset.keySoundType == value
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { currentPreset = currentPreset.copy(keySoundType = value) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                sounds.drop(3).forEach { (value, label) ->
                                    val isSelected = currentPreset.keySoundType == value
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { currentPreset = currentPreset.copy(keySoundType = value) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Pitch Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Synthesizer Sound Pitch",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "%.2fx Freq".format(currentPreset.keySoundPitch),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = currentPreset.keySoundPitch,
                            onValueChange = { currentPreset = currentPreset.copy(keySoundPitch = it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    // 5. FONTS & TEXT SIZE
                    OptionTitle(icon = Icons.Default.FontDownload, title = "Custom Typography Fonts")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { fontPicker.launch("*/*") },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick .ttf / .otf File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (!currentPreset.customFontPath.isNullOrEmpty()) {
                            IconButton(
                                onClick = { currentPreset = currentPreset.copy(customFontPath = null) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("Reset Font", fontSize = 10.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!currentPreset.customFontPath.isNullOrEmpty()) {
                        Text(
                            text = "Active: Custom Font Loaded Successfully! ✓",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Using default system design font.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Font Size Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Label Font Scaling Size",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${currentPreset.fontSize} sp",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = currentPreset.fontSize.toFloat(),
                            onValueChange = { currentPreset = currentPreset.copy(fontSize = it.toInt()) },
                            valueRange = 14f..25f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Quick helper to copy URIs cleanly from context resolver to internal sandbox storage
fun copyUriToInternalFile(context: Context, uri: Uri, targetFileName: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val targetFile = File(context.filesDir, targetFileName)
        val outputStream = FileOutputStream(targetFile)
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
        targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
