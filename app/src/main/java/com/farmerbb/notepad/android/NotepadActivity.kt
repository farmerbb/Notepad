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

@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.farmerbb.notepad.android

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.farmerbb.notepad.ui.routes.NotepadComposeAppRoute
import com.farmerbb.notepad.viewmodel.NotepadViewModel
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.callback.FSAFActivityCallbacks
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotepadActivity: ComponentActivity(), FSAFActivityCallbacks {
    private val vm: NotepadViewModel by viewModel()
    private val fileChooser: FileChooser = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileChooser.setCallbacks(this)

        vm.migrateData {
            setContent {
                NotepadComposeAppRoute()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.deleteDraft()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            vm.saveDraft()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileChooser.removeCallbacks()
    }

    override fun fsafStartActivityForResult(intent: Intent, requestCode: Int) {
        when(intent.action) {
            Intent.ACTION_OPEN_DOCUMENT -> intent.type = "text/plain"
            Intent.ACTION_OPEN_DOCUMENT_TREE -> intent.removeExtra(Intent.EXTRA_LOCAL_ONLY)
        }

        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileChooser.onActivityResult(requestCode, resultCode, data)
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
        return if (event.action == KeyEvent.ACTION_DOWN && event.isCtrlPressed) {
            vm.keyboardShortcutPressed(event.keyCode)
        } else {
            super.dispatchKeyShortcutEvent(event)
        }
    }
}