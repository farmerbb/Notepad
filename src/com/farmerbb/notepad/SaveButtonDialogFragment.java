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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SaveButtonDialogFragment extends DialogFragment {

	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event call backs.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface NoticeDialogListener {
		public void onSaveDialogPositiveClick(DialogFragment dialog);
		public void onSaveDialogNegativeClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	NoticeDialogListener listener;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			listener = (NoticeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.dialog_save_changes)
		.setTitle(R.string.dialog_save_button_title)
		.setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				listener.onSaveDialogPositiveClick(SaveButtonDialogFragment.this);
			}
		})
		.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				listener.onSaveDialogNegativeClick(SaveButtonDialogFragment.this);
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}