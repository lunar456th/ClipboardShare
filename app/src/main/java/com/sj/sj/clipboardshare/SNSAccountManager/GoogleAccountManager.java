package com.sj.sj.clipboardshare.SNSAccountManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.PlusShare;

public class GoogleAccountManager implements AccountManager {

    private static GoogleAccountManager instance;
    private Context context;

    private static final String TAG = "MainActivity";
    private static GoogleApiClient mGoogleApiClient;

    private static GoogleSignInAccount userInfo;


    public static GoogleAccountManager getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleAccountManager(context.getApplicationContext());
        }
        return instance;
    }

    private GoogleAccountManager(Context context) {
        this.context = context;
        userInfo = null;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // 유저 아이디, 이메일 주소, 기본 프로필 정보가 DEFAULT_SIGN_IN 안에 포함된다.
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context) // 위에서 작성한 gso 를 바탕으로 Sign-in ApiClient 를 생성한다.
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient); // 유저 정보가 남아있는지 확인
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            userInfo = result.getSignInAccount();
        }
    }

    public String getUserName() {
        return userInfo.getDisplayName();
    }

    @Override
    public boolean isLoggedIn() {
        return userInfo != null;
    }

    @Override
    public Intent getLoginIntent() {
        return Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    }

    public Intent getSharePostIntent(String status) {
        return new PlusShare.Builder(context)
                .setType("text/plain")
                .setText(status)
                .getIntent();
    }

    @Override
    public void logout() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

                // FirebaseAuth.getInstance().signOut();
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "User logged out");
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API client connection suspended");
            }
        });

        userInfo = null;
    }

}
