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

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.routes.RightPaneState
import com.farmerbb.notepad.ui.routes.RightPaneState.Edit
import com.farmerbb.notepad.ui.routes.RightPaneState.Empty
import com.farmerbb.notepad.ui.routes.RightPaneState.View
import com.farmerbb.notepad.ui.routes.editNote
import com.farmerbb.notepad.ui.routes.viewNote

@Composable fun BackButton(navController: NavController?) {
  IconButton(
    onClick = { navController?.popBackStack() }
  ) {
    Icon(
      imageVector = Icons.Filled.ArrowBack,
      contentDescription = null,
      tint = Color.White
    )
  }
}

@Composable fun EditButton(
  id: Long,
  navController: NavController?,
  rightPaneState: MutableState<RightPaneState>? = null
) {
  IconButton(
    onClick = {
      rightPaneState?.let {
        it.value = Edit(id)
      } ?: navController?.apply {
        popBackStack()
        editNote(id)
      }
    }
  ) {
    Icon(
      imageVector = Icons.Filled.Edit,
      contentDescription = stringResource(R.string.action_edit),
      tint = Color.White
    )
  }
}

@Composable fun SaveButton(
  id: Long,
  text: String,
  navController: NavController?,
  vm: NotepadViewModel?,
  rightPaneState: MutableState<RightPaneState>? = null
) {
  IconButton(
    onClick = {
      vm?.saveNote(id, text) {
        rightPaneState?.let {
          it.value = View(id)
        } ?: navController?.apply {
          popBackStack()
          viewNote(id)
        }
      }
    }
  ) {
    Icon(
      imageVector = Icons.Filled.Save,
      contentDescription = stringResource(R.string.action_save),
      tint = Color.White
    )
  }
}

@Composable fun DeleteButton(
  id: Long,
  navController: NavController?,
  vm: NotepadViewModel?,
  rightPaneState: MutableState<RightPaneState>? = null
) {
  val dialogIsOpen = remember { mutableStateOf(false) }

  IconButton(onClick = { dialogIsOpen.value = true }) {
    Icon(
      imageVector = Icons.Filled.Delete,
      contentDescription = stringResource(R.string.action_delete),
      tint = Color.White
    )
  }

  if(dialogIsOpen.value) {
    DeleteAlertDialog(
      onConfirm = {
        dialogIsOpen.value = false
        vm?.deleteNote(id) {
          rightPaneState?.let {
            it.value = Empty
          } ?: navController?.popBackStack()
        }
      },
      onDismiss = {
        dialogIsOpen.value = false
      }
    )
  }
}

@Composable fun MoreButton(
  showMenu: MutableState<Boolean>
) {
  IconButton(
    onClick = { showMenu.value = true }
  ) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      contentDescription = null,
      tint = Color.White
    )
  }
}