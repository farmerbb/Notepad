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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.models.NoteMetadata
import kotlinx.coroutines.launch

@Composable fun NoteList(
  dao: NotepadDAO,
  navController: NavController
) {
  val state = produceState(listOf<NoteMetadata>()) {
    launch {
      value = dao.getNoteMetadataSortedByTitle()
    }
  }

  NoteList(
    notes = state.value,
    navController = navController
  )
}

@Composable fun NoteList(
  notes: List<NoteMetadata>,
  navController: NavController,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(id = R.string.app_name),
            color = Color.White
          )
        },
        backgroundColor = colorResource(id = R.color.primary)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          // TODO create new note
        },
        backgroundColor = colorResource(id = R.color.primary),
        content = {
          Icon(
            painter = painterResource(id = R.drawable.ic_action_new),
            contentDescription = null,
            tint = Color.White
          )
        }
      )
    },
    content = {
      LazyColumn {
        items(notes.size) {
          Column(modifier = Modifier
            .clickable {
              val id = notes[it].metadataId
              navController.navigate("NoteView/$id")
            }
          ) {
            Text(
              text = notes[it].title,
              modifier = Modifier
                .padding(
                  horizontal = 16.dp,
                  vertical = 12.dp
                )
            )

            Divider()
          }
        }
      }
    })
}

@Suppress("FunctionName")
fun NavGraphBuilder.NoteListRoute(
  dao: NotepadDAO,
  navController: NavController
) = composable(route = "NoteList") {
  NoteList(
    dao = dao,
    navController = navController
  )
}

@Preview @Composable fun NoteListPreview() = MaterialTheme {
  NoteList(
    listOf(
      NoteMetadata(title = "Test Note 1"),
      NoteMetadata(title = "Test Note 2")
    ),
    rememberNavController()
  )
}