/* Copyright 2014 Braden Farmer
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

// NOTE: This BroadcastReceiver is implemented specifically for the Notepad Plugin for Android Wear, which is not open-source.
// To use, send a broadcast with action "com.farmerbb.notepad.RECEIVE_NOTE" and a byte-array extra "note".
 
package com.farmerbb.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.FileOutputStream;
import java.io.IOException;

public class WearPluginReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // Write note to disk
            FileOutputStream output = context.openFileOutput(String.valueOf(System.currentTimeMillis()), Context.MODE_PRIVATE);
            output.write(intent.getByteArrayExtra("note"));
            output.close();
        } catch (IOException e) {}

        // Send broadcast to NoteListFragment to refresh list of notes
        Intent listNotesIntent = new Intent();
        listNotesIntent.setAction("com.farmerbb.notepad.LIST_NOTES");
        context.sendBroadcast(listNotesIntent);
    }
}
