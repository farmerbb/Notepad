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

@file:OptIn(ExperimentalMaterialApi::class)

package com.farmerbb.notepad.ui

import androidx.annotation.ArrayRes
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.NotepadPrefs
import de.schnettler.datastore.compose.PreferenceScreen
import de.schnettler.datastore.compose.model.BasePreferenceItem.PreferenceItem.RadioBoxListPreferenceItem
import de.schnettler.datastore.compose.model.BasePreferenceItem.PreferenceItem.SwitchPreferenceItem

@Composable fun AppSettings() {
  PreferenceScreen(
    items = listOf(
      RadioBoxListPreferenceItem(
        metaData = NotepadPrefs.Theme,
        title = stringResource(id = R.string.action_theme),
        summary = "",
        singleLineTitle = true,
        icon = {},
        entries = listPrefEntries(
          keyRes = R.array.theme_list_values,
          valueRes = R.array.theme_list
        ),
      ),
      RadioBoxListPreferenceItem(
        metaData = NotepadPrefs.FontSize,
        title = stringResource(id = R.string.action_font_size),
        summary = "",
        singleLineTitle = true,
        icon = {},
        entries = listPrefEntries(
          keyRes = R.array.font_size_list_values,
          valueRes = R.array.font_size_list
        ),
      ),
      RadioBoxListPreferenceItem(
        metaData = NotepadPrefs.SortBy,
        title = stringResource(id = R.string.action_sort_by),
        summary = "",
        singleLineTitle = true,
        icon = {},
        entries = listPrefEntries(
          keyRes = R.array.sort_by_list_values,
          valueRes = R.array.sort_by_list
        ),
      ),
      RadioBoxListPreferenceItem(
        metaData = NotepadPrefs.ExportFilename,
        title = stringResource(id = R.string.action_export_filename),
        summary = "",
        singleLineTitle = true,
        icon = {},
        entries = listPrefEntries(
          keyRes = R.array.exported_filename_list_values,
          valueRes = R.array.exported_filename_list
        ),
      ),
      SwitchPreferenceItem(
        metaData = NotepadPrefs.ShowDialogs,
        title = stringResource(id = R.string.pref_title_show_dialogs),
        summary = "",
        singleLineTitle = true,
        icon = {}
      ),
      SwitchPreferenceItem(
        metaData = NotepadPrefs.ShowDate,
        title = stringResource(id = R.string.pref_title_show_date),
        summary = "",
        singleLineTitle = true,
        icon = {}
      ),
      SwitchPreferenceItem(
        metaData = NotepadPrefs.DirectEdit,
        title = stringResource(id = R.string.pref_title_direct_edit),
        summary = "",
        singleLineTitle = true,
        icon = {}
      ),
      SwitchPreferenceItem(
        metaData = NotepadPrefs.Markdown,
        title = stringResource(id = R.string.pref_title_markdown),
        summary = "",
        singleLineTitle = true,
        icon = {}
      )
    )
  )
}

@ReadOnlyComposable
@Composable fun listPrefEntries(
  @ArrayRes keyRes: Int,
  @ArrayRes valueRes: Int
): Map<String, String> {
  val keys = stringArrayResource(id = keyRes)
  val values = stringArrayResource(id = valueRes)

  if(keys.size != values.size) {
    throw RuntimeException("Keys and values are not the same size")
  }

  val map = mutableMapOf<String, String>()
  for(i in keys.indices) {
    map[keys[i]] = values[i]
  }

  return map.toMutableMap()
}

@Preview @Composable fun AppSettingsPreview() = MaterialTheme {
  AppSettings()
}