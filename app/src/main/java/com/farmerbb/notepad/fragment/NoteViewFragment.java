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

package com.farmerbb.notepad.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.FileUriExposedException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.farmerbb.notepad.R;
import com.farmerbb.notepad.fragment.dialog.FirstViewDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import us.feras.mdv.MarkdownView;

public class NoteViewFragment extends Fragment {

    private MarkdownView markdownView;

    String filename = "";
    String contentsOnLoad = "";
    int firstLoad;
    boolean showMessage = true;

    // Receiver used to close fragment when a note is deleted
    public class DeleteNotesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] filesToDelete = intent.getStringArrayExtra("files");

            for(Object file : filesToDelete) {
                if(filename.equals(file)) {
                    // Add NoteListFragment or WelcomeFragment
                    Fragment fragment;
                    if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                        fragment = new NoteListFragment();
                    else
                        fragment = new WelcomeFragment();

                    getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                }
            }
        }
    }

    IntentFilter filter = new IntentFilter("com.farmerbb.notepad.DELETE_NOTES");
    DeleteNotesReceiver receiver = new DeleteNotesReceiver();

    /* The activity that creates an instance of this fragment must
     * implement this interface in order to receive event call backs. */
    public interface Listener {
        void showDeleteDialog();
        String loadNote(String filename) throws IOException;
        String loadNoteTitle(String filename) throws IOException;
        void exportNote(String filename);
        void printNote(String contentToPrint);
    }

    // Use this instance of the interface to deliver action events
    Listener listener;

    // Override the Fragment.onAttach() method to instantiate the Listener
    @SuppressWarnings("deprecation")
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
        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        return inflater.inflate(
                pref.getBoolean("markdown", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? R.layout.fragment_note_view_md
                        : R.layout.fragment_note_view, container, false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set values
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // Get filename of saved note
        filename = getArguments().getString("filename");

        // Change window title
        String title;

        try {
            title = listener.loadNoteTitle(filename);
        } catch (IOException e) {
            title = getResources().getString(R.string.view_note);
        }

        getActivity().setTitle(title);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.ic_recents_logo)).getBitmap();

            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(title, bitmap, ContextCompat.getColor(getActivity(), R.color.primary));
            getActivity().setTaskDescription(taskDescription);
        }

        // Show the Up button in the action bar.
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Animate elevation change
        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout noteViewEdit = getActivity().findViewById(R.id.noteViewEdit);
            LinearLayout noteList = getActivity().findViewById(R.id.noteList);

            noteList.animate().z(0f);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation_land));
            else
                noteViewEdit.animate().z(getResources().getDimensionPixelSize(R.dimen.note_view_edit_elevation));
        }

        // Set up content view
        TextView noteContents = getActivity().findViewById(R.id.textView);
        markdownView = getActivity().findViewById(R.id.markdownView);

        // Apply theme
        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        ScrollView scrollView = getActivity().findViewById(R.id.scrollView);
        String theme = pref.getString("theme", "light-sans");
        int textSize = -1;
        int textColor = -1;

        String fontFamily = null;

        if(theme.contains("light")) {
            if(noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_primary));
                noteContents.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
            }

            if(markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
                textColor = ContextCompat.getColor(getActivity(), R.color.text_color_primary);
            }

            scrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background));
        }

        if(theme.contains("dark")) {
            if(noteContents != null) {
                noteContents.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_primary_dark));
                noteContents.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
            }

            if(markdownView != null) {
                markdownView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
                textColor = ContextCompat.getColor(getActivity(), R.color.text_color_primary_dark);
            }

            scrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.window_background_dark));
        }

        if(theme.contains("sans")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.SANS_SERIF);

            if(markdownView != null)
                fontFamily = "sans-serif";
        }

        if(theme.contains("serif")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.SERIF);

            if(markdownView != null)
                fontFamily = "serif";
        }

        if(theme.contains("monospace")) {
            if(noteContents != null)
                noteContents.setTypeface(Typeface.MONOSPACE);

            if(markdownView != null)
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

        if(noteContents != null)
            noteContents.setTextSize(textSize);

        String css = "";
        if(markdownView != null) {
            String topBottom = " " + Float.toString(getResources().getDimension(R.dimen.padding_top_bottom) / getResources().getDisplayMetrics().density) + "px";
            String leftRight = " " + Float.toString(getResources().getDimension(R.dimen.padding_left_right) / getResources().getDisplayMetrics().density) + "px";
            String fontSize = " " + Integer.toString(textSize) + "px";
            String fontColor = " #" + StringUtils.remove(Integer.toHexString(textColor), "ff");
            String linkColor = " #" + StringUtils.remove(Integer.toHexString(new TextView(getActivity()).getLinkTextColors().getDefaultColor()), "ff");

            css = "body { " +
                    "margin:" + topBottom + topBottom + leftRight + leftRight + "; " +
                    "font-family:" + fontFamily + "; " +
                    "font-size:" + fontSize + "; " +
                    "color:" + fontColor + "; " +
                    "}" +
                    "a { " +
                    "color:" + linkColor + "; " +
                    "}";

            markdownView.getSettings().setJavaScriptEnabled(false);
            markdownView.getSettings().setLoadsImagesAutomatically(false);
            markdownView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException | FileUriExposedException e) { /* Gracefully fail */ }
                    else
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) { /* Gracefully fail */ }

                    return true;
                }
            });
        }

        // Load note contents
        try {
            contentsOnLoad = listener.loadNote(filename);
        } catch (IOException e) {
            showToast(R.string.error_loading_note);

            // Add NoteListFragment or WelcomeFragment
            Fragment fragment;
            if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                fragment = new NoteListFragment();
            else
                fragment = new WelcomeFragment();

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }

        // Set TextView contents
        if(noteContents != null)
            noteContents.setText(contentsOnLoad);

        if(markdownView != null)
            markdownView.loadMarkdown(contentsOnLoad,
                    "data:text/css;base64," + Base64.encodeToString(css.getBytes(), Base64.DEFAULT));

        // Show a toast message if this is the user's first time viewing a note
        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
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

        // Detect single and double-taps using GestureDetector
        final GestureDetector detector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {}

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
        });

        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(sharedPref.getBoolean("show_double_tap_message", true)) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("show_double_tap_message", false);
                    editor.apply();
                }

                Bundle bundle = new Bundle();
                bundle.putString("filename", filename);

                Fragment fragment = new NoteEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.noteViewEdit, fragment, "NoteEditFragment")
                        .commit();

                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(sharedPref.getBoolean("show_double_tap_message", true) && showMessage) {
                    showToastLong(R.string.double_tap);
                    showMessage = false;
                }

                return false;
            }

        });

        if(noteContents != null)
            noteContents.setOnTouchListener((v, event) -> {
                detector.onTouchEvent(event);
                return false;
            });

        if(markdownView != null)
            markdownView.setOnTouchListener((v, event) -> {
                detector.onTouchEvent(event);
                return false;
            });
    }

    // Register and unregister DeleteNotesReceiver
    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(markdownView != null && markdownView.canGoBack())
            markdownView.goBack();
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
                // Send a share intent
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, contentsOnLoad);
                intent.setType("text/plain");

                // Verify that the intent will resolve to an activity, and send
                if(intent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));

                return true;

            // Export menu item
            case R.id.action_export:
                listener.exportNote(filename);
                return true;

            // Print menu item
            case R.id.action_print:
                listener.printNote(contentsOnLoad);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteNote(String filename) {
        // Build the pathname to delete file, then perform delete operation
        File fileToDelete = new File(getActivity().getFilesDir() + File.separator + filename);
        fileToDelete.delete();
    }

    private void showToast(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showToastLong(int message) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(message), Toast.LENGTH_LONG);
        toast.show();
    }

    public void onDeleteDialogPositiveClick() {
        // User touched the dialog's positive button
        deleteNote(filename);
        showToast(R.string.note_deleted);

        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-large")) {
            // Send broadcast to NoteListFragment to refresh list of notes
            Intent listNotesIntent = new Intent();
            listNotesIntent.setAction("com.farmerbb.notepad.LIST_NOTES");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(listNotesIntent);
        }

        // Add NoteListFragment or WelcomeFragment
        Fragment fragment;
        if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
            fragment = new NoteListFragment();
        else
            fragment = new WelcomeFragment();

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
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
                    // Send a share intent
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contentsOnLoad);
                    shareIntent.setType("text/plain");

                    // Verify that the intent will resolve to an activity, and send
                    if(shareIntent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

                    break;
            }
        }

        public void onBackPressed() {
            // Add NoteListFragment or WelcomeFragment
            Fragment fragment;
            if(getActivity().findViewById(R.id.layoutMain).getTag().equals("main-layout-normal"))
                fragment = new NoteListFragment();
            else
                fragment = new WelcomeFragment();

            getFragmentManager()
                .beginTransaction()
                .replace(R.id.noteViewEdit, fragment, "NoteListFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        }

    public String getFilename() {
        return getArguments().getString("filename");
    }
}
