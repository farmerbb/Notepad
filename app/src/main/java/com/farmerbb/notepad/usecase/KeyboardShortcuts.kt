package com.farmerbb.notepad.usecase

import org.koin.dsl.module

interface KeyboardShortcuts {
    fun pressed(keyCode: Int): Boolean
    fun register(vararg mappings: Pair<Int, () -> Unit>)
}

private class KeyboardShortcutsImpl: KeyboardShortcuts {
    private val registeredKeyboardShortcuts = mutableMapOf<Int, () -> Unit>()

    override fun pressed(keyCode: Int) =
        registeredKeyboardShortcuts[keyCode]?.let { action ->
            action()
            true
        } ?: false

    override fun register(vararg mappings: Pair<Int, () -> Unit>) =
        with(registeredKeyboardShortcuts) {
            clear()
            putAll(mappings)
        }
}

val keyboardShortcutsModule = module {
    single<KeyboardShortcuts> {
        KeyboardShortcutsImpl()
    }
}