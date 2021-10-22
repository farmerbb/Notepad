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

package com.farmerbb.notepad.ui.content

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerbb.notepad.utils.UnitDisposableEffect

@Composable fun EditNoteContent(
  textState: MutableState<TextFieldValue>
) {
  val focusRequester = remember { FocusRequester() }
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
      .focusRequester(focusRequester)
  )

  UnitDisposableEffect {
    focusRequester.requestFocus()
  }
}