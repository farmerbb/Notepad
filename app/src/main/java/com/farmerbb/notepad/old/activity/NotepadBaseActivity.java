package com.farmerbb.notepad.old.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.view.Window;

import com.farmerbb.notepad.R;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

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

    protected boolean thereIsNoSpoon(Class<? extends ComponentActivity> redPillActivity) {
        File migrationComplete = new File(getFilesDir(), "migration_complete");
        if (migrationComplete.exists()) {
            Intent intent = new Intent(this, redPillActivity);
            intent.putExtras(getIntent());
            intent.setAction(getIntent().getAction());
            intent.setType(getIntent().getType());

            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);

            return true;
        }

        return false;
    }
}
