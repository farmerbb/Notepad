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

package com.farmerbb.notepad.ui.widgets

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.routes.NotepadPreferenceScreen
import com.farmerbb.notepad.utils.buildYear

@Composable
fun DeleteAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.dialog_delete_button_title)) },
        text = { Text(text = stringResource(id = R.string.dialog_are_you_sure)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.action_delete).uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel).uppercase())
            }
        }
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            NotepadPreferenceScreen()
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    checkForUpdates: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.dialog_about_title)) },
        text = { Text(text = stringResource(id = R.string.dialog_about_message, buildYear)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { // dismissing the dialog is the primary action
                Text(text = stringResource(id = R.string.action_close).uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = checkForUpdates) {
                Text(text = stringResource(id = R.string.check_for_updates).uppercase())
            }
        }
    )
}