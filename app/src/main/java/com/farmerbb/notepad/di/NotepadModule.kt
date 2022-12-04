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

package com.farmerbb.notepad.di

import android.content.Context
import com.farmerbb.notepad.Database
import com.farmerbb.notepad.data.NotepadRepository
import com.farmerbb.notepad.model.NoteMetadata
import com.farmerbb.notepad.usecase.artVandelayModule
import com.farmerbb.notepad.usecase.dataMigratorModule
import com.farmerbb.notepad.usecase.keyboardShortcutsModule
import com.farmerbb.notepad.usecase.systemThemeModule
import com.farmerbb.notepad.usecase.toasterModule
import com.farmerbb.notepad.utils.dataStore
import com.farmerbb.notepad.viewmodel.viewModelModule
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.FileManager
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import de.schnettler.datastore.manager.DataStoreManager
import java.util.Date
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notepadModule = module {
    includes(
        viewModelModule,
        dataMigratorModule,
        toasterModule,
        artVandelayModule,
        keyboardShortcutsModule,
        systemThemeModule
    )

    single { provideDatabase(context = androidContext()) }
    single { NotepadRepository(database = get()) }
    single { DataStoreManager(dataStore = androidContext().dataStore) }
    single { FileManager(appContext = androidContext()) }
    single { FileChooser(appContext = androidContext()) }
}

private fun provideDatabase(context: Context) = Database(
    driver = AndroidSqliteDriver(Database.Schema, context, "notepad.db"),
    NoteMetadataAdapter = NoteMetadata.Adapter(dateAdapter = DateAdapter)
)

object DateAdapter: ColumnAdapter<Date, Long> {
    override fun decode(databaseValue: Long) = Date(databaseValue)
    override fun encode(value: Date) = value.time
}