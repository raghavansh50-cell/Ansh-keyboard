package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardPreset
import com.example.data.KeyboardRepository
import com.example.ui.editor.EditorScreen
import com.example.ui.gallery.GalleryScreen
import com.example.ui.setup.SetupGuide
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val keyboardViewModel: KeyboardViewModel = viewModel()
                    val presets by keyboardViewModel.allPresets.collectAsStateWithLifecycle()

                    NavHost(
                        navController = navController,
                        startDestination = "gallery"
                    ) {
                        // Gallery List
                        composable("gallery") {
                            GalleryScreen(
                                presets = presets,
                                onSelectActive = { presetId ->
                                    keyboardViewModel.makeDefault(presetId)
                                },
                                onEditPreset = { presetId ->
                                    navController.navigate("editor/$presetId")
                                },
                                onDeletePreset = { preset ->
                                    keyboardViewModel.deletePreset(preset)
                                },
                                onAddNewPreset = { name ->
                                    keyboardViewModel.insertPreset(name) { insertedId ->
                                        navController.navigate("editor/$insertedId")
                                    }
                                },
                                onNavigateToSetup = {
                                    navController.navigate("setup")
                                }
                            )
                        }

                        // Customizer Editor Studio
                        composable(
                            route = "editor/{presetId}",
                            arguments = listOf(navArgument("presetId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val presetId = backStackEntry.arguments?.getInt("presetId") ?: 0
                            val preset = presets.find { it.id == presetId }

                            if (preset != null) {
                                EditorScreen(
                                    initialPreset = preset,
                                    onSavePreset = { updated ->
                                        keyboardViewModel.updatePreset(updated)
                                        navController.popBackStack()
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }

                        // Setup Walkthrough
                        composable("setup") {
                            SetupGuide(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard robust MVVM ViewModel that retrieves and updates custom key preset properties.
 */
class KeyboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KeyboardRepository

    val allPresets: StateFlow<List<KeyboardPreset>>

    init {
        val database = KeyboardDatabase.getDatabase(application)
        repository = KeyboardRepository(database.keyboardDao())
        allPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun makeDefault(presetId: Int) {
        viewModelScope.launch {
            repository.makeDefault(presetId)
        }
    }

    fun deletePreset(preset: KeyboardPreset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    fun insertPreset(presetName: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val template = KeyboardPreset.createDefault(name = presetName)
            val insertedId = repository.insertPreset(template)
            onComplete(insertedId.toInt())
        }
    }

    fun updatePreset(preset: KeyboardPreset) {
        viewModelScope.launch {
            repository.insertPreset(preset)
        }
    }
}
