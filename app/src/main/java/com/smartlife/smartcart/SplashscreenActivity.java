package com.smartlife.smartcart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**This activity is where the app starts (launcher activity).
 * It serves as a splashscreen that lasts two seconds.
 * It's responsible for starting the scheduled service*/
public class SplashscreenActivity extends BaseActivity {

    private final String TAG = "StartupActivity";

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        mRunnable = () -> {
            Intent intent = new Intent(SplashscreenActivity.this, AdsActivity.class);
            startActivity(intent);
            finish();
        };

        mHandler.postDelayed(mRunnable, 2000);

        //TODO: uncomment for cart device
        //Intent scheduledService = new Intent(getApplicationContext(), ScheduledService.class);
        //getApplicationContext().startService(scheduledService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }
}