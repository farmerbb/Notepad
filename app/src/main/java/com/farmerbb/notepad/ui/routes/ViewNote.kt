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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.ViewNoteContent
import com.farmerbb.notepad.ui.menus.NoteViewEditMenu
import com.farmerbb.notepad.ui.widgets.*
import kotlinx.coroutines.launch

@Composable fun ViewNote(
  id: Long,
  navController: NavController? = null,
  vm: NotepadViewModel = hiltViewModel(),
  isMultiPane: Boolean = false
) {
  val state = produceState(Note()) {
    launch {
      value = vm.getNote(id)
    }
  }

  if(isMultiPane) {
    ViewNoteContent(state.value)
  } else {
    ViewNote(
      note = state.value,
      navController = navController,
      vm = vm
    )
  }
}

@Composable fun ViewNote(
  note: Note,
  navController: NavController? = null,
  vm: NotepadViewModel? = null
) {
  val id = note.metadata.metadataId

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = { BackButton(navController) },
        title = { AppBarText(note.metadata.title) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = {
          EditButton(id, navController)
          DeleteButton(id, navController, vm)
          NoteViewEditMenu(note.contents.text, vm)
        }
      )
    },
    content = {
      ViewNoteContent(note)
    }
  )
}

@Suppress("FunctionName")
fun NavGraphBuilder.ViewNoteRoute(
  navController: NavController
) = composable(
  route = "ViewNote/{id}",
  arguments = listOf(
    navArgument("id") { NavType.StringType }
  )
) {
  it.arguments?.getString("id")?.let { id ->
    ViewNote(
      id = id.toLong(),
      navController = navController
    )
  }
}

fun NavController.viewNote(id: Long) = navigate("ViewNote/$id")

@Preview @Composable fun ViewNotePreview() = MaterialTheme {
  ViewNote(
    note = Note(
      metadata = NoteMetadata(
        title = "Title"
      ),
      contents = NoteContents(
        text = "This is some text"
      )
    )
  )
}