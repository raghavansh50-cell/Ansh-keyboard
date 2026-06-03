package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyboardDao {
    @Query("SELECT * FROM keyboard_presets ORDER BY id DESC")
    fun getAllPresets(): Flow<List<KeyboardPreset>>

    @Query("SELECT * FROM keyboard_presets WHERE id = :id")
    suspend fun getPresetById(id: Int): KeyboardPreset?

    @Query("SELECT * FROM keyboard_presets WHERE isDefault = 1 LIMIT 1")
    fun getDefaultPresetFlow(): Flow<KeyboardPreset?>

    @Query("SELECT * FROM keyboard_presets WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPreset(): KeyboardPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: KeyboardPreset): Long

    @Delete
    suspend fun deletePreset(preset: KeyboardPreset)

    @Query("UPDATE keyboard_presets SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("UPDATE keyboard_presets SET isDefault = 1 WHERE id = :id")
    suspend fun setAsDefaultOnly(id: Int)

    @Transaction
    suspend fun makeDefault(presetId: Int) {
        clearDefaults()
        setAsDefaultOnly(presetId)
    }
}
