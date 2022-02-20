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

package com.farmerbb.notepad.ui.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.models.NoteMetadata
import com.farmerbb.notepad.ui.content.*
import com.farmerbb.notepad.ui.widgets.NoteListMenu
import com.farmerbb.notepad.ui.widgets.NoteViewEditMenu
import com.farmerbb.notepad.models.NavState
import com.farmerbb.notepad.models.NavState.Companion.EDIT
import com.farmerbb.notepad.models.NavState.Companion.VIEW
import com.farmerbb.notepad.models.NavState.Edit
import com.farmerbb.notepad.models.NavState.Empty
import com.farmerbb.notepad.models.NavState.View
import com.farmerbb.notepad.models.noteState
import com.farmerbb.notepad.models.textFieldState
import com.farmerbb.notepad.ui.widgets.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.getViewModel

@Composable
fun NotepadComposeApp() {
    val vm: NotepadViewModel = getViewModel()
    val systemUiController = rememberSystemUiController()
    val configuration = LocalConfiguration.current
    val notes = vm.noteMetadata.collectAsState(emptyList())

    MaterialTheme {
        NotepadComposeApp(
            notes = notes.value,
            vm = vm,
            isMultiPane = configuration.screenWidthDp >= 600
        )
    }

    SideEffect {
        systemUiController.setNavigationBarColor(
            color = Color.White
        )
    }
}

@Composable
fun NotepadComposeApp(
    notes: List<NoteMetadata>,
    vm: NotepadViewModel = getViewModel(),
    isMultiPane: Boolean = false,
    initState: NavState = Empty
) {
    var navState by rememberSaveable(
        saver = Saver(
            save = {
                when(val state = it.value) {
                    is View -> VIEW to state.id
                    is Edit -> EDIT to state.id
                    else -> "" to null
                }
            },
            restore = {
                mutableStateOf(
                    when(it.first) {
                        VIEW -> View(it.second ?: 0)
                        EDIT -> Edit(it.second)
                        else -> Empty
                    }
                )
            }
        )
    ) { mutableStateOf(initState) }

    var showAboutDialog by remember { mutableStateOf(false) }
    if(showAboutDialog) {
        AboutDialog(
            onDismiss = {
                showAboutDialog = false
            },
            checkForUpdates = {
                showAboutDialog = false
                vm.checkForUpdates()
            }
        )
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    if(showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                showSettingsDialog = false
            }
        )
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete: Long? by remember { mutableStateOf(null) }
    if(showDeleteDialog) {
        DeleteAlertDialog(
            onConfirm = {
                showDeleteDialog = false
                vm.deleteNote(id = noteToDelete ?: -1L) {
                    navState = Empty
                }
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    val title: String
    val backButton: @Composable (() -> Unit)?
    val actions: @Composable RowScope.() -> Unit
    val content: @Composable BoxScope.() -> Unit

    var showMenu by remember { mutableStateOf(false) }
    val onDismiss = { showMenu = false }
    val onMoreClick = { showMenu = true }
    val onDeleteClick: (Long?) -> Unit = {
        noteToDelete = it
        showDeleteDialog = true
    }
    val onShareClick: (String) -> Unit = {
        onDismiss()
        vm.shareNote(it)
    }
    val onExportClick: (String) -> Unit = {
        onDismiss()
        vm.exportNote(it)
    }
    val onPrintClick: (String) -> Unit = {
        onDismiss()
        vm.printNote(it)
    }

    when(val state = navState) {
        Empty -> {
            title = stringResource(id = R.string.app_name)
            backButton = null
            actions = {
                NoteListMenu(
                    showMenu = showMenu,
                    onDismiss = onDismiss,
                    onMoreClick = onMoreClick,
                    onSettingsClick = {
                        onDismiss()
                        showSettingsDialog = true
                    },
                    onImportClick = {
                        onDismiss()
                        vm.importNotes()
                    },
                    onAboutClick = {
                        onDismiss()
                        showAboutDialog = true
                    }
                )
            }
            content = {
                if(isMultiPane) {
                    EmptyDetails()
                } else {
                    NoteListContent(notes) { id ->
                        navState = View(id)
                    }
                }
            }
        }

        is View -> {
            val note by noteState(state.id)

            title = note.metadata.title
            backButton = { 
                BackButton { navState = Empty }
            }
            actions = {
                EditButton { navState = Edit(state.id) }
                DeleteButton { onDeleteClick(state.id) }
                NoteViewEditMenu(
                    showMenu = showMenu,
                    onDismiss = onDismiss,
                    onMoreClick = onMoreClick,
                    onShareClick = { onShareClick(note.contents.text) },
                    onExportClick = { onExportClick(note.contents.text) },
                    onPrintClick = { onPrintClick(note.contents.text) }
                )
            }
            content = { ViewNoteContent(note.contents.text) }
        }

        is Edit -> {
            val note by noteState(state.id)
            var value by textFieldState(note.contents.text)
            val id = note.metadata.metadataId

            title = note.metadata.title.ifEmpty {
                stringResource(id = R.string.action_new)
            }
            backButton = {
                BackButton { navState = Empty }
            }
            actions = {
                SaveButton {
                    vm.saveNote(id, value.text) { newId ->
                        navState = View(newId)
                    }
                }
                DeleteButton { onDeleteClick(id) }
                NoteViewEditMenu(
                    showMenu = showMenu,
                    onDismiss = onDismiss,
                    onMoreClick = onMoreClick,
                    onShareClick = { onShareClick(value.text) },
                    onExportClick = { onExportClick(value.text) },
                    onPrintClick = { onPrintClick(value.text) }
                )
            }
            content = {
                EditNoteContent(value) { value = it }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = backButton,
                title = { AppBarText(title) },
                backgroundColor = colorResource(id = R.color.primary),
                actions = actions
            )
        },
        floatingActionButton = {
            if(navState == Empty) {
                FloatingActionButton(
                    onClick = { navState = Edit() },
                    backgroundColor = colorResource(id = R.color.primary),
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )
            }
        },
        content = {
            if(isMultiPane) {
                Row {
                    Box(modifier = Modifier.weight(1f)) {
                        NoteListContent(notes) { id ->
                            navState = View(id)
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    Box(
                        modifier = Modifier.weight(2f),
                        content = content
                    )
                }
            } else {
                Box(content = content)
            }
        }
    )
}

@Composable
fun EmptyDetails() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.notepad_logo),
            contentDescription = null,
            modifier = Modifier
                .height(512.dp)
                .width(512.dp)
                .alpha(0.5f)
        )
    }
}
