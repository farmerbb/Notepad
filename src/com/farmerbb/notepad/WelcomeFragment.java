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

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.app.*;
import com.melnykov.fab.*;
import android.view.*;

public class WelcomeFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_welcome_alt, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Set values
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();

        // Change window title
        getActivity().setTitle(getResources().getString(R.string.app_name));

        // Don't show the Up button in the action bar, and disable the button
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setHomeButtonEnabled(false);

        // Floating action button
        FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.button_floating_action);
        floatingActionButton.hide(false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
	
	public void dispatchKeyShortcutEvent(int keyCode) {
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
	
	public void onBackPressed() {
		getActivity().finish();
	}
	
	public void hideFab() {
		FloatingActionButton floatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.button_floating_action);
		floatingActionButton.hide();
	}
}
