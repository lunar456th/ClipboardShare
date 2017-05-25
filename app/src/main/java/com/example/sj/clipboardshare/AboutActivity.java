package com.example.sj.clipboardshare;

import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class AboutActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        Preference github = findPreference("github");
        Preference playstore = findPreference("playstore");
        Preference naver = findPreference("naver");

        github.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/lunar456th/ClipboardShare"));
                startActivity(intent);
                return true;
            }
        });

        playstore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ClipboardShare"));
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
    }
}
