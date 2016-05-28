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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class ImportListAdapter extends ArrayAdapter<ImportableNote> {
    private final List<ImportableNote> list;
    private final ImportActivity context;

    public ImportListAdapter(ImportActivity context, List<ImportableNote> list) {
        super(context, R.layout.row_layout_import, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Workaround for CheckBoxes being recycled when put into a ScrollView.
        // Based on sample code from http://www.lalit3686.blogspot.in/2012/06/today-i-am-going-to-show-how-to-deal.html

        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row_layout_import, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.noteTitleImport);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();
                    list.get(getPosition).setSelected(buttonView.isChecked());

                    context.onCheckedChanged(list.get(getPosition).getName(), buttonView.isChecked());
                }
            });

            convertView.setTag(viewHolder);
            convertView.setTag(R.id.noteTitleImport, viewHolder.text);
            convertView.setTag(R.id.checkBox, viewHolder.checkbox);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.checkbox.setTag(position);
        viewHolder.text.setText(list.get(position).getName());
        viewHolder.checkbox.setChecked(list.get(position).isSelected());

        return convertView;
    }
}