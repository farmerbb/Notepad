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

package com.farmerbb.notepad.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver

private const val VIEW = "View"
private const val EDIT = "Edit"

sealed interface NavState {
    object Empty: NavState
    data class View(val id: Long): NavState
    data class Edit(val id: Long? = null): NavState
}

val navStateSaver = Saver<MutableState<NavState>, Pair<String, Long?>>(
    save = {
        when(val state = it.value) {
            is NavState.View -> VIEW to state.id
            is NavState.Edit -> EDIT to state.id
            else -> "" to null
        }
    },
    restore = {
        mutableStateOf(
            when(it.first) {
                VIEW -> NavState.View(it.second ?: 0)
                EDIT -> NavState.Edit(it.second)
                else -> NavState.Empty
            }
        )
    }
)
