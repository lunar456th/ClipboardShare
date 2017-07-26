package com.sj.sj.clipboardshare;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    // the abbreviation of Toast.makeText.show //
    public static void toastShort(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

}
