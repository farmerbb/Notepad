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

package com.farmerbb.notepad.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.schnettler.datastore.manager.PreferenceRequest

object PrefKeys {
    val Theme = stringPreferencesKey("theme")
    val ColorScheme = stringPreferencesKey("color_scheme")
    val FontType = stringPreferencesKey("font_type")
    val FontSize = stringPreferencesKey("font_size")
    val SortBy = stringPreferencesKey("sort_by")
    val ExportFilename = stringPreferencesKey("export_filename")
    val ShowDialogs = booleanPreferencesKey("show_dialogs")
    val ShowDate = booleanPreferencesKey("show_date")
    val DirectEdit = booleanPreferencesKey("direct_edit")
    val Markdown = booleanPreferencesKey("markdown")
    val RtlSupport = booleanPreferencesKey("rtl_layout")
    val ShowDoubleTapMessage = booleanPreferencesKey("show_double_tap_message")
    val FirstRun = intPreferencesKey("first-run")
    val FirstLoad = intPreferencesKey("first-load")
}

object Prefs {
    @Deprecated("Use ColorScheme or FontType instead")
    object Theme: PreferenceRequest<String>(
        key = PrefKeys.Theme,
        defaultValue = "light-sans"
    )

    object ColorScheme: PreferenceRequest<String>(
        key = PrefKeys.ColorScheme,
        defaultValue = "system"
    )

    object FontType: PreferenceRequest<String>(
        key = PrefKeys.FontType,
        defaultValue = "sans"
    )

    object FontSize: PreferenceRequest<String>(
        key = PrefKeys.FontSize,
        defaultValue = "normal"
    )

    object SortBy: PreferenceRequest<String>(
        key = PrefKeys.SortBy,
        defaultValue = "date"
    )

    object ExportFilename: PreferenceRequest<String>(
        key = PrefKeys.ExportFilename,
        defaultValue = "text-only"
    )

    object ShowDialogs: PreferenceRequest<Boolean>(
        key = PrefKeys.ShowDialogs,
        defaultValue = false
    )

    object ShowDate: PreferenceRequest<Boolean>(
        key = PrefKeys.ShowDate,
        defaultValue = false
    )

    object DirectEdit: PreferenceRequest<Boolean>(
        key = PrefKeys.DirectEdit,
        defaultValue = false
    )

    object Markdown: PreferenceRequest<Boolean>(
        key = PrefKeys.Markdown,
        defaultValue = false
    )

    object RtlSupport: PreferenceRequest<Boolean>(
        key = PrefKeys.RtlSupport,
        defaultValue = false
    )

    object ShowDoubleTapMessage: PreferenceRequest<Boolean>(
        key = PrefKeys.ShowDoubleTapMessage,
        defaultValue = true
    )

    object FirstRun: PreferenceRequest<Int>(
        key = PrefKeys.FirstRun,
        defaultValue = 0
    )

    object FirstLoad: PreferenceRequest<Int>(
        key = PrefKeys.FirstLoad,
        defaultValue = 0
    )
}

enum class SortOrder(val stringValue: String) {
    DateDescending("date"),
    DateAscending("date-reversed"),
    TitleDescending("name-reversed"),
    TitleAscending("name"),
}

enum class FilenameFormat(val stringValue: String) {
    TitleOnly("text-only"),
    TitleAndTimestamp("text-timestamp"),
    TimestampAndTitle("timestamp-text"),
}