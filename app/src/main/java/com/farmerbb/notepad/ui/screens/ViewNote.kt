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

package com.farmerbb.notepad.ui.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.widgets.*
import kotlinx.coroutines.launch

@Composable fun ViewNote(
  id: Long,
  dao: NotepadDAO,
  navController: NavController
) {
  val state = produceState(Note()) {
    launch {
      value = dao.getNote(id)
    }
  }

  ViewNote(
    note = state.value,
    navController = navController
  )
}

@Composable fun ViewNote(
  note: Note,
  navController: NavController
) {
  val id = note.metadata.metadataId

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = { BackButton(navController) },
        title = { AppBarText(note.metadata.title) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = {
          EditButton(navController, id)
          DeleteButton(navController, id)
          ShareButton(navController, note.contents.text)
        }
      )
    },
    content = {
      BasicText(
        text = note.contents.text,
        style = TextStyle(
          fontSize = 16.sp
        ),
        modifier = Modifier
          .padding(
            horizontal = 16.dp,
            vertical = 12.dp
          )
          .fillMaxWidth()
          .fillMaxHeight()
      )
    })
}

@Suppress("FunctionName")
fun NavGraphBuilder.ViewNoteRoute(
  dao: NotepadDAO,
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
      dao = dao,
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
    ),
    navController = rememberNavController()
  )
}