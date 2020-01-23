/* Copyright 2014 Braden Farmer
 * Copyright 2015 Sean93Park
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.farmerbb.notepad.R;
import com.farmerbb.notepad.fragment.NoteEditFragment;
import com.farmerbb.notepad.fragment.NoteListFragment;
import com.farmerbb.notepad.fragment.NoteViewFragment;
import com.farmerbb.notepad.fragment.WelcomeFragment;
import com.farmerbb.notepad.fragment.dialog.BackButtonDialogFragment;
import com.farmerbb.notepad.fragment.dialog.DeleteDialogFragment;
import com.farmerbb.notepad.fragment.dialog.FirstRunDialogFragment;
import com.farmerbb.notepad.fragment.dialog.SaveButtonDialogFragment;
import com.farmerbb.notepad.util.WebViewInitState;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import us.feras.mdv.MarkdownView;

public class MainActivity extends NotepadBaseActivity implements
BackButtonDialogFragment.Listener, 
DeleteDialogFragment.Listener, 
SaveButtonDialogFragment.Listener,
FirstRunDialogFragment.Listener,
NoteListFragment.Listener,
NoteEditFragment.Listener, 
NoteViewFragment.Listener {

    Object[] filesToExport;
    Object[] filesToDelete;
    int fileBeingExported;
    boolean successful = true;

    private ArrayList<String> cab = new ArrayList<>();
    private boolean inCabMode = false;

    public static final int IMPORT = 42;
    public static final int EXPORT = 43;
    public static final int EXPORT_TREE = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set action bar elevation
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.action_bar_elevation));
        }

        // Show dialog if this is the user's first time running Notepad
        SharedPreferences prefMain = getPreferences(Context.MODE_PRIVATE);
        if(prefMain.getInt("first-run", 0) == 0) {
            // Show welcome dialog
            if(getSupportFragmentManager().findFragmentByTag("firstrunfragment") == null) {
                DialogFragment firstRun = new FirstRunDialogFragment();
                firstRun.show(getSupportFragmentManager(), "firstrunfragment");
            }
        } else {
            // The following code is only present to support existing users of Notepad on Google Play
            // and can be removed if using this source code for a different app

            // Convert old preferences to new ones
            SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
            if(prefMain.getInt("sort-by", -1) == 0) {
                SharedPreferences.Editor editor = pref.edit();
                SharedPreferences.Editor editorMain = prefMain.edit();

                editor.putString("sort_by", "date");
                editorMain.putInt("sort-by", -1);

                editor.apply();
                editorMain.apply();
            } else if(prefMain.getInt("sort-by", -1) == 1) {
                SharedPreferences.Editor editor = pref.edit();
                SharedPreferences.Editor editorMain = prefMain.edit();

                editor.putString("sort_by", "name");
                editorMain.putInt("sort-by", -1);

                editor.apply();
                editorMain.apply();
            }

            if(pref.getString("font_size", "null").equals("null")) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("font_size", "large");
                editor.apply();
            }


            // Rename any saved drafts from 1.3.x
            File oldDraft = new File(getFilesDir() + File.separator + "draft");
            File newDraft = new File(getFilesDir() + File.separator + String.valueOf(System.currentTimeMillis()));

            if(oldDraft.exists())
                oldDraft.renameTo(newDraft);
        }

        // Begin a new FragmentTransaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // This fragment shows NoteListFragment as a sidebar (only seen in tablet mode landscape)
        if(!(getSupportFragmentManager().findFragmentById(R.id.noteList) instanceof NoteListFragment))
            transaction.replace(R.id.noteList, new NoteListFragment(), "NoteListFragment");

        // This fragment shows NoteListFragment in the main screen area (only seen on phones and tablet mode portrait),
        // but only if it doesn't already contain NoteViewFragment or NoteEditFragment.
        // If NoteListFragment is already showing in the sidebar, use WelcomeFragment instead
        if(!((getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment)
           || (getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteViewFragment))) {
            if((getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) == null
               && findViewById(R.id.layoutMain).getTag().equals("main-layout-large"))
               || ((getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteListFragment)
               && findViewById(R.id.layoutMain).getTag().equals("main-layout-large")))
                    transaction.replace(R.id.noteViewEdit, new WelcomeFragment(), "NoteListFragment");
            else if(findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                transaction.replace(R.id.noteViewEdit, new NoteListFragment(), "NoteListFragment");
        }

        // Commit fragment transaction
        transaction.commit();
        
        if(savedInstanceState != null) {
            ArrayList<String> filesToExportList = savedInstanceState.getStringArrayList("files_to_export");
            if(filesToExportList != null)
                filesToExport = filesToExportList.toArray();

            ArrayList<String> filesToDeleteList = savedInstanceState.getStringArrayList("files_to_delete");
            if(filesToDeleteList != null)
                filesToDelete = filesToDeleteList.toArray();

            ArrayList<String> savedCab = savedInstanceState.getStringArrayList("cab");
            if(savedCab != null) {
                inCabMode = true;
                cab = savedCab;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        WebViewInitState wvState = WebViewInitState.getInstance();
        wvState.initialize(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!inCabMode)
            cab.clear();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    // Keyboard shortcuts
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        super.dispatchKeyShortcutEvent(event);
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteListFragment) {
                NoteListFragment fragment = (NoteListFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
                fragment.dispatchKeyShortcutEvent(event.getKeyCode());
            } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteViewFragment) {
                NoteViewFragment fragment = (NoteViewFragment) getSupportFragmentManager().findFragmentByTag("NoteViewFragment");
                fragment.dispatchKeyShortcutEvent(event.getKeyCode());
            } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment) {
                NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
                fragment.dispatchKeyShortcutEvent(event.getKeyCode());
            } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof WelcomeFragment) {
                WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
                fragment.dispatchKeyShortcutEvent(event.getKeyCode());
            }

            return true;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public void onDeleteDialogPositiveClick() {
        if(filesToDelete != null) {
            reallyDeleteNotes();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteViewFragment) {
            NoteViewFragment fragment = (NoteViewFragment) getSupportFragmentManager().findFragmentByTag("NoteViewFragment");
            fragment.onDeleteDialogPositiveClick();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment) {
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
            fragment.onDeleteDialogPositiveClick();
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteListFragment) {
            NoteListFragment fragment = (NoteListFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.onBackPressed();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteViewFragment) {
            NoteViewFragment fragment = (NoteViewFragment) getSupportFragmentManager().findFragmentByTag("NoteViewFragment");
            fragment.onBackPressed();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment) {
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
            fragment.onBackPressed(null);
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof WelcomeFragment) {
            WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.onBackPressed();
        }
    }

    @Override
    public void viewNote(String filename) {
        viewEditNote(filename, false);
    }

    @Override
    public void editNote(String filename) {
        viewEditNote(filename, true);
    }

    // Method used by selecting a existing note from the ListView in NoteViewFragment or NoteEditFragment
    // We need this method in MainActivity because sometimes getSupportFragmentManager() is null
    public void viewEditNote(String filename, boolean isEdit) {
        String currentFilename;

        if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment) {
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
            currentFilename = fragment.getFilename();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteViewFragment) {
            NoteViewFragment fragment = (NoteViewFragment) getSupportFragmentManager().findFragmentByTag("NoteViewFragment");
            currentFilename = fragment.getFilename();
        } else
            currentFilename = "";

        if(!currentFilename.equals(filename)) {
            if(findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                cab.clear();

            if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteEditFragment) {
                NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
                fragment.switchNotes(filename);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment;
                String tag;

                if(isEdit) {
                    fragment = new NoteEditFragment();
                    tag = "NoteEditFragment";
                } else {
                    fragment = new NoteViewFragment();
                    tag = "NoteViewFragment";
                }

                fragment.setArguments(bundle);

                // Add NoteViewFragment or NoteEditFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, tag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .commit();
            }
        }
    }

    @Override
    public void onBackDialogNegativeClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogNegativeClick(filename);
    }

    @Override
    public void onBackDialogPositiveClick(String filename) {
        NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag("NoteEditFragment");
        fragment.onBackDialogPositiveClick(filename);
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
        showDeleteDialog(true);
    }

    private void showDeleteDialog(boolean clearFilesToDelete) {
        if(clearFilesToDelete) filesToDelete = null;

        Bundle bundle = new Bundle();
        bundle.putInt("dialog_title",
                filesToDelete == null || filesToDelete.length == 1
                ? R.string.dialog_delete_button_title
                : R.string.dialog_delete_button_title_plural);

        DialogFragment deleteFragment = new DeleteDialogFragment();
        deleteFragment.setArguments(bundle);
        deleteFragment.show(getSupportFragmentManager(), "delete");
    }

    @Override
    public void showSaveButtonDialog() {
        DialogFragment saveFragment = new SaveButtonDialogFragment();
        saveFragment.show(getSupportFragmentManager(), "save");
    }

    @Override
    public boolean isShareIntent() {
        return false;
    }

    @Override
    public String getCabString(int size) {
        if(size == 1)
            return getResources().getString(R.string.cab_note_selected);
        else
            return getResources().getString(R.string.cab_notes_selected);
    }

    @Override
    public void deleteNotes() {
        filesToDelete = cab.toArray();
        cab.clear();

        showDeleteDialog(false);
    }

    private void reallyDeleteNotes() {
        // Build the pathname to delete each file, them perform delete operation
        for(Object file : filesToDelete) {
            File fileToDelete = new File(getFilesDir() + File.separator + file);
            fileToDelete.delete();
        }

        String[] filesToDelete2 = new String[filesToDelete.length];
        Arrays.asList(filesToDelete).toArray(filesToDelete2);

        // Send broadcasts to update UI
        Intent deleteIntent = new Intent();
        deleteIntent.setAction("com.farmerbb.notepad.DELETE_NOTES");
        deleteIntent.putExtra("files", filesToDelete2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(deleteIntent);

        Intent listIntent = new Intent();
        listIntent.setAction("com.farmerbb.notepad.LIST_NOTES");
        LocalBroadcastManager.getInstance(this).sendBroadcast(listIntent);

        // Show toast notification
        if(filesToDelete.length == 1)
            showToast(R.string.note_deleted);
        else
            showToast(R.string.notes_deleted);

        filesToDelete = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void exportNotes() {
        filesToExport = cab.toArray();
        cab.clear();

        if(filesToExport.length == 1 || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fileBeingExported = 0;
            reallyExportNotes();
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

            try {
                startActivityForResult(intent, EXPORT_TREE);
            } catch (ActivityNotFoundException e) {
                showToast(R.string.error_exporting_notes);
            }
        }
    }

    @Override
    public void exportNote(String filename) {
        filesToExport = new Object[] {filename};
        fileBeingExported = 0;
        reallyExportNotes();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void reallyExportNotes() {
        String filename = "";
        String fileSuffix = "";

        try {
            filename = loadNoteTitle(filesToExport[fileBeingExported].toString());
        } catch (IOException e) { /* Gracefully fail */ }

        try {
            fileSuffix = getNoteTimestamp(filesToExport[fileBeingExported].toString());
        } catch (NumberFormatException e) {
            //For draft notes, get current time
            fileSuffix = getNoteTimestamp(String.valueOf(System.currentTimeMillis()));
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, generateFilename(filename, fileSuffix));

        try {
            startActivityForResult(intent, EXPORT);
        } catch (ActivityNotFoundException e) {
            showToast(R.string.error_exporting_notes);
        }
    }

    private String generateFilename(String filename, String lastModified) {
        // Remove any invalid characters
        final String[] characters = new String[]{"<", ">", ":", "\"", "/", "\\\\", "\\|", "\\?", "\\*"};

        for(String character : characters) {
            filename = filename.replaceAll(character, "");
        }

        SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        String fileNameType = pref.getString("export_filename", "text-only");

        // To ensure that the generated filename fits within filesystem limitations,
        // truncate the filename to ~245 characters.
        if(fileNameType.equals("text-only")) {
            int maxLength = 245;
            if(filename.length() > maxLength)
                filename = filename.substring(0, maxLength);
        } else {
            int maxLength = 245 - (lastModified.length() + 1);
            if(filename.length() > maxLength)
                filename = filename.substring(0, maxLength);

            if(fileNameType.equals("text-timestamp")) {
                //Add timestamp as suffix
                filename = filename + "_" + lastModified;
            } else {
                //Add timestamp as prefix
                filename = lastModified + "_" + filename;
            }
        }



        return filename + ".txt";
    }

    // Methods used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(this, getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    // Loads note from /data/data/com.farmerbb.notepad/files
    public String loadNote(String filename) throws IOException {

        // Initialize StringBuilder which will contain note
        StringBuilder note = new StringBuilder();

        // Open the file on disk
        FileInputStream input = openFileInput(filename);
        InputStreamReader reader = new InputStreamReader(input);
        BufferedReader buffer = new BufferedReader(reader);

        // Load the file
        String line = buffer.readLine();
        while (line != null ) {
            note.append(line);
            line = buffer.readLine();
            if(line != null)
                note.append("\n");
        }

        // Close file on disk
        reader.close();

        return(note.toString());
    }

    // Loads first line of a note for display in the ListView
    @Override
    public String loadNoteTitle(String filename) throws IOException {
        // Open the file on disk
        FileInputStream input = openFileInput(filename);
        InputStreamReader reader = new InputStreamReader(input);
        BufferedReader buffer = new BufferedReader(reader);

        // Load the file
        String line = buffer.readLine();

        // Close file on disk
        reader.close();

        return(line);
    }

    // Calculates last modified date/time of a note for display in the ListView
    @Override
    public String loadNoteDate(String filename) {
        Date lastModified = new Date(Long.parseLong(filename));
        return(DateFormat
                .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(lastModified));
    }

    // Calculates last modified date/time of a note for exporting
    private String getNoteTimestamp(String filename) {
        //Get the current locale
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            locale = getResources().getConfiguration().getLocales().get(0);
        } else{
            locale = getResources().getConfiguration().locale;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm", locale);
        return dateFormat.format(new Date(Long.parseLong(filename)));
    }

    @Override
    public void showFab() {
        inCabMode = false;

        if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteListFragment) {
            NoteListFragment fragment = (NoteListFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.showFab();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof WelcomeFragment) {
            WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.showFab();
        }
    }

    @Override
    public void hideFab() {
        inCabMode = true;

        if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof NoteListFragment) {
            NoteListFragment fragment = (NoteListFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.hideFab();
        } else if(getSupportFragmentManager().findFragmentById(R.id.noteViewEdit) instanceof WelcomeFragment) {
            WelcomeFragment fragment = (WelcomeFragment) getSupportFragmentManager().findFragmentByTag("NoteListFragment");
            fragment.hideFab();
        }
    }

    @Override
    public void onFirstRunDialogPositiveClick() {
        // Set some initial preferences
        SharedPreferences prefMain = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefMain.edit();
        editor.putInt("first-run", 1);
        editor.apply();

        SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref.edit();
        editor2.putBoolean("show_dialogs", false);
        editor2.apply();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if(resultCode == RESULT_OK && resultData != null) {
            successful = true;

            if(requestCode == IMPORT) {
                Uri uri = resultData.getData();
                ClipData clipData = resultData.getClipData();

                if(uri != null)
                    successful = importNote(uri);
                else if(clipData != null)
                    for(int i = 0; i < clipData.getItemCount(); i++) {
                        successful = importNote(clipData.getItemAt(i).getUri());
                    }

                // Show toast notification
                showToast(successful
                        ? (uri == null ? R.string.notes_imported_successfully : R.string.note_imported_successfully)
                        : R.string.error_importing_notes);

                // Send broadcast to NoteListFragment to refresh list of notes
                Intent listNotesIntent = new Intent();
                listNotesIntent.setAction("com.farmerbb.notepad.LIST_NOTES");
                LocalBroadcastManager.getInstance(this).sendBroadcast(listNotesIntent);
            } else if(requestCode == EXPORT) {
                try {
                    saveExportedNote(loadNote(filesToExport[fileBeingExported].toString()), resultData.getData());
                } catch (IOException e) {
                    successful = false;
                }

                fileBeingExported++;
                if(fileBeingExported < filesToExport.length)
                    reallyExportNotes();
                else
                    showToast(successful
                            ? (fileBeingExported == 1 ? R.string.note_exported_to : R.string.notes_exported_to)
                            : R.string.error_exporting_notes);

                File fileToDelete = new File(getFilesDir() + File.separator + "exported_note");
                fileToDelete.delete();
            } else if(requestCode == EXPORT_TREE) {
                DocumentFile tree = DocumentFile.fromTreeUri(this, resultData.getData());

                for(Object exportFilename : filesToExport) {
                    try {
                        DocumentFile file = tree.createFile(
                                "text/plain",
                                generateFilename(
                                        loadNoteTitle(exportFilename.toString()),
                                        getNoteTimestamp(exportFilename.toString())));

                        if(file != null)
                            saveExportedNote(loadNote(exportFilename.toString()), file.getUri());
                        else
                            successful = false;
                    } catch (IOException e) {
                        successful = false;
                    }
                }

                showToast(successful ? R.string.notes_exported_to : R.string.error_exporting_notes);
            }
        }
    }

    private void saveExportedNote(String note, Uri uri) throws IOException {
        // Convert line separators to Windows format
        note = note.replaceAll("\r\n", "\n");
        note = note.replaceAll("\n", "\r\n");

        // Write file to external storage
        OutputStream os = getContentResolver().openOutputStream(uri);
        os.write(note.getBytes());
        os.close();
    }

    private boolean importNote(Uri uri) {
        try {
            File importedFile = new File(getFilesDir(), Long.toString(System.currentTimeMillis()));
            long suffix = 0;

            // Handle cases where a note may have a duplicate title
            while(importedFile.exists()) {
                suffix++;
                importedFile = new File(getFilesDir(), Long.toString(System.currentTimeMillis() + suffix));
            }

            InputStream is = getContentResolver().openInputStream(uri);
            byte[] data = new byte[is.available()];

            if(data.length > 0) {
                OutputStream os = new FileOutputStream(importedFile);
                is.read(data);
                os.write(data);
                is.close();
                os.close();
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void printNote(String contentToPrint) {
        SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

        // Create a WebView object specifically for printing
        boolean generateHtml = !(pref.getBoolean("markdown", false)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        WebView webView = generateHtml ? new WebView(this) : new MarkdownView(this);

        // Apply theme
        String theme = pref.getString("theme", "light-sans");
        int textSize = -1;

        String fontFamily = null;

        if(theme.contains("sans")) {
            fontFamily = "sans-serif";
        }

        if(theme.contains("serif")) {
            fontFamily = "serif";
        }

        if(theme.contains("monospace")) {
            fontFamily = "monospace";
        }

        switch(pref.getString("font_size", "normal")) {
            case "smallest":
                textSize = 12;
                break;
            case "small":
                textSize = 14;
                break;
            case "normal":
                textSize = 16;
                break;
            case "large":
                textSize = 18;
                break;
            case "largest":
                textSize = 20;
                break;
        }

        String topBottom = " " + Float.toString(getResources().getDimension(R.dimen.padding_top_bottom_print) / getResources().getDisplayMetrics().density) + "px";
        String leftRight = " " + Float.toString(getResources().getDimension(R.dimen.padding_left_right_print) / getResources().getDisplayMetrics().density) + "px";
        String fontSize = " " + Integer.toString(textSize) + "px";

        String css = "body { " +
                        "margin:" + topBottom + topBottom + leftRight + leftRight + "; " +
                        "font-family:" + fontFamily + "; " +
                        "font-size:" + fontSize + "; " +
                        "}";

        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setLoadsImagesAutomatically(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                createWebPrintJob(view);
            }
        });

        // Load content into WebView
        if(generateHtml) {
            webView.loadDataWithBaseURL(null,
                    "<link rel='stylesheet' type='text/css' href='data:text/css;base64,"
                            + Base64.encodeToString(css.getBytes(), Base64.DEFAULT)
                            +"' /><html><body><p>"
                            + StringUtils.replace(contentToPrint, "\n", "<br>")
                            + "</p></body></html>",
                    "text/HTML", "UTF-8", null);
        } else
            ((MarkdownView) webView).loadMarkdown(contentToPrint,
                    "data:text/css;base64," + Base64.encodeToString(css.getBytes(), Base64.DEFAULT));
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createWebPrintJob(WebView webView) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.document, getString(R.string.app_name));
        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    @Override
    public void startMultiSelect() {
        NoteListFragment fragment = null;

        if(findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
            fragment = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteViewEdit);

        if(findViewById(R.id.layoutMain).getTag().equals("main-layout-large"))
            fragment = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.noteList);

        if(fragment != null)
            fragment.startMultiSelect();
    }

    @Override
    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences("MainActivity", mode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(filesToExport != null && filesToExport.length > 0) {
            ArrayList<String> filesToExportList = new ArrayList<>();
            for(Object file : filesToExport) {
                filesToExportList.add(file.toString());
            }

            outState.putStringArrayList("files_to_export", filesToExportList);
        }

        if(filesToDelete != null && filesToDelete.length > 0) {
            ArrayList<String> filesToDeleteList = new ArrayList<>();
            for(Object file : filesToDelete) {
                filesToDeleteList.add(file.toString());
            }

            outState.putStringArrayList("files_to_delete", filesToDeleteList);
        }

        if(inCabMode && cab.size() > 0)
            outState.putStringArrayList("cab", cab);
        
        super.onSaveInstanceState(outState);
    }

    @Override
    public ArrayList<String> getCabArray() {
        return cab;
    }
}
