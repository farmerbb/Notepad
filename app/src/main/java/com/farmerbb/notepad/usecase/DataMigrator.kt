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

package com.farmerbb.notepad.usecase

import android.content.Context
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.farmerbb.notepad.Database
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.CrossRef
import com.farmerbb.notepad.model.NoteContents
import com.farmerbb.notepad.model.NoteMetadata
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

interface DataMigrator {
    suspend fun migrate()
}

private class DataMigratorImpl(
    private val context: Context,
    private val database: Database,
    private val toaster: Toaster
): DataMigrator {
    private val job = Job()

    private val Context.dataStore by preferencesDataStore(
        name = "settings",
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(context, "${context.packageName}_preferences"),
                SharedPreferencesMigration(context, "MainActivity")
            )
        },
        scope = CoroutineScope(job)
    )

    override suspend fun migrate() {
        val migrationComplete = File(context.filesDir, "migration_complete")
        if (migrationComplete.exists() || job.isCompleted) return

        withContext(Dispatchers.IO) {
            val (draftFilename, draftText) = loadDraft()

            for (filename in context.filesDir.list().orEmpty()) {
                if (!filename.isDigitsOnly() && filename != "draft") continue

                val metadata = NoteMetadata(
                    metadataId = -1,
                    title = loadNoteTitle(filename),
                    date = Date(filename.toLong()),
                    hasDraft = false
                )

                val contents = NoteContents(
                    contentsId = -1,
                    text = loadNote(filename),
                    draftText = if (filename == draftFilename) draftText else null
                )

                with(database) {
                    noteMetadataQueries.insert(metadata)
                    noteContentsQueries.insert(contents)
                    crossRefQueries.insert(
                        CrossRef(
                            metadataId = noteMetadataQueries.getIndex().executeAsOne(),
                            contentsId = noteContentsQueries.getIndex().executeAsOne()
                        )
                    )
                }

                File(context.filesDir, filename).delete()
            }

            context.dataStore.edit {
                // no-op to force SharedPreferences migration to trigger
            }

            job.complete()
            migrationComplete.createNewFile()
            toaster.toast(R.string.migration_success)
        }
    }

    private fun loadNoteTitle(filename: String): String {
        val input = context.openFileInput(filename)
        val reader = InputStreamReader(input)
        val buffer = BufferedReader(reader)

        val line = buffer.readLine()
        reader.close()

        return line
    }

    private fun loadNote(filename: String): String {
        val note = StringBuilder()

        val input = context.openFileInput(filename)
        val reader = InputStreamReader(input)
        val buffer = BufferedReader(reader)

        var line = buffer.readLine()
        while(line != null) {
            note.append(line)
            line = buffer.readLine()
            if(line != null) note.append("\n")
        }

        reader.close()

        return note.toString()
    }

    private fun loadDraft(): Pair<String?, String?> {
        val prefs = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
        val text = prefs.getString("draft-contents", null) ?: return null to null
        val isSavedNote = prefs.getBoolean("is-saved-note", false)
        val filename = if (isSavedNote) prefs.getLong("draft-name", -1L) else -1L

        prefs.edit {
            remove("is-saved-note")
            remove("draft-name")
            remove("draft-contents")
        }

        return "$filename" to text
    }
}

val dataMigratorModule = module {
    single<DataMigrator> {
        DataMigratorImpl(
            context = androidContext(),
            database = get(),
            toaster = get()
        )
    }
}