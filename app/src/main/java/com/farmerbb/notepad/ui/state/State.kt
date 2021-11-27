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

package com.farmerbb.notepad.ui.state

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteMetadata
import kotlinx.coroutines.launch

@Composable fun viewState(
  id: Long,
  vm: NotepadViewModel?
) = produceState(Note()) {
  launch {
    vm?.getNote(id)?.let { value = it }
  }
}

@Composable fun editState(
  id: Long?,
  vm: NotepadViewModel?
) = produceState(
  Note(
    metadata = NoteMetadata(
      title = stringResource(id = R.string.action_new)
    )
  )
) {
  id?.let {
    launch {
      vm?.getNote(it)?.let { value = it }
    }
  }
}

@Composable fun textState(
  editState: State<Note>
) = remember {
  mutableStateOf(TextFieldValue())
}.apply {
  val text = editState.value.contents.text
  value = TextFieldValue(
    text = text,
    selection = TextRange(text.length)
  )
}