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

package com.farmerbb.notepad.android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.routes.StandaloneEditorRoute
import com.farmerbb.notepad.viewmodel.NotepadViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StandaloneEditorActivity: ComponentActivity() {
    private val vm: NotepadViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when(intent.action) {
            Intent.ACTION_MAIN -> openEditor()

            Intent.ACTION_SEND -> checkPlainText {
                getExternalContent()?.let(::openEditor) ?: externalContentFailed()
            }

            Intent.ACTION_EDIT -> checkPlainText {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let(::openEditor) ?: externalContentFailed()
            }

            Intent.ACTION_VIEW -> checkPlainText {
                vm.loadFileFromIntent(intent) { file ->
                    file?.let(::openEditor) ?: externalContentFailed()
                }
            }

            else -> externalContentFailed()
        }
    }

    private fun checkPlainText(onSuccess: () -> Unit) =
        if (intent.type == "text/plain") onSuccess() else externalContentFailed()

    private fun getExternalContent(): String? {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: return text
        return "$subject\n\n$text".trim()
    }

    private fun externalContentFailed() = run {
        Toast.makeText(this, R.string.loading_external_file, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun openEditor(initialText: String = "") {
        setContent {
            StandaloneEditorRoute(
                initialText = initialText
            ) { finish() }
        }
    }
}