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

package com.farmerbb.notepad.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.models.CrossRef
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class NotepadViewModel @Inject constructor(
  private val dao: NotepadDAO
): ViewModel() {
  suspend fun getNoteMetadata() = dao.getNoteMetadataSortedByTitle()
  suspend fun getNote(id: Long) = dao.getNote(id)

  fun save(id: Long, text: String, onSuccess: (Long) -> Unit) {
    viewModelScope.launch {
      try {
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
            insertCrossRef(CrossRef(
              metadataId = metadataId,
              contentsId = contentsId
            ))

            onSuccess.invoke(metadataId)
          } else
            onSuccess.invoke(id)
        }
      } catch (e: Exception) {
        // Something bad happened
      }
    }
  }
}