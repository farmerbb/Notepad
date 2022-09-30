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
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

interface Toaster {
    suspend fun toast(@StringRes text: Int)

    suspend fun toastIf(
        condition: Boolean,
        @StringRes text: Int,
        block: () -> Unit
    )
}

private class ToasterImpl(
    private val context: Context
): Toaster {
    override suspend fun toast(@StringRes text: Int) = withContext(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override suspend fun toastIf(
        condition: Boolean,
        @StringRes text: Int,
        block: () -> Unit
    ) = if (condition) toast(text) else block()
}

val toasterModule = module {
    single<Toaster> {
        ToasterImpl(androidContext())
    }
}