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

package com.farmerbb.notepad.fragment.dialog;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.farmerbb.notepad.BuildConfig;
import com.farmerbb.notepad.R;
import com.farmerbb.notepad.util.SignatureUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.TimeZone;

public class AboutDialogFragment extends DialogFragment {

    TextView textView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.fragment_dialogs, null);

        builder.setView(view)
        .setTitle(R.string.dialog_about_title)
        .setPositiveButton(R.string.action_close, null);

        SignatureUtils.ReleaseType releaseType = SignatureUtils.getReleaseType(getActivity());
        if(!releaseType.equals(SignatureUtils.ReleaseType.UNKNOWN)) {
            builder.setNegativeButton(R.string.check_for_updates,
                    (dialogInterface, i) -> checkForUpdates(releaseType));
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Denver"));
        calendar.setTimeInMillis(BuildConfig.TIMESTAMP);

        int year = calendar.get(Calendar.YEAR);

        textView = view.findViewById(R.id.dialogMessage);
        textView.setText(getString(R.string.dialog_about_message, year));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void checkForUpdates(SignatureUtils.ReleaseType releaseType) {
        String url = "";

        switch(releaseType) {
            case PLAY_STORE:
                url = isPlayStoreInstalled()
                    ? "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                    : "https://github.com/farmerbb/Notepad/releases";
                break;
            case AMAZON:
                url = "https://www.amazon.com/gp/mas/dl/android?p=" + BuildConfig.APPLICATION_ID;
                break;
            case F_DROID:
                url = "https://f-droid.org/repository/browse/?fdid=" + BuildConfig.APPLICATION_ID;
                break;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) { /* Gracefully fail */ }
    }

    private boolean isPlayStoreInstalled() {
        try {
            getActivity().getPackageManager().getPackageInfo("com.android.vending", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
