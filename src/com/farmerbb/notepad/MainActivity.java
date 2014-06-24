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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SortByDialogFragment.NoticeDialogListener {

	public static final String FILENAME = "com.farmerbb.notepad.NAME";
	static Context context;
	String filename;
	int sortBy;
	int firstRun;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Show a toast message if this is the user's first time running Notepad
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		firstRun = sharedPref.getInt("first-run", 0);
		if(firstRun == 0) {
			// Show welcome dialog
			DialogFragment firstRun = new FirstRunDialogFragment();
			firstRun.show(getFragmentManager(), "firstrunfragment");

			// Set first-run preference to 1; we don't need to show the dialog anymore
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("first-run", 1);
			editor.apply();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Refresh list of notes onResume (instead of onCreate) to reflect additions/deletions
		listNotes();
	}

	// Keyboard shortcuts	
	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		super.dispatchKeyShortcutEvent(event);
		if(event.getAction()==KeyEvent.ACTION_DOWN &&
				event.isCtrlPressed()){
			final int keyCode = event.getKeyCode();
			switch(keyCode){

			// CTRL+N: New Note
			case KeyEvent.KEYCODE_N:
				newNote();
				break;

			}
			return true;
		}
		return super.dispatchKeyShortcutEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate action bar menu
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		// New button
		case R.id.action_new:
			newNote();
			return true;

			// Sort by button
		case R.id.action_sort_by:
			DialogFragment sortByFragment = new SortByDialogFragment();
			sortByFragment.show(getFragmentManager(), "sort");
			return true;

			// Settings button
		case R.id.action_settings:
			Intent intent = new Intent (this, SettingsActivity.class);
			startActivity(intent);
			return true;            
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteNote(Object[] filesToDelete) {
		// Build the pathname to delete each file, them perform delete operation
		for(int i = 0; i < filesToDelete.length; i++) {
			File fileToDelete = new File(getFilesDir().getAbsolutePath().toString() + "/" + filesToDelete[i]);
			fileToDelete.delete();
		}

		// Show toast notification
		if(filesToDelete.length == 1)
			showToast(R.string.note_deleted);
		else
			showToast(R.string.notes_deleted);

		// Refresh notes list to reflect deletions
		listNotes();
	}

	// Intent sent when a draft is detected
	private void editDraft() {		
		// Notify the user that a draft is being restored
		showToast(R.string.draft_restored);

		Intent intent = new Intent (this, NoteEditActivity.class);
		// Get filename of selected note
		intent.putExtra(FILENAME, "draft");
		startActivity(intent);
		overridePendingTransition(0, 0);
	}

	// Returns list of filenames in /data/data/com.farmerbb.notepad/files/
	private static String[] getListOfNotes(File file) {	
		String[] list = file.list();
		return list;
	}

	// Returns an integer with number of files in /data/data/com.farmerbb.notepad/files/
	private static int getNumOfFiles(File file){
		int files = new File(file.getPath()).list().length;
		return files;
	}

	private void listNotes() {

		// Declare ListView
		final ListView listView = (ListView) findViewById(R.id.listView1);

		// Get sort-by preference
		// 0: "by date", 1: "by name"
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		sortBy = sharedPref.getInt("sort-by", 0);

		// Get number of notes saved
		int numOfFiles = getNumOfFiles(this.getFilesDir());

		// Get array of file names and create additional arrays
		String[] listOfNotesByDate = getListOfNotes(this.getFilesDir());
		String[] listOfNotesByName = new String[numOfFiles];

		String[] listOfTitlesByDate = new String[numOfFiles];
		String[] listOfTitlesByName = new String[numOfFiles];

		ArrayList<String> list = new ArrayList<String>(numOfFiles);

		// Before we do anything else, check for a saved draft; if one exists, load it
		for(int i = 0; i < numOfFiles; i++) {
			if(listOfNotesByDate[i].equals("draft"))
				editDraft();
		}

		// If sort-by is "by date", sort in reverse order			
		if(sortBy == 0)
			Arrays.sort(listOfNotesByDate, Collections.reverseOrder());

		// Get array of first lines of each note
		for(int i = 0; i < numOfFiles; i++) {
			try {
				listOfTitlesByDate[i] = loadNoteTitle(listOfNotesByDate[i]);
			} catch (IOException e) {
				showToast(R.string.error_loading_list);
			}
		}

		// If sort-by is "by name", sort alphabetically
		if(sortBy == 1) {

			// Copy titles array
			for(int i = 0; i < numOfFiles; i++)
				listOfTitlesByName[i] = listOfTitlesByDate[i];

			// Sort titles
			Arrays.sort(listOfTitlesByName);

			// Initialize notes array
			for(int i = 0; i < numOfFiles; i++)
				listOfNotesByName[i] = "new";

			// Copy filenames array with new sort order of titles and nullify date arrays
			for(int i = 0; i < numOfFiles; i++) {
				for(int j = 0; j < numOfFiles; j++) {
					if(listOfTitlesByName[i].equals(listOfTitlesByDate[j])) {
						if(listOfNotesByName[i].equals("new")) {
							listOfNotesByName[i] = listOfNotesByDate[j];
							listOfNotesByDate[j] = "";
							listOfTitlesByDate[j] = "";
						}
					}
				}
			}
		}

		// Populate ArrayList with notes, showing name as first line of the notes
		for(int i = 0; i < numOfFiles; i++ ) {
			if(sortBy == 0)
				list.add(" " + listOfTitlesByDate[i]);
			if(sortBy == 1)
				list.add(" " + listOfTitlesByName[i]);
		}

		// Create the custom adapter to bind the array to the ListView
		final NoteListAdapter adapter = new NoteListAdapter(this, list);

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
				if(sortBy == 0)
					viewNote(finalListByDate[position]);
				if(sortBy == 1)
					viewNote(finalListByName[position]);
			}
		});

		// Make ListView handle contextual action bar
		final ArrayList<String> cab = new ArrayList<String>(numOfFiles);

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
				case R.id.action_delete:
					mode.finish(); // Action picked, so close the CAB
					deleteNote(cab.toArray());
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Inflate the menu for the CAB
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.context_menu, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {

				// Add/remove filenames to cab array as they are checked/unchecked
				if(checked) {
					if(sortBy == 0)
						cab.add(finalListByDate[position]);
					if(sortBy == 1)
						cab.add(finalListByName[position]);
				} else {
					if(sortBy == 0)
						cab.remove(finalListByDate[position]);
					if(sortBy == 1)
						cab.remove(finalListByName[position]);
				}

				// Update the title in CAB 
				if(cab.size() == 0)
					mode.setTitle("");
				else if(cab.size() == 1)
					mode.setTitle("1 " + getResources().getString(R.string.cab_note_selected));
				else
					mode.setTitle(cab.size() + " " + getResources().getString(R.string.cab_notes_selected));
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Clear any old values from cab array
				cab.clear();

				return false;
			}
		});

		// If there are no saved notes, then display the empty view
		if(numOfFiles == 0) { 
			TextView empty = (TextView) findViewById(R.id.empty);
			listView.setEmptyView(empty);
		}
	}

	// Loads first line of a note for display in the ListView
	private String loadNoteTitle(String filename) throws IOException {

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

	// Intent sent by New Note action bar button
	private void newNote() {		
		Intent intent = new Intent (this, NoteEditActivity.class);
		startActivity(intent);
	}

	// Method used to generate toast notifications
	private void showToast(int message){
		Context toastContext = getApplicationContext();
		CharSequence text = getResources().getString(message);
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(toastContext, text, duration);
		toast.show();
	}

	// Intent sent by selecting a existing note from the ListView
	private void viewNote(String filename) {		
		Intent intent = new Intent (this, NoteViewActivity.class);
		// Get filename of selected note
		intent.putExtra(FILENAME, filename);
		startActivity(intent);
	}

	@Override
	public void onSortOptionSelect(DialogFragment dialog) {
		// Refresh notes list to reflect new sorting order
		listNotes();		
	}
}