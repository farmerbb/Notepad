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

package com.farmerbb.notepad.ui.previews

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.EditNoteContent
import com.farmerbb.notepad.ui.menus.NoteViewEditMenu
import com.farmerbb.notepad.ui.widgets.*

@Deprecated("For preview purposes only")
@Composable fun EditNote(
  note: Note,
  textState: MutableState<TextFieldValue>,
  vm: NotepadViewModel? = null
) {
  val id = note.metadata.metadataId

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = { BackButton() },
        title = { AppBarText(note.metadata.title) },
        backgroundColor = colorResource(id = R.color.primary),
        actions = {
          SaveButton(id, textState.value.text, vm)
          DeleteButton(id, vm)
          NoteViewEditMenu(textState.value.text, vm)
        }
      )
    },
    content = {
      EditNoteContent(textState)
    }
  )
}

@Suppress("deprecation")
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
    textState = remember { mutableStateOf(TextFieldValue()) }
  )
}