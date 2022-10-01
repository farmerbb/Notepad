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

import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.R
import java.util.Calendar
import java.util.TimeZone

private val buildYear: Int get() {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Denver")).apply {
        timeInMillis = BuildConfig.TIMESTAMP
    }

    return calendar.get(Calendar.YEAR)
}

@Composable
fun DeleteDialog(
    isMultiple: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (isMultiple) {
        R.string.dialog_delete_button_title_plural
    } else {
        R.string.dialog_delete_button_title
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(id = title) },
        text = { DialogText(id = R.string.dialog_are_you_sure) },
        confirmButton = {
            DialogButton(
                onClick = onConfirm,
                id = R.string.action_delete
            )
        },
        dismissButton = {
            DialogButton(
                onClick = onDismiss,
                id = R.string.action_cancel
            )
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    checkForUpdates: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(id = R.string.dialog_about_title) },
        text = { DialogText(id = R.string.dialog_about_message, buildYear) },
        confirmButton = {
            DialogButton(
                onClick = onDismiss, // dismissing the dialog is the primary action
                id = R.string.action_close
            )
        },
        dismissButton = {
            DialogButton(
                onClick = checkForUpdates,
                id = R.string.check_for_updates
            )
        }
    )
}

@Composable
fun SaveDialog(
    onConfirm: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(id = R.string.dialog_save_button_title) },
        text = { DialogText(id = R.string.dialog_save_changes) },
        confirmButton = {
            DialogButton(
                onClick = onConfirm,
                id = R.string.action_save
            )
        },
        dismissButton = {
            DialogButton(
                onClick = onDiscard,
                id = R.string.action_discard
            )
        }
    )
}

@Composable
fun FirstRunDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(id = R.string.app_name) },
        text = { DialogText(id = R.string.first_run) },
        confirmButton = {
            DialogButton(
                onClick = onDismiss,
                id = R.string.action_close
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

@Composable
fun FirstViewDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(id = R.string.app_name) },
        text = { DialogText(id = R.string.first_view) },
        confirmButton = {
            DialogButton(
                onClick = onDismiss,
                id = R.string.action_close
            )
        }
    )
}