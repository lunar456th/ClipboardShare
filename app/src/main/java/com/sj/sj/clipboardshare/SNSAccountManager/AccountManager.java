package com.sj.sj.clipboardshare.SNSAccountManager;

import android.content.Intent;

interface AccountManager {
    boolean isLoggedIn();
    Intent getLoginIntent();
    void logout();
}
