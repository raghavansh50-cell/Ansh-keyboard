package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyboard_presets")
data class KeyboardPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val layoutType: String = "QWERTY", // "QWERTY", "AZERTY", "QWERTZ", "NUMERIC"
    val backgroundColor: Int = 0xFF121212.toInt(),
    val backgroundType: String = "COLOR", // "COLOR", "STOCK_NATURE", "STOCK_SPACE", "STOCK_ABSTRACT", "STOCK_ANIME", "CUSTOM"
    val customBackgroundImagePath: String? = null,
    val keycapShape: String = "ROUNDED", // "ROUNDED", "SQUARE", "PILL", "SKEWED", "HEXAGONAL"
    val keycapColor: Int = 0xFF242424.toInt(),
    val keycapTextColor: Int = 0xFFFFFFFF.toInt(),
    val keycapActiveColor: Int = 0xFF6200EE.toInt(),
    val keySoundType: String = "STANDARD", // "STANDARD", "MECHANICAL", "TYPEWRITER", "SYNTH", "SILENT"
    val keySoundPitch: Float = 1.0f,
    val customFontPath: String? = null,
    val fontSize: Int = 18,
    val isDefault: Boolean = false
) {
    companion object {
        fun createDefault(id: Int = 0, name: String = "Cosmic Night", bgColor: Int = 0xFF0D0D15.toInt()): KeyboardPreset {
            return KeyboardPreset(
                id = id,
                name = name,
                layoutType = "QWERTY",
                backgroundColor = bgColor,
                backgroundType = "STOCK_SPACE",
                keycapShape = "ROUNDED",
                keycapColor = 0x22FFFFFF, // semi transparent
                keycapTextColor = 0xFFECEFF4.toInt(),
                keycapActiveColor = 0xFF81A1C1.toInt(),
                keySoundType = "MECHANICAL",
                keySoundPitch = 1.0f,
                fontSize = 18,
                isDefault = false
            )
        }
    }
}
