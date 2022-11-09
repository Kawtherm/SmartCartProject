package com.smartlife.smartcart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**this broadcast receiver is used to start the app when device is turned on*/
public class StartupService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, SplashscreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}