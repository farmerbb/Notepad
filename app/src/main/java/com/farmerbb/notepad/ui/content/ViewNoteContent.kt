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

@file:OptIn(ExperimentalComposeUiApi::class)

package com.farmerbb.notepad.ui.content

import android.content.ActivityNotFoundException
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.components.RtlTextWrapper
import com.farmerbb.notepad.ui.previews.ViewNotePreview
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.RichTextThemeIntegration
import com.linkifytext.LinkifyText

@Composable
fun ViewNoteContent(
    text: String,
    baseTextStyle: TextStyle = TextStyle(),
    markdown: Boolean = false,
    rtlLayout: Boolean = false,
    isPrinting: Boolean = false,
    showDoubleTapMessage: Boolean = false,
    doubleTapMessageShown: () -> Unit = {},
    onDoubleTap: () -> Unit = {}
) {
    val textStyle = if (isPrinting) {
        baseTextStyle.copy(color = Color.Black)
    } else baseTextStyle

    var doubleTapTime by remember { mutableStateOf(0L) }
    var lastOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val radius = with(LocalDensity.current) { 24.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val now = System.currentTimeMillis()
                    val offset = Offset(motionEvent.x, motionEvent.y)
                    val rect = Rect(center = lastOffset, radius = radius)

                    when {
                        doubleTapTime > now && rect.contains(offset) -> onDoubleTap()
                        showDoubleTapMessage -> doubleTapMessageShown()
                    }

                    doubleTapTime = now + 200
                    lastOffset = offset
                }

                false
            }
    ) {
        Box(
            modifier = if (isPrinting) Modifier else Modifier
                .verticalScroll(state = rememberScrollState())
        ) {
            val modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
                .fillMaxWidth()

            RtlTextWrapper(text, rtlLayout) {
                SelectionContainer {
                    if (markdown) {
                        val localTextStyle = compositionLocalOf {
                            textStyle.copy(color = Color.Unspecified)
                        }
                        val localContentColor = compositionLocalOf {
                            textStyle.color
                        }

                        RichTextThemeIntegration(
                            textStyle = { localTextStyle.current },
                            contentColor = { localContentColor.current },
                            ProvideTextStyle = { textStyle, content ->
                                CompositionLocalProvider(
                                    localTextStyle provides textStyle,
                                    content = content
                                )
                            },
                            ProvideContentColor = { color, content ->
                                CompositionLocalProvider(
                                    localContentColor provides color,
                                    content = content
                                )
                            }
                        ) {
                            RichText(modifier = modifier) {
                                val uriHandler = LocalUriHandler.current

                                Markdown(
                                    // Replace markdown images with links
                                    text.replace(Regex("!\\[([^\\[]*)](\\(.*\\))")) {
                                        it.value.replaceFirst("![", "[")
                                    }
                                ) { uri ->
                                    val sanitizedUri = when {
                                        uri.startsWith("http://") -> uri
                                        uri.startsWith("https://") -> uri
                                        else -> "http://$uri"
                                    }

                                    try {
                                        uriHandler.openUri(sanitizedUri)
                                    } catch (ignored: ActivityNotFoundException) {}
                                }
                            }
                        }
                    } else {
                        LinkifyText(
                            text = text,
                            style = textStyle,
                            linkColor = colorResource(id = R.color.primary),
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ViewNoteContentPreview() = ViewNotePreview()