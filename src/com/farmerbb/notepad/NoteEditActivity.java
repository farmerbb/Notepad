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

package com.farmerbb.notepad;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;

public class NoteEditActivity extends Activity implements 
BackButtonDialogFragment.Listener, 
DeleteDialogFragment.Listener, 
SaveButtonDialogFragment.Listener, 
NoteEditFragment.Listener {

String external;

    @Override
    public boolean isShareIntent() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        // Set action bar elevation
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.action_bar_elevation));

        if(!(getFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment)) {
            // Handle intents
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            // Intent sent through an external application
            if(Intent.ACTION_SEND.equals(action) && type != null) {
                if("text/plain".equals(type)) {
                    external = intent.getStringExtra(Intent.EXTRA_TEXT);

                    Bundle bundle = new Bundle();
                    bundle.putString("filename", "new");

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

                    // Add NoteEditFragment
                    getFragmentManager()
                            .beginTransaction()
                            .add(R.id.noteViewEdit, fragment, "NoteEditFragment")
                            .commit();
                } else {
                    showToast(R.string.loading_external_file);
                    finish();
                }

            // Intent sent through Google Now "note to self"
            } else if("com.google.android.gm.action.AUTO_SEND".equals(action) && type != null) {
                if("text/plain".equals(type)) {
                    external = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if(external != null) {
                        try {
                            // Write note to disk
                            FileOutputStream output = openFileOutput(String.valueOf(System.currentTimeMillis()), Context.MODE_PRIVATE);
                            output.write(external.getBytes());
                            output.close();

                            // Show toast notification and finish
                            showToast(R.string.note_saved);
                            finish();
                        } catch (IOException e) {
                            // Show error message as toast if file fails to save
                            showToast(R.string.failed_to_save);
                            finish();
                        }
                    }
                }
            } else
                finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set text in EditView
        if(external != null) {
            EditText noteContents = (EditText) findViewById(R.id.editText1);
            noteContents.setText(external);
            noteContents.setSelection(external.length(), external.length());
        }
    }

    // Keyboard shortcuts
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        super.dispatchKeyShortcutEvent(event);
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
            fragment.dispatchKeyShortcutEvent(event.getKeyCode());

            return true;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public void onBackPressed() {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackPressed(null);
    }

    @Override
    public void onBackDialogNegativeClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogNegativeClick(null);
    }

    @Override
    public void onBackDialogPositiveClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogPositiveClick(null);
    }

    @Override
    public void onDeleteDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onDeleteDialogPositiveClick();
    }

    @Override
    public void onSaveDialogNegativeClick() {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogNegativeClick();
    }

    @Override
    public void onSaveDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogPositiveClick();
    }

    @Override
    public void showBackButtonDialog(String filename) {
        Bundle bundle = new Bundle();
        bundle.putString("filename", filename);

        DialogFragment backFragment = new BackButtonDialogFragment();
        backFragment.setArguments(bundle);
        backFragment.show(getFragmentManager(), "back");
    }

    @Override
    public void showDeleteDialog() {
        DialogFragment deleteFragment = new DeleteDialogFragment();
        deleteFragment.show(getFragmentManager(), "delete");
    }

    @Override
    public void showSaveButtonDialog() {
        DialogFragment saveFragment = new SaveButtonDialogFragment();
        saveFragment.show(getFragmentManager(), "save");
    }

    // Method used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(this, getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public String loadNote(String filename) {
        return null;
    }

    @Override
    public String loadNoteTitle(String filename) {
        return null;
    }
}
