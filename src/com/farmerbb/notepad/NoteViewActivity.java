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

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NoteViewActivity extends Activity implements DeleteDialogFragment.NoticeDialogListener {

	private TextView noteContents;
	static Context context;
	String filename = "new";
	String contentsOnLoad = "";
	int firstLoad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up content view
		setContentView(R.layout.activity_note_view);	
		noteContents = (TextView) findViewById(R.id.textView);

		// Handle intents
		Intent intent = getIntent();

		// Intent sent through MainActivity or NoteEditActivity
		if(intent.getStringExtra(MainActivity.FILENAME) != null)
			filename = intent.getStringExtra(MainActivity.FILENAME);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Load note contents
		if(filename.equals("new")) {
			showToast(R.string.note_view_error);
			finish();
		} else {
			try {
				noteContents.setText(loadNote(filename));
			} catch (IOException e) {
				showToast(R.string.error_loading_note);
				finish();
			}		
		}

		// Show a toast message if this is the user's first time viewing a note
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		firstLoad = sharedPref.getInt("first-load", 0);
		if(firstLoad == 0) {
			// Show dialog with info
			DialogFragment firstLoad = new FirstViewDialogFragment();
			firstLoad.show(getFragmentManager(), "firstloadfragment");

			// Set first-load preference to 1; we don't need to show the dialog anymore
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("first-load", 1);
			editor.apply();
		}

		// Make TextView edit note onClick for convenience
		noteContents.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editNote();
			}
		});
	}

	// Keyboard shortcuts	
	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		super.dispatchKeyShortcutEvent(event);
		if(event.getAction() == KeyEvent.ACTION_DOWN &&
				event.isCtrlPressed()){
			final int keyCode = event.getKeyCode();
			switch(keyCode){

			// CTRL+E: Edit
			case KeyEvent.KEYCODE_E:
				Intent intentEdit = new Intent (this, NoteEditActivity.class);
				// Get filename of selected note
				intentEdit.putExtra(MainActivity.FILENAME, filename);
				startActivity(intentEdit);
				finish();
				overridePendingTransition(0, 0);
				break;

				// CTRL+D: Delete
			case KeyEvent.KEYCODE_D:
				// Show delete dialog
				DialogFragment deleteFragment = new DeleteDialogFragment();
				deleteFragment.show(getFragmentManager(), "back");
				break;

				// CTRL+H: Share
			case KeyEvent.KEYCODE_H:
				// Set current note contents to a String
				String contents = noteContents.getText().toString();

				// Send a share intent
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, contents);
				intent.setType("text/plain");

				// Verify that the intent will resolve to an activity, and send
				if (intent.resolveActivity(getPackageManager()) != null)
					startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

				break;
			}
			return true;
		}
		return super.dispatchKeyShortcutEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate action bar menu
		getMenuInflater().inflate(R.menu.note_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Override default Android "up" behavior to instead mimic the back button
			this.onBackPressed();
			return true;

			// Edit button
		case R.id.action_edit:
			editNote();
			return true;

			// Delete button
		case R.id.action_delete:
			// Show delete dialog
			DialogFragment deleteFragment = new DeleteDialogFragment();
			deleteFragment.show(getFragmentManager(), "back");
			return true;

			// Share menu item
		case R.id.action_share:
			// Set current note contents to a String
			String contents = noteContents.getText().toString();

			// Send a share intent
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, contents);
			intent.setType("text/plain");

			// Verify that the intent will resolve to an activity, and send
			if (intent.resolveActivity(getPackageManager()) != null)
				startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

			return true;             
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteNote(String filename) {		
		// Build the pathname to delete file, then perform delete operation
		File fileToDelete = new File(getFilesDir().getAbsolutePath().toString() + "/" + filename);
		fileToDelete.delete();
	}

	private void editNote() {
		Intent intentEdit = new Intent (this, NoteEditActivity.class);
		// Get filename of selected note
		intentEdit.putExtra(MainActivity.FILENAME, filename);
		startActivity(intentEdit);
		finish();
		overridePendingTransition(0, 0);
	}

	// Loads note from /data/data/com.farmerbb.notepad/files
	private StringBuffer loadNote(String filename) throws IOException {

		// Initialize StringBuffer which will contain note
		StringBuffer note = new StringBuffer("");

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

		// Write contents to variable to compare when discarding changes
		contentsOnLoad = note.toString();

		return(note);		
	}

	// Method used to generate toast notifications
	private void showToast(int message){
		Context toastContext = getApplicationContext();
		CharSequence text = getResources().getString(message);
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(toastContext, text, duration);
		toast.show();
	}

	@Override
	public void onDeleteDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button
		deleteNote(filename);
		showToast(R.string.note_deleted);
		finish();
	}
}
