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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.util.Base64
import androidx.datastore.preferences.preferencesDataStore
import com.farmerbb.notepad.BuildConfig
import com.farmerbb.notepad.R
import com.farmerbb.notepad.model.ReleaseType

fun Context.checkForUpdates() {
    val id = BuildConfig.APPLICATION_ID
    val url = when(releaseType) {
        ReleaseType.PlayStore -> {
            if(isPlayStoreInstalled)
                "https://play.google.com/store/apps/details?id=$id"
            else
                "https://github.com/farmerbb/Notepad/releases"
        }
        ReleaseType.Amazon -> "https://www.amazon.com/gp/mas/dl/android?p=$id"
        ReleaseType.FDroid -> "https://f-droid.org/repository/browse/?fdid=$id"
        ReleaseType.Unknown -> ""
    }

    try {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (ignored: ActivityNotFoundException) {}
}

fun Context.showShareSheet(text: String) = try {
    startActivity(
        Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            getString(R.string.send_to)
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
} catch (e: Exception) {
    e.printStackTrace()
}

val Context.dataStore by preferencesDataStore("settings")

@Suppress("Deprecation")
private val Context.isPlayStoreInstalled get() = try {
    packageManager.getPackageInfo("com.android.vending", 0)
    true
} catch(e: PackageManager.NameNotFoundException) {
    false
}

private val Context.releaseType: ReleaseType
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