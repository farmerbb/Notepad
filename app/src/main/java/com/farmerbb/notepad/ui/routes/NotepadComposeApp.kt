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

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.farmerbb.notepad.R
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.content.*
import com.farmerbb.notepad.ui.widgets.NoteListMenu
import com.farmerbb.notepad.ui.widgets.NoteViewEditMenu
import com.farmerbb.notepad.models.NavState
import com.farmerbb.notepad.models.NavState.Companion.EDIT
import com.farmerbb.notepad.models.NavState.Companion.VIEW
import com.farmerbb.notepad.models.NavState.Edit
import com.farmerbb.notepad.models.NavState.Empty
import com.farmerbb.notepad.models.NavState.View
import com.farmerbb.notepad.ui.widgets.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.zachklipp.richtext.ui.printing.Printable
import com.zachklipp.richtext.ui.printing.rememberPrintableController
import org.koin.androidx.compose.getViewModel

@Composable
fun NotepadComposeApp() {
    val vm: NotepadViewModel = getViewModel()
    val systemUiController = rememberSystemUiController()
    val configuration = LocalConfiguration.current
    val isLightTheme by vm.prefs.isLightTheme.collectAsState()

    MaterialTheme {
        NotepadComposeApp(
            vm = vm,
            isMultiPane = configuration.screenWidthDp >= 600
        )
    }

    LaunchedEffect(isLightTheme) {
        systemUiController.setNavigationBarColor(
            color = if (isLightTheme) Color.White else Color.Black
        )
    }
}

@Composable
fun NotepadComposeApp(
    vm: NotepadViewModel = getViewModel(),
    isMultiPane: Boolean = false,
    initState: NavState = Empty
) {
    val printController = rememberPrintableController()
    var isPrinting by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isPrinting = false
        }
    }

    val notes by vm.noteMetadata.collectAsState(emptyList())
    val note by vm.noteState.collectAsState()
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
    val onExportClick: (String, String) -> Unit = { noteTitle, text ->
        onDismiss()
        vm.exportNote(noteTitle, text)
    }
    val onPrintClick: (String) -> Unit = { noteTitle ->
        onDismiss()

        isPrinting = true
        printController.print(noteTitle)
    }

    val onBack = { navState = Empty }

    BackHandler(
        enabled = navState != Empty,
        onBack = onBack
    )

    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val primaryColorRes by vm.prefs.primaryColorRes.collectAsState()
    val secondaryColorRes by vm.prefs.secondaryColorRes.collectAsState()
    val textFontSize by vm.prefs.textFontSize.collectAsState()
    val dateFontSize by vm.prefs.dateFontSize.collectAsState()
    val fontFamily by vm.prefs.fontFamily.collectAsState()
    val showDate by vm.prefs.showDate.collectAsState()
    val markdown by vm.prefs.markdown.collectAsState()

    val textStyle = TextStyle(
        color = colorResource(id = primaryColorRes),
        fontSize = textFontSize,
        fontFamily = fontFamily
    )
    val dateStyle = TextStyle(
        color = colorResource(id = secondaryColorRes),
        fontSize = dateFontSize,
        fontFamily = fontFamily
    )

    @Composable
    fun NoteListContentShared() = NoteListContent(
        notes = notes,
        textStyle = textStyle,
        dateStyle = dateStyle,
        showDate = showDate
    ) { id ->
        navState = View(id)
    }

    when(val state = navState) {
        Empty -> {
            LaunchedEffect(Unit) {
                vm.clearNote()
            }

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
                    NoteListContentShared()
                }
            }
        }

        is View -> {
            LaunchedEffect(state.id) {
                vm.getNote(state.id)
            }

            title = note.metadata.title
            backButton = { BackButton(onBack) }
            actions = {
                EditButton { navState = Edit(state.id) }
                DeleteButton { onDeleteClick(state.id) }
                NoteViewEditMenu(
                    showMenu = showMenu,
                    onDismiss = onDismiss,
                    onMoreClick = onMoreClick,
                    onShareClick = { onShareClick(note.contents.text) },
                    onExportClick = { onExportClick(title, note.contents.text) },
                    onPrintClick = { onPrintClick(title) }
                )
            }
            content = {
                Printable(printController) {
                    ViewNoteContent(
                        text = note.contents.text,
                        baseTextStyle = textStyle,
                        markdown = markdown,
                        isPrinting = isPrinting
                    )
                }
            }
        }

        is Edit -> {
            LaunchedEffect(state.id) {
                vm.getNote(state.id)
            }

            var text by rememberSaveable { mutableStateOf(note.contents.text) }
            val id = note.metadata.metadataId

            title = note.metadata.title.ifEmpty {
                stringResource(id = R.string.action_new)
            }
            backButton = { BackButton(onBack) }
            actions = {
                SaveButton {
                    vm.saveNote(id, text) { newId ->
                        navState = View(newId)
                    }
                }
                DeleteButton { onDeleteClick(id) }
                NoteViewEditMenu(
                    showMenu = showMenu,
                    onDismiss = onDismiss,
                    onMoreClick = onMoreClick,
                    onShareClick = { onShareClick(text) },
                    onExportClick = { onExportClick(title, text) },
                    onPrintClick = { onPrintClick(title) }
                )
            }
            content = {
                Printable(printController) {
                    EditNoteContent(
                        text = text,
                        baseTextStyle = textStyle,
                        isPrinting = isPrinting
                    ) { text = it }
                }
            }
        }
    }

    Scaffold(
        backgroundColor = colorResource(id = backgroundColorRes),
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
                        NoteListContentShared()
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
