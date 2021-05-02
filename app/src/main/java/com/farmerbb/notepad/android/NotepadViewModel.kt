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

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.models.CrossRef
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class NotepadViewModel @Inject constructor(
  private val context: Application,
  private val repo: NotepadRepository
): AndroidViewModel(context) {
  suspend fun getNoteMetadata() = repo.getNoteMetadata()
  suspend fun getNote(id: Long) = repo.getNote(id)

  fun saveNote(
    id: Long,
    text: String,
    onSuccess: (Long) -> Unit
  ) = viewModelScope.launch {
    repo.saveNote(id, text, onSuccess)
  }

  fun deleteNote(
    id: Long,
    onSuccess: () -> Unit
  ) = viewModelScope.launch {
    repo.deleteNote(id, onSuccess)
  }

  fun shareNote(text: String) = try {
    context.startActivity(Intent().apply {
      action = Intent.ACTION_SEND
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, text)
    })
  } catch (e: Exception) {
    e.printStackTrace()
  }
}