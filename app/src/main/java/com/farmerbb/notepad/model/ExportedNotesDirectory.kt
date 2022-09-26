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

package com.farmerbb.notepad.model

import android.net.Uri
import com.github.k1rakishou.fsaf.manager.base_directory.BaseDirectory

class ExportedNotesDirectory(
    private val uri: Uri?,
) : BaseDirectory() {
    override fun getDirUri() = uri
    override fun getDirFile() = null
    override fun currentActiveBaseDirType() = ActiveBaseDirType.SafBaseDir
}
