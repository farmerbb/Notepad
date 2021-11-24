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

package com.farmerbb.notepad.ui.routes

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.NoteListContent
import com.farmerbb.notepad.ui.menus.NoteListMenu
import com.farmerbb.notepad.ui.widgets.AboutDialog
import com.farmerbb.notepad.ui.widgets.AppBarText
import com.farmerbb.notepad.ui.widgets.SettingsDialog
import kotlinx.coroutines.launch

@Composable fun NoteList(
  navController: NavController,
  vm: NotepadViewModel = hiltViewModel()
) {
  val state = produceState(listOf<NoteMetadata>()) {
    launch {
      value = vm.getNoteMetadata()
    }
  }

  NoteList(
    notes = state.value,
    navController = navController,
    vm = vm
  )
}

@Composable fun NoteList(
  notes: List<NoteMetadata>,
  navController: NavController? = null,
  vm: NotepadViewModel? = null
) {
  val showAboutDialog = remember { mutableStateOf(false) }
  AboutDialog(showAboutDialog, vm)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { AppBarText(stringResource(id = R.string.app_name)) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = {
          NoteListMenu(
            navController = navController,
            vm = vm,
            showAboutDialog = showAboutDialog
          )
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { navController?.newNote() },
        backgroundColor = colorResource(id = R.color.primary),
        content = {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = Color.White
          )
        }
      )
    },
    content = {
      NoteListContent(notes, navController)
    }
  )
}

@Suppress("FunctionName")
fun NavGraphBuilder.NoteListRoute(
  navController: NavController
) = composable(route = "NoteList") {
  NoteList(
    navController = navController
  )
}

@Preview @Composable fun NoteListPreview() = MaterialTheme {
  NoteList(
    notes = listOf(
      NoteMetadata(title = "Test Note 1"),
      NoteMetadata(title = "Test Note 2")
    )
  )
}

@Preview @Composable fun NoteListEmptyPreview() = MaterialTheme {
  NoteList(
    notes = emptyList()
  )
}