package com.example.noteapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.noteapp.R;

public final class ThemeModeManager {

    private static final String PREFS_NAME = "noteapp_theme";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_CUSTOM_THEME = "custom_theme";

    // 自定义主题常量
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    public static final int THEME_EYE_CARE = 3;

    private ThemeModeManager() {
    }

    public static void applySavedMode(Context context) {
        int customTheme = getCustomTheme(context);
        if (customTheme == THEME_EYE_CARE) {
            // 护眼模式使用白天模式基础 + 自定义主题
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            int mode = getSavedMode(context);
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    /**
     * 应用自定义主题到Activity（须在setContentView之前调用）
     */
    public static void applyTheme(Activity activity) {
        int customTheme = getCustomTheme(activity);
        if (customTheme == THEME_EYE_CARE) {
            activity.setTheme(R.style.Theme_NoteApp_EyeCare);
        }
    }

    public static void setMode(Context context, int mode) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void setCustomTheme(Context context, int theme) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_CUSTOM_THEME, theme).apply();

        if (theme == THEME_EYE_CARE) {
            // 护眼模式强制白天 + 自定义主题
            preferences.edit().putInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO).apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == THEME_LIGHT) {
            setMode(context, AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == THEME_DARK) {
            setMode(context, AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme == THEME_SYSTEM) {
            setMode(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static int getCustomTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_CUSTOM_THEME, THEME_LIGHT);
    }

    public static int getSavedMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
    }
}
