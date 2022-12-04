/* Copyright 2022 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farmerbb.notepad.data

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.FilenameFormat
import com.farmerbb.notepad.model.Prefs
import com.farmerbb.notepad.model.SortOrder
import com.farmerbb.notepad.usecase.SystemTheme
import de.schnettler.datastore.manager.DataStoreManager
import de.schnettler.datastore.manager.PreferenceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PreferenceManager private constructor(
    private val dataStoreManager: DataStoreManager,
    private val scope: CoroutineScope,
    private val systemTheme: SystemTheme
) {
    val isLightTheme get() = Prefs.ColorScheme.mapToFlow { theme -> theme == "light" }

    val backgroundColorRes get() = Prefs.ColorScheme.mapToFlow { theme ->
        when(theme) {
            "light" -> R.color.window_background
            else -> R.color.window_background_dark
        }
    }

    val primaryColorRes get() = Prefs.ColorScheme.mapToFlow { theme ->
        when(theme) {
            "light" -> R.color.text_color_primary
            else -> R.color.text_color_primary_dark
        }
    }

    val secondaryColorRes get() = Prefs.ColorScheme.mapToFlow { theme ->
        when(theme) {
            "light" -> R.color.text_color_secondary
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

    val fontFamily get() = Prefs.FontType.mapToFlow { theme ->
        when(theme) {
            "sans" -> FontFamily.SansSerif
            "serif" -> FontFamily.Serif
            else -> FontFamily.Monospace
        }
    }

    val sortOrder get() = Prefs.SortBy.mapToFlow(::toSortOrder)
    val filenameFormat get() = Prefs.ExportFilename.mapToFlow(::toFilenameFormat)
    val showDialogs get() = Prefs.ShowDialogs.asFlow
    val showDate get() = Prefs.ShowDate.asFlow
    val directEdit get() = Prefs.DirectEdit.asFlow
    val markdown get() = Prefs.Markdown.asFlow
    val rtlLayout get() = Prefs.RtlSupport.asFlow
    val firstRunComplete get() = Prefs.FirstRun.mapToFlow(::toBoolean)
    val firstViewComplete get() = Prefs.FirstLoad.mapToFlow(::toBoolean)
    val showDoubleTapMessage get() = Prefs.ShowDoubleTapMessage.asFlow

    @Suppress("UNCHECKED_CAST")
    private fun <T, R> PreferenceRequest<T>.mapToFlow(transform: (value: T) -> R) =
        dataStoreManager.getPreferenceFlow(this)
            .map {
                if (this is Prefs.ColorScheme && it is String && it == "system") {
                    systemTheme.colorScheme.stringValue as T
                } else it
            }
            .map(transform)
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = transform(defaultValue)
            )

    private val <T> PreferenceRequest<T>.asFlow get() = mapToFlow { it }

    private fun toSortOrder(value: String) = SortOrder.values().first {
        it.stringValue == value
    }

    private fun toFilenameFormat(value: String) = FilenameFormat.values().first {
        it.stringValue == value
    }

    private fun toBoolean(value: Int) = value > 0

    companion object {
        fun DataStoreManager.prefs(
            scope: CoroutineScope,
            systemTheme: SystemTheme
        ) = PreferenceManager(
            dataStoreManager = this,
            scope = scope,
            systemTheme = systemTheme
        )
    }
}