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
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.ViewNoteContent
import com.farmerbb.notepad.ui.widgets.NoteViewEditMenu
import com.farmerbb.notepad.ui.widgets.*
import java.util.Date

@Composable
private fun ViewNote(note: Note) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { AppBarText(note.metadata.title) },
                backgroundColor = colorResource(id = R.color.primary),
                actions = {
                    EditButton()
                    DeleteButton()
                    NoteViewEditMenu()
                }
            )
        },
        content = { ViewNoteContent(note.contents.text) }
    )
}

@Preview
@Composable
fun ViewNotePreview() = MaterialTheme {
    ViewNote(
        note = Note(
            metadata = NoteMetadata(
                metadataId = -1,
                title = "Title",
                date = Date()
            ),
            contents = NoteContents(
                contentsId = -1,
                text = "This is some text",
                isDraft = false
            )
        ),
    )
}