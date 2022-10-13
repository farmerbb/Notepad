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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerbb.notepad.R
import com.farmerbb.notepad.ui.components.RtlTextWrapper
import com.farmerbb.notepad.ui.previews.EditNotePreview
import kotlinx.coroutines.delay

private fun String.toTextFieldValue() = TextFieldValue(
    text = this,
    selection = TextRange(length)
)

@Composable
fun EditNoteContent(
    text: String,
    baseTextStyle: TextStyle = TextStyle(),
    isLightTheme: Boolean = true,
    isPrinting: Boolean = false,
    waitForAnimation: Boolean = false,
    rtlLayout: Boolean = false,
    onTextChanged: (String) -> Unit = {}
) {
    val textStyle = if (isPrinting) {
        baseTextStyle.copy(color = Color.Black)
    } else baseTextStyle

    val focusRequester = remember { FocusRequester() }
    var value by remember { mutableStateOf(text.toTextFieldValue()) }

    LaunchedEffect(text) {
        if (text != value.text) {
            value = text.toTextFieldValue()
        }
    }

    val brush = SolidColor(
        value = when {
            isPrinting -> Color.Transparent
            isLightTheme -> Color.Black
            else -> Color.White
        }
    )

    RtlTextWrapper(text, rtlLayout) {
        BasicTextField(
            value = value,
            onValueChange = {
                value = it
                onTextChanged(it.text)
            },
            textStyle = textStyle,
            cursorBrush = brush,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
                .fillMaxSize()
                .focusRequester(focusRequester)
        )
    }

    if(value.text.isEmpty()) {
        BasicText(
            text = stringResource(id = R.string.edit_text),
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.LightGray
            ),
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        )
    }

    LaunchedEffect(Unit) {
        if (waitForAnimation) {
            delay(200)
        }

        focusRequester.requestFocus()
    }
}

@Preview
@Composable
fun EditNoteContentPreview() = EditNotePreview()