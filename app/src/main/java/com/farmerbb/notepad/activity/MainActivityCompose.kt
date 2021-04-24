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
import com.farmerbb.notepad.util.NoteListItem
import kotlinx.coroutines.launch
import org.apache.commons.lang3.math.NumberUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivityCompose: ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      val notes = listNotes()
      setContent {
        NotesList(notes)
      }
    }
  }


  @Composable fun NotesList(notes: List<NoteListItem>) = MaterialTheme {
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
                text = notes[it].note,
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
      NoteListItem(
        "Test Note 1",
        "123456"
      ),
      NoteListItem(
        "Test Note 2",
        "123456"
      )
    ))
  }

  fun listNotes(): List<NoteListItem> {
    // Get array of file names
    val listOfNotes = arrayListOf<String>()
    val noteListItems = arrayListOf<NoteListItem>()

    // Remove any files from the list that aren't notes
    for(file in filesDir.list().orEmpty()) {
      if(NumberUtils.isCreatable(file))
        listOfNotes.add(file)
    }

    // Get array of first lines of each note
    for(file in listOfNotes) {
      noteListItems.add(NoteListItem(
        loadNoteTitle(file),
        file
      ))
    }

    return noteListItems
  }

  fun loadNoteTitle(filename: String): String {
    // Open the file on disk
    val input = openFileInput(filename)
    val reader = InputStreamReader(input)
    val buffer = BufferedReader(reader)

    // Load the file
    val line = buffer.readLine()

    // Close file on disk
    reader.close()

    return line
  }
}