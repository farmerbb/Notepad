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

package com.farmerbb.notepad.data

import com.farmerbb.notepad.models.CrossRef
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import javax.inject.Inject

class NotepadRepository @Inject constructor(
  private val dao: NotepadDAO
) {
  fun noteMetadataFlow() = dao.getNoteMetadataSortedByTitle()
  suspend fun getNote(id: Long) = dao.getNote(id)

  suspend fun saveNote(id: Long, text: String, onSuccess: (Long) -> Unit) = try {
    val crossRef = dao.getCrossRef(id) ?: CrossRef()

    val metadata = NoteMetadata(
      metadataId = crossRef.metadataId,
      title = text.substringBefore("\n")
    )

    val contents = NoteContents(
      contentsId = crossRef.contentsId,
      text = text
    )

    with(dao) {
      val metadataId = insertNoteMetadata(metadata)
      val contentsId = insertNoteContents(contents)

      if(id == 0L) {
        insertCrossRef(
          CrossRef(
            metadataId = metadataId,
            contentsId = contentsId
          )
        )

        onSuccess(metadataId)
      } else
        onSuccess(id)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }

  suspend fun deleteNote(id: Long, onSuccess: () -> Unit) = try {
    with(dao) {
      getCrossRef(id)?.let {
        deleteNoteMetadata(it.metadataId)
        deleteNoteContents(it.contentsId)
        deleteCrossRef(id)
      }
    }

    onSuccess()
  } catch (e: Exception) {
    e.printStackTrace()
  }
}