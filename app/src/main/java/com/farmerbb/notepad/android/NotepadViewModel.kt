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

package com.farmerbb.notepad.android

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.R
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.data.PreferenceManager.Companion.prefs
import com.farmerbb.notepad.model.ExportedNotesDirectory
import com.farmerbb.notepad.model.FilenameFormat
import com.farmerbb.notepad.model.FilenameFormat.TimestampAndTitle
import com.farmerbb.notepad.model.FilenameFormat.TitleAndTimestamp
import com.farmerbb.notepad.model.FilenameFormat.TitleOnly
import com.farmerbb.notepad.model.Note
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.utils.ReleaseType.Amazon
import com.farmerbb.notepad.utils.ReleaseType.FDroid
import com.farmerbb.notepad.utils.ReleaseType.PlayStore
import com.farmerbb.notepad.utils.ReleaseType.Unknown
import com.farmerbb.notepad.utils.isPlayStoreInstalled
import com.farmerbb.notepad.utils.releaseType
import com.farmerbb.notepad.utils.showToast
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.callback.FileCreateCallback
import com.github.k1rakishou.fsaf.callback.FileMultiSelectChooserCallback
import com.github.k1rakishou.fsaf.callback.directory.DirectoryChooserCallback
import com.github.k1rakishou.fsaf.file.FileSegment
import de.schnettler.datastore.manager.DataStoreManager
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
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
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source

class NotepadViewModel(
    private val context: Application,
    private val repo: NotepadRepository,
    dataStoreManager: DataStoreManager,
    private val fileChooser: FileChooser,
    private val fileManager: FileManager
): ViewModel() {
    private val _noteState = MutableStateFlow(Note())
    val noteState: StateFlow<Note> = _noteState

    private val selectedNotes = mutableMapOf<Long, Boolean>()
    private val _selectedNotesFlow = MutableSharedFlow<Map<Long, Boolean>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val selectedNotesFlow: SharedFlow<Map<Long, Boolean>> = _selectedNotesFlow

    val noteMetadata get() = prefs.sortOrder.flatMapConcat(repo::noteMetadataFlow)
    val prefs = dataStoreManager.prefs(viewModelScope)

    private val registeredBaseDirs = mutableListOf<Uri>()

    private var draftText: String = ""
    private var draftId: Long = -1L

    private val _savedDraftId = MutableStateFlow<Long?>(null)
    val savedDraftId: StateFlow<Long?> = _savedDraftId
    private var savedDraftIdJob: Job? = null

    fun getSavedDraftId() {
        savedDraftIdJob = viewModelScope.launch(Dispatchers.IO) {
            repo.savedDraftId.collect { id ->
                _savedDraftId.value = id

                if (id != -1L) {
                    context.showToast(R.string.draft_restored)
                }

                savedDraftIdJob?.cancel()
            }
        }
    }

    fun setDraftText(text: String) {
        draftText = text
    }

    suspend fun getNote(id: Long?) = withContext(Dispatchers.IO) {
        id?.let {
            _noteState.value = repo.getNote(it)
        } ?: run {
            clearNote()
        }
    }

    fun clearNote() {
        _noteState.value = Note()
        draftText = ""
        draftId = -1L
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

                context.showToast(toastId)
            }
        }
    }

    fun exportSelectedNotes(
        notes: List<NoteMetadata>,
        filenameFormat: FilenameFormat
    ) = fileChooser.openChooseDirectoryDialog(
        directoryChooserCallback = exportFolderCallback(notes, filenameFormat)
    )

    fun saveNote(
        id: Long,
        text: String,
        onSuccess: (Long) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        text.checkLength {
            repo.saveNote(id, text) {
                context.showToast(R.string.note_saved)
                onSuccess(it)
            }
        }
    }

    fun saveDraft() {
        if (draftText.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            repo.saveNote(
                id = noteState.value.metadata.metadataId,
                text = noteState.value.text,
                draftText = draftText
            ) {
                draftId = it
                context.showToast(R.string.draft_saved)
            }
        }
    }

    fun deleteDraft() {
        if (draftText.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val savedId = noteState.value.metadata.metadataId
            if (draftId == savedId) {
                repo.saveNote(
                    id = noteState.value.metadata.metadataId,
                    text = noteState.value.text
                )
            } else {
                repo.deleteNote(draftId)
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

    private fun saveExportedNote(
        output: OutputStream,
        text: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        output.sink().buffer().use {
            it.writeUtf8(text)
        }
    }

    fun deleteNote(
        id: Long,
        onSuccess: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteNote(id) {
            context.showToast(R.string.note_deleted)
            onSuccess()
        }
    }

    fun shareNote(text: String) = viewModelScope.launch {
        text.checkLength {
            showShareSheet(text)
        }
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
        0 -> context.showToast(R.string.empty_note)
        else -> onSuccess()
    }

    fun importNotes() = fileChooser.openChooseMultiSelectFileDialog(importCallback)
    fun exportNote(
        metadata: NoteMetadata,
        text: String,
        filenameFormat: FilenameFormat
    ) = fileChooser.openCreateFileDialog(
        fileName = generateFilename(metadata, filenameFormat),
        fileCreateCallback = exportFileCallback(text)
    )

    private val importCallback = object: FileMultiSelectChooserCallback() {
        override fun onResult(uris: List<Uri>) {
            with(fileManager) {
                for (uri in uris) {
                    fromUri(uri)?.let(::getInputStream)?.let(::saveImportedNote)
                }

                val toastId = when (uris.size) {
                    1 -> R.string.note_imported_successfully
                    else -> R.string.notes_imported_successfully
                }

                viewModelScope.launch {
                    context.showToast(toastId)
                }
            }
        }

        override fun onCancel(reason: String) = Unit // no-op
    }

    private fun exportFileCallback(text: String) = object: FileCreateCallback() {
        override fun onResult(uri: Uri) {
            with(fileManager) {
                fromUri(uri)?.let(::getOutputStream)?.let { output ->
                    saveExportedNote(output, text)
                }

                viewModelScope.launch {
                    context.showToast(R.string.note_exported_to)
                }
            }
        }
        override fun onCancel(reason: String) = Unit // no-op
    }

    private fun exportFolderCallback(
        notes: List<NoteMetadata>,
        filenameFormat: FilenameFormat
    ) = object: DirectoryChooserCallback() {
        override fun onResult(uri: Uri) {
            viewModelScope.launch(Dispatchers.IO) {
                val hydratedNotes = repo.getNotes(
                    notes.filter {
                        selectedNotes.getOrDefault(it.metadataId, false)
                    }
                ).also {
                    clearSelectedNotes()
                }

                with(fileManager) {
                    if (!registeredBaseDirs.contains(uri)) {
                        registerBaseDir<ExportedNotesDirectory>(ExportedNotesDirectory(uri))
                        registeredBaseDirs.add(uri)
                    }

                    newBaseDirectoryFile<ExportedNotesDirectory>()?.let { baseDir ->
                        for (note in hydratedNotes) {
                            val filename = generateFilename(note.metadata, filenameFormat)
                            create(baseDir, FileSegment(filename))
                                ?.let(::getOutputStream)
                                ?.let { output ->
                                    saveExportedNote(output, note.text)
                                }
                        }
                    }
                }
            }
        }

        override fun onCancel(reason: String) = clearSelectedNotes()
    }

    private fun generateFilename(
        metadata: NoteMetadata,
        filenameFormat: FilenameFormat
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
        val timestamp = dateFormat.format(metadata.date)
        val filename = when(filenameFormat) {
            TitleOnly -> metadata.title
            TimestampAndTitle -> "${timestamp}_${metadata.title}"
            TitleAndTimestamp -> "${metadata.title}_$timestamp"
        }

        return "$filename.txt"
    }
}