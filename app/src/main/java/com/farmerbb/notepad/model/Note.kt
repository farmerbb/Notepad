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

import java.util.Date

data class Note(
    val metadata: NoteMetadata = NoteMetadata(
        metadataId = -1,
        title = "",
        date = Date(),
        hasDraft = false
    ),
    private val contents: NoteContents = NoteContents(
        contentsId = -1,
        text = null,
        draftText = null
    )
) {
    val id: Long get() = metadata.metadataId
    val text: String get() = contents.text ?: ""
    val draftText: String get() = contents.draftText ?: ""
    val title: String get() = metadata.title
}