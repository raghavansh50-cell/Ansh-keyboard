package com.example.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.*
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardPreset
import com.example.ui.editor.KeyboardPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Custom InputMethodService (IME) that lets Android recognize this app as a system keyboard.
 * Displays Jetpack Compose layout custom profiles in real-time inside any active app.
 */
class CustomIME : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val scope = CoroutineScope(Dispatchers.Main)
    private val activePresetState = mutableStateOf<KeyboardPreset?>(null)
    private var isUppercase = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
    }

    override fun onCreateInputView(): View {
        val view = ComposeView(this).apply {
            androidx.lifecycle.ViewTreeLifecycleOwner.set(this, this@CustomIME)
            androidx.savedstate.ViewTreeSavedStateRegistryOwner.set(this, this@CustomIME)
            androidx.lifecycle.ViewTreeViewModelStoreOwner.set(this, this@CustomIME)

            setContent {
                var preset by remember { activePresetState }
                val currentPreset = preset ?: KeyboardPreset.createDefault()

                KeyboardPreview(
                    preset = currentPreset,
                    modifier = Modifier.fillMaxWidth(),
                    onKeyClick = { key ->
                        handleKeyPress(key)
                    }
                )
            }
        }
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        
        // Dynamic load default/active theme configuration
        loadActivePreset()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun loadActivePreset() {
        scope.launch {
            try {
                val db = KeyboardDatabase.getDatabase(this@CustomIME)
                val active = withContext(Dispatchers.IO) {
                    db.keyboardDao().getDefaultPreset()
                }
                if (active != null) {
                    activePresetState.value = active
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleKeyPress(key: String) {
        val inputConnection = currentInputConnection ?: return
        when (key) {
            "Space" -> inputConnection.commitText(" ", 1)
            "Delete" -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }
            "Enter" -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            "Shift" -> {
                isUppercase = !isUppercase
            }
            "Custom", "123", "ABC", "Symbols" -> {
                // Swap Layouts dynamically inside IME
                val current = activePresetState.value ?: KeyboardPreset.createDefault()
                val targetLayout = if (current.layoutType == "NUMERIC") "QWERTY" else "NUMERIC"
                activePresetState.value = current.copy(layoutType = targetLayout)
            }
            else -> {
                val charToCommit = if (isUppercase) key.uppercase() else key.lowercase()
                inputConnection.commitText(charToCommit, 1)
            }
        }
    }
}
