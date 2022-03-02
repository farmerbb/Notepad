package com.farmerbb.notepad.data

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.Prefs
import de.schnettler.datastore.manager.DataStoreManager
import de.schnettler.datastore.manager.PreferenceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ThemeManager(
    private val dataStoreManager: DataStoreManager,
    private val scope: CoroutineScope
) {
    val backgroundColorRes get() = Prefs.Theme.mapToFlow { theme ->
        when {
            theme.contains("light") -> R.color.window_background
            else -> R.color.window_background_dark
        }
    }

    val primaryColorRes get() = Prefs.Theme.mapToFlow { theme ->
        when {
            theme.contains("light") -> R.color.text_color_primary
            else -> R.color.text_color_primary_dark
        }
    }

    val secondaryColorRes get() = Prefs.Theme.mapToFlow { theme ->
        when {
            theme.contains("light") -> R.color.text_color_secondary
            else -> R.color.text_color_secondary_dark
        }
    }

    val textFontSize get() = Prefs.FontSize.mapToFlow { fontSize ->
        when (fontSize) {
            "smallest" -> 12.sp
            "small" -> 14.sp
            "normal" -> 16.sp
            "large" -> 18.sp
            else -> 20.sp
        }
    }

    val dateFontSize get() = Prefs.FontSize.mapToFlow { fontSize ->
        when (fontSize) {
            "smallest" -> 8.sp
            "small" -> 10.sp
            "normal" -> 12.sp
            "large" -> 14.sp
            else -> 16.sp
        }
    }

    val textTypeface get() = Prefs.Theme.mapToFlow { theme ->
        when {
            theme.contains("sans") -> FontFamily.SansSerif
            theme.contains("serif") -> FontFamily.Serif
            else -> FontFamily.Monospace
        }
    }

    val markdownTypeface get() = Prefs.Theme.mapToFlow { theme ->
        when {
            theme.contains("sans") -> "sans"
            theme.contains("serif") -> "serif"
            else -> "monospace"
        }
    }

    private fun <T, R> PreferenceRequest<T>.mapToFlow(transform: (value: T) -> R) =
        dataStoreManager.getPreferenceFlow(this).map(transform).stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = transform(defaultValue)
        )
}