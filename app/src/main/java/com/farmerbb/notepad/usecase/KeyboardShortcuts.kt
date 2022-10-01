/* Copyright 2022 Braden Farmer
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