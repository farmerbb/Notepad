package com.farmerbb.notepad.models

import android.net.Uri
import com.github.k1rakishou.fsaf.manager.base_directory.BaseDirectory

class ExportedNotesDirectory(
    private val uri: Uri?,
) : BaseDirectory() {
    override fun getDirUri() = uri
    override fun getDirFile() = null
    override fun currentActiveBaseDirType() = ActiveBaseDirType.SafBaseDir
}
