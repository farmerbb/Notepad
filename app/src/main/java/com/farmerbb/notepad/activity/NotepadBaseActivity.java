package com.farmerbb.notepad.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.farmerbb.notepad.R;

public abstract class NotepadBaseActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            SharedPreferences pref = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
            String theme = pref.getString("theme", "light-sans");

            int navbarColorId = -1;
            int navbarDividerColorId = -1;
            int sysUiVisibility = -1;

            if(theme.contains("light")) {
                navbarColorId = R.color.navbar_color_light;
                navbarDividerColorId = R.color.navbar_divider_color_light;
                sysUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }

            if(theme.contains("dark")) {
                navbarColorId = R.color.navbar_color_dark;
                navbarDividerColorId = R.color.navbar_divider_color_dark;
                sysUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            getWindow().setNavigationBarColor(ContextCompat.getColor(this, navbarColorId));
            findViewById(Window.ID_ANDROID_CONTENT).setSystemUiVisibility(sysUiVisibility);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                getWindow().setNavigationBarDividerColor(ContextCompat.getColor(this, navbarDividerColorId));
        }
    }
}
