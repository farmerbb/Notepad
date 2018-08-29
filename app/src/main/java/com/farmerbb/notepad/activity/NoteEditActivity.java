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

package com.farmerbb.notepad.activity;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.farmerbb.notepad.R;
import com.farmerbb.notepad.fragment.NoteEditFragment;
import com.farmerbb.notepad.fragment.dialog.BackButtonDialogFragment;
import com.farmerbb.notepad.fragment.dialog.DeleteDialogFragment;
import com.farmerbb.notepad.fragment.dialog.SaveButtonDialogFragment;

import java.io.FileOutputStream;
import java.io.IOException;

public class NoteEditActivity extends NotepadBaseActivity implements
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

        // Apply theme
        SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String theme = pref.getString("theme", "light-sans");

        LinearLayout noteViewEdit = findViewById(R.id.noteViewEdit);

        if(theme.contains("light"))
            noteViewEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background));

        if(theme.contains("dark"))
            noteViewEdit.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background_dark));

        // Set action bar elevation
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.action_bar_elevation));

        if(!(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment)) {
            // Handle intents
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            // Intent sent through an external application
            if(Intent.ACTION_SEND.equals(action) && type != null) {
                if("text/plain".equals(type)) {
                    external = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if(external != null) {
                        newNote();
                    } else {
                        showToast(R.string.loading_external_file);
                        finish();
                    }
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
            } else if(Intent.ACTION_EDIT.equals(action) && "text/plain".equals(type)) {
                external = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(external != null) {
                    newNote();
                    return;
                }
                finish();
            } else
                newNote();
        }
    }

    private void newNote() {
        Bundle bundle = new Bundle();
        bundle.putString("filename", "new");

        Fragment fragment = new NoteEditFragment();
        fragment.setArguments(bundle);

        // Add NoteEditFragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.noteViewEdit, fragment, "NoteEditFragment")
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set text in EditView
        if(external != null) {
            EditText noteContents = findViewById(R.id.editText1);
            noteContents.setText(external);
            noteContents.setSelection(external.length(), external.length());
        }
    }

    // Keyboard shortcuts
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        super.dispatchKeyShortcutEvent(event);
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
            fragment.dispatchKeyShortcutEvent(event.getKeyCode());

            return true;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public void onBackPressed() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackPressed(null);
    }

    @Override
    public void onBackDialogNegativeClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogNegativeClick(null);
    }

    @Override
    public void onBackDialogPositiveClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogPositiveClick(null);
    }

    @Override
    public void onDeleteDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onDeleteDialogPositiveClick();
    }

    @Override
    public void onSaveDialogNegativeClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogNegativeClick();
    }

    @Override
    public void onSaveDialogPositiveClick() {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onSaveDialogPositiveClick();
    }

    @Override
    public void showBackButtonDialog(String filename) {
        Bundle bundle = new Bundle();
        bundle.putString("filename", filename);

        DialogFragment backFragment = new BackButtonDialogFragment();
        backFragment.setArguments(bundle);
        backFragment.show(getSupportFragmentManager(), "back");
    }

    @Override
    public void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt("dialog_title", R.string.dialog_delete_button_title);

        DialogFragment deleteFragment = new DeleteDialogFragment();
        deleteFragment.setArguments(bundle);
        deleteFragment.show(getSupportFragmentManager(), "delete");
    }

    @Override
    public void showSaveButtonDialog() {
        DialogFragment saveFragment = new SaveButtonDialogFragment();
        saveFragment.show(getSupportFragmentManager(), "save");
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

    @Override
    public void exportNote(String filename) {}

    @Override
    public void printNote(String contentToPrint) {}
}
