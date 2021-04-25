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

package com.farmerbb.notepad.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.farmerbb.notepad.data.NoteMigrator
import com.farmerbb.notepad.data.NotepadDAO
import com.farmerbb.notepad.ui.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint class MainActivityCompose: ComponentActivity() {
  @Inject lateinit var migrator: NoteMigrator
  @Inject lateinit var dao: NotepadDAO

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      migrator.migrate()

      setContent {
        val navController = rememberNavController()
        MaterialTheme {
          NavHost(
            navController = navController,
            startDestination = "NoteList"
          ) {
            NoteListRoute(dao, navController)
            NoteViewRoute(dao, navController)
            NoteEditRoute(dao, navController)
          }
        }
      }
    }
  }
}