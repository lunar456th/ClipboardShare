package com.example.sj.clipboardshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.sj.clipboardshare.ClipboardManager.ClipboardService;
import com.example.sj.clipboardshare.SNSAccountManager.GoogleAccountManager;
import com.example.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;
import com.example.sj.clipboardshare.ClipboardManager.ClipboardAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

public class MainActivity extends AppCompatActivity {

    private GoogleAccountManager googleAccountManager;
    private TwitterAccountManager twitterAccountManager;

    private static final int CODE_TWITTER_LOGIN_IN = 1111;
    private static final int CODE_GOOGLE_LOGIN_IN = 2222;
    private static final int CODE_PROCESS_INTENT = 3333;

    ImageView imageBackground;

    RecyclerView recyclerView;
    private ClipboardAdapter clipboardAdapter;

    private SwitchManager switchManager;

    ConnectivityManager manager;
    private NetworkInfo mobile;
    private NetworkInfo wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleAccountManager = GoogleAccountManager.getInstance(this);
        twitterAccountManager = TwitterAccountManager.getInstance(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        clipboardAdapter = ClipboardAdapter.getInstance(this);
        recyclerView.setAdapter(clipboardAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        imageBackground = (ImageView)findViewById(R.id.image_background);
        imageBackground.setAlpha((float)0.5);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        switchManager = SwitchManager.getInstance(this);

        manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(!wifi.isConnected() && !mobile.isConnected()) {
            Toast.makeText(this, getString(R.string.network_login_check), Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.button_start).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(clipboardAdapter.isEmpty()) {
                    shortToast(getString(R.string.no_copied));
                    return;
                }

                if(!wifi.isConnected() && !mobile.isConnected()) {
                    Toast.makeText(MainActivity.this, getString(R.string.network_login_check), Toast.LENGTH_LONG).show();
                }

                final boolean isTwitterLoggedIn = twitterAccountManager.isLoggedIn();
                final boolean isGoogleLoggedIn = googleAccountManager.isLoggedIn();

                if(!isTwitterLoggedIn && !isGoogleLoggedIn) {
                    shortToast(getString(R.string.not_logged_in));
                    return;
                }

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                alt_bld.setMessage(clipboardAdapter.getCount() + getString(R.string.question_before_share))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                                startActivityForResult(intent, CODE_PROCESS_INTENT);
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);

        final MenuItem toggleService = menu.findItem(R.id.menu_activate);
        toggleService.setActionView(R.layout.actionbar_switch);

        final Switch actionbarSwitch = (Switch)menu.findItem(R.id.menu_activate).getActionView().findViewById(R.id.switchForActionBar);
        actionbarSwitch.setChecked(switchManager.getStatus());
        actionbarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    switchManager.setStatus(true);
                    shortToast(getString(R.string.activated));
                    if(!isClipboardServiceRunning()) {
                        startService(new Intent(MainActivity.this, ClipboardService.class));
                    }
                } else {
                    switchManager.setStatus(false);
                    shortToast(getString(R.string.inactivated));
                    stopService(new Intent(MainActivity.this, ClipboardService.class));
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    // 메뉴 버튼 //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_twitter_google:
                String twitterSign;
                if(twitterAccountManager.isLoggedIn()) {
                    twitterSign = getString(R.string.twitter_logout) + "\n(" + twitterAccountManager.getUserName() + ")";
                } else {
                    twitterSign = getString(R.string.twitter_login);
                }

                String googleSign;
                if(googleAccountManager.isLoggedIn()) {
                    googleSign = getString(R.string.google_logout) + "\n(" + googleAccountManager.getUserName() + ")";
                } else {
                    googleSign = getString(R.string.google_login);
                }

                new AlertDialog.Builder(this)
                        .setTitle("")
                        .setItems(new String[]{twitterSign, googleSign}, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if(twitterAccountManager.isLoggedIn()) {
                                            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                                            alt_bld.setMessage(getString(R.string.question_before_logout)).setCancelable(
                                                    false).setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            twitterAccountManager.logout();
                                                            shortToast(getString(R.string.logged_out));
                                                        }
                                                    }).setNegativeButton(getString(R.string.no),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alert = alt_bld.create();
                                            alert.show();
                                        } else {
                                            getTwitterLoginIntent();
                                        }
                                        break;
                                    case 1:
                                        if(googleAccountManager.isLoggedIn()) {
                                            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                                            alt_bld.setMessage(getString(R.string.question_before_logout)).setCancelable(
                                                    false).setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            googleAccountManager.logout();
                                                            shortToast(getString(R.string.logged_out));
                                                        }
                                                    }).setNegativeButton(getString(R.string.no),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alert = alt_bld.create();
                                            alert.show();
                                        } else {
                                            getGoogleLoginIntent();
                                        }
                                        break;
                                }
                            }
                        }).setNegativeButton("", null).show();

                break;

            case R.id.menu_delete_all:
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                alt_bld.setMessage(getString(R.string.question_before_remove_all))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clipboardAdapter.clear();
                                clipboardAdapter.notifyDataSetChanged();
                                shortToast(getString(R.string.removed_all));
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.show();

                break;

            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_TWITTER_LOGIN_IN:
                if(resultCode == Activity.RESULT_OK) {
                    twitterAccountManager.handleLoginResult(data);
                    shortToast(getString(R.string.twitter_logged_in));
                }
                break;

            case CODE_GOOGLE_LOGIN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                googleAccountManager.handleSignInResult(result);
                shortToast(getString(R.string.google_logged_in));
                break;

            case CODE_PROCESS_INTENT:
                if(resultCode == RESULT_OK) {
                    shortToast(getString(R.string.shared_all));
                }
                break;

            default:
                break;
        }
    }

    private void getTwitterLoginIntent() {
        Intent intent = twitterAccountManager.getLoginIntent();
        startActivityForResult(intent, CODE_TWITTER_LOGIN_IN);
    }

    private void getGoogleLoginIntent() {
        Intent intent = googleAccountManager.getLoginIntent();
        startActivityForResult(intent, CODE_GOOGLE_LOGIN_IN);
    }

    public void shortToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isClipboardServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(MainActivity.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
