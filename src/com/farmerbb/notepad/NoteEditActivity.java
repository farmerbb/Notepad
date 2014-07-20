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
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class NoteEditActivity extends Activity implements BackButtonDialogFragment.NoticeDialogListener, DeleteDialogFragment.NoticeDialogListener, SaveButtonDialogFragment.NoticeDialogListener {

	private EditText noteContents;
	String filename = String.valueOf(System.currentTimeMillis());
	String contentsOnLoad = "";
	int length = 0;
	long draftName;
	boolean isSavedNote = false;
	boolean isShareIntent = false;
	String contents;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up content view
		setContentView(R.layout.activity_note_edit);	
		noteContents = (EditText) findViewById(R.id.editText1);

		// Handle intents
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		// Intent sent through MainActivity or NoteViewActivity
		if(intent.getStringExtra(MainActivity.FILENAME) != null) {
			filename = intent.getStringExtra(MainActivity.FILENAME);
			if(!filename.equals("draft"))
				isSavedNote = true;
		}

		// Intent sent through an external application
		if(Intent.ACTION_SEND.equals(action) && type != null) {
			if("text/plain".equals(type)) {
				String external = intent.getStringExtra(Intent.EXTRA_TEXT);
				if(external != null) {
					noteContents.setText(external);
                    noteContents.setSelection(external.length(), external.length());
					isShareIntent = true;
				}
			}
		}

		// Intent sent through Google Now "note to self"
		if("com.google.android.gm.action.AUTO_SEND".equals(action) && type != null) {
			if("text/plain".equals(type)) {
				String external = intent.getStringExtra(Intent.EXTRA_TEXT);
				if(external != null) {
					noteContents.setText(external);
                    noteContents.setSelection(external.length(), external.length());
					isShareIntent = true;

                    try {
                        saveNote();
                        finish();
                    } catch (IOException e) {
                        // Show error message as toast if file fails to save
                        showToast(R.string.failed_to_save);
                    }
				}
			}
		}

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Load note from existing file
		if(isSavedNote || filename.equals("draft")) {
			try {
				noteContents.setText(loadNote(filename));
				noteContents.setSelection(length, length);
			} catch (IOException e) {
				showToast(R.string.error_loading_note);
				finish();
			}		
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Disable saving drafts if user launched Notepad through a share intent
		if(!isShareIntent) {

			// Delete any drafts if user is finishing the activity (through successful save/discard/delete)
			if(super.isFinishing())
				deleteNote("draft");
			else {
				SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

				// Save filename to draft-name preference
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putLong("draft-name", Long.parseLong(filename));
				editor.putBoolean("is-saved-note", isSavedNote);
				editor.apply();

				filename = "draft";
				try {
					saveNote();
				} catch (IOException e) {
					// Show error message as toast if file fails to save
					showToast(R.string.failed_to_save);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Disable restoring drafts if user launched Notepad through a share intent
		if(filename.equals("draft") && !isShareIntent) {

			// Restore draft preferences 
			SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
			draftName = sharedPref.getLong("draft-name", 0);
			isSavedNote = sharedPref.getBoolean("is-saved-note", false);

			// Restore filename of draft
			filename = Long.toString(draftName);

			// Reload old file into memory, so that correct contentsOnLoad is set
			if(isSavedNote) {
				try {
					loadNote(filename);
				} catch (IOException e) {
					showToast(R.string.error_loading_note);
					finish();
				}
			} else
				contentsOnLoad = "";
		}

		// Change window title if editing a saved note
		if(isSavedNote)
			setTitle("Edit Note");
	}

	// Keyboard shortcuts	
	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		super.dispatchKeyShortcutEvent(event);
		if(event.getAction() == KeyEvent.ACTION_DOWN &&
				event.isCtrlPressed()) {
			final int keyCode = event.getKeyCode();
			switch(keyCode) {

			// CTRL+S: Save
			case KeyEvent.KEYCODE_S:
                // Set current note contents to a String
                contents = noteContents.getText().toString();

                // If EditText is empty, show toast informing user to enter some text
                if(contents.equals(""))
                    showToast(R.string.empty_note);
                else {
                    try {
                        // Keyboard shortcut just saves the note; no dialog shown
                        saveNote();
                    } catch (IOException e) {
                        // Show error message as toast if file fails to save
                        showToast(R.string.failed_to_save);
                    }
                }
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
				contents = noteContents.getText().toString();

				// If EditText is empty, show toast informing user to enter some text
				if(contents.equals(""))
					showToast(R.string.empty_note);
				else {
					// Send a share intent
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_TEXT, contents);
					intent.setType("text/plain");

					// Verify that the intent will resolve to an activity, and send
					if (intent.resolveActivity(getPackageManager()) != null)
						startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
				}
				break;
			}
			return true;
		}
		return super.dispatchKeyShortcutEvent(event);
	}

	@Override
	public void onBackPressed() {
		// Finish activity if no changes were made since last save
		if(contentsOnLoad.equals(noteContents.getText().toString())) {
			finish();
		} else {
			// Finish activity if EditText is empty
			if(noteContents.getText().toString().isEmpty())
				finish();
			else {
				SharedPreferences pref = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
				if(pref.getBoolean("show_dialogs", true)) {
					// Show back button dialog
					DialogFragment backFragment = new BackButtonDialogFragment();
					backFragment.show(getFragmentManager(), "back");
				} else {
					try {
						saveNote();
						finish();
					} catch (IOException e) {
						// Show error message as toast if file fails to save
						showToast(R.string.failed_to_save);
					}
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate action bar menu
		getMenuInflater().inflate(R.menu.note_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Override default Android "up" behavior to instead mimic the back button
			this.onBackPressed();
			return true;

			// Save button
		case R.id.action_save:
			// Get current note contents from EditText
			noteContents = (EditText) findViewById(R.id.editText1);
			contents = noteContents.getText().toString();

			// If EditText is empty, show toast informing user to enter some text
			if(contents.equals(""))
				showToast(R.string.empty_note);
			else {
				// If no changes were made since last save, switch back to NoteViewActivity
				if(contentsOnLoad.equals(noteContents.getText().toString())) {
					Intent intentView = new Intent (this, NoteViewActivity.class);
					// Get filename of selected note
					intentView.putExtra(MainActivity.FILENAME, filename);
					startActivity(intentView);
					finish();
					overridePendingTransition(0, 0);
				} else {
					SharedPreferences pref = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
					if(pref.getBoolean("show_dialogs", true)) {
						// Show save button dialog
						DialogFragment saveFragment = new SaveButtonDialogFragment();
						saveFragment.show(getFragmentManager(), "back");
					} else {
						try {
                            saveNote();

                            if(isShareIntent)
                                finish();
                            else {
                                Intent intentView = new Intent(this, NoteViewActivity.class);
                                // Get filename of selected note
                                intentView.putExtra(MainActivity.FILENAME, filename);
                                startActivity(intentView);
                                finish();
                                overridePendingTransition(0, 0);
                            }
						} catch (IOException e) {
							// Show error message as toast if file fails to save
							showToast(R.string.failed_to_save);
						}
					}
				}
			}
			return true;

			// Delete button
		case R.id.action_delete:
			DialogFragment deleteFragment = new DeleteDialogFragment();
			deleteFragment.show(getFragmentManager(), "back");
			return true;

			// Share menu item
		case R.id.action_share:
			// Set current note contents to a String
			contents = noteContents.getText().toString();

			// If EditText is empty, show toast informing user to enter some text
			if(contents.equals(""))
				showToast(R.string.empty_note);
			else {
				// Send a share intent
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, contents);
				intent.setType("text/plain");

				// Verify that the intent will resolve to an activity, and send
				if (intent.resolveActivity(getPackageManager()) != null)
					startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
			}

			return true;             
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteNote(String filename) {		
		// Build the pathname to delete file, then perform delete operation
		File fileToDelete = new File(getFilesDir().getAbsolutePath() + "/" + filename);
		fileToDelete.delete();
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
		length = contentsOnLoad.length();

		return(note);		
	}

	// Saves notes to /data/data/com.farmerbb.notepad/files
	private void saveNote() throws IOException {
		// Set current note contents to a String
		noteContents = (EditText) findViewById(R.id.editText1);
		contents = noteContents.getText().toString(); 

		// Write the String to a new file with filename of current milliseconds of Unix time
		if(contents.equals("") && filename.equals("draft"))
			finish();
		else {

			// Set a new filename if this is not a draft
			String newFilename;
			if(filename.equals("draft"))
				newFilename = filename;
			else 
				newFilename = String.valueOf(System.currentTimeMillis());

			// Write note to disk
			FileOutputStream output = openFileOutput(newFilename, Context.MODE_PRIVATE);
			output.write(contents.getBytes());
			output.close();

			// Delete old file
			if(!filename.equals("draft"))
				deleteNote(filename);

			// Show toast notification
			if(filename.equals("draft"))
				showToast(R.string.draft_saved);
			else
				showToast(R.string.note_saved);

			// Old file is no more
			if(!filename.equals("draft")) {
				filename = newFilename;
				contentsOnLoad = contents;
				length = contentsOnLoad.length();
			}
		}
	}

	// Method used to generate toast notifications
	private void showToast(int message){
		Context toastContext = getApplicationContext();
		CharSequence text = getResources().getString(message);
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(toastContext, text, duration);
		toast.show();
	}

	// Handle button actions from dialog fragments
	@Override
	public void onBackDialogNegativeClick(DialogFragment dialog) {
		// User touched the dialog's negative button
		showToast(R.string.changes_discarded);
		finish();
	}

	@Override
	public void onBackDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button
		try {
			saveNote();
			finish();
		} catch (IOException e) {
			// Show error message as toast if file fails to save
			showToast(R.string.failed_to_save);
		}
	}

	@Override
	public void onDeleteDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button
		deleteNote(filename);
		showToast(R.string.note_deleted);
		finish();
	}

	@Override
	public void onSaveDialogNegativeClick(DialogFragment dialog) {
		// User touched the dialog's negative button
		if(isSavedNote) {
			showToast(R.string.changes_discarded);
			Intent intentView = new Intent (this, NoteViewActivity.class);
			// Get filename of selected note
			intentView.putExtra(MainActivity.FILENAME, filename);
			startActivity(intentView);
			finish();
			overridePendingTransition(0, 0);
		} else {
			showToast(R.string.changes_discarded);
			finish();
		}
	}

	@Override
	public void onSaveDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button
		try {
			saveNote();

            if(isShareIntent)
                finish();
            else {
                Intent intentView = new Intent(this, NoteViewActivity.class);
                // Get filename of selected note
                intentView.putExtra(MainActivity.FILENAME, filename);
                startActivity(intentView);
                finish();
                overridePendingTransition(0, 0);
            }
		} catch (IOException e) {
			// Show error message as toast if file fails to save
			showToast(R.string.failed_to_save);
		}
	}
}