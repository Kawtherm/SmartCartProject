package com.smartlife.smartcart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.android.volley.VolleyError;
import com.smartlife.smartcart.classes.AppProviders;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**This activity is for storing important RULs and registering the cart to SmartLife*/
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private EditText mSmartlifeApiTxt;
    private EditText mSmartlifeAdsTxt;
    private EditText mSmartlifeCmsTxt;
    private EditText mCartNumberTxt;
    private EditText mInventoryApiTxt;
    private EditText mStoreNumberTxt;

    private RadioGroup mInventoriesRG;
    private RadioButton mEEMCRBtn;
    private RadioButton mSmartLifeRBtn;
    private RadioButton mIntegrationRBtn;

    private String mMacAddress;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSmartlifeApiTxt = findViewById(R.id.settings_smartlife_api_url);
        mSmartlifeAdsTxt = findViewById(R.id.settings_smartlife_ads_url);
        mSmartlifeCmsTxt = findViewById(R.id.settings_smartlife_cms_url);
        mCartNumberTxt = findViewById(R.id.settings_cart_nb);
        mInventoryApiTxt = findViewById(R.id.settings_inventory_api_url);
        EditText mMacAddressTxt = findViewById(R.id.settings_macaddress);
        mStoreNumberTxt = findViewById(R.id.settings_store_number);
        mMacAddressTxt.setEnabled(false);

        mInventoriesRG = findViewById(R.id.inventories_radio_group);
        mEEMCRBtn = findViewById(R.id.inventory_eemc_rbtn);
        mSmartLifeRBtn = findViewById(R.id.inventory_smartlife_rbtn);
        mIntegrationRBtn = findViewById(R.id.inventory_integration_rbtn);

        Button mBackBtn = findViewById(R.id.settings_back_btn);
        Button mSaveBtn = findViewById(R.id.settings_save_btn);

        mBackBtn.setOnClickListener(view -> finish());
        mSaveBtn.setOnClickListener(view -> saveSettings());

        try {
            //TODO: uncomment when fixing macaddress
            /*WifiManager manager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            mMacAddress = info.getMacAddress();

            mMacAddressTxt.setText(mMacAddress);*/
            //mMacAddressTxt.setText("88:83:c9:96:9f:77");
            mMacAddress = getMacAddress();
            mMacAddressTxt.setText(mMacAddress);//"81:70:a4:67:8e:53");

        } catch (Exception e) {
            logError(TAG+" => retrieving MAC address => "+e.getMessage());
        }

        loadSettings();
    }

    public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /*
     * Get the STB MacAddress
     */
    public String getMacAddress(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {

            return null;
        }
    }

    private void saveSettings() {

        String smartlifeApiUrl = mSmartlifeApiTxt.getText().toString().trim();
        String smartlifeAdsUrl = mSmartlifeAdsTxt.getText().toString().trim();
        String smartlifeCmsUrl = mSmartlifeCmsTxt.getText().toString().trim();
        String cartNumber = mCartNumberTxt.getText().toString().trim();
        String inventoryApiUrl = mInventoryApiTxt.getText().toString().trim();
        String storeNumber = mStoreNumberTxt.getText().toString().trim();

        if(mInventoriesRG.getCheckedRadioButtonId() == R.id.inventory_smartlife_rbtn) {
            mPreferences.edit().putInt(SharedPreferencesKey.INVENTORY_INDEX.name(), AppProviders.SMART_LIFE).apply();
        }
        else if(mInventoriesRG.getCheckedRadioButtonId() == R.id.inventory_eemc_rbtn) {
            mPreferences.edit().putInt(SharedPreferencesKey.INVENTORY_INDEX.name(), AppProviders.EEMC).apply();
        }
        else if(mInventoriesRG.getCheckedRadioButtonId() == R.id.inventory_integration_rbtn) {
            mPreferences.edit().putInt(SharedPreferencesKey.INVENTORY_INDEX.name(), AppProviders.INTEGRATION).apply();
        }

        mPreferences.edit().putString(SharedPreferencesKey.SMART_LIFE_API_URL.name(), smartlifeApiUrl).apply();
        mPreferences.edit().putString(SharedPreferencesKey.SMART_LIFE_ADS_URL.name(), smartlifeAdsUrl).apply();
        mPreferences.edit().putString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), smartlifeCmsUrl).apply();
        mPreferences.edit().putString(SharedPreferencesKey.CART_MAC_ADDRESS.name(), mMacAddress).apply();
        mPreferences.edit().putString(SharedPreferencesKey.CART_NUMBER.name(), cartNumber).apply();
        mPreferences.edit().putString(SharedPreferencesKey.INVENTORY_API_URL.name(), inventoryApiUrl).apply();
        mPreferences.edit().putString(SharedPreferencesKey.STORE_NUMBER.name(), storeNumber).apply();

        showActivityIndicator(getString(R.string.saving));

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float)scale;
        int batteryLevel = Math.round(batteryPct);

        JSONObject parameters = new JSONObject();

        try {
            parameters.put("number", cartNumber);
            //TODO: fix macaddress issue
            //parameters.put("mac_address", "88:83:c9:96:9f:77");
            //parameters.put("mac_address", "81:70:a4:67:8e:53");
            parameters.put("mac_address", mMacAddress);
            //parameters.put("battery", batteryLevel);
            parameters.put("battery", 20);
            makeHttpPostRequest(ApiUrl.UPDATE_CART_BAT, parameters, null);
        } catch (Exception e) {
            logError(TAG+" => SaveSettings => "+e.getMessage());
        }
    }

    private void loadSettings() {

        mSmartlifeApiTxt.setText(mPreferences.getString(SharedPreferencesKey.SMART_LIFE_API_URL.name(), ""));
        mSmartlifeAdsTxt.setText(mPreferences.getString(SharedPreferencesKey.SMART_LIFE_ADS_URL.name(), ""));
        mSmartlifeCmsTxt.setText(mPreferences.getString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), ""));
        mCartNumberTxt.setText(mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), ""));
        mInventoryApiTxt.setText(mPreferences.getString(SharedPreferencesKey.INVENTORY_API_URL.name(), ""));
        mStoreNumberTxt.setText(mPreferences.getString(SharedPreferencesKey.STORE_NUMBER.name(), ""));

        int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);
        switch (inventoryIndex) {
            case AppProviders.SMART_LIFE:
                mSmartLifeRBtn.setChecked(true);
                break;
            case AppProviders.EEMC:
                mEEMCRBtn.setChecked(true);
                break;
            case AppProviders.INTEGRATION:
                mIntegrationRBtn.setChecked(true);
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);

        runOnUiThread(() -> {
            hideActivityIndicator();
            try {
                int code = response.getInt("code");
                String msg = response.getString("msg");

                if(code == 0) {

                    String companyId = response.getString("company");
                    String customerCartNumber = response.getString("customerCartNumber");

                    mPreferences.edit().putString(SharedPreferencesKey.COMPANY_ID.name(), companyId).apply();
                    mPreferences.edit().putString(SharedPreferencesKey.CUSTOMER_CART_NUMBER.name(), customerCartNumber).apply();

                    doSuccess(getString(R.string.title_success), getString(R.string.successful_settings_save));
                }
                else {
                    doError(getString(R.string.error), msg);
                }

            } catch(Exception e) {
                logError(TAG+" => handleResponse => updateBattery => "+e.getMessage());
            }
        });
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        runOnUiThread(() -> {
            try {
                hideActivityIndicator();
                doError(getString(R.string.error), getString(R.string.error_settings_save));
            }
            catch (Exception e) {
                logError(TAG+" => handleError => updateBattery => "+e.getMessage());
            }

        });
    }
}