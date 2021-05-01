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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.screens.editNote
import com.farmerbb.notepad.ui.screens.viewNote

@Composable fun BackButton(navController: NavController) {
  IconButton(
    onClick = { navController.popBackStack() }
  ) {
    Icon(
      imageVector = Icons.Filled.ArrowBack,
      contentDescription = null,
      tint = Color.White
    )
  }
}

@Composable fun EditButton(
  navController: NavController,
  id: Long
) {
  IconButton(
    onClick = {
      with(navController) {
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
  navController: NavController,
  id: Long,
  text: String,
  vm: NotepadViewModel = hiltNavGraphViewModel()
) {
  IconButton(
    onClick = {
      vm.save(id, text) {
        with(navController) {
          popBackStack()
          viewNote(it)
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
  navController: NavController,
  id: Long,
  vm: NotepadViewModel = hiltNavGraphViewModel()
) {
  IconButton(
    onClick = {
      vm.delete(id) {
        navController.popBackStack()
      }
    }
  ) {
    Icon(
      imageVector = Icons.Filled.Delete,
      contentDescription = stringResource(R.string.action_delete),
      tint = Color.White
    )
  }
}

@Composable fun ShareButton(
  navController: NavController,
  text: String
) {
  IconButton(
    onClick = { /* TODO */ }
  ) {
    Icon(
      imageVector = Icons.Filled.Share,
      contentDescription = stringResource(R.string.action_share),
      tint = Color.White
    )
  }
}