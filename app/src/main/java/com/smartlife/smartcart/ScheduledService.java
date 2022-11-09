package com.smartlife.smartcart;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**This service is responsible for sending battery status to the server.
 * With the API request's response, company ID is stored*/
public class ScheduledService extends Service  {

    private static final String TAG = "ScheduledService";
    private final Timer timer = new Timer();
    private SharedPreferences mPreferences;
    private int batteryLevel;

    private final BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level * 100 / (float)scale;
            batteryLevel = Math.round(batteryPct);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = getApplicationContext().getSharedPreferences(SharedPreferencesKey.SMART_CART_PREFS.name(), MODE_PRIVATE);
        String cartNumber = mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), "");
        String macAddress = mPreferences.getString(SharedPreferencesKey.CART_MAC_ADDRESS.name(), "");

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(batteryChangeReceiver, ifilter);


        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float)scale;
        batteryLevel = Math.round(batteryPct);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    JSONObject parameters = new JSONObject();
                    parameters.put("number", cartNumber);
                    parameters.put("mac_address", macAddress);
                    parameters.put("battery", batteryLevel);
                    makeHttpRequest(ApiUrl.UPDATE_CART_BAT, parameters);
                } catch (Exception e) {
                    logError(TAG+" => onCreate => inside timer => "+e.getMessage());
                }
            }
        }, 0, 120*1000);//2 Minutes


    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryChangeReceiver);
        timer.cancel();
        timer.purge();
        stopSelf();
        super.onDestroy();
    }

    private void logError(String errorMessage) {

        JSONObject parameters = new JSONObject();
        try {
            String companyID = mPreferences.getString(SharedPreferencesKey.COMPANY_ID.name(), "");
            String cartID = mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), "");

            parameters.put("CartId", cartID);
            parameters.put("CompanyId", companyID);
            parameters.put("LogDetails", errorMessage);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        makeHttpRequest(ApiUrl.LOG_ERROR, parameters);
    }

    final public void makeHttpRequest(final ApiUrl method, JSONObject parameters) {

        RequestQueue queue = SmartCartApplication.getInstance().getRequestQueue();

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, method.url(getApplicationContext()), parameters,
                this::handleResponse, null) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("Username", "");
                params.put("Password", "");

                return params;
            }
        };

        queue.add(jsonRequest);
    }

    protected void handleResponse(JSONObject response) {

        try {
            String companyId = response.getString("company");

            if(!companyId.isEmpty() && companyId != null && !companyId .equals("null")) {
                mPreferences.edit().putString(SharedPreferencesKey.COMPANY_ID.name(), companyId).apply();
            } else {
                mPreferences.edit().putString(SharedPreferencesKey.COMPANY_ID.name(), "5").apply();
            }
        } catch(Exception e) {
            logError(TAG+" => handleResponse => updateBattery => "+e.getMessage());
        }
    }
}
