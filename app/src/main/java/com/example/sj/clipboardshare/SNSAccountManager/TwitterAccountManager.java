package com.example.sj.clipboardshare.SNSAccountManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.example.sj.clipboardshare.R;
import com.example.sj.clipboardshare.WebViewActivity;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static android.content.ContentValues.TAG;

public class TwitterAccountManager implements AccountManager {

    private static final String PREF_NAME = "twitter_prefname";
    private static final String PREF_KEY_OAUTH_TOKEN = "twitter_oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "twitter_oauth_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "twitter_isloggedin";
    private static final String PREF_USER_NAME = "twitter_username";

    private Context context;
    private static TwitterAccountManager instance;
    private static SharedPreferences sharedPreferences;

    public static TwitterAccountManager getInstance(Context context) {
        if (instance == null) {
            instance = new TwitterAccountManager(context.getApplicationContext());
        }
        return instance;
    }

    private TwitterAccountManager(Context context) {
        this.context = context;

        consumerKey = context.getString(R.string.twitter_consumer_key);
        consumerSecret = context.getString(R.string.twitter_consumer_secret);
        callbackUrl = context.getString(R.string.twitter_callbackUrl);
        oAuthVerifier = context.getString(R.string.twitter_oauth_verifier);

        if (!isOAuthConfigured()) return;

        sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);

    }


    public String getUserName() {
        return sharedPreferences.getString(PREF_USER_NAME, "");
    }

    private static String consumerKey;
    private static String consumerSecret;
    private static String callbackUrl;

    private static String oAuthVerifier;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private static User userInfo;

    private boolean isOAuthConfigured() {
        if(TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Log.e(TAG, "Twitter Consumer Key or Secret is not configured.");
            return false;
        } else {
            return false;
        }
    }

    @Override
    public Intent getLoginIntent() {
        if(!isLoggedIn()) {
            final Configuration configuration = new ConfigurationBuilder()
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .build();

            twitter = new TwitterFactory(configuration).getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
            return intent;
        }
        return null;
    }

    @Override
    public boolean isLoggedIn() {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
        return sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    public void logout() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.apply();

        CookieManager.getInstance().removeAllCookies(null); // 쿠키 삭제(트위터 로그인 정보 삭제)

        userInfo = null;
    }

    public void share(String status) {
        new updateTwitterStatus().execute(status);
    }

    private void saveTwitterInfo(AccessToken accessToken) {
        long userId = accessToken.getUserId();

        try {
            userInfo = twitter.showUser(userId);
            String username = userInfo.getName();

            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.apply(); // 원래 e.commit().
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void handleLoginResult(Intent data) {
        String verifier = data.getExtras().getString(oAuthVerifier);
        try {
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

            long userId = accessToken.getUserId();
            userInfo = twitter.showUser(userId);

            saveTwitterInfo(accessToken);

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private class updateTwitterStatus extends AsyncTask<String, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            String status = params[0];
            try {
                Configuration configuration = new ConfigurationBuilder()
                        .setOAuthConsumerKey(consumerKey)
                        .setOAuthConsumerSecret(consumerSecret)
                        .build();

                String access_token = sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                String access_token_secret = sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(configuration).getInstance(accessToken);
                StatusUpdate statusUpdate = new StatusUpdate(status);

                twitter.updateStatus(statusUpdate);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context, "트위터에 공유하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

}

// Toast.makeText(MainActivity.this, "이미 로그인되어있습니다.", Toast.LENGTH_SHORT).show();
