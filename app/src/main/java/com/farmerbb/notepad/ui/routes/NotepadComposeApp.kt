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

@file:OptIn(ExperimentalAnimationApi::class)

package com.farmerbb.notepad.ui.routes

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.NavState
import com.farmerbb.notepad.model.NavState.Edit
import com.farmerbb.notepad.model.NavState.Empty
import com.farmerbb.notepad.model.NavState.View
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.model.navStateSaver
import com.farmerbb.notepad.ui.components.AboutDialog
import com.farmerbb.notepad.ui.components.AppBarText
import com.farmerbb.notepad.ui.components.BackButton
import com.farmerbb.notepad.ui.components.DeleteButton
import com.farmerbb.notepad.ui.components.DeleteDialog
import com.farmerbb.notepad.ui.components.EditButton
import com.farmerbb.notepad.ui.components.ExportButton
import com.farmerbb.notepad.ui.components.FirstRunDialog
import com.farmerbb.notepad.ui.components.FirstViewDialog
import com.farmerbb.notepad.ui.components.MultiSelectButton
import com.farmerbb.notepad.ui.components.NoteListMenu
import com.farmerbb.notepad.ui.components.NoteViewEditMenu
import com.farmerbb.notepad.ui.components.NotepadTheme
import com.farmerbb.notepad.ui.components.SaveButton
import com.farmerbb.notepad.ui.components.SaveDialog
import com.farmerbb.notepad.ui.components.SelectAllButton
import com.farmerbb.notepad.ui.content.EditNoteContent
import com.farmerbb.notepad.ui.content.NoteListContent
import com.farmerbb.notepad.ui.content.ViewNoteContent
import com.farmerbb.notepad.viewmodel.NotepadViewModel
import com.zachklipp.richtext.ui.printing.Printable
import com.zachklipp.richtext.ui.printing.rememberPrintableController
import org.koin.androidx.compose.getViewModel

@Composable
fun NotepadComposeAppRoute() {
    val vm: NotepadViewModel = getViewModel()
    val configuration = LocalConfiguration.current

    val isLightTheme by vm.prefs.isLightTheme.collectAsState()
    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val rtlLayout by vm.prefs.rtlLayout.collectAsState()
    val draftId by vm.savedDraftId.collectAsState()

    LaunchedEffect(Unit) {
        vm.getSavedDraftId()
    }

    if (draftId == null) return

    NotepadTheme(isLightTheme, backgroundColorRes, rtlLayout) {
        NotepadComposeApp(
            vm = vm,
            isMultiPane = configuration.screenWidthDp >= 600,
            initState = when (draftId) {
                -1L -> Empty
                else -> Edit(draftId)
            }
        )
    }
}

@Composable
private fun NotepadComposeApp(
    vm: NotepadViewModel = getViewModel(),
    isMultiPane: Boolean = false,
    initState: NavState = Empty
) {
    /*********************** Data ***********************/

    val notes by vm.noteMetadata.collectAsState(emptyList())
    val note by vm.noteState.collectAsState()
    val text by vm.text.collectAsState()
    val selectedNotes by vm.selectedNotesFlow.collectAsState(emptyMap())

    val isLightTheme by vm.prefs.isLightTheme.collectAsState()
    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val primaryColorRes by vm.prefs.primaryColorRes.collectAsState()
    val secondaryColorRes by vm.prefs.secondaryColorRes.collectAsState()
    val textFontSize by vm.prefs.textFontSize.collectAsState()
    val dateFontSize by vm.prefs.dateFontSize.collectAsState()
    val fontFamily by vm.prefs.fontFamily.collectAsState()
    val showDate by vm.prefs.showDate.collectAsState()
    val markdown by vm.prefs.markdown.collectAsState()
    val directEdit by vm.prefs.directEdit.collectAsState()
    val filenameFormat by vm.prefs.filenameFormat.collectAsState()
    val showDialogs by vm.prefs.showDialogs.collectAsState()
    val rtlLayout by vm.prefs.rtlLayout.collectAsState()
    val firstRunComplete by vm.prefs.firstRunComplete.collectAsState()
    val firstViewComplete by vm.prefs.firstViewComplete.collectAsState()
    val showDoubleTapMessage by vm.prefs.showDoubleTapMessage.collectAsState()

    var navState by rememberSaveable(saver = navStateSaver) { mutableStateOf(initState) }
    var isPrinting by remember { mutableStateOf(false) }
    var isSaveButton by rememberSaveable { mutableStateOf(false) }
    var onSaveComplete by remember { mutableStateOf({ _: Long -> }) }
    var multiSelectEnabled by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMultiDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showFirstRunDialog by rememberSaveable { mutableStateOf(false) }
    var showFirstViewDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    val printController = rememberPrintableController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
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
    val printJobTitle = stringResource(
        id = R.string.document,
        stringResource(id = R.string.app_name)
    )

    var title = ""
    var backButton: @Composable (() -> Unit)? = null
    var actions: @Composable RowScope.() -> Unit = {}
    val content: @Composable BoxScope.() -> Unit
    
    /*********************** Callbacks ***********************/

    val updateNavState: (id: Long) -> Unit = { id ->
        navState = if (directEdit || !isSaveButton) Empty else View(id)
    }
    val onSave: () -> Unit = {
        vm.saveNote(note.id, text, onSaveComplete)
    }
    val onPrint: () -> Unit = {
        isPrinting = true
        printController.print(printJobTitle)
    }
    val onMultiDeleteClick = { showMultiDeleteDialog = true }
    val onDismiss = { showMenu = false }
    val onMoreClick = { showMenu = true }
    val onSaveClick: (
        fromSaveButton: Boolean,
        onComplete: (Long) -> Unit
    ) -> Unit = { fromSaveButton, onComplete ->
        isSaveButton = fromSaveButton
        onSaveComplete = onComplete

        when {
            text.isNotEmpty() && text == note.text -> onComplete(note.id)
            showDialogs -> showSaveDialog = true
            else -> onSave()
        }
    }
    val onDeleteClick: () -> Unit = {
        showDeleteDialog = true
    }
    val onShareClick: (String) -> Unit = {
        onDismiss()
        vm.shareNote(it)
    }
    val onExportClick: (NoteMetadata, String) -> Unit = { metadata, exportedText ->
        onDismiss()
        vm.exportSingleNote(metadata, exportedText, filenameFormat)
    }
    val onPrintClick: () -> Unit = {
        onDismiss()

        vm.showToastIf(text.isEmpty(), R.string.empty_note) {
            when(navState) {
                is Edit -> vm.saveDraft { onPrint() }
                else -> onPrint()
            }
        }
    }
    val onBack = {
        when {
            multiSelectEnabled -> {
                multiSelectEnabled = false
                vm.clearSelectedNotes()
            }
            navState is Edit && text.isNotEmpty() -> {
                onSaveClick(false, updateNavState)
            }
            else -> navState = Empty
        }
    }

    /*********************** Dialogs ***********************/
    
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

    if(showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                showSettingsDialog = false
            }
        )
    }

    if(showDeleteDialog) {
        DeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                vm.deleteNote(id = note.id) {
                    navState = Empty
                }
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    if(showMultiDeleteDialog) {
        DeleteDialog(
            isMultiple = selectedNotes.size > 1,
            onConfirm = {
                showMultiDeleteDialog = false

                val currentNoteDeleted = selectedNotes.getOrDefault(note.id, false)
                vm.deleteSelectedNotes {
                    if (currentNoteDeleted) {
                        navState = Empty
                    }
                }
            },
            onDismiss = {
                showMultiDeleteDialog = false
            }
        )
    }

    if(showSaveDialog) {
        SaveDialog(
            onConfirm = {
                showSaveDialog = false
                onSave()
            },
            onDiscard = {
                showSaveDialog = false
                vm.showToast(R.string.changes_discarded)
                onSaveComplete(note.id)
            },
            onDismiss = {
                showSaveDialog = false
            }
        )
    }

    if(showFirstRunDialog) {
        FirstRunDialog {
            showFirstRunDialog = false
            vm.firstRunComplete()
        }
    }

    if(showFirstViewDialog) {
        FirstViewDialog {
            showFirstViewDialog = false
            vm.firstViewComplete()
        }
    }

    /*********************** UI Logic ***********************/

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isPrinting = false
            vm.deleteDraft()
        }
    }

    LaunchedEffect(navState) {
        vm.setIsEditing(navState is Edit)
    }

    LaunchedEffect(selectedNotes) {
        if (selectedNotes.filterValues { it }.isEmpty()) {
            multiSelectEnabled = false
        }
    }

    BackHandler(
        enabled = multiSelectEnabled || navState != Empty,
        onBack = onBack
    )

    @Composable
    fun NoteListContentShared() = NoteListContent(
        notes = notes,
        selectedNotes = selectedNotes,
        textStyle = textStyle,
        dateStyle = dateStyle,
        showDate = showDate,
        rtlLayout = rtlLayout,
        onNoteLongClick = { id ->
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            multiSelectEnabled = true
            vm.toggleSelectedNote(id)
        }
    ) { id ->
        val updateNavStateNoteList = {
            navState = if (directEdit) Edit(id) else View(id)
        }

        val handleMultiPaneNav = {
            when {
                id != note.id -> vm.clearNote()
                directEdit -> vm.getNote(id)
            }

            updateNavStateNoteList()
        }

        when {
            multiSelectEnabled -> vm.toggleSelectedNote(id)
            isMultiPane -> {
                if (navState is Edit && text.isNotEmpty()) {
                    onSaveClick(false) { handleMultiPaneNav() }
                } else {
                    handleMultiPaneNav()
                }
            }

            else -> updateNavStateNoteList()
        }
    }

    when(val state = navState) {

        /*********************** Note List ***********************/

        Empty -> {
            LaunchedEffect(Unit) {
                vm.clearNote()

                if (!firstRunComplete) {
                    showFirstRunDialog = true
                }
            }

            vm.registerKeyboardShortcuts(
                KeyEvent.KEYCODE_N to {
                    vm.clearSelectedNotes()
                    navState = Edit()
                }
            )

            if (!multiSelectEnabled) {
                title = stringResource(id = R.string.app_name)
                backButton = null
                actions = {
                    MultiSelectButton {
                        vm.showToastIf(notes.isEmpty(), R.string.no_notes_to_select) {
                            multiSelectEnabled = true
                        }
                    }

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
            }

            content = {
                if (isMultiPane) {
                    EmptyDetails()
                } else {
                    NoteListContentShared()
                }
            }
        }

        /*********************** View Note ***********************/

        is View -> {
            LaunchedEffect(state.id) {
                vm.getNote(state.id)

                if (!firstViewComplete) {
                    showFirstViewDialog = true
                }
            }

            vm.registerKeyboardShortcuts(
                KeyEvent.KEYCODE_E to { navState = Edit(state.id) },
                KeyEvent.KEYCODE_D to onDeleteClick,
                KeyEvent.KEYCODE_H to { onShareClick(note.text) }
            )

            if (!multiSelectEnabled) {
                title = note.title
                backButton = { BackButton(onBack) }
                actions = {
                    EditButton { navState = Edit(state.id) }
                    DeleteButton(onDeleteClick)
                    NoteViewEditMenu(
                        showMenu = showMenu,
                        onDismiss = onDismiss,
                        onMoreClick = onMoreClick,
                        onShareClick = { onShareClick(note.text) },
                        onExportClick = { onExportClick(note.metadata, note.text) },
                        onPrintClick = onPrintClick
                    )
                }
            }

            content = {
                Printable(printController) {
                    ViewNoteContent(
                        text = note.text,
                        baseTextStyle = textStyle,
                        markdown = markdown,
                        rtlLayout = rtlLayout,
                        isPrinting = isPrinting,
                        showDoubleTapMessage = showDoubleTapMessage,
                        doubleTapMessageShown = vm::doubleTapMessageShown
                    ) { navState = Edit(note.id) }
                }
            }
        }

        /*********************** Edit Note ***********************/

        is Edit -> {
            LaunchedEffect(state.id) {
                vm.getNote(state.id)
            }

            vm.registerKeyboardShortcuts(
                KeyEvent.KEYCODE_S to { vm.saveNote(note.id, text, vm::getNote) },
                KeyEvent.KEYCODE_D to onDeleteClick,
                KeyEvent.KEYCODE_H to { onShareClick(text) }
            )

            if (!multiSelectEnabled) {
                title = note.title.ifEmpty {
                    stringResource(id = R.string.action_new)
                }
                backButton = { BackButton(onBack) }
                actions = {
                    SaveButton { onSaveClick(true, updateNavState) }
                    DeleteButton(onDeleteClick)
                    NoteViewEditMenu(
                        showMenu = showMenu,
                        onDismiss = onDismiss,
                        onMoreClick = onMoreClick,
                        onShareClick = { onShareClick(text) },
                        onExportClick = { onExportClick(note.metadata.copy(title = title), text) },
                        onPrintClick = onPrintClick
                    )
                }
            }

            content = {
                Printable(printController) {
                    EditNoteContent(
                        text = text,
                        baseTextStyle = textStyle,
                        isLightTheme = isLightTheme,
                        isPrinting = isPrinting,
                        waitForAnimation = note.id == -1L || directEdit,
                        rtlLayout = rtlLayout,
                        onTextChanged = vm::setText
                    )
                }
            }
        }
    }

    /*********************** Multi-Select ***********************/

    if (multiSelectEnabled) {
        title = stringResource(
            id = if (selectedNotes.size == 1) {
                R.string.cab_note_selected
            } else {
                R.string.cab_notes_selected
            },
            selectedNotes.size
        )
        backButton = { BackButton(onBack) }
        actions = {
            SelectAllButton { vm.selectAllNotes(notes) }
            ExportButton {
                vm.showToastIf(selectedNotes.isEmpty(), R.string.no_notes_to_export) {
                    vm.exportNotes(notes, filenameFormat)
                }
            }
            DeleteButton {
                vm.showToastIf(
                    selectedNotes.isEmpty(),
                    R.string.no_notes_to_delete,
                    onMultiDeleteClick
                )
            }
        }
    }

    /*********************** Scaffold ***********************/

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
            AnimatedVisibility(
                visible = navState == Empty && !multiSelectEnabled,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { navState = Edit() },
                    backgroundColor = colorResource(id = R.color.primary_dark),
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
private fun EmptyDetails() {
    Column(
        modifier = Modifier.fillMaxSize(),
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
