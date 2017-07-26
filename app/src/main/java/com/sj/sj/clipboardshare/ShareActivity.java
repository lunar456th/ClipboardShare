package com.sj.sj.clipboardshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sj.sj.clipboardshare.ClipboardManager.ClipboardAdapter;
import com.sj.sj.clipboardshare.SNSAccountManager.GoogleAccountManager;
import com.sj.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareActivity extends AppCompatActivity {

    private static final int CODE_GOOGLE_SHARE_DIALOG = 4444;
    private static final int TWITTER_LENGTH_LIMIT = 140;

    TwitterAccountManager twitterAccountManager;
    GoogleAccountManager googleAccountManager;
    ClipboardAdapter clipboardAdapter;
    SelectManager selectManager;

    TextView twitterProgress;
    TextView googlePlusProgress;

    private int count;
    private int numOfShared = 0;
    private int twitterSize;
    private int googlePlusSize;

    ArrayList<String> statusList;

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

        twitterSize = clipboardAdapter.getCount();

        statusList = new ArrayList<>();
        for(int i = 0; i < clipboardAdapter.getCount(); i++) {
            String status = clipboardAdapter.getItem(i).getString();
            if(status.contains("리트윗") || status.contains("RETWEET") || status.contains("Retweet") || status.contains("retweet") || status.contains("ReTweet") || status.contains("reTweet")) {
                continue;
            } else {
                statusList.add(status);
            }
        }

        googlePlusSize = statusList.size();

        if(selectManager.getTwitter()) {
            boolean isLengthLimit = false;
            for (int i = 0; i < twitterSize; i++) {
                String status = clipboardAdapter.getItem(i).getString();

                if(status.contains("리트윗") || status.contains("RETWEET") || status.contains("Retweet") || status.contains("retweet") || status.contains("ReTweet") || status.contains("reTweet")) {
                    twitterAccountManager.retweet(getStatusId(getUrl(status)));
                }
                else {
                    if(status.length() <= TWITTER_LENGTH_LIMIT) {
                        twitterAccountManager.share(status);
                    } else {
                        isLengthLimit = true;
                    }
                }

                twitterProgress.setText(i + 1 + "/" + twitterSize);

            }
            if(isLengthLimit) {
                Toast.makeText(this, getString(R.string.shared_twitter) + " " + getString(R.string.excluded_from_sharing), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.shared_twitter), Toast.LENGTH_SHORT).show();
            }
        }

        int per_one = 3;
        if(selectManager.getGoogle()) {
            for (int i = 0; i < (googlePlusSize < per_one ? googlePlusSize : per_one); i++) {
                String status = statusList.get(count++);
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
                if (count == googlePlusSize) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
                if (resultCode == RESULT_OK) {
                    googlePlusProgress.setText(++numOfShared + "/" + googlePlusSize);
                    if(count < googlePlusSize) {
                        String status = statusList.get(count++);
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

    public static String getUrl(String str){
        StringBuffer sb = new StringBuffer();
        String regex ="[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

        Pattern p = Pattern.compile(regex);
        Matcher m=p.matcher(str);

        if(m.find()){
            sb.append(m.group(0));
        }
        return m.group(0);
    }

    public static String getStatusId(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
