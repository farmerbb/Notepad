/* Copyright 2021 Braden Farmer
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

package com.farmerbb.notepad.models

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.schnettler.datastore.manager.PreferenceRequest

sealed interface RightPaneState {
    object Empty: RightPaneState
    data class View(val id: Long): RightPaneState
    data class Edit(val id: Long? = null): RightPaneState

    companion object {
        const val VIEW = "View"
        const val EDIT = "Edit"
    }
}

object Prefs {
    object Theme: PreferenceRequest<String>(
        key = stringPreferencesKey("theme"),
        defaultValue = "light-sans"
    )

    object FontSize: PreferenceRequest<String>(
        key = stringPreferencesKey("font_size"),
        defaultValue = "normal"
    )

    object SortBy: PreferenceRequest<String>(
        key = stringPreferencesKey("sort_by"),
        defaultValue = "date"
    )

    object ExportFilename: PreferenceRequest<String>(
        key = stringPreferencesKey("export_filename"),
        defaultValue = "text-only"
    )

    object ShowDialogs: PreferenceRequest<Boolean>(
        key = booleanPreferencesKey("show_dialogs"),
        defaultValue = false
    )

    object ShowDate: PreferenceRequest<Boolean>(
        key = booleanPreferencesKey("show_date"),
        defaultValue = false
    )

    object DirectEdit: PreferenceRequest<Boolean>(
        key = booleanPreferencesKey("direct_edit"),
        defaultValue = false
    )

    object Markdown: PreferenceRequest<Boolean>(
        key = booleanPreferencesKey("markdown"),
        defaultValue = false
    )
}
