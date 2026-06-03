package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [KeyboardPreset::class], version = 1, exportSchema = false)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun keyboardDao(): KeyboardDao

    companion object {
        @Volatile
        private var INSTANCE: KeyboardDatabase? = null

        fun getDatabase(context: Context): KeyboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyboardDatabase::class.java,
                    "keyboard_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.keyboardDao()
                    
                    // Add default visual themes
                    val spaceTheme = KeyboardPreset(
                        id = 1,
                        name = "Space Stellar",
                        layoutType = "QWERTY",
                        backgroundColor = 0xFF0D0D15.toInt(),
                        backgroundType = "STOCK_SPACE",
                        keycapShape = "ROUNDED",
                        keycapColor = 0x25FFFFFF, // Soft transparent glass
                        keycapTextColor = 0xFFECEFF4.toInt(),
                        keycapActiveColor = 0xFF88C0D0.toInt(),
                        keySoundType = "SYNTH",
                        keySoundPitch = 1.0f,
                        fontSize = 18,
                        isDefault = true
                    )

                    val retroTheme = KeyboardPreset(
                        id = 2,
                        name = "Classic Typewriter",
                        layoutType = "QWERTY",
                        backgroundColor = 0xFFF2EFE9.toInt(),
                        backgroundType = "STOCK_ABSTRACT",
                        keycapShape = "SQUARE",
                        keycapColor = 0xFFE0DBD3.toInt(),
                        keycapTextColor = 0xFF2B2A27.toInt(),
                        keycapActiveColor = 0xFF8FBCBB.toInt(),
                        keySoundType = "TYPEWRITER",
                        keySoundPitch = 0.9f,
                        fontSize = 17,
                        isDefault = false
                    )

                    val animeTheme = KeyboardPreset(
                        id = 3,
                        name = "Blossom Anime",
                        layoutType = "QWERTY",
                        backgroundColor = 0xFFFFEFF3.toInt(),
                        backgroundType = "STOCK_ANIME",
                        keycapShape = "PILL",
                        keycapColor = 0x66FFFFFF,
                        keycapTextColor = 0xFFE84A5F.toInt(),
                        keycapActiveColor = 0xFFFF8E9E.toInt(),
                        keySoundType = "STANDARD",
                        keySoundPitch = 1.1f,
                        fontSize = 18,
                        isDefault = false
                    )

                    val natureTheme = KeyboardPreset(
                        id = 4,
                        name = "Zen Nature",
                        layoutType = "QWERTY",
                        backgroundColor = 0xFF121A1A.toInt(),
                        backgroundType = "STOCK_NATURE",
                        keycapShape = "HEXAGONAL",
                        keycapColor = 0x1A80CBC4.toInt(),
                        keycapTextColor = 0xFFB2DFDB.toInt(),
                        keycapActiveColor = 0xFF00796B.toInt(),
                        keySoundType = "MECHANICAL",
                        keySoundPitch = 0.85f,
                        fontSize = 18,
                        isDefault = false
                    )

                    val midnightClassic = KeyboardPreset(
                        id = 5,
                        name = "Brutalist Neon",
                        layoutType = "AZERTY",
                        backgroundColor = 0xFF101014.toInt(),
                        backgroundType = "STOCK_ABSTRACT",
                        keycapShape = "SKEWED",
                        keycapColor = 0xFF18181F.toInt(),
                        keycapTextColor = 0xFF00FFCC.toInt(),
                        keycapActiveColor = 0xFFFF007F.toInt(),
                        keySoundType = "MECHANICAL",
                        keySoundPitch = 1.2f,
                        fontSize = 19,
                        isDefault = false
                    )

                    dao.insertPreset(spaceTheme)
                    dao.insertPreset(retroTheme)
                    dao.insertPreset(animeTheme)
                    dao.insertPreset(natureTheme)
                    dao.insertPreset(midnightClassic)
                }
            }
        }
    }
}
