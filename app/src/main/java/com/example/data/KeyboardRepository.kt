package com.example.data

import kotlinx.coroutines.flow.Flow

class KeyboardRepository(private val keyboardDao: KeyboardDao) {
    val allPresets: Flow<List<KeyboardPreset>> = keyboardDao.getAllPresets()
    val defaultPresetFlow: Flow<KeyboardPreset?> = keyboardDao.getDefaultPresetFlow()

    suspend fun getPresetById(id: Int): KeyboardPreset? {
        return keyboardDao.getPresetById(id)
    }

    suspend fun insertPreset(preset: KeyboardPreset): Long {
        return keyboardDao.insertPreset(preset)
    }

    suspend fun deletePreset(preset: KeyboardPreset) {
        keyboardDao.deletePreset(preset)
    }

    suspend fun makeDefault(presetId: Int) {
        keyboardDao.makeDefault(presetId)
    }

    suspend fun getCurrentDefaultPreset(): KeyboardPreset? {
        return keyboardDao.getDefaultPreset()
    }
}
