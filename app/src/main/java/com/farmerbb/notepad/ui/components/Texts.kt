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

package com.farmerbb.notepad.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.farmerbb.notepad.R

@Composable
fun AppBarText(text: String) {
    Text(
        text = text,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun DialogTitle(@StringRes id: Int) {
    Text(
        text = stringResource(id),
        fontWeight = FontWeight.W500
    )
}

@Composable
fun DialogText(@StringRes id: Int, vararg formatArgs: Any) {
    Text(
        text = stringResource(id, *formatArgs)
    )
}

@Composable
fun DialogButton(onClick: () -> Unit, @StringRes id: Int) {
    TextButton(onClick) {
        Text(
            text = stringResource(id).uppercase(),
            color = colorResource(id = R.color.primary)
        )
    }
}