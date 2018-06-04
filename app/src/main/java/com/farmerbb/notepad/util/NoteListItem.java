/* Copyright 2015 Sean93Park
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

package com.farmerbb.notepad.util;

import java.text.Collator;
import java.util.Comparator;

public class NoteListItem {
    private String note;
    private String date;

    public NoteListItem(String note, String date) {
        this.note = note;
        this.date = date;
    }

    public String getNote() {
      return note;
    }

    public String getDate() {
        return date;
    }

    public static Comparator<NoteListItem> NoteComparatorTitle = (arg1, arg2) -> Collator.getInstance().compare(arg1.getNote(), arg2.getNote());
}
