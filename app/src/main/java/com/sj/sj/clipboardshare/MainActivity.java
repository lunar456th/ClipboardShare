package com.sj.sj.clipboardshare;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.sj.sj.clipboardshare.ClipboardManager.ClipboardService;
import com.sj.sj.clipboardshare.SNSAccountManager.GoogleAccountManager;
import com.sj.sj.clipboardshare.ClipboardManager.ClipboardAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.sj.sj.clipboardshare.SNSAccountManager.TwitterAccountManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // declare local variables //
    private GoogleAccountManager googleAccountManager;
    private TwitterAccountManager twitterAccountManager;
    private SwitchManager switchManager;
    private SelectManager selectManager;

    // declare request&result codes //
    private static final int CODE_TWITTER_LOGIN_IN = 1111;
    private static final int CODE_GOOGLE_LOGIN_IN = 2222;
    private static final int CODE_PROCESS_INTENT = 3333;
    private static final int RESULT_REQUEST_LOGIN = 1212;
    private static final int CODE_TWITTER_ACCOUNT = 4444;

    private ClipboardAdapter clipboardAdapter;

    private NetworkInfo mobile;
    private NetworkInfo wifi;

    private boolean isGooglePlusSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleAccountManager = GoogleAccountManager.getInstance(this);
        twitterAccountManager = TwitterAccountManager.getInstance(this);

        // initiate recycler view //
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        clipboardAdapter = ClipboardAdapter.getInstance(this);
        recyclerView.setAdapter(clipboardAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ImageView imageBackground = (ImageView) findViewById(R.id.image_background);
        imageBackground.setAlpha((float)0.5);

        // Enable Actionbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        switchManager = SwitchManager.getInstance(this);
        selectManager = SelectManager.getInstance(this);

        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // thread policy for using network //
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // check if the network is connected //
        if(!wifi.isConnected() && !mobile.isConnected()) {
            Utils.toastLong(MainActivity.this, getString(R.string.network_login_check));
        }

        // check if the google+ app is installed //
        isGooglePlusSetup = false;
        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> packs = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        for(ApplicationInfo app : packs) {
            if(app.packageName.equals(getString(R.string.package_name_google_plus))) {
                isGooglePlusSetup = true;
                break;
            }
        }

        // share it! button //
        findViewById(R.id.button_start).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                // check if message stack is empty //
                if(clipboardAdapter.isEmpty()) {
                    Utils.toastShort(MainActivity.this, getString(R.string.no_copied));
                    return;
                }

                // check the network status //
                if(!wifi.isConnected() && !mobile.isConnected()) {
                    Utils.toastLong(MainActivity.this, getString(R.string.network_login_check));
                }

                // check the sign-in status and installation and the status of app selection //
                final boolean isTwitterLoggedIn = twitterAccountManager.isLoggedIn();
                final boolean isGoogleLoggedIn = googleAccountManager.isLoggedIn();

                if(isGoogleLoggedIn && !isGooglePlusSetup) {
                    Utils.toastShort(MainActivity.this, getString(R.string.app_not_installed_google_plus));
                    return;
                }

                if(!isTwitterLoggedIn && selectManager.getTwitter()) {
                    Utils.toastShort(MainActivity.this, getString(R.string.please_login_twitter));
                    return;
                }

                if(!selectManager.getTwitter() && !selectManager.getGoogle()) {
                    Utils.toastShort(MainActivity.this, getString(R.string.please_select_app));
                    return;
                }

                if(selectManager.getTwitter() && !twitterAccountManager.isUserActivated()) {
                    Utils.toastShort(MainActivity.this, getString(R.string.select_twitter_account));
                    return;
                }

                // if it has no problem //
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

    // activation switch //
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
                    Utils.toastShort(MainActivity.this, getString(R.string.activated));
                    if(!isClipboardServiceRunning()) {
                        startService(new Intent(MainActivity.this, ClipboardService.class));
                    }
                } else {
                    switchManager.setStatus(false);
                    Utils.toastShort(MainActivity.this, getString(R.string.inactivated));
                    stopService(new Intent(MainActivity.this, ClipboardService.class));
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // menu button //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // app selection button //
            case R.id.menu_select_app:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_to_share_on))
                        .setMultiChoiceItems(new String[]{getString(R.string.twitter), getString(R.string.google_plus)}, new boolean[]{selectManager.getTwitter(), selectManager.getGoogle()}, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                switch (which) {
                                    case 0:
                                        selectManager.setTwitter(isChecked);
                                        break;
                                    case 1:
                                        selectManager.setGoogle(isChecked);
                                        break;
                                }
                            }
                        })
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.toastShort(MainActivity.this, getString(R.string.set));
                            }
                        }).show();

                break;

            // twitter login button //
            case R.id.menu_account_twitter:
                Intent intent = new Intent(this, PopupActivity.class);
                startActivityForResult(intent, CODE_TWITTER_ACCOUNT);
                break;

            // google login button //
            case R.id.menu_account_google:

                String googleSign;
                if(googleAccountManager.isLoggedIn()) {
                    googleSign = getString(R.string.google_logout) + "\n(" + googleAccountManager.getUserName() + ")";
                } else {
                    googleSign = getString(R.string.google_login);
                }

                new AlertDialog.Builder(this)
                        .setTitle("")
                        .setItems(new String[]{googleSign}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(googleAccountManager.isLoggedIn()) {
                                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                                    alt_bld.setMessage(getString(R.string.question_before_logout)).setCancelable(
                                            false).setPositiveButton(getString(R.string.yes),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    googleAccountManager.logout();
                                                    Utils.toastShort(MainActivity.this, getString(R.string.logged_out));
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
                            }
                        })
                        .setNegativeButton("", null).show();

                break;

            // select account //
            case R.id.menu_select_account:
                String displayName[] = new String[twitterAccountManager.size()];
                boolean displayChecked[] = new boolean[twitterAccountManager.size()];
                for(int i = 0; i < twitterAccountManager.size(); i++) {
                    displayName[i] = twitterAccountManager.getUserName(i);
                    displayChecked[i] = twitterAccountManager.getUserActivated(i);
                }

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.select_sharing_account))
                        .setMultiChoiceItems(displayName, displayChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                twitterAccountManager.setUserActivated(which, isChecked);
                            }
                        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.toastShort(MainActivity.this, getString(R.string.set));
                    }
                }).show();
                break;

            // delete all //
            case R.id.menu_delete_all:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.question_before_remove_all))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clipboardAdapter.clear();
                                clipboardAdapter.notifyDataSetChanged();
                                Utils.toastShort(MainActivity.this, getString(R.string.removed_all));
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
                break;

            // about //
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // when receiving the result of sign-in and sharing //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CODE_TWITTER_LOGIN_IN:
                if(resultCode == Activity.RESULT_OK) {
                    twitterAccountManager.handleLoginResult(data);
                    Utils.toastShort(this, getString(R.string.twitter_logged_in));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().removeAllCookies(null); // remove cookies (remaining in webview)
                    } else {
                        CookieManager.getInstance().removeAllCookie();
                    }
                }
                break;

            case CODE_GOOGLE_LOGIN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                googleAccountManager.handleSignInResult(result);
                Utils.toastShort(MainActivity.this, getString(R.string.google_logged_in));
                break;

            case CODE_PROCESS_INTENT:
                //if(resultCode == RESULT_OK) {
                //    Utils.toastShort(MainActivity.this, getString(R.string.shared_all));
                //}
                break;

            case CODE_TWITTER_ACCOUNT:
                if(resultCode == RESULT_REQUEST_LOGIN) {
                    getTwitterLoginIntent();
                }
                break;

            default:
                break;
        }
    }

    // start intent for sign-in //
    private void getTwitterLoginIntent() {
        Intent intent = twitterAccountManager.getLoginIntent();
        startActivityForResult(intent, CODE_TWITTER_LOGIN_IN);
    }

    private void getGoogleLoginIntent() {
        Intent intent = googleAccountManager.getLoginIntent();
        startActivityForResult(intent, CODE_GOOGLE_LOGIN_IN);
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
