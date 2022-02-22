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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.utils.showToast
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.Note
import com.farmerbb.notepad.utils.ReleaseType.PlayStore
import com.farmerbb.notepad.utils.ReleaseType.Amazon
import com.farmerbb.notepad.utils.ReleaseType.FDroid
import com.farmerbb.notepad.utils.ReleaseType.Unknown
import com.farmerbb.notepad.utils.isPlayStoreInstalled
import com.farmerbb.notepad.utils.releaseType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotepadViewModel(
    private val context: Application,
    private val repo: NotepadRepository
): ViewModel() {
    private val _noteState = MutableStateFlow(Note())
    val noteState: StateFlow<Note> = _noteState
    val noteMetadata get() = repo.noteMetadataFlow()

    fun getNote(id: Long?) = id?.let {
        _noteState.value = repo.getNote(it)
    } ?: run {
        clearNote()
    }

    fun clearNote() {
        _noteState.value = Note()
    }

    fun saveNote(
        id: Long,
        text: String,
        onSuccess: (Long) -> Unit
    ) = viewModelScope.launch {
        text.checkLength {
            repo.saveNote(id, text, onSuccess)
        }
    }

    fun deleteNote(
        id: Long,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        repo.deleteNote(id, onSuccess)
    }

    fun shareNote(text: String) = viewModelScope.launch {
        text.checkLength {
            showShareSheet(text)
        }
    }

    fun checkForUpdates() = with(context) {
        val url = when(releaseType) {
            PlayStore -> {
                if(isPlayStoreInstalled)
                    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                else
                    "https://github.com/farmerbb/Notepad/releases"
            }
            Amazon -> "https://www.amazon.com/gp/mas/dl/android?p=${BuildConfig.APPLICATION_ID}"
            FDroid -> "https://f-droid.org/repository/browse/?fdid=${BuildConfig.APPLICATION_ID}"
            Unknown -> ""
        }

        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (ignored: ActivityNotFoundException) {}
    }

    private fun showShareSheet(text: String) = with(context) {
        try {
            startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    },
                    getString(R.string.send_to)
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun String.checkLength(
        onSuccess: suspend () -> Unit
    ) = when(length) {
        0 -> context.showToast(R.string.empty_note)
        else -> onSuccess()
    }

    // TODO implement the following functions:
    fun importNotes() {}
    fun exportNote(text: String) {}
    fun printNote(text: String) {}
}