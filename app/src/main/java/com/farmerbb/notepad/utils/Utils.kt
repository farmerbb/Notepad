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
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import androidx.datastore.preferences.preferencesDataStore
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.model.ReleaseType
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

val Context.dataStore by preferencesDataStore("settings")

val buildYear: Int get() {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Denver")).apply {
        timeInMillis = BuildConfig.TIMESTAMP
    }

    return calendar.get(Calendar.YEAR)
}

@Suppress("Deprecation")
val Context.isPlayStoreInstalled get() = try {
    packageManager.getPackageInfo("com.android.vending", 0)
    true
} catch(e: PackageManager.NameNotFoundException) {
    false
}

val Context.releaseType: ReleaseType
    @Suppress("Deprecation", "PackageManagerGetSignatures")
    get() {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        for(enum in ReleaseType.values()) {
            try {
                val enumSignature = Signature(Base64.decode(enum.signature, Base64.DEFAULT))
                for(signature in info.signatures) {
                    if(signature == enumSignature) return enum
                }
            } catch (ignored: Exception) {}
        }

        return ReleaseType.Unknown
    }

val Date.noteListFormat: String get() = DateFormat
    .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    .format(this)