package com.sj.sj.clipboardshare;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SelectManager {
    private static SelectManager instance;
    private static SharedPreferences sharedPreferences;

    private static final String PREF_TWITTER_SELECT = "twitter_select";
    private static final String PREF_GOOGLE_PLUS_SELECT = "google_plus_select";

    public static SelectManager getInstance(Context context) {
        if (instance == null) {
            instance = new SelectManager();
            sharedPreferences = context.getSharedPreferences("Select", MODE_PRIVATE);
            if(!sharedPreferences.contains(PREF_TWITTER_SELECT)) {
                instance.setTwitter(false);
            }
            if(!sharedPreferences.contains(PREF_GOOGLE_PLUS_SELECT)) {
                instance.setGoogle(false);
            }
        }
        return instance;
    }

    public boolean getTwitter() {
        return sharedPreferences.getBoolean(PREF_TWITTER_SELECT, true);
    }

    public boolean getGoogle() {
        return sharedPreferences.getBoolean(PREF_GOOGLE_PLUS_SELECT, true);
    }

    void setTwitter(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_TWITTER_SELECT);
        editor.putBoolean(PREF_TWITTER_SELECT, status);
        editor.apply();
    }

    void setGoogle(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_GOOGLE_PLUS_SELECT);
        editor.putBoolean(PREF_GOOGLE_PLUS_SELECT, status);
        editor.apply();
    }
}
