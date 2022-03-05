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

package com.farmerbb.notepad.ui.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farmerbb.notepad.ui.previews.ViewNotePreview
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.RichTextThemeIntegration
import com.linkifytext.LinkifyText

@Composable
fun ViewNoteContent(
    text: String,
    textStyle: TextStyle = TextStyle(),
    markdown: Boolean = false
) {
    Box(
        modifier = Modifier.verticalScroll(
            state = rememberScrollState()
        )
    ) {
        val modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
            .fillMaxWidth()
            .fillMaxHeight()

        SelectionContainer {
            if(markdown) {
                RichTextThemeIntegration {
                    ProvideTextStyle(value = textStyle) {
                        RichText(modifier = modifier) {
                            Markdown(text)
                        }
                    }
                }
            } else {
                LinkifyText(
                    text = text,
                    style = textStyle,
                    modifier = modifier
                )
            }
        }
    }
}

@Preview
@Composable
fun ViewNoteContentPreview() = ViewNotePreview()