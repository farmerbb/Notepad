package com.farmerbb.notepad.data

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
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
        get() = Prefs.Theme.mapToFlow { theme ->
            when {
                theme.contains("light") -> R.color.window_background
                else -> R.color.window_background_dark
            }
        }

    val primaryColorRes: Flow<Int>
        get() = Prefs.Theme.mapToFlow { theme ->
            when {
                theme.contains("light") -> R.color.text_color_primary
                else -> R.color.text_color_primary_dark
            }
        }

    val secondaryColorRes: Flow<Int>
        get() = Prefs.Theme.mapToFlow { theme ->
            when {
                theme.contains("light") -> R.color.text_color_secondary
                else -> R.color.text_color_secondary_dark
            }
        }

    val textFontSize: Flow<TextUnit>
        get() = Prefs.FontSize.mapToFlow { fontSize ->
            when (fontSize) {
                "smallest" -> 12.sp
                "small" -> 14.sp
                "normal" -> 16.sp
                "large" -> 18.sp
                else -> 20.sp
            }
        }

    val dateFontSize: Flow<TextUnit>
        get() = Prefs.FontSize.mapToFlow { fontSize ->
            when (fontSize) {
                "smallest" -> 8.sp
                "small" -> 10.sp
                "normal" -> 12.sp
                "large" -> 14.sp
                else -> 16.sp
            }
        }

    val textTypeface: Flow<FontFamily>
        get() = Prefs.Theme.mapToFlow { theme ->
            when {
                theme.contains("sans") -> FontFamily.SansSerif
                theme.contains("serif") -> FontFamily.Serif
                else -> FontFamily.Monospace
            }
        }

    val markdownTypeface: Flow<String>
        get() = Prefs.Theme.mapToFlow { theme ->
            when {
                theme.contains("sans") -> "sans"
                theme.contains("serif") -> "serif"
                else -> "monospace"
            }
        }

    private inline fun <T, R> PreferenceRequest<T>.mapToFlow(crossinline transform: suspend (value: T) -> R)
        = dataStoreManager.getPreferenceFlow(this).map(transform)
}