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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.widgets.*
import kotlinx.coroutines.launch

@Composable fun EditNote(
  id: Long?,
  navController: NavController,
  vm: NotepadViewModel = hiltNavGraphViewModel()
) {
  val state = produceState(
    Note(
      metadata = NoteMetadata(
        title = stringResource(id = R.string.action_new)
      )
    )
  ) {
    id?.let {
      launch {
        value = vm.getNote(it)
      }
    }
  }

  EditNote(
    note = state.value,
    navController = navController,
    vm = vm
  )
}

@Composable fun EditNote(
  note: Note,
  navController: NavController,
  vm: NotepadViewModel? = null
) {
  val id = note.metadata.metadataId
  val textState = remember {
    mutableStateOf(TextFieldValue())
  }.apply {
    value = TextFieldValue(
      text = note.contents.text
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = { BackButton(navController) },
        title = { AppBarText(note.metadata.title) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = {
          SaveButton(navController, id, textState.value.text, vm)
          DeleteButton(navController, id, vm)
          ShareButton(navController, textState.value.text)
        }
      )
    },
    content = {
      BasicTextField(
        value = textState.value,
        onValueChange = { textState.value = it },
        textStyle = TextStyle(
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
fun NavGraphBuilder.EditNoteRoute(
  navController: NavController
) = composable(
  route = "EditNote?id={id}",
  arguments = listOf(
    navArgument("id") { nullable = true }
  )
) {
  EditNote(
    id = it.arguments?.getString("id")?.toLong(),
    navController = navController
  )
}

fun NavController.newNote() = navigate("EditNote")
fun NavController.editNote(id: Long) = navigate("EditNote?id=$id")

@Preview @Composable fun EditNotePreview() = MaterialTheme {
  EditNote(
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