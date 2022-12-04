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

@file:OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)

package com.farmerbb.notepad.ui.routes

import androidx.annotation.ArrayRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.Prefs
import com.farmerbb.notepad.viewmodel.NotepadViewModel
import de.schnettler.datastore.compose.material.PreferenceScreen
import de.schnettler.datastore.compose.material.model.Preference.PreferenceItem.ListPreference
import de.schnettler.datastore.compose.material.model.Preference.PreferenceItem.SwitchPreference
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            NotepadPreferenceScreen()
        }
    }
}

@Composable
fun NotepadPreferenceScreen(
    vm: NotepadViewModel = getViewModel()
) {
    val markdown by vm.prefs.markdown.collectAsState()
    val directEdit by vm.prefs.directEdit.collectAsState()

    PreferenceScreen(
        items = listOf(
            ListPreference(
                request = Prefs.ColorScheme,
                title = stringResource(id = R.string.action_theme),
                singleLineTitle = false,
                entries = listPrefEntries(
                    keyRes = R.array.theme_list_values,
                    valueRes = R.array.theme_list
                ),
            ),
            ListPreference(
                request = Prefs.FontType,
                title = stringResource(id = R.string.pref_title_font_type),
                singleLineTitle = false,
                entries = listPrefEntries(
                    keyRes = R.array.font_type_list_values,
                    valueRes = R.array.font_type_list
                ),
            ),
            ListPreference(
                request = Prefs.FontSize,
                title = stringResource(id = R.string.action_font_size),
                singleLineTitle = false,
                entries = listPrefEntries(
                    keyRes = R.array.font_size_list_values,
                    valueRes = R.array.font_size_list
                ),
            ),
            ListPreference(
                request = Prefs.SortBy,
                title = stringResource(id = R.string.action_sort_by),
                singleLineTitle = false,
                entries = listPrefEntries(
                    keyRes = R.array.sort_by_list_values,
                    valueRes = R.array.sort_by_list
                ),
            ),
            ListPreference(
                request = Prefs.ExportFilename,
                title = stringResource(id = R.string.action_export_filename),
                singleLineTitle = false,
                entries = listPrefEntries(
                    keyRes = R.array.exported_filename_list_values,
                    valueRes = R.array.exported_filename_list
                ),
            ),
            SwitchPreference(
                request = Prefs.ShowDialogs,
                title = stringResource(id = R.string.pref_title_show_dialogs),
                singleLineTitle = false
            ),
            SwitchPreference(
                request = Prefs.ShowDate,
                title = stringResource(id = R.string.pref_title_show_date),
                singleLineTitle = false
            ),
            SwitchPreference(
                request = Prefs.DirectEdit,
                title = stringResource(id = R.string.pref_title_direct_edit),
                singleLineTitle = false,
                enabled = !markdown
            ),
            SwitchPreference(
                request = Prefs.Markdown,
                title = stringResource(id = R.string.pref_title_markdown),
                singleLineTitle = false,
                enabled = !directEdit
            ),
            SwitchPreference(
                request = Prefs.RtlSupport,
                title = stringResource(id = R.string.rtl_layout),
                singleLineTitle = false
            )
        ),
        contentPadding = PaddingValues(8.dp),
        dataStoreManager = vm.dataStoreManager
    )
}

@ReadOnlyComposable
@Composable
private fun listPrefEntries(
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