package com.farmerbb.notepad.ui.routes

import androidx.activity.compose.BackHandler
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.components.AppBarText
import com.farmerbb.notepad.ui.components.BackButton
import com.farmerbb.notepad.ui.components.DeleteButton
import com.farmerbb.notepad.ui.components.DeleteDialog
import com.farmerbb.notepad.ui.components.NotepadTheme
import com.farmerbb.notepad.ui.components.SaveButton
import com.farmerbb.notepad.ui.components.SaveDialog
import com.farmerbb.notepad.ui.components.StandaloneEditorMenu
import com.farmerbb.notepad.ui.content.EditNoteContent
import com.farmerbb.notepad.viewmodel.NotepadViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun StandaloneEditorRoute(
    initialText: String,
    onExit: () -> Unit
) {
    val vm: NotepadViewModel = getViewModel()
    val isLightTheme by vm.prefs.isLightTheme.collectAsState()
    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val rtlLayout by vm.prefs.rtlLayout.collectAsState()

    NotepadTheme(isLightTheme, backgroundColorRes, rtlLayout) {
        StandaloneEditor(
            vm = vm,
            initialText = initialText,
            onExit = onExit
        )
    }
}

@Composable
private fun StandaloneEditor(
    vm: NotepadViewModel = getViewModel(),
    initialText: String,
    onExit: () -> Unit
) {
    /*********************** Data ***********************/

    val isLightTheme by vm.prefs.isLightTheme.collectAsState()
    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val primaryColorRes by vm.prefs.primaryColorRes.collectAsState()
    val textFontSize by vm.prefs.textFontSize.collectAsState()
    val fontFamily by vm.prefs.fontFamily.collectAsState()
    val showDialogs by vm.prefs.showDialogs.collectAsState()
    val rtlLayout by vm.prefs.rtlLayout.collectAsState()

    var text: String by rememberSaveable { mutableStateOf(initialText) }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    val textStyle = TextStyle(
        color = colorResource(id = primaryColorRes),
        fontSize = textFontSize,
        fontFamily = fontFamily
    )

    /*********************** Callbacks ***********************/

    val onSave = {
        vm.saveNote(-1L, text) { onExit() }
    }

    val onDismiss = { showMenu = false }
    val onMoreClick = { showMenu = true }
    val onSaveClick: () -> Unit = {
        if (showDialogs) {
            showSaveDialog = true
        } else {
            onSave()
        }
    }
    val onDeleteClick: () -> Unit = {
        showDeleteDialog = true
    }
    val onShareClick: () -> Unit = {
        onDismiss()
        vm.shareNote(text)
    }
    val onBack: () -> Unit = {
        if (text.isNotEmpty()) onSaveClick() else onExit()
    }

    /*********************** Dialogs ***********************/

    if (showDeleteDialog) {
        DeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                onExit()
            },
            onDismiss = {
                showDeleteDialog = false
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
                onExit()
            },
            onDismiss = {
                showSaveDialog = false
            }
        )
    }

    /*********************** UI Logic ***********************/

    BackHandler(onBack = onBack)

    Scaffold(
        backgroundColor = colorResource(id = backgroundColorRes),
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onBack) },
                title = { AppBarText(stringResource(id = R.string.action_new)) },
                backgroundColor = colorResource(id = R.color.primary),
                actions = {
                    SaveButton(onSaveClick)
                    DeleteButton(onDeleteClick)
                    StandaloneEditorMenu(
                        showMenu = showMenu,
                        onDismiss = onDismiss,
                        onMoreClick = onMoreClick,
                        onShareClick = onShareClick
                    )
                }
            )
        },
        content = {
            EditNoteContent(
                text = text,
                baseTextStyle = textStyle,
                isLightTheme = isLightTheme,
                rtlLayout = rtlLayout
            ) { text = it }
        }
    )
}