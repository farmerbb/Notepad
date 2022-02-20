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

package com.farmerbb.notepad.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.farmerbb.notepad.android.NotepadViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

sealed interface NavState {
    object Empty: NavState
    data class View(val id: Long): NavState
    data class Edit(val id: Long? = null): NavState

    companion object {
        const val VIEW = "View"
        const val EDIT = "Edit"
    }
}

@Composable
fun noteState(
    id: Long?,
    vm: NotepadViewModel = getViewModel()
) = produceState(Note()) {
    id?.let {
        launch {
            value = vm.getNote(it)
        }
    }
}

@Composable
fun textFieldState(text: String) = remember {
    mutableStateOf(TextFieldValue())
}.apply {
    value = TextFieldValue(
        text = text,
        selection = TextRange(text.length)
    )
}
