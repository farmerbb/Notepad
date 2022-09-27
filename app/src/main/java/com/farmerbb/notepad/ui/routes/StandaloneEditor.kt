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
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.ui.content.EditNoteContent
import com.farmerbb.notepad.ui.widgets.AppBarText
import com.farmerbb.notepad.ui.widgets.BackButton
import com.farmerbb.notepad.ui.widgets.DeleteButton
import com.farmerbb.notepad.ui.widgets.DeleteDialog
import com.farmerbb.notepad.ui.widgets.SaveButton
import com.farmerbb.notepad.ui.widgets.SaveDialog
import com.farmerbb.notepad.ui.widgets.StandaloneEditorMenu
import org.koin.androidx.compose.getViewModel

@Composable
fun StandaloneEditor(
    vm: NotepadViewModel = getViewModel(),
    initialText: String,
    onExit: () -> Unit
) {
    val backgroundColorRes by vm.prefs.backgroundColorRes.collectAsState()
    val primaryColorRes by vm.prefs.primaryColorRes.collectAsState()
    val textFontSize by vm.prefs.textFontSize.collectAsState()
    val fontFamily by vm.prefs.fontFamily.collectAsState()
    val showDialogs by vm.prefs.showDialogs.collectAsState()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
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

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var textToSave: String by rememberSaveable { mutableStateOf("") }
    val onSave: (String) -> Unit = { text ->
        vm.saveNote(-1L, text) { onExit() }
    }

    if(showSaveDialog) {
        SaveDialog(
            onConfirm = {
                showSaveDialog = false
                onSave(textToSave)
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

    var showMenu by rememberSaveable { mutableStateOf(false) }
    val onDismiss = { showMenu = false }
    val onMoreClick = { showMenu = true }

    val onSaveClick: (String) -> Unit = { text ->
        if (showDialogs) {
            textToSave = text
            showSaveDialog = true
        } else {
            onSave(text)
        }
    }

    val onDeleteClick: () -> Unit = {
        showDeleteDialog = true
    }
    val onShareClick: (String) -> Unit = {
        onDismiss()
        vm.shareNote(it)
    }
    val onBack = {
        // TODO onSaveClick(id, text)
    }

    BackHandler(onBack = onBack)

    val textStyle = TextStyle(
        color = colorResource(id = primaryColorRes),
        fontSize = textFontSize,
        fontFamily = fontFamily
    )

    var text by rememberSaveable { mutableStateOf(initialText) }

    Scaffold(
        backgroundColor = colorResource(id = backgroundColorRes),
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onBack) },
                title = { AppBarText(stringResource(id = R.string.action_new)) },
                backgroundColor = colorResource(id = R.color.primary),
                actions = {
                    SaveButton { onSaveClick(text) }
                    DeleteButton { onDeleteClick() }
                    StandaloneEditorMenu(
                        showMenu = showMenu,
                        onDismiss = onDismiss,
                        onMoreClick = onMoreClick,
                        onShareClick = { onShareClick(text) }
                    )
                }
            )
        },
        content = {
            EditNoteContent(
                text = initialText,
                baseTextStyle = textStyle
            ) { text = it }
        }
    )
}