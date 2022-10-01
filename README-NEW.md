![notepad readme](https://user-images.githubusercontent.com/36028424/39695245-83b15cfc-521c-11e8-935c-c4a9cdcfbe90.png)

Notepad has been rewritten from the ground up!  The app's 8-year old codebase has been replaced with one that is fully modernized and aims to follow Android development's best practices for 2022.

This README is meant to track the progress of the rewrite, and also give a general overview of the architecture of the app in comparison to the old one.

## Overview

|                          | Old App                                                                                                                                                                                   | New App                                                                                                            |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| **Language**             | Old, crusty Java with some of the [worst code](https://github.com/farmerbb/Notepad/blob/112/app/src/main/java/com/farmerbb/notepad/activity/MainActivity.java#L160-L172) you'll ever read | 100% [Kotlin](https://kotlinlang.org/) for all new code                                                            |
| **Architecture**         | Spaghetti                                                                                                                                                                                 | MVVM-ish, generally following Google's [recommended app architecture](https://developer.android.com/jetpack/guide) |
| **UI Framework**         | Standard Android views, activities, fragments                                                                                                                                             | [Jetpack Compose](https://developer.android.com/jetpack/compose)                                                   |
| **Data Persistence**     | Raw plaintext on the filesystem. Seriously.                                                                                                                                               | [SQLDelight](https://cashapp.github.io/sqldelight/)                                                                |
| **Preferences**          | SharedPreferences                                                                                                                                                                         | [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)                                  |
| **Dependency Injection** | What is that?                                                                                                                                                                             | [Koin](https://insert-koin.io/)                                                                                    |

## Features

This table will track the features of the app as they are reimplemented in the new codebase.

|                                               | Implemented In New App |
|-----------------------------------------------|------------------------|
| Migrate old notes and preferences             | ✔                      |
| List all notes                                | ✔                      |
| Notes list empty state                        | ✔                      |
| Create new note                               | ✔                      |
| View saved notes                              | ✔                      |
| Edit saved notes                              | ✔                      |
| Share notes                                   | ✔                      |
| Delete note (while viewing/editing)           | ✔                      |
| Settings screen                               | ✔                      |
| About screen                                  | ✔                      |
| Overflow menus                                | ✔                      |
| Dual-pane view for tablets                    | ✔                      |
| Light/dark themes                             | ✔                      |
| Sans serif / serif / monospace fonts          | ✔                      |
| Font sizes                                    | ✔                      |
| Sort notes by date or name                    | ✔                      |
| Show date/time in notes list                  | ✔                      |
| Markdown / HTML support                       | ✔                      |
| Import text files                             | ✔                      |
| Export to filesystem                          | ✔                      |
| Print notes                                   | ✔                      |
| Multi-select notes list (button)              | ✔                      |
| Multi-select notes list (long-press)          | ✔                      |
| Delete notes (from list)                      | ✔                      |
| Export notes (from list)                      | ✔                      |
| Options for exported filename                 | ✔                      |
| Edit notes directly                           | ✔                      |
| Ask before saving                             | ✔                      |
| Save / load drafts                            | ✔                      |
| New Note launcher shortcut                    | ✔                      |
| Share text to Notepad from other apps         | ✔                      |
| Open text files in Notepad from file managers | ✔                      |
| Keyboard shortcuts                            | ✔                      |
| Double-tap to edit                            | ✔                      |
| Probably some other things I forgot           |                        |
