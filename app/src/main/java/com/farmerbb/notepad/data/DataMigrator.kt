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

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.farmerbb.notepad.models.CrossRef
import com.farmerbb.notepad.models.NoteContents
import com.farmerbb.notepad.models.NoteMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.apache.commons.lang3.math.NumberUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.*
import javax.inject.Inject

class DataMigrator @Inject constructor(
  @ApplicationContext val context: Context,
  private val dao: NotepadDAO
) {
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

  suspend fun migrate() {
    for(filename in context.filesDir.list().orEmpty()) {
      if(!NumberUtils.isCreatable(filename)) continue

      val metadata = NoteMetadata(
        title = loadNoteTitle(filename),
        date = Date(filename.toLong())
      )

      val contents = NoteContents(
        text = loadNote(filename),
        isDraft = false
      )

      with(dao) {
        insertCrossRef(CrossRef(
          metadataId = insertNoteMetadata(metadata),
          contentsId = insertNoteContents(contents)
        ))
      }

      File(context.filesDir, filename).delete()
    }

    val draft = File(context.filesDir, "draft")
    if(draft.exists()) {
      val contents = NoteContents(
        text = loadNote("draft"),
        isDraft = true
      )

      dao.insertNoteContents(contents)
      draft.delete()
    }

    context.dataStore.edit {
      // no-op to force SharedPreferences migration to trigger
    }

    job.complete()
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
}