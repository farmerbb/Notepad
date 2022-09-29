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

@file:OptIn(FlowPreview::class)

package com.farmerbb.notepad.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.data.PreferenceManager.Companion.prefs
import com.farmerbb.notepad.model.FilenameFormat
import com.farmerbb.notepad.model.Note
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.model.ReleaseType.Amazon
import com.farmerbb.notepad.model.ReleaseType.FDroid
import com.farmerbb.notepad.model.ReleaseType.PlayStore
import com.farmerbb.notepad.model.ReleaseType.Unknown
import com.farmerbb.notepad.usecase.ArtVandelay
import com.farmerbb.notepad.usecase.DataMigrator
import com.farmerbb.notepad.usecase.KeyboardShortcuts
import com.farmerbb.notepad.usecase.Toaster
import com.farmerbb.notepad.utils.isPlayStoreInstalled
import com.farmerbb.notepad.utils.releaseType
import de.schnettler.datastore.manager.DataStoreManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class NotepadViewModel(
    private val context: Application,
    private val repo: NotepadRepository,
    dataStoreManager: DataStoreManager,
    private val dataMigrator: DataMigrator,
    private val toaster: Toaster,
    private val artVandelay: ArtVandelay,
    private val keyboardShortcuts: KeyboardShortcuts
): ViewModel() {
    private val _noteState = MutableStateFlow(Note())
    val noteState: StateFlow<Note> = _noteState

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val selectedNotes = mutableMapOf<Long, Boolean>()
    private val _selectedNotesFlow = MutableSharedFlow<Map<Long, Boolean>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val selectedNotesFlow: SharedFlow<Map<Long, Boolean>> = _selectedNotesFlow

    val noteMetadata get() = prefs.sortOrder.flatMapConcat(repo::noteMetadataFlow)
    val prefs = dataStoreManager.prefs(viewModelScope)

    private val _savedDraftId = MutableStateFlow<Long?>(null)
    val savedDraftId: StateFlow<Long?> = _savedDraftId
    private var savedDraftIdJob: Job? = null

    fun migrateData(onComplete: () -> Unit) = viewModelScope.launch {
        dataMigrator.migrate()
        onComplete()
    }

    fun getSavedDraftId() {
        savedDraftIdJob = viewModelScope.launch(Dispatchers.IO) {
            repo.savedDraftId.collect { id ->
                _savedDraftId.value = id

                if (id != -1L) {
                    toaster.toast(R.string.draft_restored)
                }

                savedDraftIdJob?.cancel()
            }
        }
    }

    fun setText(text: String) {
        _text.value = text
    }

    fun getNote(id: Long?) = viewModelScope.launch(Dispatchers.IO) {
        id?.let {
            _noteState.value = repo.getNote(it)
            _text.value = noteState.value.text
        } ?: run {
            clearNote()
        }
    }

    fun clearNote() {
        _noteState.value = Note()
        _text.value = ""
    }

    fun toggleSelectedNote(id: Long) {
        selectedNotes[id] = !selectedNotes.getOrDefault(id, false)
        _selectedNotesFlow.tryEmit(selectedNotes.filterValues { it })
    }

    fun clearSelectedNotes() {
        selectedNotes.clear()
        _selectedNotesFlow.tryEmit(emptyMap())
    }

    fun selectAllNotes(notes: List<NoteMetadata>) {
        notes.forEach {
            selectedNotes[it.metadataId] = true
        }

        _selectedNotesFlow.tryEmit(selectedNotes.filterValues { it })
    }

    fun deleteSelectedNotes() = viewModelScope.launch(Dispatchers.IO) {
        selectedNotes.filterValues { it }.keys.let { ids ->
            repo.deleteNotes(ids.toList()) {
                clearSelectedNotes()

                val toastId = when (ids.size) {
                    1 -> R.string.note_deleted
                    else -> R.string.notes_deleted
                }

                toaster.toast(toastId)
            }
        }
    }

    fun deleteNote(
        id: Long,
        onSuccess: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteNote(id) {
            toaster.toast(R.string.note_deleted)
            onSuccess()
        }
    }

    fun shareNote(text: String) = viewModelScope.launch {
        text.checkLength {
            showShareSheet(text)
        }
    }

    fun saveNote(
        id: Long,
        text: String,
        onSuccess: (Long) -> Unit = {}
    ) = viewModelScope.launch(Dispatchers.IO) {
        text.checkLength {
            repo.saveNote(id, text) {
                toaster.toast(R.string.note_saved)
                onSuccess(it)
            }
        }
    }

    fun saveDraft(
        onSuccess: suspend () -> Unit = { toaster.toast(R.string.draft_saved) }
    ) {
        if (text.value.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            repo.saveNote(
                id = noteState.value.id,
                text = noteState.value.text,
                draftText = text.value
            ) { id ->
                getNote(id)
                onSuccess()
            }
        }
    }

    fun deleteDraft() = viewModelScope.launch(Dispatchers.IO) {
        with(noteState.value) {
            if (text.isEmpty()) {
                repo.deleteNote(id)
            } else {
                repo.saveNote(id, text)
            }
        }
    }

    fun importNotes() = artVandelay.importNotes(::saveImportedNote) { size ->
        val toastId = when (size) {
            1 -> R.string.note_imported_successfully
            else -> R.string.notes_imported_successfully
        }

        viewModelScope.launch {
            toaster.toast(toastId)
        }
    }

    fun exportNotes(
        metadata: List<NoteMetadata>,
        filenameFormat: FilenameFormat
    ) = viewModelScope.launch(Dispatchers.IO) {
        val hydratedNotes = repo.getNotes(
            metadata.filter {
                selectedNotes.getOrDefault(it.metadataId, false)
            }
        ).also {
            clearSelectedNotes()
        }

        artVandelay.exportNotes(hydratedNotes, filenameFormat, ::saveExportedNote, ::clearSelectedNotes)
    }

    fun exportSingleNote(
        metadata: NoteMetadata,
        text: String,
        filenameFormat: FilenameFormat
    ) = viewModelScope.launch {
        text.checkLength {
            artVandelay.exportSingleNote(metadata, filenameFormat, { saveExportedNote(it, text) }) {
                viewModelScope.launch {
                    toaster.toast(R.string.note_exported_to)
                }
            }
        }
    }

    private fun saveImportedNote(
        input: InputStream
    ) = viewModelScope.launch(Dispatchers.IO) {
        input.source().buffer().use {
            val text = it.readUtf8()
            if (text.isNotEmpty()) {
                repo.saveNote(text = text)
            }
        }
    }

    private fun saveExportedNote(
        output: OutputStream,
        text: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        output.sink().buffer().use {
            it.writeUtf8(text)
        }
    }

    fun loadFileFromIntent(
        intent: Intent,
        onLoad: (String?) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        intent.data?.let { uri ->
            val input = context.contentResolver.openInputStream(uri) ?: run {
                onLoad(null)
                return@launch
            }

            input.source().buffer().use {
                onLoad(it.readUtf8())
            }
        } ?: onLoad(null)
    }

    fun checkForUpdates() = with(context) {
        val id = BuildConfig.APPLICATION_ID
        val url = when(releaseType) {
            PlayStore -> {
                if(isPlayStoreInstalled)
                    "https://play.google.com/store/apps/details?id=$id"
                else
                    "https://github.com/farmerbb/Notepad/releases"
            }
            Amazon -> "https://www.amazon.com/gp/mas/dl/android?p=$id"
            FDroid -> "https://f-droid.org/repository/browse/?fdid=$id"
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
        0 -> toaster.toast(R.string.empty_note)
        else -> onSuccess()
    }

    fun showToastIf(
        condition: Boolean,
        @StringRes text: Int,
        block: () -> Unit
    ) = viewModelScope.launch {
        toaster.toastIf(condition, text, block)
    }

    fun keyboardShortcutPressed(keyCode: Int) = keyboardShortcuts.pressed(keyCode)
    fun registerKeyboardShortcuts(vararg mappings: Pair<Int, () -> Unit>) =
        keyboardShortcuts.register(*mappings)
}

val viewModelModule = module {
    viewModel {
        NotepadViewModel(
            context = androidApplication(),
            repo = get(),
            dataStoreManager = get(),
            dataMigrator = get(),
            toaster = get(),
            artVandelay = get(),
            keyboardShortcuts = get()
        )
    }
}