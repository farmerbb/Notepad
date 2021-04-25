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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import kotlinx.coroutines.launch

@Composable fun NoteView(
  id: Long,
  dao: NotepadDAO,
  navController: NavController
) {
  val state = produceState(Note()) {
    launch {
      value = dao.getNote(id)
    }
  }

  NoteView(
    note = state.value,
    navController = navController
  )
}

@Composable fun NoteView(
  note: Note,
  navController: NavController
) {
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
      Text(
        text = note.contents.text,
        modifier = Modifier
          .padding(
            horizontal = 16.dp,
            vertical = 12.dp
          )
      )
    })
}

@Suppress("FunctionName")
fun NavGraphBuilder.NoteViewRoute(
  dao: NotepadDAO,
  navController: NavController
) = composable(
  route = "NoteView/{id}",
  arguments = listOf(
    navArgument("id") { NavType.StringType }
  )
) {
  it.arguments?.getString("id")?.let { id ->
    NoteView(
      id = id.toLong(),
      dao = dao,
      navController = navController
    )
  }
}

@Preview @Composable fun NoteViewPreview() = MaterialTheme {
  NoteView(
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