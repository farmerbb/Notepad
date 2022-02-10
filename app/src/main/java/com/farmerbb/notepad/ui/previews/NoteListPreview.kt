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

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.routes.NotepadComposeApp

@Preview(device = Devices.PIXEL_C)
@Composable
fun MultiPanePreview() = MaterialTheme {
    NotepadComposeApp(
        notes = listOf(
            NoteMetadata(title = "Test Note 1"),
            NoteMetadata(title = "Test Note 2")
        ),
        isMultiPane = true
    )
}

@Preview(device = Devices.PIXEL_C)
@Composable
fun MultiPaneEmptyPreview() = MaterialTheme {
    NotepadComposeApp(
        notes = emptyList(),
        isMultiPane = true
    )
}

@Preview
@Composable
fun NoteListPreview() = MaterialTheme {
    NotepadComposeApp(
        notes = listOf(
            NoteMetadata(title = "Test Note 1"),
            NoteMetadata(title = "Test Note 2")
        )
    )
}

@Preview
@Composable
fun NoteListEmptyPreview() = MaterialTheme {
    NotepadComposeApp(notes = emptyList())
}
