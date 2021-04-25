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

package com.farmerbb.notepad.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import kotlinx.coroutines.launch

@Composable fun NoteEdit(
  id: Long?,
  dao: NotepadDAO,
  navController: NavController
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
        value = dao.getNote(it)
      }
    }
  }

  NoteEdit(
    note = state.value,
    navController = navController
  )
}

@Composable fun NoteEdit(
  note: Note,
  navController: NavController
) {
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
        navigationIcon = {
          Box(
            modifier = Modifier
              .clickable {
                navController.popBackStack()
              }
          ) {
            Icon(
              imageVector = Icons.Filled.ArrowBack,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier
                .padding(12.dp)
            )
          }
        },
        title = {
          Text(
            text = note.metadata.title,
            color = Color.White
          )
        },
        backgroundColor = colorResource(id = R.color.primary)
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
fun NavGraphBuilder.NoteEditRoute(
  dao: NotepadDAO,
  navController: NavController
) = composable(
  route = "NoteEdit?id={id}",
  arguments = listOf(
    navArgument("id") { nullable = true }
  )
) {
  NoteEdit(
    id = it.arguments?.getString("id")?.toLong(),
    dao = dao,
    navController = navController
  )
}

@Preview @Composable fun NoteEditPreview() = MaterialTheme {
  NoteEdit(
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