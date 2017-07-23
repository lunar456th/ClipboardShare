package com.sj.sj.clipboardshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.widget.Toast;

public class AboutActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_about);

        Preference github = findPreference("github");
        Preference playstore = findPreference("playstore");
        Preference naver = findPreference("naver");
        Preference version = findPreference("version");

        github.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/lunar456th/ClipboardShare"));
                startActivity(intent);
                return true;
            }
        });

        playstore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sj.sj.ClipboardShare"));
                startActivity(intent);
                return true;
            }
        });

        naver.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String uriText =
                        "mailto:lunar456th@naver.com" +
                                "?subject=" + Uri.encode("Some subject text here") +
                                "&body=" + Uri.encode("Some text here");
                Uri uri = Uri.parse(uriText);
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "Send E-mail"));
                return true;
            }
        });

        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                final Toast toast = Toast.makeText(getBaseContext(), getString(R.string.hello), Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);
                return true;
            }
        });
    }
}
