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
import com.farmerbb.notepad.model.CrossRef
import com.farmerbb.notepad.model.NoteContents
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.model.Prefs
import java.io.File
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

interface DataMigrator {
    suspend fun migrate()
}

private class DataMigratorImpl(
    private val context: Context,
    private val database: Database
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
        if (job.isCancelled) return

        withContext(Dispatchers.IO) {
            val notesMigrationComplete = File(context.filesDir, "migration_complete")
            if (!notesMigrationComplete.exists()) {
                val (draftFilename, draftText) = loadDraft()

                for (filename in context.filesDir.list().orEmpty()) {
                    if (!filename.isDigitsOnly() && filename != "draft") continue

                    val text = loadNote(filename)
                    val hasDraft = filename == draftFilename

                    val metadata = NoteMetadata(
                        metadataId = -1,
                        title = text.substringBefore("\n"),
                        date = Date(filename.toLong()),
                        hasDraft = hasDraft
                    )

                    val contents = NoteContents(
                        contentsId = -1,
                        text = text,
                        draftText = if (hasDraft) draftText else null
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

                notesMigrationComplete.createNewFile()
            }

            migratePreferences()
            job.cancel()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun migratePreferences() = context.dataStore.edit { prefs ->
        val theme = prefs[Prefs.Theme.key]
        theme?.let {
            prefs[Prefs.ColorScheme.key] = it.split("-").first()
            prefs[Prefs.FontType.key] = it.split("-").last()
            prefs.remove(Prefs.Theme.key)
        } ?: return@edit
    }

    private fun loadNote(filename: String): String {
        val input = context.openFileInput(filename)
        input.source().buffer().use {
            return it.readUtf8()
        }
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
            database = get()
        )
    }
}