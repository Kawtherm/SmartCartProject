package com.smartlife.smartcart;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.smartlife.smartcart.classes.AppConsts;
import com.smartlife.smartcart.classes.Device;
import com.smartlife.smartcart.classes.DeviceIO;
import com.smartlife.smartcart.classes.HttpsTrustManager;
import com.smartlife.smartcart.classes.SerialPortManager;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.sspp.library.SweetAlertDialog;

/**this activity is inherited by other activities that use makeHttpRequest, activityIndicator, etc.*/
public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivity";

    private SweetAlertDialog mDialog;
    public Boolean mStillAlive = false;
    private Boolean mIsSystemUiShown = true;

    protected boolean mOpened = false;
    protected Device mDevice;
    protected SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mPreferences = getSharedPreferences(SharedPreferencesKey.SMART_CART_PREFS.name(), MODE_PRIVATE);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                visibility -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        mIsSystemUiShown = true;
                    } else {
                        mIsSystemUiShown = false;
                    }
                    checkHideSystemUI();
                });
    }

    protected void loadImageURL(ImageView view) {

        String url = mPreferences.getString(SharedPreferencesKey.LOGO.name(), null);
        if(url != null && !url.isEmpty()) Glide.with(this).load(url).into(view);
    }

    // region Led Control
    protected void switchBothLights(boolean on) {
        if(on) {
            lightOn("led1");
            lightOn("led2");
        }
        else {
            lightOff("led1");
            lightOff("led2");
        }
    }

    protected void lightOn(String ledName) {
        String fileName = "/sys/class/leds/" + ledName + "/brightness";
        control(fileName, "0");
        control(fileName, "1");
    }

    protected void lightOff(String ledName) {
        control("/sys/class/leds/" + ledName + "/brightness", "0");
    }

    private void control(String pilePath, String data) {

        try {
            DeviceIO.write(pilePath, data);
        } catch (Exception e) {
            e.printStackTrace();
            logError(TAG+" => control(path, data) exception  => "+e.getMessage());
        }
    }
    // endregion

    //region Serial Port Switch
    protected void switchToScanMode(IScaleReadCallback callback) {
        mDevice = new Device("/dev/ttyS4", "19200");
        switchSerialPort(callback);
    }

    protected void switchToScaleMode(IScaleReadCallback callback) {
        mDevice = new Device("/dev/ttySAC2", "9600");
        switchSerialPort(callback);
    }

    private void switchSerialPort(IScaleReadCallback callback) {
        if (mOpened) {
            switchSerialOff();
        }

        SerialPortManager serialPortManager = SerialPortManager.instance();
        serialPortManager.setScaleReadCallback(callback);
        mOpened = serialPortManager.open(mDevice) != null;
    }

    protected void switchSerialOff() {
        SerialPortManager.instance().close();
    }

    protected void turnScannerHardwareOn() {
        // turn on the scanner
        DeviceIO.write("/sys/devices/platform/x6818-scanner/scanner_power", String.valueOf(0));
    }

    protected void turnScannerHardwareOff() {
        DeviceIO.write("/sys/devices/platform/x6818-scanner/scanner_power", String.valueOf(1));
    }

    protected void switchHardwareOff() {

        mOpened = false;
        switchSerialOff();
        lightOff("led1");
        lightOff("led2");
        turnScannerHardwareOff();

        mDevice = null;
    }

    protected void rebaseScale() {

        try {
            mDevice = new Device("/dev/ttySAC2", "9600");
            SerialPortManager.instance().sendCommand("7A");
        }
        catch (Exception e) { }
    }
    //endregion

    // region HTTP Requests
    public void logError(String errorMessage) {

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
        makeHttpPostRequest(ApiUrl.LOG_ERROR, parameters, null);
    }

    final public void makeHttpPostRequest(final ApiUrl method, JSONObject parameters, String barcode) {

        try{
            HttpsTrustManager.nuke();
            mStillAlive = true;
            RequestQueue queue = SmartCartApplication.getInstance().getRequestQueue();

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, method.url(this), parameters, response -> {

                handleResponse(response, method, barcode);
                mStillAlive = false;
            }, error -> {

                handleError(error, method);
                mStillAlive = false;
            }) {
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
        catch (Exception e) { }
    }

    final public void makeHttpGetRequest(final ApiUrl method, String parameters, String barcode) {

        try {
            HttpsTrustManager.nuke();
            mStillAlive = true;
            RequestQueue queue = SmartCartApplication.getInstance().getRequestQueue();

            JsonObjectRequest jsonRequest = new JsonObjectRequest(method.url(this) + parameters, null, response -> {

                handleResponse(response, method, barcode);
                mStillAlive = false;
            }, error -> {
                handleError(error, method);
                mStillAlive = false;
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Username", "api@insurance");
                    params.put("Password", "API@insurance_123654");
                    params.put("Authorization", "Basic YXBpQGluc3VyYW5jZTpBUElAaW5zdXJhbmNlXzEyMzY1NA==");

                    return params;
                }
            };

            queue.add(jsonRequest);
        }
        catch(Exception e) { }
    }

    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) { }

    protected void handleError(VolleyError error, ApiUrl method) {
        if(method != ApiUrl.LOG_ERROR) {
            if(error != null) logError(TAG+" => Volley error in: "+method.name()+" => "+error.getMessage());
        }
    }
    // endregion

    // region UI
    protected boolean isArabic() {
        return mPreferences.getString(SharedPreferencesKey.LANGUAGE.name(), "En").equals("Ar");
    }

    protected int convertDpToPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    protected final void showActivityIndicator(String msg) {
        mDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mDialog.setTitleText(msg);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    protected final void hideActivityIndicator() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.hide();
    }

    protected final void doError(String title, String content) {

        if(!this.isFinishing()) {
            try {
                SweetAlertDialog mErrorDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                mErrorDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                mErrorDialog.setTitleText(title);
                mErrorDialog.setContentText(content);
                mErrorDialog.setCancelable(false);
                mErrorDialog.show();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected final void doSuccess(String title, String content) {

        if(!this.isFinishing()) {
            try {
                SweetAlertDialog mSuccessDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                mSuccessDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                mSuccessDialog.setTitleText(title);
                mSuccessDialog.setContentText(content);
                mSuccessDialog.setCancelable(false);
                mSuccessDialog.show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkHideSystemUI() {
        // Check if system UI is shown and hide it by post a delayed handler
        if (mIsSystemUiShown) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        checkHideSystemUI();
    }
    //endregion

    // region Checkout Block
    protected boolean canCheckout() {
        return !mPreferences.getBoolean(SharedPreferencesKey.CANNOT_CHECKOUT.name(), false);
    }

    private int getTotalWeight() {
        return mPreferences.getInt(SharedPreferencesKey.TOTAL_CHECKOUT_WEIGHT.name(), 0);
    }

    protected void checkWeightViolation(int weight) {

        int totalWeight = getTotalWeight();

        if(Math.abs(totalWeight - weight) > AppConsts.SCALE_ERROR_MARGIN) {
            blockCheckout();
        }
        else {
            unBlockCheckout();
        }
    }

    private void blockCheckout() {
        lightOn("led1");
        mPreferences.edit().putBoolean(SharedPreferencesKey.CANNOT_CHECKOUT.name(), true).apply();
    }

    private void unBlockCheckout() {
        lightOff("led1");
        mPreferences.edit().putBoolean(SharedPreferencesKey.CANNOT_CHECKOUT.name(), false).apply();
    }
    // endregion
}