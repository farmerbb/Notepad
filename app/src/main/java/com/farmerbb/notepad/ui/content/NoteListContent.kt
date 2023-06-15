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

@file:OptIn(ExperimentalFoundationApi::class)

package com.farmerbb.notepad.ui.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.ui.components.RtlTextWrapper
import com.farmerbb.notepad.ui.previews.NoteListPreview
import java.text.DateFormat
import java.util.Date

private val Date.noteListFormat: String get() = DateFormat
    .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    .format(this)

private data class ListParams(
    val index: Int,
    val scrollOffset: Int
)

private var listParams = ListParams(0, 0)

@Composable
fun RememberNoteListScrollState(
): LazyListState {
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            listParams.index,
            listParams.scrollOffset
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            val lastIndex = scrollState.firstVisibleItemIndex
            val lastOffset = scrollState.firstVisibleItemScrollOffset
            listParams = ListParams(lastIndex, lastOffset)
        }
    }
    return scrollState
}

@Composable
fun NoteListContent(
    notes: List<NoteMetadata>,
    selectedNotes: Map<Long, Boolean> = emptyMap(),
    textStyle: TextStyle = TextStyle(),
    dateStyle: TextStyle = TextStyle(),
    showDate: Boolean = false,
    rtlLayout: Boolean = false,
    onNoteLongClick: (Long) -> Unit = {},
    onNoteClick: (Long) -> Unit = {}
) {
    when (notes.size) {
        0 -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.no_notes_found),
                color = colorResource(id = R.color.primary),
                fontWeight = FontWeight.Thin,
                fontSize = 30.sp
            )
        }

        else -> LazyColumn(state = RememberNoteListScrollState()) {
            itemsIndexed(notes) { _, note ->
                val isSelected = selectedNotes.getOrDefault(note.metadataId, false)
                Column(modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier.background(color = colorResource(id = R.color.praise_duarte))
                        } else Modifier
                    )
                    .combinedClickable(
                        onClick = { onNoteClick(note.metadataId) },
                        onLongClick = { onNoteLongClick(note.metadataId) }
                    )
                ) {
                    RtlTextWrapper(note.title, rtlLayout) {
                        BasicText(
                            text = note.title,
                            style = textStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = if (showDate) 8.dp else 12.dp,
                                    bottom = if (showDate) 0.dp else 12.dp
                                )
                        )
                    }

                    if (showDate) {
                        BasicText(
                            text = note.date.noteListFormat,
                            style = dateStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 8.dp
                                )
                        )
                    }

                    Divider()
                }
            }
        }
    }
}

@Preview
@Composable
fun NoteListContentPreview() = NoteListPreview()