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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.*
import com.farmerbb.notepad.ui.menus.NoteListMenu
import com.farmerbb.notepad.ui.menus.NoteViewEditMenu
import com.farmerbb.notepad.models.RightPaneState
import com.farmerbb.notepad.models.RightPaneState.Companion.EDIT
import com.farmerbb.notepad.models.RightPaneState.Companion.VIEW
import com.farmerbb.notepad.models.RightPaneState.Edit
import com.farmerbb.notepad.models.RightPaneState.Empty
import com.farmerbb.notepad.models.RightPaneState.View
import com.farmerbb.notepad.ui.widgets.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable fun NotepadComposeApp() {
  val vm = viewModel<NotepadViewModel>()
  val systemUiController = rememberSystemUiController()
  val configuration = LocalConfiguration.current
  val notes = vm.noteMetadata.collectAsState(emptyList())

  MaterialTheme {
    NotepadComposeApp(
      notes = notes.value,
      vm = vm,
      isMultiPane = configuration.screenWidthDp >= 600
    )
  }

  SideEffect {
    systemUiController.setNavigationBarColor(
      color = Color.White
    )
  }
}

@Composable fun NotepadComposeApp(
  notes: List<NoteMetadata>,
  vm: NotepadViewModel? = null,
  isMultiPane: Boolean = false,
  initState: RightPaneState = Empty
) {
  val rightPaneState = rememberSaveable(
    saver = Saver(
      save = {
        when(val state = it.value) {
          is View -> VIEW to state.id
          is Edit -> EDIT to state.id
          else -> "" to null
        }
      },
      restore = {
        mutableStateOf(
          when(it.first) {
            VIEW -> View(it.second ?: 0)
            EDIT -> Edit(it.second)
            else -> Empty
          }
        )
      }
    )
  ) { mutableStateOf(initState) }

  val showAboutDialog = remember { mutableStateOf(false) }
  AboutDialog(showAboutDialog, vm)

  val showSettingsDialog = remember { mutableStateOf(false) }
  SettingsDialog(showSettingsDialog)

  val title: String
  val backButton: @Composable (() -> Unit)?
  val actions: @Composable RowScope.() -> Unit
  val content: @Composable BoxScope.() -> Unit

  when(val state = rightPaneState.value) {
    Empty -> {
      title = stringResource(id = R.string.app_name)
      backButton = null
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
      backButton = { BackButton(rightPaneState) }
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
      backButton = { BackButton(rightPaneState) }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = backButton,
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
