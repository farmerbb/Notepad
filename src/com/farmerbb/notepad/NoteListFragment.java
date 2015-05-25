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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NoteListFragment extends Fragment {

    String sortBy;

    // Receiver used to refresh list of notes (in tablet layout)
    public class ListNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            listNotes();
        }
    }

    IntentFilter filter = new IntentFilter("com.farmerbb.notepad.LIST_NOTES");
    ListNotesReceiver receiver = new ListNotesReceiver();

    /* The activity that creates an instance of this fragment must
 * implement this interface in order to receive event call backs. */
    public interface Listener {
        void viewNote(String filename);
        String getCabString(int size);
        void exportNote(Object[] filesToExport);
        void deleteNote(Object[] filesToDelete);
        String loadNoteTitle(String filename) throws IOException;
        void showFab();
        void hideFab();
    }

    // Use this instance of the interface to deliver action events
    Listener listener;

    // Override the Fragment.onAttach() method to instantiate the Listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the Listener so we can send events to the host
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set values
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Before we do anything else, check for a saved draft; if one exists, load it
        SharedPreferences prefMain = getActivity().getPreferences(Context.MODE_PRIVATE);
        if(getId() == R.id.noteViewEdit && prefMain.getLong("draft-name", 0) != 0) {
            Bundle bundle = new Bundle();
            bundle.putString("filename", "draft");

            Fragment fragment = new NoteEditFragment();
            fragment.setArguments(bundle);

            // Add NoteEditFragment
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            if(getId() == R.id.noteViewEdit) {
                // Change window title
                String title = getResources().getString(R.string.app_name);

                getActivity().setTitle(title);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, null, getResources().getColor(R.color.primary));
                    getActivity().setTaskDescription(taskDescription);
                }

                // Don't show the Up button in the action bar, and disable the button
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                getActivity().getActionBar().setHomeButtonEnabled(false);
            }

            // Read preferences
            SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
            sortBy = pref.getString("sort_by", "date");

            // Refresh list of notes onResume (instead of onCreate) to reflect additions/deletions and preference changes
            listNotes();
        }
    }

    // Register and unregister ListNotesReceiver (for tablet layout)
    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(receiver, filter);

        // Floating action button
        FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.hide(false);

        SharedPreferences prefMain = getActivity().getPreferences(Context.MODE_PRIVATE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP 
            && getId() == R.id.noteViewEdit
            && prefMain.getLong("draft-name", 0) == 0) {
            floatingActionButton.show();
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideFab();

                    Bundle bundle = new Bundle();
                    bundle.putString("filename", "new");

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

                    // Add NoteEditFragment
                    getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getId() == R.id.noteViewEdit)
            inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        // New button
        case R.id.action_new:
            Bundle bundle = new Bundle();
            bundle.putString("filename", "new");

            Fragment fragment = new NoteEditFragment();
            fragment.setArguments(bundle);

                // Add NoteEditFragment
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();

            return true;

            // Settings button
        case R.id.action_settings:
            Intent intentSettings = new Intent (getActivity(), SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // Returns list of filenames in /data/data/com.farmerbb.notepad/files/
    private static String[] getListOfNotes(File file) {
        return file.list();
    }

    // Returns an integer with number of files in /data/data/com.farmerbb.notepad/files/
    private static int getNumOfFiles(File file){
        return new File(file.getPath()).list().length;
    }

    private void listNotes() {
        // Get number of files
        int numOfFiles = getNumOfFiles(getActivity().getFilesDir());

        // Get array of file names
        String[] listOfFiles = getListOfNotes(getActivity().getFilesDir());

        // Newer Samsung devices (Galaxy S5, Galaxy Note 4) like to save junk files inside the app's internal storage.
        // Delete them so that they don't cause problems with lists
        for(int i = 0; i < numOfFiles; i++) {
            File file = new File(getActivity().getFilesDir() + File.separator + listOfFiles[i]);

            if(listOfFiles[i].contains("rList"))
                file.delete();

            listOfFiles[i] = "";
        }

        // Declare ListView
        final ListView listView = (ListView) getActivity().findViewById(R.id.listView1);

        // Refresh number of files
        numOfFiles = getNumOfFiles(getActivity().getFilesDir());

        // Create arrays of note lists
        String[] listOfNotesByDate = getListOfNotes(getActivity().getFilesDir());
        String[] listOfNotesByName = new String[numOfFiles];

        String[] listOfTitlesByDate = new String[numOfFiles];
        String[] listOfTitlesByName = new String[numOfFiles];

        ArrayList<String> list = new ArrayList<>(numOfFiles);

        // If sort-by is "by date", sort in reverse order
        if(sortBy.equals("date"))
            Arrays.sort(listOfNotesByDate, Collections.reverseOrder());

        // Get array of first lines of each note
        for(int i = 0; i < numOfFiles; i++) {
            try {
                listOfTitlesByDate[i] = listener.loadNoteTitle(listOfNotesByDate[i]);
            } catch (IOException e) {
                showToast(R.string.error_loading_list);
            }
        }

        // If sort-by is "by name", sort alphabetically
        if(sortBy.equals("name")) {
            // Copy titles array
            System.arraycopy(listOfTitlesByDate, 0, listOfTitlesByName, 0, numOfFiles);

            // Sort titles
            Arrays.sort(listOfTitlesByName);

            // Initialize notes array
            for(int i = 0; i < numOfFiles; i++)
                listOfNotesByName[i] = "new";

            // Copy filenames array with new sort order of titles and nullify date arrays
            for(int i = 0; i < numOfFiles; i++) {
                for(int j = 0; j < numOfFiles; j++) {
                    if(listOfTitlesByName[i].equals(listOfTitlesByDate[j]) && listOfNotesByName[i].equals("new")) {
                        listOfNotesByName[i] = listOfNotesByDate[j];
                        listOfNotesByDate[j] = "";
                        listOfTitlesByDate[j] = "";
                    }
                }
            }

            // Populate ArrayList with notes, showing name as first line of the notes
            list.addAll(Arrays.asList(listOfTitlesByName));
        } else if(sortBy.equals("date"))
            list.addAll(Arrays.asList(listOfTitlesByDate));

        // Create the custom adapter to bind the array to the ListView
        final NoteListAdapter adapter = new NoteListAdapter(getActivity(), list);

        // Display the ListView
        listView.setAdapter(adapter);

        // Finalize arrays to prepare for handling clicked items
        final String[] finalListByDate = listOfNotesByDate;
        final String[] finalListByName = listOfNotesByName;

        // Make ListView handle clicked items
        listView.setClickable(true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if(sortBy.equals("date"))
                    listener.viewNote(finalListByDate[position]);
                if(sortBy.equals("name"))
                    listener.viewNote(finalListByName[position]);
            }
        });

        // Make ListView handle contextual action bar
        final ArrayList<String> cab = new ArrayList<>(numOfFiles);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_export:
                        mode.finish(); // Action picked, so close the CAB
                        listener.exportNote(cab.toArray());
                        return true;
                    case R.id.action_delete:
                        mode.finish(); // Action picked, so close the CAB
                        listener.deleteNote(cab.toArray());
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    listener.hideFab();

                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);

                // Clear any old values from cab array
                cab.clear();

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    listener.showFab();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Add/remove filenames to cab array as they are checked/unchecked
                if(checked) {
                    if(sortBy.equals("date"))
                        cab.add(finalListByDate[position]);
                    if(sortBy.equals("name"))
                        cab.add(finalListByName[position]);
                } else {
                    if(sortBy.equals("date"))
                        cab.remove(finalListByDate[position]);
                    if(sortBy.equals("name"))
                        cab.remove(finalListByName[position]);
                }

                // Update the title in CAB
                if(cab.size() == 0)
                    mode.setTitle("");
                else
                    mode.setTitle(cab.size() + " " + listener.getCabString(cab.size()));
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        // If there are no saved notes, then display the empty view
        if(numOfFiles == 0) {
            TextView empty = (TextView) getActivity().findViewById(R.id.empty);
            listView.setEmptyView(empty);
        }
    }

    // Method used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void dispatchKeyShortcutEvent(int keyCode) {
        if(getId() == R.id.noteViewEdit) {
            switch(keyCode) {
                // CTRL+N: New Note
                case KeyEvent.KEYCODE_N:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        hideFab();

                    Bundle bundle = new Bundle();
                    bundle.putString("filename", "new");

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

                    // Add NoteEditFragment
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                    break;
            }
        }
    }

    public void onBackPressed() {
        getActivity().finish();
    }

    public void showFab() {
        FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.show();
    }

    public void hideFab() {
        FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.hide();
    }
}
