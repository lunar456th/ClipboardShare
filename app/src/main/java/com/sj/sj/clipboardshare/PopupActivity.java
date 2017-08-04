package com.sj.sj.clipboardshare;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
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

        // create the UI object.
        final ListView listView = (ListView)findViewById(R.id.account_list);

        // import data
        twitterAccountManager = TwitterAccountManager.getInstance(this);
        final ListViewAdapter listViewAdapter = new ListViewAdapter();
        TextView description = (TextView)findViewById(R.id.no_account);

        if (twitterAccountManager.isLoggedIn()) {
            description.setText(getResources().getString(R.string.long_click_to_remove));
            try {
                for(int i=0;i<twitterAccountManager.size();i++)
                listViewAdapter.addItem(twitterAccountManager.getProfileImage(i), twitterAccountManager.getUserName(i), "@" + twitterAccountManager.getUserScreenName(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(getResources().getString(R.string.no_login_account));
        }
        listView.setAdapter(listViewAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(PopupActivity.this);
                alt_bld.setMessage(getString(R.string.question_before_logout))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                twitterAccountManager.logout(position);
                                listViewAdapter.removeItem(position);
                                listViewAdapter.notifyDataSetChanged();
                                listView.setAdapter(listViewAdapter);
                                listView.invalidateViews();
                                listView.refreshDrawableState();
                                Utils.toastShort(PopupActivity.this, getString(R.string.logged_out));
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.show();

                return true;
            }
        });
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
        for(int i = 0; i < twitterAccountManager.size(); i++) {
            twitterAccountManager.logout(i);
        }
        Utils.toastShort(this, getResources().getString(R.string.removed_all));
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // do not close when clicking outside layer.
//        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
//    }

//    @Override
//    public void onBackPressed() {
//        // disable the android back button.
//    }

//    private void getTwitterLoginIntent() {
//        Intent intent = twitterAccountManager.getLoginIntent();
//        startActivityForResult(intent, CODE_TWITTER_LOGIN_IN);
//    }
}
