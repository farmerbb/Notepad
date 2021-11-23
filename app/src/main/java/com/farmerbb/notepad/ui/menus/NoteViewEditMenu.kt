package com.farmerbb.notepad.ui.menus

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.widgets.MoreButton

@Composable fun NoteViewEditMenu(
  text: String,
  vm: NotepadViewModel? = null,
) {
  val showMenu = remember { mutableStateOf(false) }

  Box {
    MoreButton(showMenu)
    DropdownMenu(
      expanded = showMenu.value,
      onDismissRequest = { showMenu.value = false }
    ) {
      ShareMenuItem(text, showMenu, vm)
      ExportMenuItem(text, showMenu, vm)
      PrintMenuItem(text, showMenu, vm)
    }
  }
}

@Composable fun ShareMenuItem(
  text: String,
  showMenu: MutableState<Boolean>,
  vm: NotepadViewModel?
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
      vm?.shareNote(text)
    }
  ) {
    Text(text = stringResource(R.string.action_share))
  }
}

@Composable fun ExportMenuItem(
  text: String,
  showMenu: MutableState<Boolean>,
  vm: NotepadViewModel?
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
   // vm?.exportNote(text)
    }
  ) {
    Text(text = stringResource(R.string.action_export))
  }
}

@Composable fun PrintMenuItem(
  text: String,
  showMenu: MutableState<Boolean>,
  vm: NotepadViewModel?
) {
  DropdownMenuItem(
    onClick = {
      showMenu.value = false
   // vm?.printNote(text)
    }
  ) {
    Text(text = stringResource(R.string.action_print))
  }
}
