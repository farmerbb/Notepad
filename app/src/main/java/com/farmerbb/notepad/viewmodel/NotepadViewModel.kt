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

@file:OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)

package com.farmerbb.notepad.viewmodel

import android.app.Application
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.data.PreferenceManager.Companion.prefs
import com.farmerbb.notepad.model.FilenameFormat
import com.farmerbb.notepad.model.Note
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.model.PrefKeys
import com.farmerbb.notepad.usecase.ArtVandelay
import com.farmerbb.notepad.usecase.DataMigrator
import com.farmerbb.notepad.usecase.KeyboardShortcuts
import com.farmerbb.notepad.usecase.SystemTheme
import com.farmerbb.notepad.usecase.Toaster
import com.farmerbb.notepad.utils.checkForUpdates
import com.farmerbb.notepad.utils.deserializeNoteJson
import com.farmerbb.notepad.utils.serializeNotes
import com.farmerbb.notepad.utils.showShareSheet
import de.schnettler.datastore.manager.DataStoreManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class NotepadViewModel(
    private val context: Application,
    private val repo: NotepadRepository,
    val dataStoreManager: DataStoreManager,
    private val dataMigrator: DataMigrator,
    private val toaster: Toaster,
    private val artVandelay: ArtVandelay,
    private val keyboardShortcuts: KeyboardShortcuts,
    systemTheme: SystemTheme
): ViewModel() {

    /*********************** Data ***********************/

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

    private val foundNotes = mutableMapOf<Long, Boolean>()
    private val _foundNotesFlow = MutableSharedFlow<Map<Long, Boolean>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val foundNotesFlow: SharedFlow<Map<Long, Boolean>> = _foundNotesFlow

    val noteMetadata get() = prefs.sortOrder.flatMapConcat(repo::noteMetadataFlow)
    val prefs = dataStoreManager.prefs(viewModelScope, systemTheme)

    private val _savedDraftId = MutableStateFlow<Long?>(null)
    val savedDraftId: StateFlow<Long?> = _savedDraftId
    private var savedDraftIdJob: Job? = null

    private var isEditing = false

    var currentMimeType: String = ""

    /*********************** UI Operations ***********************/

    private fun withArtVandelay(
        mimeType: String,
        callback: ArtVandelay.() -> Unit,
    ) {
        currentMimeType = mimeType
        with(artVandelay, callback)
    }

    fun setText(text: String) {
        _text.value = text
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

    fun setAllNotesAsFound(notes: List<NoteMetadata>) {
        notes.forEach {
            foundNotes[it.metadataId] = true
        }

        _foundNotesFlow.tryEmit(foundNotes.filterValues { it })
    }

    fun setSomeNotesAsNotFound(
        notes: List<NoteMetadata>,
        searchTerm: String,
    ) {
        // This is where the actual search is done
        setAllNotesAsFound(notes)
        repo.getNotes(notes).forEach {
            if(!it.text.contains(searchTerm, ignoreCase = true)) {
                foundNotes[it.id] = false
            }
        }

        _foundNotesFlow.tryEmit(foundNotes.filterValues { it })
    }


    fun showToast(@StringRes text: Int) = viewModelScope.launch {
        toaster.toast(text)
    }

    fun showToastIf(
        condition: Boolean,
        @StringRes text: Int,
        block: () -> Unit
    ) = viewModelScope.launch {
        toaster.toastIf(condition, text, block)
    }

    fun shareNote(text: String) = viewModelScope.launch {
        text.checkLength {
            context.showShareSheet(text)
        }
    }

    fun checkForUpdates() = context.checkForUpdates()

    fun setIsEditing(value: Boolean) {
        isEditing = value
    }

    /*********************** Database Operations ***********************/

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

    fun getNote(id: Long?) = viewModelScope.launch(Dispatchers.IO) {
        id?.let {
            _noteState.value = repo.getNote(it)

            if (text.value.isEmpty()) {
                _text.value = with(noteState.value) {
                    draftText.ifEmpty { text }
                }
            }
        } ?: run {
            _noteState.value = Note()
        }
    }

    fun deleteSelectedNotes(
        onSuccess: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        selectedNotes.filterValues { it }.keys.let { ids ->
            repo.deleteNotes(ids.toList()) {
                clearSelectedNotes()

                val toastId = when (ids.size) {
                    1 -> R.string.note_deleted
                    else -> R.string.notes_deleted
                }

                toaster.toast(toastId)
                onSuccess()
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
        val draftText = text.value
        if (!isEditing || draftText.isEmpty()) return

        if (noteState.value.text == draftText) {
            viewModelScope.launch { onSuccess() }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            with(noteState.value) {
                repo.saveNote(id, text, date, draftText) { newId ->
                    getNote(newId)
                    onSuccess()
                }
            }
        }
    }

    fun deleteDraft() = viewModelScope.launch(Dispatchers.IO) {
        with(noteState.value) {
            when {
                text.isEmpty() -> repo.deleteNote(id)
                !isEditing -> repo.saveNote(id, text, date)
            }
        }
    }

    /*********************** Preference Operations ***********************/

    fun firstRunComplete() = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.editPreference(
            key = PrefKeys.FirstRun,
            newValue = 1
        )
    }

    fun firstViewComplete() = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.editPreference(
            key = PrefKeys.FirstLoad,
            newValue = 1
        )
    }

    fun doubleTapMessageShown() = viewModelScope.launch(Dispatchers.IO) {
        toaster.toast(R.string.double_tap)

        dataStoreManager.editPreference(
            key = PrefKeys.ShowDoubleTapMessage,
            newValue = false
        )
    }

    /*********************** Import / Export ***********************/

    fun importNotes() = withArtVandelay(PLAIN_TEXT) {
        importNotes(::saveImportedNote) { size ->
            val toastId = when (size) {
                1 -> R.string.note_imported_successfully
                else -> R.string.notes_imported_successfully
            }

            viewModelScope.launch {
                toaster.toast(toastId)
            }
        }
    }

    fun importAllNotes() = withArtVandelay(JSON) {
        importAllNotes(
            saveImportedNotes = ::saveImportedNotes,
            onError = {
                viewModelScope.launch {
                    toaster.toast(R.string.error_importing_notes)
                }
            },
            onComplete = {
                viewModelScope.launch {
                    toaster.toast(R.string.notes_imported_successfully)
                }
            },
        )
    }

    fun exportAllNotes() = viewModelScope.launch(Dispatchers.IO) {
        val allNoteMetadata = noteMetadata.first()
        val hydratedNotes = repo.getNotes(
            allNoteMetadata
        )

        withArtVandelay(JSON) {
            exportAllNotes(
                hydratedNotes = hydratedNotes,
                saveExportedNotes = ::saveExportedNotes,
                onError = {
                    viewModelScope.launch {
                        toaster.toast(R.string.error_exporting_notes)
                    }
                },
                onComplete = {
                    viewModelScope.launch {
                        toaster.toast(R.string.notes_exported_to)
                    }
                },
            )
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

        if (hydratedNotes.size == 1) {
            val note = hydratedNotes.first()
            exportSingleNote(note.metadata, note.text, filenameFormat)
            return@launch
        }

        withArtVandelay(PLAIN_TEXT) {
            exportNotes(
                hydratedNotes,
                filenameFormat,
                ::saveExportedNote,
                ::clearSelectedNotes
            ) {
                viewModelScope.launch {
                    toaster.toast(R.string.notes_exported_to)
                }
            }
        }
    }

    fun exportSingleNote(
        metadata: NoteMetadata,
        text: String,
        filenameFormat: FilenameFormat
    ) = viewModelScope.launch {
        text.checkLength {
            withArtVandelay(PLAIN_TEXT) {
                exportSingleNote(
                    metadata,
                    filenameFormat,
                    { saveExportedNote(it, text) }
                ) {
                    viewModelScope.launch {
                        toaster.toast(R.string.note_exported_to)
                    }
                }
            }
        }
    }

    private fun parseDateFromFileName(filePath: String): Date? {
        // Extracting the filename from the full path
        val fileName = filePath.substring(filePath.lastIndexOf('/') + 1)

        // Pattern to match the date in the filename
        val datePattern = Regex("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}")

        // Trying to find the date in the filename
        val matchResult = datePattern.find(fileName)
        return matchResult?.value?.let { dateString ->
            try {
                SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).parse(dateString)
            } catch (_: Exception) {
                null // Return null if the date cannot be parsed
            }
        }
    }

    private fun saveImportedNote(
        input: InputStream,
        filePath: String = ""
    ) = viewModelScope.launch(Dispatchers.IO) {
        input.source().buffer().use {
            val text = it.readUtf8()
            if (text.isNotEmpty()) {
                val modifiedDate = parseDateFromFileName(filePath)
                // If the modified date couldn't be parsed, use current date
                val nonNullModifiedDate: Date = modifiedDate ?: Date()
                repo.saveNote(text = text, date = nonNullModifiedDate)
            }
        }
    }

    private fun saveImportedNotes(
        input: InputStream,
    ) = viewModelScope.launch(Dispatchers.IO) {
        input.source().buffer().use {
            val text = it.readUtf8()
            if (text.isNotEmpty()) {
                val deserializedNotes = deserializeNoteJson(text)
                deserializedNotes.forEach { note ->
                    repo.saveNote(text = note.text, date = note.date)
                }
            }
        }
    }

    private fun saveExportedNotes(
        output: OutputStream,
        notes: List<Note>
    ) = viewModelScope.launch(Dispatchers.IO) {
        output.sink().buffer().use {
            it.writeUtf8(serializeNotes(notes))
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
                val text = it.readUtf8()
                withContext(Dispatchers.Main) {
                    onLoad(text)
                }
            }
        } ?: onLoad(null)
    }

    /*********************** Miscellaneous ***********************/

    private suspend fun String.checkLength(
        onSuccess: suspend () -> Unit
    ) = when(length) {
        0 -> toaster.toast(R.string.empty_note)
        else -> onSuccess()
    }

    fun migrateData(onComplete: () -> Unit) = viewModelScope.launch {
        dataMigrator.migrate()
        onComplete()
    }

    fun keyboardShortcutPressed(keyCode: Int) = keyboardShortcuts.pressed(keyCode)
    fun registerKeyboardShortcuts(vararg mappings: Pair<Int, () -> Unit>) =
        keyboardShortcuts.register(*mappings)

    private companion object {
        const val PLAIN_TEXT = "text/plain"
        const val JSON = "application/json"
    }
}

val viewModelModule = module {
    viewModel { new(::NotepadViewModel) }
}