![notepad readme](https://user-images.githubusercontent.com/36028424/39695245-83b15cfc-521c-11e8-935c-c4a9cdcfbe90.png)

Notepad is currently being rewritten from the ground up!  The app's 7-year old codebase is being replaced with one that is fully modernized and aims to follow Android development's best practices for 2021.

This README is meant to track the progress of the rewrite, and also give a general overview of the architecture of the app in comparison to the old one.

## Overview

|                          | Old App                                                                                                                                                                                          | New App                                                                                                            |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| **Language**             | Old, crusty Java with some of the [worst code](https://github.com/farmerbb/Notepad/blob/master/app/src/main/java/com/farmerbb/notepad/old/activity/MainActivity.java#L173-L185) you'll ever read | 100% [Kotlin](https://kotlinlang.org/) for all new code                                                            |
| **Architecture**         | Spaghetti                                                                                                                                                                                        | MVVM-ish, generally following Google's [recommended app architecture](https://developer.android.com/jetpack/guide) |
| **UI Framework**         | Standard Android views, activities, fragments                                                                                                                                                    | [Jetpack Compose](https://developer.android.com/jetpack/compose)                                                   |
| **Data Persistence**     | Raw plaintext on the filesystem. Seriously.                                                                                                                                                      | [Room](https://developer.android.com/training/data-storage/room)                                                   |
| **Preferences**          | SharedPreferences                                                                                                                                                                                | [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)                                  |
| **Dependency Injection** | What is that?                                                                                                                                                                                    | [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)                            |                                                                                                                 |

## Features

This table will track the features of the app as they are reimplemented in the new codebase.

|                                       | Implemented In New App |
|---------------------------------------|------------------------|
| Migrate old notes and preferences     | ✔                      |
| List all notes                        | ✔                      |
| Notes list empty state                | ✔                      |
| Create new note                       | ✔                      |
| View saved notes                      | ✔                      |
| Edit saved notes                      | ✔                      |
| Share notes                           | ✔                      |
| Delete note (while viewing/editing)   | ✔                      |
| Settings screen                       | ✔                      |
| About screen                          | ✔                      |
| Overflow menus                        | ✔                      |
| Delete notes (from list)              |                        |
| Print notes                           |                        |
| Import text files                     |                        |
| Export to filesystem                  |                        |
| Double-tap to edit                    |                        |
| Multi-select notes list (long-press)  |                        |
| Multi-select notes list (button)      |                        |
| Light/dark themes                     |                        |
| Sans serif / serif / monospace fonts  |                        |
| Font sizes                            |                        |
| Sort notes by date or name            |                        |
| Options for exported filename         |                        |
| Ask before saving                     |                        |
| Show date/time in notes list          |                        |
| Edit notes directly                   |                        |
| Markdown / HTML support               |                        |
| Dual-pane view for tablets            |                        |
| Save / load drafts                    |                        |
| Keyboard shortcuts                    |                        |
| Share text to Notepad from other apps |                        |
| Google Assistant "Note to self"       |                        |
| Probably some other things I forgot   |                        |

## How do I try out the new app?

[Follow the white rabbit.](https://i.imgflip.com/57yweh.jpg)
