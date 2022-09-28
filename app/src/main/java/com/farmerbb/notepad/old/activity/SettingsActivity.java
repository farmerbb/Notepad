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

package com.farmerbb.notepad.old.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import com.farmerbb.notepad.BuildConfig;
import com.farmerbb.notepad.R;
import com.farmerbb.notepad.android.NotepadActivity;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Add preferences
        addPreferencesFromResource(R.xml.settings_preferences);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("theme"));
        bindPreferenceSummaryToValue(findPreference("font_size"));
        bindPreferenceSummaryToValue(findPreference("sort_by"));
        bindPreferenceSummaryToValue(findPreference("export_filename"));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addPreferencesFromResource(R.xml.settings_preferences_md);
            SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
            findPreference("direct_edit").setOnPreferenceChangeListener(this);
            findPreference("direct_edit").setEnabled(!pref.getBoolean("markdown", false));

            findPreference("markdown").setOnPreferenceChangeListener(this);
            findPreference("markdown").setEnabled(!pref.getBoolean("direct_edit", false));
        }

        boolean userIsNeo;
        if(BuildConfig.DEBUG)
            userIsNeo = true;
        else
            userIsNeo = BuildConfig.VERSION_NAME.startsWith("3");

        if(!userIsNeo) {
            getPreferenceScreen().removePreference(findPreference("morpheus"));
            return;
        }

        findPreference("morpheus").setOnPreferenceClickListener(preference -> {
            Spanned message = Html.fromHtml(getString(R.string.preview_app_message, BuildConfig.VERSION_CODE));

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle(R.string.preview_app_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.blue_pill, null)
                    .setPositiveButton(R.string.red_pill, (dialog, which) -> enterTheMatrix());

            AlertDialog dialog = builder.create();
            dialog.show();

            Button redPill = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            redPill.setTextColor(Color.RED);

            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setMovementMethod(LinkMovementMethod.getInstance());

            return true;
        });
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if(preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }

        return true;
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
        .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                                ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        switch(preference.getKey()) {
            case "direct_edit":
                findPreference("markdown").setEnabled(!(Boolean) value);
                break;
            case "markdown":
                findPreference("direct_edit").setEnabled(!(Boolean) value);
                break;
        }

        return true;
    }

    public void enterTheMatrix() {
        Intent restartIntent = new Intent(this, NotepadActivity.class);
        restartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartIntent);
        overridePendingTransition(0, 0);
    }
}
