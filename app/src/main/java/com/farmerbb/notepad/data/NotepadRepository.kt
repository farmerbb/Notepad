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

import com.farmerbb.notepad.Database
import com.farmerbb.notepad.model.CrossRef
import com.farmerbb.notepad.model.Note
import com.farmerbb.notepad.model.NoteContents
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.model.SortOrder
import com.farmerbb.notepad.model.SortOrder.DateAscending
import com.farmerbb.notepad.model.SortOrder.DateDescending
import com.farmerbb.notepad.model.SortOrder.TitleAscending
import com.farmerbb.notepad.model.SortOrder.TitleDescending
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import java.util.Date

class NotepadRepository(
    private val database: Database
) {
    val savedDraftId get() = database.noteMetadataQueries.getDraftId().asFlow().mapToOneOrDefault(-1L)

    fun noteMetadataFlow(order: SortOrder) = with(database.noteMetadataQueries) {
        when(order) {
            DateDescending -> getSortedByDateDescending()
            DateAscending -> getSortedByDateAscending()
            TitleDescending -> getSortedByTitleDescending()
            TitleAscending -> getSortedByTitleAscending()
        }
    }.asFlow().mapToList()

    fun getNote(id: Long): Note = with(database) {
        transactionWithResult {
            val metadata = noteMetadataQueries.get(id).executeAsOne()
            val crossRef = crossRefQueries.get(metadata.metadataId).executeAsOne()
            val contents = noteContentsQueries.get(crossRef.contentsId).executeAsOne()

            Note(
                metadata = metadata,
                contents = contents
            )
        }
    }

    fun getNotes(metadataList: List<NoteMetadata>): List<Note> = with(database) {
        transactionWithResult {
            val crossRefList = crossRefQueries.getMultiple(metadataList.map { it.metadataId }).executeAsList()
            val contentsList = noteContentsQueries.getMultiple(crossRefList.map { it.contentsId }).executeAsList()

            buildList {
                metadataList.sortedBy { it.metadataId }.forEachIndexed { index, metadata ->
                    Note(
                        metadata = metadata,
                        contents = contentsList[index]
                    ).also(::add)
                }
            }
        }
    }

    suspend fun saveNote(
        id: Long = -1,
        text: String,
        draftText: String? = null,
        onSuccess: suspend (Long) -> Unit = {}
    ) = try {
        val crossRef = database.crossRefQueries.get(id).executeAsOneOrNull()

        val metadata = NoteMetadata(
            metadataId = crossRef?.metadataId ?: -1,
            title = text.substringBefore("\n"),
            date = Date(),
            hasDraft = draftText != null
        )

        val contents = NoteContents(
            contentsId = crossRef?.contentsId ?: -1,
            text = text,
            draftText = draftText
        )

        with(database) {
            crossRef?.let {
                noteMetadataQueries.update(metadata)
                noteContentsQueries.update(contents)
                onSuccess(id)
            } ?: run {
                noteMetadataQueries.insert(metadata)
                noteContentsQueries.insert(contents)

                val newCrossRef = CrossRef(
                    metadataId = noteMetadataQueries.getIndex().executeAsOne(),
                    contentsId = noteContentsQueries.getIndex().executeAsOne()
                )

                crossRefQueries.insert(newCrossRef)
                onSuccess(newCrossRef.metadataId)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    suspend fun deleteNote(id: Long, onSuccess: suspend () -> Unit = {}) = try {
        with(database) {
            crossRefQueries.get(id).executeAsOneOrNull()?.let {
                noteMetadataQueries.delete(it.metadataId)
                noteContentsQueries.delete(it.contentsId)
                crossRefQueries.delete(id)
            }
        }

        onSuccess()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    suspend fun deleteNotes(ids: List<Long>, onSuccess: suspend () -> Unit) = try {
        with(database) {
            crossRefQueries.getMultiple(ids).executeAsList().let { refs ->
                noteMetadataQueries.deleteMultiple(refs.map { it.metadataId })
                noteContentsQueries.deleteMultiple(refs.map { it.contentsId })
                crossRefQueries.deleteMultiple(ids)
            }
        }

        onSuccess()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}