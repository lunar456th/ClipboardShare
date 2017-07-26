package com.sj.sj.clipboardshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.sj.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;

import java.io.IOException;

public class PopupActivity extends Activity {

//    private static final int CODE_TWITTER_LOGIN_IN = 1111;
    private static final int RESULT_REQUEST_LOGIN = 1212;
    TwitterAccountManager twitterAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // dim behind the layout
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.activity_popup);

        //UI 객체생성
        ListView listView = (ListView)findViewById(R.id.account_list);

        //데이터 가져오기
        twitterAccountManager = TwitterAccountManager.getInstance(this);
        ListViewAdapter listViewAdapter = new ListViewAdapter();
        TextView noAccount = (TextView)findViewById(R.id.no_account);
        if (twitterAccountManager.isLoggedIn()) {
            findViewById(R.id.no_account).setVisibility(View.GONE);
            try {
                listViewAdapter.addItem(twitterAccountManager.getProfileImage(), twitterAccountManager.getUserName(), "@" + twitterAccountManager.getUserScreenName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            noAccount.setVisibility(View.VISIBLE);
            noAccount.setText("로그인된 계정이 없습니다.");
        }
        listView.setAdapter(listViewAdapter);
    }
    public void onClickOk(View v) {

        //Intent intent = new Intent();
        //intent.putExtra("result", "Close Popup");
        //setResult(RESULT_OK, intent);

        finish();
    }

    public void onClickAddAccount(View v) {
        Intent intent = new Intent();
        setResult(RESULT_REQUEST_LOGIN, intent);
        finish();
    }

    public void onClickDelAccount(View v) {
        twitterAccountManager.logout();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //바깥레이어 클릭시 안닫히게
//        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
//    }

//    @Override
//    public void onBackPressed() {
//        //안드로이드 백버튼 막기
//    }

//    private void getTwitterLoginIntent() {
//        Intent intent = twitterAccountManager.getLoginIntent();
//        startActivityForResult(intent, CODE_TWITTER_LOGIN_IN);
//    }
}
