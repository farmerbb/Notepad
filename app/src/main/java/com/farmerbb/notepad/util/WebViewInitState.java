/* Copyright 2018 Braden Farmer
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.WebView;

public class WebViewInitState {
    private static WebViewInitState instance = new WebViewInitState();
    private boolean isInitialized = false;
    
    private WebViewInitState() {}

    public static WebViewInitState getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        if(isInitialized) return;

        SharedPreferences pref = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        if(pref.getBoolean("markdown", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView webView = new WebView(context);
            webView.loadUrl("about:blank");

            isInitialized = true;
        }
    }
}