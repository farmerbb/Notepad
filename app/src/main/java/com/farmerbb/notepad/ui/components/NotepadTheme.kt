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

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.LayoutDirection
import com.farmerbb.notepad.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun NotepadTheme(
    isLightTheme: Boolean,
    backgroundColorRes: Int,
    rtlLayout: Boolean,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    val layoutDirection = when (rtlLayout) {
        true -> LayoutDirection.Rtl
        false -> LayoutDirection.Ltr
    }

    val colorPrimary = colorResource(id = R.color.primary)
    val textSelectionColors = TextSelectionColors(
        handleColor = colorPrimary,
        backgroundColor = colorPrimary.copy(alpha = 0.4f)
    )

    MaterialTheme(
        colors = if (isLightTheme) lightColors() else darkColors()
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalTextSelectionColors provides textSelectionColors,
            content = content
        )
    }

    val navbarColor = colorResource(id = backgroundColorRes)

    LaunchedEffect(isLightTheme) {
        systemUiController.setNavigationBarColor(color = navbarColor)
    }
}