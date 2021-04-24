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

package com.farmerbb.notepad.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.farmerbb.notepad.R
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.data.NoteMigrator
import com.farmerbb.notepad.data.NotepadDAO
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint class MainActivityCompose: ComponentActivity() {
  @Inject lateinit var migrator: NoteMigrator
  @Inject lateinit var dao: NotepadDAO

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      migrator.migrate()

      val notes = dao.getNoteMetadataSortedByTitle()
      setContent {
        NotesList(notes)
      }
    }
  }

  @Composable fun NotesList(notes: List<NoteMetadata>) = MaterialTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(id = R.string.app_name),
              color = Color.White
            )
          },
          backgroundColor = colorResource(id = R.color.primary)
        )
      },
      content = {
        LazyColumn {
          items(notes.size) {
            Column(modifier = Modifier
              .clickable {
                // TODO navigate to note view
              }
            ) {
              Text(
                text = notes[it].title,
                modifier = Modifier
                  .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                  )
              )

              Divider()
            }
          }
        }
      })
  }

  @Preview @Composable fun Preview() {
    NotesList(listOf(
      NoteMetadata(title = "Test Note 1"),
      NoteMetadata(title = "Test Note 2")
    ))
  }
}