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

package com.farmerbb.notepad.ui.menus

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.widgets.MoreButton

@Composable fun NoteListMenu(
  vm: NotepadViewModel?,
  showAboutDialog: MutableState<Boolean>,
  showSettingsDialog: MutableState<Boolean>? = null,
) {
  val showMenu = remember { mutableStateOf(false) }

  Box {
    MoreButton(showMenu)
    DropdownMenu(
      expanded = showMenu.value,
      onDismissRequest = { showMenu.value = false }
    ) {
      showSettingsDialog?.let {
        SettingsDialogMenuItem(showMenu, showSettingsDialog)
      } ?: SettingsMenuItem(showMenu)

      ImportMenuItem(showMenu, vm)
      AboutMenuItem(showMenu, showAboutDialog)
    }
  }
}

@Composable fun SettingsMenuItem(
  showMenu: MutableState<Boolean>
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
      // TODO navigate to app settings
    }
  ) {
    Text(text = stringResource(R.string.action_settings))
  }
}

@Composable fun SettingsDialogMenuItem(
  showMenu: MutableState<Boolean>,
  showSettingsDialog: MutableState<Boolean>?
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
      showSettingsDialog?.value = true
    }
  ) {
    Text(text = stringResource(R.string.action_settings))
  }
}

@Composable fun ImportMenuItem(
  showMenu: MutableState<Boolean>,
  vm: NotepadViewModel?
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
      vm?.importNotes()
    }
  ) {
    Text(text = stringResource(R.string.import_notes))
  }
}

@Composable fun AboutMenuItem(
  showMenu: MutableState<Boolean>,
  showAboutDialog: MutableState<Boolean>
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
      showAboutDialog.value = true
    }
  ) {
    Text(text = stringResource(R.string.dialog_about_title))
  }
}
