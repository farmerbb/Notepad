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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.farmerbb.notepad.data.DataMigrator
import com.farmerbb.notepad.ui.routes.NotepadComposeApp
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.callback.FSAFActivityCallbacks
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class NotepadActivity: ComponentActivity(), FSAFActivityCallbacks {
    private val migrator: DataMigrator = get()
    private val fileChooser: FileChooser = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileChooser.setCallbacks(this)

        lifecycleScope.launch {
            migrator.migrate()
            setContent {
                NotepadComposeApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileChooser.removeCallbacks()
    }

    override fun fsafStartActivityForResult(intent: Intent, requestCode: Int) {
        // Override type to plaintext only
        intent.type = "text/plain"

        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileChooser.onActivityResult(requestCode, resultCode, data)
    }
}