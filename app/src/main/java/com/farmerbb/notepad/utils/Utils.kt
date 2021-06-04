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

package com.farmerbb.notepad.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.datastore.preferences.preferencesDataStore

fun Context.showToast(
  @StringRes text: Int
) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

val Context.dataStore by preferencesDataStore("settings")