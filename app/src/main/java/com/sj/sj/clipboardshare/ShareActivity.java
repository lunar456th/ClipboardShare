package com.sj.sj.clipboardshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sj.sj.clipboardshare.ClipboardManager.ClipboardAdapter;
import com.sj.sj.clipboardshare.SNSAccountManager.GoogleAccountManager;
import com.sj.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;

public class ShareActivity extends AppCompatActivity {

    private static final int CODE_GOOGLE_SHARE_DIALOG = 4444;

    TwitterAccountManager twitterAccountManager;
    GoogleAccountManager googleAccountManager;
    ClipboardAdapter clipboardAdapter;
    SelectManager selectManager;

    TextView twitterProgress;
    TextView googlePlusProgress;

    private int count;
    private int numOfShared = 0;
    private int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        twitterProgress = (TextView)findViewById(R.id.sharing_twitter);
        googlePlusProgress = (TextView)findViewById(R.id.sharing_google_plus);

        twitterAccountManager = TwitterAccountManager.getInstance(this);
        googleAccountManager = GoogleAccountManager.getInstance(this);
        clipboardAdapter = ClipboardAdapter.getInstance(this);
        selectManager = SelectManager.getInstance(this);

        count = 0;
        size = clipboardAdapter.getCount();

        if(selectManager.getTwitter()) {
            for (int i = 0; i < size; i++) {
                String status = clipboardAdapter.getItem(i).getString();
                twitterAccountManager.share(status);
                twitterProgress.setText(i + 1 + "/" + size);
            }
            Toast.makeText(this, getString(R.string.shared_twitter), Toast.LENGTH_SHORT).show();
        }

        int per_one = 3;
        if(selectManager.getGoogle()) {
            for (int i = 0; i < (size < per_one ? size : per_one); i++) {
                String status = clipboardAdapter.getItem(count++).getString();
                startActivityForResult(googleAccountManager.getSharePostIntent(status), CODE_GOOGLE_SHARE_DIALOG);
            }
        } else {
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_GOOGLE_SHARE_DIALOG:
                if (count == size) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
                if (resultCode == RESULT_OK) {
                    googlePlusProgress.setText(++numOfShared + "/" + size);
                    if(count < size) {
                        String status = clipboardAdapter.getItem(count++).getString();
                        startActivityForResult(googleAccountManager.getSharePostIntent(status), CODE_GOOGLE_SHARE_DIALOG);
                    }
                } else {
                    Intent intent = getIntent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    break;
                }
            default:
                break;
        }
    }
}
