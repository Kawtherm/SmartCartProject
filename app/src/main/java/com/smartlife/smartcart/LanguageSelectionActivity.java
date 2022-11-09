package com.smartlife.smartcart;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.print.PrintHelper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.usb.USBPort;
import com.printer.sdk.utils.PrefUtils;
import com.printer.sdk.utils.Utils;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**This activity is for choosing a language for the app to start putting order.
 * And you can access Admin activity from here by clicking 4 times to SmartLife logo.
 * This activity is also responsible for the retrieval of company details and storing the prefixes*/
public class LanguageSelectionActivity extends BaseActivity implements View.OnClickListener /*TODO: uncomment if device is cart IScaleReadCallback*/ {

    private final String TAG = "PageThree";

    private int mAdminClicks = 0;
    private AlertDialog mAdminPassDialog;
    private EditText mDialogPasswordTxt;
    private Button englishBtn;
    private Button arabicBtn;
    private ProgressBar mProgressBar;


    //TODO: uncomment for cart device
    /*private final BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ImageView mBatLevel = findViewById(R.id.pagethree_bat_level);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level * 100 / (float)scale;
            int batteryLevel = Math.round(batteryPct);

            if(mBatLevel != null) {
                if(batteryLevel >= 1 && batteryLevel <= 20) {
                    mBatLevel.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bat_20, null));
                } else if (batteryLevel >= 20 && batteryLevel <= 40) {
                    mBatLevel.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bat_40, null));
                } else if(batteryLevel >= 40 && batteryLevel <= 60) {
                    mBatLevel.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bat_60, null));
                } else if(batteryLevel >= 60 && batteryLevel <= 80) {
                    mBatLevel.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bat_80, null));
                } else if(batteryLevel >= 80 && batteryLevel <= 100) {
                    mBatLevel.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.bat_100, null));
                }
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_language_selection);

        loadImageURL(findViewById(R.id.pagethree_comp_logo));
        englishBtn = findViewById(R.id.english_btn);
        arabicBtn = findViewById(R.id.arabic_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        ImageView smartLifeLogo = findViewById(R.id.pagethree_smartlife_logo);

        TextView versionNum = findViewById(R.id.tv_app_version);
        versionNum.setText("v"+BuildConfig.VERSION_NAME);

        // Prepare Dialogs
        preparePasswordInputDialog();

        englishBtn.setOnClickListener(this);
        arabicBtn.setOnClickListener(this);
        smartLifeLogo.setOnClickListener(this);

        //TODO: uncomment for cart device
        /*IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryChangeReceiver, ifilter);
        switchToScaleMode(this);*/

    }

    private void updatePrefixesInMemory(String fruits, String dairy, String nuts, String four, String five, String six) {
        mPreferences.edit().putString(SharedPreferencesKey.FRUITS_PREFIX.name(), fruits).apply();
        mPreferences.edit().putString(SharedPreferencesKey.DAIRY_PREFIX.name(), dairy).apply();
        mPreferences.edit().putString(SharedPreferencesKey.NUTS_PREFIX.name(), nuts).apply();
        mPreferences.edit().putString(SharedPreferencesKey.FOUR_PREFIX.name(), four).apply();
        mPreferences.edit().putString(SharedPreferencesKey.FIVE_PREFIX.name(), five).apply();
        mPreferences.edit().putString(SharedPreferencesKey.SIX_PREFIX.name(), six).apply();
    }

    private void updatePrefixTolerancesInMemory(String fruits, String dairy, String nuts, String four, String five, String six) {
        try {
            mPreferences.edit().putInt(SharedPreferencesKey.FRUITS_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(fruits)).apply();
            mPreferences.edit().putInt(SharedPreferencesKey.DAIRY_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(dairy)).apply();
            mPreferences.edit().putInt(SharedPreferencesKey.NUTS_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(nuts)).apply();
            mPreferences.edit().putInt(SharedPreferencesKey.FOUR_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(four)).apply();
            mPreferences.edit().putInt(SharedPreferencesKey.FIVE_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(five)).apply();
            mPreferences.edit().putInt(SharedPreferencesKey.SIX_PREFIX_TOLERANCE.name(), (int) Double.parseDouble(six)).apply();
        }
        catch(Exception e) {
            updatePrefixTolerancesInMemory("0", "0", "0", "0", "0", "0");
        }
    }

    private void updatePrefixUsageInMemory(boolean fruits, boolean dairy, boolean nuts, boolean four, boolean five, boolean six) {
        mPreferences.edit().putBoolean(SharedPreferencesKey.FRUITS_PREFIX_USAGE.name(), fruits).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.DAIRY_PREFIX_USAGE.name(), dairy).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.NUTS_PREFIX_USAGE.name(), nuts).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.FOUR_PREFIX_USAGE.name(), four).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.FIVE_PREFIX_USAGE.name(), five).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.SIX_PREFIX_USAGE.name(), six).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: uncomment for cart device
        /*switchSerialOff();
        unregisterReceiver(batteryChangeReceiver);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdminClicks = 0;
    }

    private void preparePasswordInputDialog() {
        mAdminPassDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.input_admin_password, null);

        mDialogPasswordTxt = dialogView.findViewById(R.id.admin_password_no);
        mDialogPasswordTxt.setShowSoftInputOnFocus(false);

        Button oneBtn = dialogView.findViewById(R.id.pass_one_btn);
        Button twoBtn = dialogView.findViewById(R.id.pass_two_btn);
        Button threeBtn = dialogView.findViewById(R.id.pass_three_btn);
        Button fourBtn = dialogView.findViewById(R.id.pass_four_btn);
        Button fiveBtn = dialogView.findViewById(R.id.pass_five_btn);
        Button sixBtn = dialogView.findViewById(R.id.pass_six_btn);
        Button sevenBtn = dialogView.findViewById(R.id.pass_seven_btn);
        Button eightBtn = dialogView.findViewById(R.id.pass_eight_btn);
        Button nineBtn = dialogView.findViewById(R.id.pass_nine_btn);
        Button zeroBtn = dialogView.findViewById(R.id.pass_zero_btn);
        Button deleteNumberBtn = dialogView.findViewById(R.id.pass_delete_nb_btn);
        Button submitBtn = dialogView.findViewById(R.id.pass_submit_btn);

        oneBtn.setOnClickListener(this);
        twoBtn.setOnClickListener(this);
        threeBtn.setOnClickListener(this);
        fourBtn.setOnClickListener(this);
        fiveBtn.setOnClickListener(this);
        sixBtn.setOnClickListener(this);
        sevenBtn.setOnClickListener(this);
        eightBtn.setOnClickListener(this);
        nineBtn.setOnClickListener(this);
        zeroBtn.setOnClickListener(this);
        deleteNumberBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

        mAdminPassDialog.setView(dialogView);

        mAdminPassDialog.setOnShowListener(listener -> {
            mAdminClicks = 0;
            mDialogPasswordTxt.requestFocus();
        });

        mAdminPassDialog.setOnDismissListener(listener -> {
            mDialogPasswordTxt.clearFocus();
            mDialogPasswordTxt.setText("");
        });
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.english_btn) {

            //TODO: uncomment for cart device
            //rebaseScale();

            englishBtn.setEnabled(false);
            arabicBtn.setEnabled(false);


            mProgressBar.setVisibility(View.VISIBLE);
            makeHttpPostRequest(ApiUrl.RETRIEVE_WEIGHT_TOLERANCES, null, null);

            mPreferences.edit().putString(SharedPreferencesKey.LANGUAGE.name(), "En").apply();
        }
        else if(v.getId() == R.id.arabic_btn) {

            //TODO: ucomment for cart device
            //rebaseScale();

            englishBtn.setEnabled(false);
            arabicBtn.setEnabled(false);


            mProgressBar.setVisibility(View.VISIBLE);
            makeHttpPostRequest(ApiUrl.RETRIEVE_WEIGHT_TOLERANCES, null, null);

            mPreferences.edit().putString(SharedPreferencesKey.LANGUAGE.name(), "Ar").apply();
        }
        else if(v.getId() == R.id.pagethree_smartlife_logo) {

            if(mAdminClicks == 3) {

                mAdminPassDialog.show();
                mAdminPassDialog.getWindow().setLayout(convertDpToPixels(300), convertDpToPixels(620));
            } else {
                mAdminClicks++;
            }
        }
        else if(v.getId() == R.id.pass_delete_nb_btn) {

            if (mDialogPasswordTxt.getText().length() > 0) {
                CharSequence currentText = mDialogPasswordTxt.getText();
                mDialogPasswordTxt.setText(currentText.subSequence(0, currentText.length() - 1));
                mDialogPasswordTxt.setSelection(mDialogPasswordTxt.getText().length());
            } else {
                mDialogPasswordTxt.setText("");
            }
        }
        else if(v.getId() == R.id.pass_submit_btn) {

            mAdminClicks = 0;
            mAdminPassDialog.dismiss();

            if (mDialogPasswordTxt.getText().length() > 0) {
                String password = mDialogPasswordTxt.getText().toString().trim();
                if(password.equals("123456")) {
                    Intent adminIntent = new Intent(LanguageSelectionActivity.this, AdminActivity.class);
                    startActivity(adminIntent);
                }
            }
        }
        else {
            mDialogPasswordTxt.setText(mDialogPasswordTxt.getText().append(((Button)v).getText()));
            mDialogPasswordTxt.setSelection(mDialogPasswordTxt.getText().length());
        }
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);

        runOnUiThread(() -> {

            if(method == ApiUrl.RETRIEVE_WEIGHT_TOLERANCES) {

                try {
                    int code = response.getInt("code");

                    if (code == 0) {
                        JSONArray tolerance = response.getJSONArray("data");
                        mPreferences.edit().putString(SharedPreferencesKey.TOLERANCE_ARRAY.name(), tolerance.toString()).apply();
                    }
                } catch (JSONException e) {
                    logError(TAG+" => handleResponse => retrieveWeightTolerance => "+e.getMessage());
                    e.printStackTrace();
                } finally {
                    makeHttpGetRequest(ApiUrl.RETRIEVE_COMPANY_DETAILS, "", null);
                }

            }
            else if(method == ApiUrl.RETRIEVE_COMPANY_DETAILS) {

                englishBtn.setEnabled(true);
                arabicBtn.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);

                try {
                    int code = response.getInt("code");
                    String msg = response.getString("msg");

                    if (code == 0) {

                        JSONObject company = response.getJSONObject("data");

                        String fruits = company.getString("fruits_prefix").equals("null") ? null : company.getString("fruits_prefix");
                        String dairy = company.getString("dairy_prefix").equals("null") ? null : company.getString("dairy_prefix");
                        String nuts = company.getString("nuts_prefix").equals("null") ? null : company.getString("nuts_prefix");
                        String four = company.getString("prefix4").equals("null") ? null : company.getString("prefix4");
                        String five = company.getString("prefix5").equals("null") ? null : company.getString("prefix5");
                        String six = company.getString("prefix6").equals("null") ? null : company.getString("prefix6");

                        String fruitsTolerance = company.getString("fruitsPrefixTolerance").equals("null") ? "0" : company.getString("fruitsPrefixTolerance");
                        String dairyTolerance = company.getString("dairyPrefixTolerance").equals("null") ? "0" : company.getString("dairyPrefixTolerance");
                        String nutsTolerance = company.getString("nutsPrefixTolerance").equals("null") ? "0" : company.getString("nutsPrefixTolerance");
                        String fourTolerance = company.getString("prefix4Tolerance").equals("null") ? "0" : company.getString("prefix4Tolerance");
                        String fiveTolerance = company.getString("prefix5Tolerance").equals("null") ? "0" : company.getString("prefix5Tolerance");
                        String sixTolerance = company.getString("prefix6Tolerance").equals("null") ? "0" : company.getString("prefix6Tolerance");

                        boolean includeFruitsPrefix = company.getBoolean("useFruitsPrefixInApi");
                        boolean includeDairyPrefix = company.getBoolean("useDairyPrefixInApi");
                        boolean includeNutsPrefix = company.getBoolean("useNutsPrefixInApi");
                        boolean includeFourPrefix = company.getBoolean("usePrefix4InApi");
                        boolean includeFivePrefix = company.getBoolean("usePrefix5InApi");
                        boolean includeSixPrefix = company.getBoolean("usePrefix6InApi");

                        updatePrefixesInMemory(fruits, dairy, nuts, four, five, six);
                        updatePrefixTolerancesInMemory(fruitsTolerance, dairyTolerance, nutsTolerance, fourTolerance, fiveTolerance, sixTolerance);
                        updatePrefixUsageInMemory(includeFruitsPrefix, includeDairyPrefix, includeNutsPrefix, includeFourPrefix, includeFivePrefix, includeSixPrefix);

                        boolean isTwoLights = false;
                        boolean hasEmailConfig = false;
                        boolean isPrefixInDinars = false;
                        if(company.getJSONArray("configurations").length() > 0) {
                            JSONArray array = company.getJSONArray("configurations");

                            for (int i = 0; i < array.length(); i++) {

                                JSONObject obj = array.getJSONObject(i);
                                if(obj.getString("name").equals("IsTwoColorPayment")) isTwoLights = obj.getBoolean("value");
                                else if(obj.getString("name").equals("AllowEmailEntry")) hasEmailConfig = obj.getBoolean("value");
                                else if(obj.getString("name").equals("DinarToFilsForVegetablesWeightEvaluation")) isPrefixInDinars = obj.getBoolean("value");
                            }
                        }

                        mPreferences.edit().putBoolean(SharedPreferencesKey.HAS_CREDIT_CARD_PAYMENT.name(), company.getBoolean("hasCreditCardPayment")).apply();
                        mPreferences.edit().putBoolean(SharedPreferencesKey.HAS_KNET_PAYMENT.name(), company.getBoolean("hasKnetPayment")).apply();
                        mPreferences.edit().putBoolean(SharedPreferencesKey.IS_TWO_LIGHTS.name(), isTwoLights).apply();
                        mPreferences.edit().putBoolean(SharedPreferencesKey.HAS_EMAIL_CONFIG.name(), hasEmailConfig).apply();
                        mPreferences.edit().putBoolean(SharedPreferencesKey.IS_PREFIX_IN_DINARS.name(), isPrefixInDinars).apply();

                        mPreferences.edit().putString(SharedPreferencesKey.LOGO.name(), company.getString("logo").equals("null") ? null
                                : company.getString("logo").trim()).apply();

                        loadImageURL(findViewById(R.id.pagethree_comp_logo));

                        Intent intent = new Intent(LanguageSelectionActivity.this, CustomerInfoActivity.class);
                        startActivity(intent);
                    }
                    else {
                        updatePrefixesInMemory(null, null, null, null, null, null);
                        updatePrefixTolerancesInMemory("0", "0", "0", "0", "0", "0");
                        doError(getString(R.string.error), msg);
                    }
                } catch (JSONException e) {
                    logError(TAG+" => handleResponse => retrieveCompanyDetails => "+e.getMessage());
                    updatePrefixesInMemory(null, null, null, null, null, null);
                    updatePrefixTolerancesInMemory("0", "0", "0", "0", "0", "0");
                    doError(getString(R.string.error), getString(R.string.error_request_failed));
                }
            }
        });
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        runOnUiThread(() -> {

            if (method == ApiUrl.RETRIEVE_WEIGHT_TOLERANCES) {
                makeHttpGetRequest(ApiUrl.RETRIEVE_COMPANY_DETAILS, "", null);
            } else if (method == ApiUrl.RETRIEVE_COMPANY_DETAILS) {

                englishBtn.setEnabled(true);
                arabicBtn.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);

                updatePrefixesInMemory(null, null, null, null, null, null);
                updatePrefixTolerancesInMemory("0", "0", "0", "0", "0", "0");
                doError(getString(R.string.error), getString(R.string.error_request_failed));
            }
        });
    }

    // TODO: uncomment for cart device
    /*@Override
    public void onScaleReadData(String data) { }

     */
}