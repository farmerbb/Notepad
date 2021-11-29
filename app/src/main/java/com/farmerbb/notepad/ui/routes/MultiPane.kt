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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.*
import com.farmerbb.notepad.ui.menus.NoteListMenu
import com.farmerbb.notepad.ui.menus.NoteViewEditMenu
import com.farmerbb.notepad.ui.routes.RightPaneState.Edit
import com.farmerbb.notepad.ui.routes.RightPaneState.Empty
import com.farmerbb.notepad.ui.routes.RightPaneState.View
import com.farmerbb.notepad.ui.widgets.*

sealed interface RightPaneState {
  object Empty: RightPaneState
  data class View(val id: Long): RightPaneState
  data class Edit(val id: Long? = null): RightPaneState
}

@Composable fun MultiPane(
  vm: NotepadViewModel,
  isMultiPane: Boolean
) {
  val state = vm.noteMetadata.collectAsState(emptyList())

  MultiPane(
    notes = state.value,
    vm = vm,
    isMultiPane = isMultiPane
  )
}

@Composable fun MultiPane(
  notes: List<NoteMetadata> = emptyList(),
  vm: NotepadViewModel? = null,
  isMultiPane: Boolean = false,
  initState: RightPaneState = Empty
) {
  val rightPaneState = remember { mutableStateOf(initState) }

  val showAboutDialog = remember { mutableStateOf(false) }
  AboutDialog(showAboutDialog, vm)

  val showSettingsDialog = remember { mutableStateOf(false) }
  SettingsDialog(showSettingsDialog)

  val title: String
  val actions: @Composable RowScope.() -> Unit
  val content: @Composable BoxScope.() -> Unit

  when(val state = rightPaneState.value) {
    Empty -> {
      title = stringResource(id = R.string.app_name)
      actions = {
        NoteListMenu(
          vm = vm,
          showAboutDialog = showAboutDialog,
          showSettingsDialog = showSettingsDialog,
        )
      }
      content = {
        if(isMultiPane) {
          EmptyDetails()
        } else {
          NoteListContent(notes, rightPaneState)
        }
      }
    }

    is View -> {
      val viewState = viewState(state.id, vm)

      title = viewState.value.metadata.title
      actions = {
        EditButton(state.id, rightPaneState)
        DeleteButton(state.id, vm, rightPaneState)
        NoteViewEditMenu(viewState.value.contents.text, vm)
      }
      content = { ViewNoteContent(viewState.value) }
    }

    is Edit -> {
      val editState = editState(state.id, vm)
      val textState = textState(editState.value.contents.text)
      val id = editState.value.metadata.metadataId

      title = editState.value.metadata.title
      actions = {
        SaveButton(id, textState.value.text, vm, rightPaneState)
        DeleteButton(id, vm, rightPaneState)
        NoteViewEditMenu(textState.value.text, vm)
      }
      content = { EditNoteContent(textState) }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { AppBarText(title) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = actions
      )
    },
    floatingActionButton = {
      if(rightPaneState.value == Empty) {
        FloatingActionButton(
          onClick = { rightPaneState.value = Edit() },
          backgroundColor = colorResource(id = R.color.primary),
          content = {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              tint = Color.White
            )
          }
        )
      }
    },
    content = {
      if(isMultiPane) {
        Row {
          Box(modifier = Modifier.weight(1f)) {
            NoteListContent(notes, rightPaneState)
          }

          Divider(
            modifier = Modifier
              .fillMaxHeight()
              .width(1.dp)
          )

          Box(
            modifier = Modifier.weight(2f),
            content = content
          )
        }
      } else {
        Box(content = content)
      }
    }
  )
}

@Composable fun EmptyDetails() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(
      painter = painterResource(id = R.drawable.notepad_logo),
      contentDescription = null,
      modifier = Modifier
        .height(512.dp)
        .width(512.dp)
        .alpha(0.5f)
    )
  }
}

@Preview(device = Devices.PIXEL_C)
@Composable fun MultiPanePreview() = MaterialTheme {
  MultiPane(
    notes = listOf(
      NoteMetadata(title = "Test Note 1"),
      NoteMetadata(title = "Test Note 2")
    ),
    isMultiPane = true
  )
}

@Preview(device = Devices.PIXEL_C)
@Composable fun MultiPaneEmptyPreview() = MaterialTheme {
  MultiPane(isMultiPane = true)
}

@Preview @Composable fun NoteListPreview() = MaterialTheme {
  MultiPane(
    notes = listOf(
      NoteMetadata(title = "Test Note 1"),
      NoteMetadata(title = "Test Note 2")
    )
  )
}

@Preview @Composable fun NoteListEmptyPreview() = MaterialTheme {
  MultiPane()
}
