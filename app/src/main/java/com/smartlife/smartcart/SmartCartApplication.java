package com.smartlife.smartcart;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SmartCartApplication extends Application {

    private static SmartCartApplication sInstance;
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized SmartCartApplication getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
}