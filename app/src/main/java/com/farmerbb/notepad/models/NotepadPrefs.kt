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

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.schnettler.datastore.manager.PreferenceMetaData

sealed class NotepadPrefs<T>(
  override val key: String,
  override val defaultValue: T,
  override val keyProvider: (String) -> Preferences.Key<T>,
): PreferenceMetaData<T> {
  object Theme : NotepadPrefs<String>(
    key = "theme",
    defaultValue = "light-sans",
    keyProvider = ::stringPreferencesKey
  )

  object FontSize : NotepadPrefs<String>(
    key = "font_size",
    defaultValue = "normal",
    keyProvider = ::stringPreferencesKey
  )

  object SortBy : NotepadPrefs<String>(
    key = "sort_by",
    defaultValue = "date",
    keyProvider = ::stringPreferencesKey
  )

  object ExportFilename : NotepadPrefs<String>(
    key = "export_filename",
    defaultValue = "text-only",
    keyProvider = ::stringPreferencesKey
  )

  object ShowDialogs : NotepadPrefs<Boolean>(
    key = "show_dialogs",
    defaultValue = false,
    keyProvider = ::booleanPreferencesKey
  )

  object ShowDate : NotepadPrefs<Boolean>(
    key = "show_date",
    defaultValue = false,
    keyProvider = ::booleanPreferencesKey
  )

  object DirectEdit : NotepadPrefs<Boolean>(
    key = "direct_edit",
    defaultValue = false,
    keyProvider = ::booleanPreferencesKey
  )

  object Markdown : NotepadPrefs<Boolean>(
    key = "markdown",
    defaultValue = false,
    keyProvider = ::booleanPreferencesKey
  )
}