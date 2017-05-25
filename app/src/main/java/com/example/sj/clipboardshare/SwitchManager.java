package com.example.sj.clipboardshare;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SwitchManager {

    private static SwitchManager instance;

    private static SharedPreferences mSwitch;

    private static String key = "Activate";

    public static SwitchManager getInstance(Context context) {
        if (instance == null) {
            instance = new SwitchManager();
            mSwitch = context.getSharedPreferences("Switch", MODE_PRIVATE);
            if(!mSwitch.contains(key)) {
                instance.setStatus(false);
            }
        }
        return instance;
    }

    public boolean getStatus() {
        return mSwitch.getBoolean(key, true);
    }

    void setStatus(boolean status) {
        SharedPreferences.Editor editor = mSwitch.edit();
        editor.remove(key);
        editor.putBoolean(key, status);
        editor.apply();
    }
}
