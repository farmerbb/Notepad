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
import androidx.room.Room
import com.farmerbb.notepad.android.NotepadViewModel
import com.farmerbb.notepad.data.DataMigrator
import com.farmerbb.notepad.data.NotepadDatabase
import com.farmerbb.notepad.data.NotepadRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notepadModule = module {
    viewModel { NotepadViewModel(androidApplication(), get()) }
    single { provideDatabase(androidApplication()) }
    single { provideDAO(get()) }
    single { NotepadRepository(get()) }
    single { DataMigrator(androidContext(), get()) }
}

private fun provideDatabase(context: Context) =
    Room.databaseBuilder(context, NotepadDatabase::class.java, "notepad").build()

private fun provideDAO(db: NotepadDatabase) = db.getDAO()
