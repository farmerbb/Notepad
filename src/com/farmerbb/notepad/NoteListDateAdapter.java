/* Copyright 2014 Braden Farmer
 * Copyright 2015 Sean93Park
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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteListDateAdapter extends ArrayAdapter<NoteListItem> {
    public NoteListDateAdapter(Context context, ArrayList<NoteListItem> notes) {
        super(context, R.layout.row_layout_date, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        NoteListItem item = getItem(position);
        String note = item.getNote();
        String date = item.getDate();

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null)
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_date, parent, false);

        // Lookup view for data population
        TextView noteTitle = (TextView) convertView.findViewById(R.id.noteTitle);
        TextView noteDate = (TextView) convertView.findViewById(R.id.noteDate);

        // Populate the data into the template view using the data object
        noteTitle.setText(note);
        noteDate.setText(date);

        // Apply theme
        SharedPreferences pref = getContext().getSharedPreferences(getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        String theme = pref.getString("theme", "light-sans");

        if(theme.contains("light")) {
            noteTitle.setTextColor(getContext().getResources().getColor(R.color.text_color_primary));
            noteDate.setTextColor(getContext().getResources().getColor(R.color.text_color_secondary));
        }

        if(theme.contains("dark")) {
            noteTitle.setTextColor(getContext().getResources().getColor(R.color.text_color_primary_dark));
            noteDate.setTextColor(getContext().getResources().getColor(R.color.text_color_secondary_dark));
        }

        if(theme.contains("sans")) {
            noteTitle.setTypeface(Typeface.SANS_SERIF);
            noteDate.setTypeface(Typeface.SANS_SERIF);
        }

        if(theme.contains("serif")) {
            noteTitle.setTypeface(Typeface.SERIF);
            noteDate.setTypeface(Typeface.SERIF);
        }

        if(theme.contains("monospace")) {
            noteTitle.setTypeface(Typeface.MONOSPACE);
            noteDate.setTypeface(Typeface.MONOSPACE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}