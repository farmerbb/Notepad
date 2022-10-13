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

package com.farmerbb.notepad.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.text.Bidi

@Composable
fun RtlTextWrapper(
    text: String,
    rtlLayout: Boolean,
    content: @Composable () -> Unit
) {
    val flags = if (rtlLayout) {
        Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT
    } else {
        Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT
    }

    val bidi = Bidi(text, flags)

    val layoutDirection = if (text.isBlank()) {
        when (rtlLayout) {
            true -> LayoutDirection.Rtl
            false -> LayoutDirection.Ltr
        }
    } else {
        when (bidi.baseIsLeftToRight()) {
            true -> LayoutDirection.Ltr
            false -> LayoutDirection.Rtl
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        content = content
    )
}