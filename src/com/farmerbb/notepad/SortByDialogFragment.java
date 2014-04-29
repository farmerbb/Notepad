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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SortByDialogFragment extends DialogFragment {

	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event call backs.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface NoticeDialogListener {
		public void onSortOptionSelect(DialogFragment dialog);
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

		// Get existing preference; if no preference exists, set default 
		// 0 is "by date", 1 is "by name"

		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		int sortBy = sharedPref.getInt("sort-by", 0);

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.action_sort_by)
		.setSingleChoiceItems(R.array.sort_by_list, sortBy, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// which: 0 is Date, 1 is Name
				// Set preference
				SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putInt("sort-by", which);
				editor.commit();

				// Refresh list
				listener.onSortOptionSelect(SortByDialogFragment.this);

				// Dismiss dialog
				dismiss();

			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}