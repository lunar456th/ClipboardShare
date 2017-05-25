package com.sj.sj.clipboardshare.ClipboardManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.sj.sj.clipboardshare.MainActivity;
import com.sj.sj.clipboardshare.R;
import com.sj.sj.clipboardshare.SwitchManager;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class ClipboardService extends Service {

    private ClipboardManager clipboardManager;
    private ClipboardAdapter clipboardAdapter;
    private SwitchManager switchManager;
    private ClipboardManager.OnPrimaryClipChangedListener listener;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardAdapter = ClipboardAdapter.getInstance(this);
        switchManager = SwitchManager.getInstance(this);
        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                if(switchManager.getStatus()) {
                    final Toast toast = Toast.makeText(getBaseContext(), getString(R.string.copied), Toast.LENGTH_SHORT);
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 1000);

                    String string = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                    if (!string.trim().isEmpty() && !string.equals("null")) {
                        clipboardAdapter.add(string);
                        clipboardAdapter.notifyDataSetChanged();
                        updateNtf();
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(listener);

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);
        updateNtf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(1);
        if(listener != null) {
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).removePrimaryClipChangedListener(listener);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void updateNtf() {
        notificationManager.cancel(1);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setSmallIcon(R.drawable.ic_ntf)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_app))
                .setContentIntent(contentIntent)
                .setContentTitle(clipboardAdapter.getCount() + getString(R.string.main_copied) + "")
                .setContentText(getString(R.string.shortcut_app));
        notification = notificationBuilder.build();
        notificationManager.notify(1, notification);
    }
}
