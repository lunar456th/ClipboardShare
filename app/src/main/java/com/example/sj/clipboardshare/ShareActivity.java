package com.example.sj.clipboardshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sj.clipboardshare.ClipboardManager.ClipboardAdapter;
import com.example.sj.clipboardshare.SNSAccountManager.GoogleAccountManager;
import com.example.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;

public class ShareActivity extends AppCompatActivity {

    private static final int CODE_GOOGLE_SHARE_DIALOG = 8031;

    TwitterAccountManager twitterAccountManager;
    GoogleAccountManager googleAccountManager;
    ClipboardAdapter clipboardAdapter;

    private int count = 0;
    private int size;
    private int per_one = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        twitterAccountManager = TwitterAccountManager.getInstance(this);
        googleAccountManager = GoogleAccountManager.getInstance(this);
        clipboardAdapter = ClipboardAdapter.getInstance(this);

        count = 0;
        size = clipboardAdapter.getCount();

        for(int i = 0; i < size; i++) {
            String status = clipboardAdapter.getItem(i).getString();
            twitterAccountManager.share(status);
        }

        for(int i = 0; i < (size < per_one ? size : per_one); i++) {
            String status = clipboardAdapter.getItem(count++).getString();
            startActivityForResult(googleAccountManager.getSharePostIntent(status), CODE_GOOGLE_SHARE_DIALOG);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_GOOGLE_SHARE_DIALOG:
                if (resultCode == RESULT_CANCELED) {
                    break;
                }
                if (count >= size) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    String status = clipboardAdapter.getItem(count++).getString();
                    startActivityForResult(googleAccountManager.getSharePostIntent(status), CODE_GOOGLE_SHARE_DIALOG);
                }
                break;

            default:
                break;
        }
    }
}
