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

package com.farmerbb.notepad.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.farmerbb.notepad.R

@Composable
fun NoteListMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onMoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onImportClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Box {
        MoreButton(onMoreClick)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss
        ) {
            MenuItem(R.string.action_settings, onSettingsClick)
            MenuItem(R.string.import_notes, onImportClick)
            MenuItem(R.string.dialog_about_title, onAboutClick)
        }
    }
}

@Composable
fun NoteViewEditMenu(
    showMenu: Boolean = false,
    onDismiss: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onPrintClick: () -> Unit = {}
) {
    Box {
        MoreButton(onMoreClick)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss
        ) {
            MenuItem(R.string.action_share, onShareClick)
            MenuItem(R.string.action_export, onExportClick)
            MenuItem(R.string.action_print, onPrintClick)
        }
    }
}

@Composable
fun StandaloneEditorMenu(
    showMenu: Boolean = false,
    onDismiss: () -> Unit,
    onMoreClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Box {
        MoreButton(onMoreClick)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss
        ) {
            MenuItem(R.string.action_share, onShareClick)
        }
    }
}

@Composable
fun MenuItem(
    @StringRes stringRes: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Text(text = stringResource(id = stringRes))
    }
}