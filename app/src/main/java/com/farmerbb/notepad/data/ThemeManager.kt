package com.farmerbb.notepad.data

import androidx.compose.ui.text.font.FontFamily
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.Prefs
import de.schnettler.datastore.manager.DataStoreManager
import de.schnettler.datastore.manager.PreferenceRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeManager(
    private val dataStoreManager: DataStoreManager
) {
    val backgroundColorRes: Flow<Int>
        get() = Prefs.Theme.toFlow { theme ->
            when {
                theme.contains("light") -> R.color.window_background
                else -> R.color.window_background_dark
            }
        }

    val primaryColorRes: Flow<Int>
        get() = Prefs.Theme.toFlow { theme ->
            when {
                theme.contains("light") -> R.color.text_color_primary
                else -> R.color.text_color_primary_dark
            }
        }

    val secondaryColorRes: Flow<Int>
        get() = Prefs.Theme.toFlow { theme ->
            when {
                theme.contains("light") -> R.color.text_color_secondary
                else -> R.color.text_color_secondary_dark
            }
        }

    val textFontSize: Flow<Float>
        get() = Prefs.FontSize.toFlow { fontSize ->
            when (fontSize) {
                "smallest" -> 12f
                "small" -> 14f
                "normal" -> 16f
                "large" -> 18f
                else -> 20f
            }
        }

    val dateFontSize: Flow<Float>
        get() = Prefs.FontSize.toFlow { fontSize ->
            when (fontSize) {
                "smallest" -> 8f
                "small" -> 10f
                "normal" -> 12f
                "large" -> 14f
                else -> 16f
            }
        }

    val textTypeface: Flow<FontFamily>
        get() = Prefs.Theme.toFlow { theme ->
            when {
                theme.contains("sans") -> FontFamily.SansSerif
                theme.contains("serif") -> FontFamily.Serif
                else -> FontFamily.Monospace
            }
        }

    val markdownTypeface: Flow<String>
        get() = Prefs.Theme.toFlow { theme ->
            when {
                theme.contains("sans") -> "sans"
                theme.contains("serif") -> "serif"
                else -> "monospace"
            }
        }

    private inline fun <T, R> PreferenceRequest<T>.toFlow(crossinline transform: suspend (value: T) -> R)
        = dataStoreManager.getPreferenceFlow(this).map(transform)
}