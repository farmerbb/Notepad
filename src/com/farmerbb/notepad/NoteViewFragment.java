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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class NoteViewFragment extends Fragment {
	
	private TextView noteContents;
	String filename = "";
	String contentsOnLoad = "";
	int firstLoad;

    // Receiver used to close fragment when a note is deleted
    public class DeleteNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] filesToDelete = intent.getStringArrayExtra("files");

            for(Object file : filesToDelete) {
                if(filename.equals(file))
                    // Add NoteListFragment
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.noteViewEdit, new NoteListFragment(), "NoteListFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .commit();
            }
        }
    }

    IntentFilter filter = new IntentFilter("com.farmerbb.notepad.DELETE_NOTES");
    DeleteNotesReceiver receiver = new DeleteNotesReceiver();
	
	/* The activity that creates an instance of this fragment must
	 * implement this interface in order to receive event call backs. */
	public interface Listener {
		public void showDeleteDialog();
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

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // Set values
        setRetainInstance(true);
		setHasOptionsMenu(true);

        // Change window title
        getActivity().setTitle(getResources().getString(R.string.view_note));

		// Show the Up button in the action bar.
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        // If on Lollipop or above, hide the app icon in the action bar
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().getActionBar().setDisplayShowHomeEnabled(false);

        // Set up content view
        noteContents = (TextView) getActivity().findViewById(R.id.textView);
		
		// Get filename of saved note
		filename = getArguments().getString("filename");

		// Load note contents
			try {
				noteContents.setText(loadNote(filename));
			} catch (IOException e) {
				showToast(R.string.error_loading_note);

				// Add NoteListFragment
				getFragmentManager()
					.beginTransaction()
					.replace(R.id.noteViewEdit, new NoteListFragment(), "NoteListFragment")
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
					.commit();
			}

		// Show a toast message if this is the user's first time viewing a note
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
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
                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();
			}
		});
	}

    // Register and unregister DeleteNotesReceiver
    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(receiver);
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.note_view, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// Override default Android "up" behavior to instead mimic the back button
				getActivity().onBackPressed();
				return true;

				// Edit button
			case R.id.action_edit:
                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();

				return true;

				// Delete button
			case R.id.action_delete:
				listener.showDeleteDialog();
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
				if(intent.resolveActivity(getActivity().getPackageManager()) != null)
					startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

				return true;             
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void deleteNote(String filename) {		
		// Build the pathname to delete file, then perform delete operation
		File fileToDelete = new File(getActivity().getFilesDir() + "/" + filename);
		fileToDelete.delete();
	}

	// Loads note from /data/data/com.farmerbb.notepad/files
	private StringBuffer loadNote(String filename) throws IOException {

		// Initialize StringBuffer which will contain note
		StringBuffer note = new StringBuffer("");

		// Open the file on disk
		FileInputStream input = getActivity().openFileInput(filename);
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

    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

	public void onDeleteDialogPositiveClick() {
		// User touched the dialog's positive button
		deleteNote(filename);
		showToast(R.string.note_deleted);
		
		// Add NoteListFragment
		getFragmentManager()
			.beginTransaction()
			.replace(R.id.noteViewEdit, new NoteListFragment(), "NoteListFragment")
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
			.commit();
	}
	
	// Nested class used to listen for keyboard shortcuts
        public void dispatchKeyShortcutEvent(int keyCode) {
			switch(keyCode){

					// CTRL+E: Edit
				case KeyEvent.KEYCODE_E:
                    Bundle bundle = new Bundle();
                    bundle.putString("filename", filename);

                    Fragment fragment = new NoteEditFragment();
                    fragment.setArguments(bundle);

					getFragmentManager()
						.beginTransaction()
						.replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
						.commit();
					break;

					// CTRL+D: Delete
				case KeyEvent.KEYCODE_D:
					// Show delete dialog
					listener.showDeleteDialog();
					break;

					// CTRL+H: Share
				case KeyEvent.KEYCODE_H:
					// Set current note contents to a String
					String contents = noteContents.getText().toString();

					// Send a share intent
					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.putExtra(Intent.EXTRA_TEXT, contents);
					shareIntent.setType("text/plain");

					// Verify that the intent will resolve to an activity, and send
					if(shareIntent.resolveActivity(getActivity().getPackageManager()) != null)
						startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

					break;
			}
        }
	
		public void onBackPressed() {
			// Add NoteListFragment
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.noteViewEdit, new NoteListFragment(), "NoteListFragment")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
				.commit();
		}
		
	public String getFilename() {
		return getArguments().getString("filename");
	}
}
