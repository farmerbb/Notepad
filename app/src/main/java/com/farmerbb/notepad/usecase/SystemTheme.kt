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

package com.farmerbb.notepad.usecase

import android.content.Context
import android.content.res.Configuration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

enum class ColorScheme(val stringValue: String) {
    Light("light"),
    Dark("dark")
}

interface SystemTheme {
    val colorScheme: ColorScheme
}

private class SystemThemeImpl(
    val context: Context
): SystemTheme {
    override val colorScheme: ColorScheme get() {
        val configuration = context.resources.configuration
        return when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> ColorScheme.Dark
            else -> ColorScheme.Light
        }
    }
}

val systemThemeModule = module {
    single<SystemTheme> {
        SystemThemeImpl(androidContext())
    }
}