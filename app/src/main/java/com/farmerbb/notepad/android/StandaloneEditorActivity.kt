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
import androidx.annotation.StringRes
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.routes.StandaloneEditor
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import org.koin.androidx.viewmodel.ext.android.viewModel

class StandaloneEditorActivity: ComponentActivity() {
    private val vm: NotepadViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
    }

    private fun handleIntent() {
        // Handle intents

        val isPlainText = intent.type == "text/plain"
        var initialText: String? = null

        when(intent.action) {
            // Intent sent through an external application
            Intent.ACTION_SEND -> {
                if (isPlainText) {
                    initialText = getExternalContent()
                    if (initialText != null) {
                        newNote()
                    } else {
                        showToast(R.string.loading_external_file)
                        finish()
                    }
                } else {
                    showToast(R.string.loading_external_file)
                    finish()
                }

                // Intent sent through Google Now "note to self"
            }
            "com.google.android.gm.action.AUTO_SEND" -> {
                if (isPlainText) {
                    initialText = getExternalContent()
                    if (initialText != null) {
                        try {
                            // Write note to disk
                            val output =
                                openFileOutput(System.currentTimeMillis().toString(), MODE_PRIVATE)
                            output.write(initialText.toByteArray())
                            output.close()

                            // Show toast notification and finish
                            showToast(R.string.note_saved)
                            finish()
                        } catch (e: IOException) {
                            // Show error message as toast if file fails to save
                            showToast(R.string.failed_to_save)
                            finish()
                        }
                    }
                }
            }
            Intent.ACTION_EDIT -> {
                if (isPlainText) {
                    initialText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (initialText != null) {
                        newNote()
                        return
                    }
                    finish()
                } else newNote()
            }
            Intent.ACTION_VIEW -> {
                if (isPlainText) {
                    try {
                        val `in` = contentResolver.openInputStream(intent.data!!)
                        val rd: Reader = InputStreamReader(`in`, "UTF-8")
                        val buffer = CharArray(4096)
                        var len: Int
                        val sb = StringBuilder()
                        while (rd.read(buffer).also { len = it } != -1) {
                            sb.append(buffer, 0, len)
                        }
                        rd.close()
                        `in`!!.close()
                        initialText = sb.toString()
                    } catch (e: Exception) {
                        // show msg error loading data?
                    }
                    if (initialText != null) {
                        newNote()
                        return
                    }
                    finish()
                } else newNote()
            }
            else -> newNote()
        }
    }

    private fun showToast(
        @StringRes text: Int
    ) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun getExternalContent(): String? {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: return text
        return "$subject\n\n$text"
    }

    private fun newNote(initialText: String = "") {
        setContent {
            StandaloneEditor(
                initialText = initialText
            ) { finish() }
        }
    }
}