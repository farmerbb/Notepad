/* Copyright 2022 Braden Farmer
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

package com.farmerbb.notepad.usecase

import android.net.Uri
import com.farmerbb.notepad.model.ExportedNotesDirectory
import com.farmerbb.notepad.model.FilenameFormat
import com.farmerbb.notepad.model.FilenameFormat.TimestampAndTitle
import com.farmerbb.notepad.model.FilenameFormat.TitleAndTimestamp
import com.farmerbb.notepad.model.FilenameFormat.TitleOnly
import com.farmerbb.notepad.model.Note
import com.farmerbb.notepad.model.NoteMetadata
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.callback.FileCreateCallback
import com.github.k1rakishou.fsaf.callback.FileMultiSelectChooserCallback
import com.github.k1rakishou.fsaf.callback.directory.DirectoryChooserCallback
import com.github.k1rakishou.fsaf.file.FileSegment
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import org.koin.dsl.module

interface ArtVandelay {
    fun importNotes(
        saveImportedNote: (InputStream) -> Unit,
        onComplete: (Int) -> Unit
    )

    fun exportNotes(
        hydratedNotes: List<Note>,
        filenameFormat: FilenameFormat,
        saveExportedNote: (OutputStream, String) -> Unit,
        onCancel: () -> Unit,
        onComplete: () -> Unit
    )

    fun exportSingleNote(
        metadata: NoteMetadata,
        filenameFormat: FilenameFormat,
        saveExportedNote: (OutputStream) -> Unit,
        onComplete: () -> Unit
    )
}

private class ArtVandelayImpl(
    private val fileChooser: FileChooser,
    private val fileManager: FileManager
): ArtVandelay {
    override fun importNotes(
        saveImportedNote: (InputStream) -> Unit,
        onComplete: (Int) -> Unit
    ) = fileChooser.openChooseMultiSelectFileDialog(
        importCallback(saveImportedNote, onComplete)
    )

    override fun exportNotes(
        hydratedNotes: List<Note>,
        filenameFormat: FilenameFormat,
        saveExportedNote: (OutputStream, String) -> Unit,
        onCancel: () -> Unit,
        onComplete: () -> Unit
    ) = fileChooser.openChooseDirectoryDialog(
        directoryChooserCallback = exportFolderCallback(
            hydratedNotes,
            filenameFormat,
            saveExportedNote,
            onCancel,
            onComplete
        )
    )

    override fun exportSingleNote(
        metadata: NoteMetadata,
        filenameFormat: FilenameFormat,
        saveExportedNote: (OutputStream) -> Unit,
        onComplete: () -> Unit
    ) = fileChooser.openCreateFileDialog(
        fileName = generateFilename(metadata, filenameFormat),
        fileCreateCallback = exportFileCallback(saveExportedNote, onComplete)
    )

    private val registeredBaseDirs = mutableListOf<Uri>()

    private fun importCallback(
        saveImportedNote: (InputStream) -> Unit,
        onComplete: (Int) -> Unit
    ) = object: FileMultiSelectChooserCallback() {
        override fun onResult(uris: List<Uri>) {
            with(fileManager) {
                for (uri in uris) {
                    fromUri(uri)?.let(::getInputStream)?.let(saveImportedNote)
                }

                onComplete(uris.size)
            }
        }

        override fun onCancel(reason: String) = Unit // no-op
    }

    private fun exportFolderCallback(
        hydratedNotes: List<Note>,
        filenameFormat: FilenameFormat,
        saveExportedNote: (OutputStream, String) -> Unit,
        onCancel: () -> Unit,
        onComplete: () -> Unit
    ) = object: DirectoryChooserCallback() {
        override fun onResult(uri: Uri) {
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

                onComplete()
            }
        }

        override fun onCancel(reason: String) = onCancel()
    }

    private fun exportFileCallback(
        saveExportedNote: (OutputStream) -> Unit,
        onComplete: () -> Unit
    ) = object: FileCreateCallback() {
        override fun onResult(uri: Uri) {
            with(fileManager) {
                fromUri(uri)?.let(::getOutputStream)?.let(saveExportedNote)
                onComplete()
            }
        }

        override fun onCancel(reason: String) = Unit // no-op
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

val artVandelayModule = module {
    single<ArtVandelay> {
        ArtVandelayImpl(
            fileChooser = get(),
            fileManager = get()
        )
    }
}