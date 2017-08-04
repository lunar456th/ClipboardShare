package com.sj.sj.clipboardshare.SNSAccountManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.sj.sj.clipboardshare.R;
import com.sj.sj.clipboardshare.WebViewActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAccountManager {

    private static final String PREF_NAME = "twitter_pref_name";
    private static final String PREF_COUNT = "twitter_pref_count";
    private static final String PREF_KEY_OAUTH_TOKEN = "twitter_oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "twitter_oauth_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "twitter_is_logged_in";
    private static final String PREF_USER_PROFILE_IMAGE_URL = "twitter_profile_image_url";
    private static final String PREF_USER_NAME = "twitter_user_name";
    private static final String PREF_USER_SCREEN_NAME = "twitter_user_screen_name";
    private static final String PREF_USER_ACTIVATED = "twitter_activated";

    private static String consumerKey;
    private static String consumerSecret;
    private static String callbackUrl;
    private static String oAuthVerifier;
    private static Twitter twitter;
    private static RequestToken requestToken;

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

        sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
        if(!sharedPreferences.contains(PREF_COUNT)) {
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putInt(PREF_COUNT, 0).apply();
        }
        if (!isOAuthConfigured()) return;
    }

    public Drawable getProfileImage(int position) throws IOException {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        String string = mSharedPref.getString(PREF_USER_PROFILE_IMAGE_URL, "");
        URL url = new URL(string);
        URLConnection conn = url.openConnection();
        conn.connect();
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
        Bitmap bitmap = BitmapFactory.decodeStream(bis);
        bis.close();
        return new BitmapDrawable(bitmap); // return Drawable
    }

    public String getUserName(int position) {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        return mSharedPref.getString(PREF_USER_NAME, "");
    }

    public String getUserScreenName(int position) {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        return mSharedPref.getString(PREF_USER_SCREEN_NAME, "");
    }

    public boolean getUserActivated(int position) {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        return mSharedPref.getBoolean(PREF_USER_ACTIVATED, true);
    }

    public boolean isUserActivated() {
        boolean exist = false;
        SharedPreferences mSharedPref;
        for(int i = 0; i < size(); i++) {
            exist |= context.getSharedPreferences(PREF_NAME + i, 0).getBoolean(PREF_USER_ACTIVATED, true);
        }
        return exist;
    }

    public void setUserActivated(int position, boolean activate) {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        SharedPreferences.Editor e = mSharedPref.edit();
        e.putBoolean(PREF_USER_ACTIVATED, activate).apply();
    }

    private boolean isOAuthConfigured() {
        if(TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            // Log.e(TAG, "Twitter Consumer Key or Secret is not configured.");
            return false;
        } else {
            return false;
        }
    }

    public Intent getLoginIntent() {
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

    private void saveTwitterInfo(AccessToken accessToken) {
        long userId = accessToken.getUserId();

        try {
            int count = sharedPreferences.getInt(PREF_COUNT, 0);

            User user = twitter.showUser(userId);
            String userName = user.getName();
            String userScreenName = user.getScreenName();
            String imageUrl = user.getProfileImageURL();

            SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + count, 0);
            SharedPreferences.Editor e = mSharedPref.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_PROFILE_IMAGE_URL, imageUrl);
            e.putString(PREF_USER_NAME, userName);
            e.putString(PREF_USER_SCREEN_NAME, userScreenName);
            e.putBoolean(PREF_USER_ACTIVATED, false);
            e.apply(); // e.commit() originally.

            sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
            SharedPreferences.Editor e2 = sharedPreferences.edit();
            e2.remove(PREF_COUNT);
            e2.putInt(PREF_COUNT, count + 1);
            e2.apply();

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void handleLoginResult(Intent data) {
        String verifier = data.getExtras().getString(oAuthVerifier);
        try {
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
            saveTwitterInfo(accessToken);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return size() > 0;
    }

    public void logout(int position) {
        SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
        SharedPreferences.Editor e = mSharedPref.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.remove(PREF_USER_PROFILE_IMAGE_URL);
        e.remove(PREF_USER_NAME);
        e.remove(PREF_USER_SCREEN_NAME);
        e.remove(PREF_USER_ACTIVATED);
        e.apply();

        mSharedPref = context.getSharedPreferences(PREF_NAME, 0);
        int count = mSharedPref.getInt(PREF_COUNT, 0);
        e = mSharedPref.edit();
        e.putInt(PREF_COUNT, count - 1).apply();

        fillUpSharedPreference(position);
    }

    public void share(int position, String status) {
        new updateTwitterStatus(position).execute(status);
    }

    public void retweet(int position, String statusId) {
        new retweetStatus(position).execute(statusId);
    }

    private class retweetStatus extends AsyncTask<String, String, Void> {

        private int position;

        public retweetStatus(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            Long statusId = Long.parseLong(params[0]);
            try {
                Configuration configuration = new ConfigurationBuilder()
                        .setOAuthConsumerKey(consumerKey)
                        .setOAuthConsumerSecret(consumerSecret)
                        .build();

                SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
                String access_token = mSharedPref.getString(PREF_KEY_OAUTH_TOKEN, "");
                String access_token_secret = mSharedPref.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(configuration).getInstance(accessToken);

                twitter.retweetStatus(statusId);

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    private class updateTwitterStatus extends AsyncTask<String, String, Void> {

        private int position;

        public updateTwitterStatus(int position) {
            this.position = position;
        }

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

                SharedPreferences mSharedPref = context.getSharedPreferences(PREF_NAME + position, 0);
                String access_token = mSharedPref.getString(PREF_KEY_OAUTH_TOKEN, "");
                String access_token_secret = mSharedPref.getString(PREF_KEY_OAUTH_SECRET, "");

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
        }
    }

    public int size() {
        if(sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putInt(PREF_COUNT, 0).apply();
        }
        return sharedPreferences.getInt(PREF_COUNT, 0);
    }

    private void fillUpSharedPreference(int position) {
        SharedPreferences dest, src;
        for(int i = position; i < size(); i++) {
            dest = context.getSharedPreferences(PREF_NAME + i, 0);
            src = context.getSharedPreferences(PREF_NAME + (i + 1), 0);

            SharedPreferences.Editor e = dest.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, src.getString(PREF_KEY_OAUTH_TOKEN, ""));
            e.putString(PREF_KEY_OAUTH_SECRET, src.getString(PREF_KEY_OAUTH_SECRET, ""));
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, src.getBoolean(PREF_KEY_TWITTER_LOGIN, true));
            e.putString(PREF_USER_NAME, src.getString(PREF_USER_NAME, ""));
            e.putString(PREF_USER_SCREEN_NAME, src.getString(PREF_USER_SCREEN_NAME, ""));
            e.putString(PREF_USER_PROFILE_IMAGE_URL, src.getString(PREF_USER_PROFILE_IMAGE_URL, ""));
            e.apply();

        }
    }

}
