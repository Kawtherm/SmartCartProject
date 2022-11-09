package com.smartlife.smartcart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpResponse;
import com.google.android.gms.vision.clearcut.LogUtils;
import com.google.gson.Gson;
import com.smartlife.smartcart.classes.AppProviders;
import com.smartlife.smartcart.classes.AppConsts;
import com.smartlife.smartcart.classes.Device;
import com.smartlife.smartcart.classes.ResizeAnimation;
import com.smartlife.smartcart.classes.SDKConstants;
import com.smartlife.smartcart.classes.ScannerSerialPortManager;
import com.smartlife.smartcart.classes.SoundPoolPlayer;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;
import com.smartlife.smartcart.interfaces.IScannerReadCallback;
import com.smartlife.smartcart.model.Order;
import com.smartlife.smartcart.model.Product;
import com.smartlife.smartcart.enums.ActivityExtraKey;
import com.smartlife.smartcart.model.Tolerance;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShoppingActivity extends BaseActivity implements IScaleReadCallback, IScannerReadCallback, View.OnClickListener {

    private final String TAG = "PageFive";
    private RecyclerView mRecyclerView;
    private Button mPayNowBtn;
    private  String ID="02-50-4B-0F-E1-91";
    private final String Barcode="7340131601947";
    private final String API="https://smartlifeserver.com/api";
    private ProductAdapter mProductAdapter;
    private SoundPoolPlayer mPlayer;
    private TextView mTotalVal;
    private TextView mStatusNotification;
    private EditText mHiddenBarcode;
    private ArrayList<Product> mProducts;

    private boolean mScanOn = true;
    private boolean mIsNotifying = false;

    AlertDialog mExitDialog;
    AlertDialog mBarcodeInputDialog;
    AlertDialog mDeleteItemDialog;
    AlertDialog mCartErrorDialog;
    private EditText mBarcodeTxt;
    String barcode = "";
    Handler mHandler = new Handler();
    Runnable mRunnable;
    ShoppingActivity mContext;

    private int mCurWeight;
    private int count = 0;
    private int[] mStaWeights = new int[SDKConstants.WStableArraySize];
    private int mAveCount = 0;
    private int[] mAveWeights = new int[SDKConstants.WAveSize];
    private int mStableWeight = 0;
    private int mPreWeight;
    private boolean mProcessingWeightAddition = false;
    private boolean mIsAddingOrder = false;
    private String mFruitsPrefix;
    private String mNutsPrefix;
    private String mDairyPrefix;
    private String mFourPrefix;
    private String mFivePrefix;
    private String mSixPrefix;
    private int mFruitsPrefixTolerance;
    private int mNutsPrefixTolerance;
    private int mDairyPrefixTolerance;
    private int mFourPrefixTolerance;
    private int mFivePrefixTolerance;
    private int mSixPrefixTolerance;
    private boolean mFruitsPrefixUsage;
    private boolean mNutsPrefixUsage;
    private boolean mDairyPrefixUsage;
    private boolean mFourPrefixUsage;
    private boolean mFivePrefixUsage;
    private boolean mSixPrefixUsage;
    private boolean isPrefixInDinars;
    private final HashMap<String, Double> mPendingPrefixedProducts = new HashMap<>();

    private Order mCurrentOrder;

    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams lp;
    ResizeAnimation a;

    private static int mCount = 0;






    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_shopping);
        loadImageURL(findViewById(R.id.pagefive_comp_logo));

        mProducts = new ArrayList<>();
        mContext = this;
        ID=getMacAddress();


        mHiddenBarcode = findViewById(R.id.scanned_barcode);
        mHiddenBarcode.setShowSoftInputOnFocus(false);
        mHiddenBarcode.setShowSoftInputOnFocus(false);

        mCurrentOrder = null;
        String userID = getIntent().getStringExtra(ActivityExtraKey.USER_ID.name());
        String userEmail = getIntent().getStringExtra(ActivityExtraKey.USER_EMAIL.name());
        String cartUser = getIntent().getStringExtra(ActivityExtraKey.CART_USER.name());
        String membershipNo = getIntent().getStringExtra(ActivityExtraKey.MEMBERSHIP_NUM.name());
        String membershipID = getIntent().getStringExtra(ActivityExtraKey.MEMBERSHIP_ID.name());
        double membershipCurrentBalance = getIntent().getDoubleExtra(ActivityExtraKey.MEMBERSHIP_BALANCE.name(), 0);

        mFruitsPrefix = mPreferences.getString(SharedPreferencesKey.FRUITS_PREFIX.name(), "");
        mNutsPrefix = mPreferences.getString(SharedPreferencesKey.NUTS_PREFIX.name(), "");
        mDairyPrefix = mPreferences.getString(SharedPreferencesKey.DAIRY_PREFIX.name(), "");
        mFourPrefix = mPreferences.getString(SharedPreferencesKey.FOUR_PREFIX.name(), "");
        mFivePrefix = mPreferences.getString(SharedPreferencesKey.FIVE_PREFIX.name(), "");
        mSixPrefix = mPreferences.getString(SharedPreferencesKey.SIX_PREFIX.name(), "");
        mFruitsPrefixTolerance = mPreferences.getInt(SharedPreferencesKey.FRUITS_PREFIX_TOLERANCE.name(), 0);
        mNutsPrefixTolerance = mPreferences.getInt(SharedPreferencesKey.NUTS_PREFIX_TOLERANCE.name(), 0);
        mDairyPrefixTolerance = mPreferences.getInt(SharedPreferencesKey.DAIRY_PREFIX_TOLERANCE.name(), 0);
        mFourPrefixTolerance = mPreferences.getInt(SharedPreferencesKey.FOUR_PREFIX_TOLERANCE.name(), 0);
        mFivePrefixTolerance = mPreferences.getInt(SharedPreferencesKey.FIVE_PREFIX_TOLERANCE.name(), 0);
        mSixPrefixTolerance = mPreferences.getInt(SharedPreferencesKey.SIX_PREFIX_TOLERANCE.name(), 0);
        mFruitsPrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.FRUITS_PREFIX_USAGE.name(), true);
        mNutsPrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.NUTS_PREFIX_USAGE.name(), true);
        mDairyPrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.DAIRY_PREFIX_USAGE.name(), true);
        mFourPrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.FOUR_PREFIX_USAGE.name(), true);
        mFivePrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.FIVE_PREFIX_USAGE.name(), true);
        mSixPrefixUsage = mPreferences.getBoolean(SharedPreferencesKey.SIX_PREFIX_USAGE.name(), true);
        isPrefixInDinars = mPreferences.getBoolean(SharedPreferencesKey.IS_PREFIX_IN_DINARS.name(), false);

        relativeLayout = findViewById(R.id.status_notification_bar_holder);
        lp = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        a = new ResizeAnimation(relativeLayout);

        WebView adPage = findViewById(R.id.pagefive_ad);
        adPage.getSettings().setJavaScriptEnabled(true);
        adPage.getSettings().setMediaPlaybackRequiresUserGesture(false);
        adPage.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        adPage.getSettings().setSupportZoom(false);
        adPage.getSettings().setLoadWithOverviewMode(true);
        adPage.getSettings().setDomStorageEnabled(true);
        adPage.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        adPage.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // this is to allow redirects from http to https

        adPage.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                handler.proceed(); // this is to accept all ssl certificates even the failed ones
            }
        });

        adPage.setWebChromeClient(new WebChromeClient() {

            @Nullable
            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(convertDpToPixels(10), convertDpToPixels(10), Bitmap.Config.ARGB_8888);
            }
        });

        String url = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_ADS_URL.name(), "");

        adPage.loadUrl("http://smartlifekwt2.engine.adglare.net/?100912867&iframe");

        mPlayer = new SoundPoolPlayer(this);
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        TextView header_item_lbl = findViewById(R.id.header_item_lbl);
        TextView header_price_lbl = findViewById(R.id.header_price_lbl);
        TextView header_qty_lbl = findViewById(R.id.header_qty_lbl);
        TextView header_subtotal_lbl = findViewById(R.id.header_subtotal_lbl);
        TextView exit_lbl = findViewById(R.id.pagefive_exit_lbl);
        TextView barcode_input_lbl = findViewById(R.id.pagefive_barcode_lbl);
        TextView total_lbl = findViewById(R.id.pagefive_total_lbl);
        mStatusNotification = findViewById(R.id.status_notification_lbl);

        mPayNowBtn = findViewById(R.id.pagefive_pay_btn);
        enablePayBtn(false);
        ImageView exitBtn = findViewById(R.id.pagefive_exit_btn);
        ImageView inputBarcodeBtn = findViewById(R.id.pagefive_barcode_btn);

        // Prepare Dialogs and Alerts
        prepareExitDialog();
        prepareBarcodeInputDialog();
        prepareCartErrorDialog();

        exitBtn.setOnClickListener(view -> {
            mExitDialog.show();
            mExitDialog.getWindow().setLayout(convertDpToPixels(400), convertDpToPixels(258));
        });

        inputBarcodeBtn.setOnClickListener(view -> {

            mBarcodeInputDialog.show();
            mBarcodeInputDialog.getWindow().setLayout(convertDpToPixels(300), convertDpToPixels(620));
        });

        if (isArabic()) {
            header_item_lbl.setText(R.string.header_item_ar);
            header_price_lbl.setText(R.string.header_price_ar);
            header_qty_lbl.setText(R.string.header_qty_ar);
            header_subtotal_lbl.setText(R.string.header_subtotal_ar);
            exit_lbl.setText(R.string.exit_ar);
            barcode_input_lbl.setText(R.string.barcode_input_ar);
            total_lbl.setText(R.string.total_ar);
            mPayNowBtn.setText(R.string.pay_now_ar);
        }

        mTotalVal = findViewById(R.id.pagefive_total_val);

        mPayNowBtn.setOnClickListener(view -> {

            mPayNowBtn.setEnabled(false);
            mIsAddingOrder = true;

            String cartNo = mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), "");
            String storeNo = mPreferences.getString(SharedPreferencesKey.STORE_NUMBER.name(), "");
            String companyId = mPreferences.getString(SharedPreferencesKey.COMPANY_ID.name(), "");
            Gson gson = new Gson();
            String invoiceDetail = gson.toJson(mProducts);

            mCurrentOrder = new Order();
            mCurrentOrder.invoiceDetail = invoiceDetail;
            mCurrentOrder.companyId = companyId;
            mCurrentOrder.storeNo = storeNo;
            mCurrentOrder.cartNo = cartNo;
            mCurrentOrder.totalAmount = getTotal();
            mCurrentOrder.userID = userID;
            mCurrentOrder.customerNumber = membershipNo;
            mCurrentOrder.customerID = membershipID;
            mCurrentOrder.customerBalance = membershipCurrentBalance;

            JSONObject parameters = new JSONObject();
            try {
                parameters.put("company_id", mCurrentOrder.companyId);
                parameters.put("store_number", mCurrentOrder.storeNo);
                parameters.put("shift_number", mCurrentOrder.shiftNo);
                parameters.put("cart_number", mCurrentOrder.cartNo);
                parameters.put("invoice_detail",  new JSONArray(mCurrentOrder.invoiceDetail));
                parameters.put("total_amount", mCurrentOrder.totalAmount);
                parameters.put("cart_user", cartUser);
                parameters.put("email", userEmail);

                if(mCurrentOrder.customerNumber != null && !mCurrentOrder.customerNumber.isEmpty()) {
                    parameters.put("user_id", mCurrentOrder.customerNumber);
                    parameters.put("current_balance", mCurrentOrder.customerBalance);
                    parameters.put("new_balance", (mCurrentOrder.customerBalance+mCurrentOrder.totalAmount));
                }
            }
            catch (Exception e) { }

            makeHttpPostRequest(ApiUrl.ADD_ORDER, parameters, null);
            init(ID);
            if(initResponse()){
                event(ID,"","");
                transaction(ID,"transaction_id", String.valueOf(mCurrentOrder.totalAmount));
                if(initResponse()){
                    close(ID);
                }
                else{
                    doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                            getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
                }
            }
            else{
                doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                        getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
            }
        });

        mRecyclerView = findViewById(R.id.items_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ShoppingActivity.this));

        mProductAdapter = new ProductAdapter();
        setupAdapter();

        //TODO: Turn scanner on
        //turnScannerHardwareOn();
        switchToScanMode(this);
    }

    @Override
    protected void onResume() {
        //TODO: turn scanner on
       // turnScannerHardwareOn();
        //switchToScanMode(this);
        //TODO: uncomment to turn scanner on
        /*mHandler.postDelayed(mRunnable = () -> {
            mHandler.postDelayed(mRunnable, AppConsts.SERIAL_PORT_DELAY);
            if (mScanOn) {
                mScanOn = false;
                ScannerSerialPortManager scannerSerialPortManager = ScannerSerialPortManager.instance();
                scannerSerialPortManager.setScannerReadCallback(mContext);
                scannerSerialPortManager.open(new Device("/dev/ttySAC3", "19200"));

            } else {
                mScanOn = true;
                switchToScaleMode(this);
            }
        }, AppConsts.SERIAL_PORT_DELAY);*/

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        //TODO: turn scanner on
        //switchHardwareOff();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        //TODO: turn scanner on
        //switchHardwareOff();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {



        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            char pressedKey = (char) event.getUnicodeChar();
            barcode  = barcode + pressedKey;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if(event.getAction()==KeyEvent.ACTION_UP) {

                String str = barcode.substring(0, barcode.length()-1);
                barcode = str;
                verifyProduct(barcode);
                barcode="";
                mHiddenBarcode.setText("");



                return true;

            }



            /*View view2 = this.getCurrentFocus();
            if (view2 != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
            }*/




        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //TODO: turn scanner on
        //switchHardwareOff();
        finish();
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.barcode_delete_nb_btn) {

            if (mBarcodeTxt.getText().length() > 0) {
                CharSequence currentText = mBarcodeTxt.getText();
                mBarcodeTxt.setText(currentText.subSequence(0, currentText.length() - 1));
                mBarcodeTxt.setSelection(mBarcodeTxt.getText().length());
            } else {
                mBarcodeTxt.setText("");
            }
        } else if(v.getId() == R.id.barcode_submit_btn) {

            mBarcodeInputDialog.dismiss();

            if (mBarcodeTxt.getText().length() > 0) {
                String barcode = mBarcodeTxt.getText().toString().replace("\n", "").replace("\r", "").trim();
                verifyProduct(barcode);
            }
        } else {
            mBarcodeTxt.setText(mBarcodeTxt.getText().append(((Button)v).getText()));
            mBarcodeTxt.setSelection(mBarcodeTxt.getText().length());
        }
    }

    private void enablePayBtn(boolean isEnable) {
        if(mIsNotifying) { // added this condition because it was enabling the button only by pressing increment/decrement
            mPayNowBtn.setEnabled(false);
            mPayNowBtn.setBackgroundColor(Color.rgb(200, 200, 200));
        }
        else if(!mIsAddingOrder) {
            if (isEnable) {
                mPayNowBtn.setEnabled(true);
                mPayNowBtn.setBackgroundColor(Color.rgb(139, 209, 42));
            } else {
                mPayNowBtn.setEnabled(false);
                mPayNowBtn.setBackgroundColor(Color.rgb(200, 200, 200));
            }
        }
    }

    // region Product

    // region Prefix
    // Check first if the product's barcode is for Fruits, Dairy or Peanuts, etc
    private boolean isPrefixedProduct(String barcode) {
        return isVegetablePrefix(barcode) || isDairyPrefix(barcode) || isNutsPrefix(barcode)
                || isFourPrefix(barcode) || isFivePrefix(barcode) || isSixPrefix(barcode);
    }

    private String getBarcodeFromPrefix(String barcode) {
        if((isVegetablePrefix(barcode) && mFruitsPrefixUsage) ||
                (isDairyPrefix(barcode) && mDairyPrefixUsage) ||
                (isNutsPrefix(barcode) && mNutsPrefixUsage) ||
                (isFourPrefix(barcode) && mFourPrefixUsage) ||
                (isFivePrefix(barcode) && mFivePrefixUsage) ||
                (isSixPrefix(barcode) && mSixPrefixUsage)) {
            return barcode;
        }
        else {
            return barcode.substring(2);
        }
    }

    private int getPrefixTolerance(String barcode) {
        int tolerance = 0;

        if(isVegetablePrefix(barcode)) tolerance = Math.max(tolerance, mFruitsPrefixTolerance);
        else if(isDairyPrefix(barcode)) tolerance = Math.max(tolerance, mDairyPrefixTolerance);
        else if(isNutsPrefix(barcode)) tolerance = Math.max(tolerance, mNutsPrefixTolerance);
        else if(isFourPrefix(barcode)) tolerance = Math.max(tolerance, mFourPrefixTolerance);
        else if(isFivePrefix(barcode)) tolerance = Math.max(tolerance, mFivePrefixTolerance);
        else if(isSixPrefix(barcode)) tolerance = Math.max(tolerance, mSixPrefixTolerance);

        return tolerance;
    }

    private boolean isVegetablePrefix(String barcode) {
        return !mFruitsPrefix.isEmpty() && barcode.startsWith(mFruitsPrefix);
    }

    private boolean isDairyPrefix(String barcode) {
        return !mDairyPrefix.isEmpty() && barcode.startsWith(mDairyPrefix);
    }

    private boolean isNutsPrefix(String barcode) {
        return !mNutsPrefix.isEmpty() && barcode.startsWith(mNutsPrefix);
    }

    private boolean isFourPrefix(String barcode) {
        return !mFourPrefix.isEmpty() && barcode.startsWith(mFourPrefix);
    }

    private boolean isFivePrefix(String barcode) {
        return !mFivePrefix.isEmpty() && barcode.startsWith(mFivePrefix);
    }

    private boolean isSixPrefix(String barcode) {
        return !mSixPrefix.isEmpty() && barcode.startsWith(mSixPrefix);
    }

    private double getPrefixPrice(String barcode) {

        double price = 0;
        try {
            for (String key: mPendingPrefixedProducts.keySet()) {
                if(key.equals(barcode)) {
                    price = mPendingPrefixedProducts.get(key) / 1000.0;
                    break;
                }
            }
            mPendingPrefixedProducts.remove(barcode);
        }
        catch(Exception e) { }

        return price;
    }
    // endregion

    private void verifyProduct(String barcode) {

        int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);

        if (inventoryIndex == AppProviders.SMART_LIFE) {


            if(barcode.length() == 13) {
                if(isPrefixedProduct(barcode)) {

                    String productBarcode = getBarcodeFromPrefix(barcode.substring(0, 7));
                    double productPrice = Double.parseDouble(barcode.substring(7, 12));

                    mPendingPrefixedProducts.put(productBarcode, productPrice);

                    JSONObject parameters = new JSONObject();
                    try{
                        parameters.put("barcode", productBarcode);
                    }
                    catch (Exception e){ }
                    makeHttpPostRequest(ApiUrl.VERIFY_PRODUCT, parameters, barcode.substring(0, 7));
                    return;
                }
            }

            JSONObject parameters = new JSONObject();
            try{

                parameters.put("barcode", barcode);
            }
            catch (Exception e) { }

            makeHttpPostRequest(ApiUrl.VERIFY_PRODUCT, parameters, barcode);

        } else if (inventoryIndex == AppProviders.EEMC) {

            if(barcode.length() == 13) {
                if(isPrefixedProduct(barcode)) {

                    String productBarcode = getBarcodeFromPrefix(barcode.substring(0, 7));
                    double productPrice = Double.parseDouble(barcode.substring(7, 12));

                    mPendingPrefixedProducts.put(productBarcode, productPrice);
                    makeHttpGetRequest(ApiUrl.VERIFY_PRODUCT, productBarcode, barcode.substring(0, 7));
                    return;
                }
            }

            makeHttpGetRequest(ApiUrl.VERIFY_PRODUCT, barcode, barcode);

        } else if (inventoryIndex == AppProviders.INTEGRATION) {

            if(barcode.length() == 13) {
                if(isPrefixedProduct(barcode)) {

                    String productBarcode = getBarcodeFromPrefix(barcode.substring(0, 7));
                    double productPrice = Double.parseDouble(barcode.substring(7, 12));

                    mPendingPrefixedProducts.put(productBarcode, productPrice);
                    String params = String.format("%s?isWeightedProduct=true", productBarcode);
                    makeHttpGetRequest(ApiUrl.VERIFY_PRODUCT, params, barcode.substring(0, 7));
                    return;
                }
            }

            String params = String.format("%s?isWeightedProduct=false", barcode);
            makeHttpGetRequest(ApiUrl.VERIFY_PRODUCT, params, barcode);
        }
    }

    private int findProduct(String barcode) {
        for (int i = 0; i < mProducts.size(); i++) {
            if(mProducts.get(i).barcode.equals(barcode)) return i;
        }
        return -1;
    }
    // endregion

    // region Dialogs
    private void prepareExitDialog() {
        mExitDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.exit_dialog, null);
        TextView exitLbl = dialogView.findViewById(R.id.exit_quest_lbl);
        Button yesBtn = dialogView.findViewById(R.id.exit_yes_btn);
        Button noBtn = dialogView.findViewById(R.id.exit_no_btn);

        if (isArabic()) {
            RelativeLayout btnGrp = dialogView.findViewById(R.id.exit_dlg_btn_grp);
            btnGrp.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            exitLbl.setText(R.string.exit_quest_ar);
            yesBtn.setText(R.string.exit_yes_ar);
            noBtn.setText(R.string.exit_no_ar);
        }

        yesBtn.setOnClickListener(view -> {

            mExitDialog.dismiss();

            Intent intent = new Intent(ShoppingActivity.this, AdsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        noBtn.setOnClickListener(view -> mExitDialog.dismiss());
        mExitDialog.setView(dialogView);
    }

    private void prepareBarcodeInputDialog() {
        mBarcodeInputDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.input_barcode_dialog, null);
        TextView headlerLbl = dialogView.findViewById(R.id.enter_barcode_lbl);
        mBarcodeTxt = dialogView.findViewById(R.id.barcode_number_txt);
        mBarcodeTxt.setShowSoftInputOnFocus(false);

        Button oneBtn = dialogView.findViewById(R.id.barcode_one_btn);
        Button twoBtn = dialogView.findViewById(R.id.barcode_two_btn);
        Button threeBtn = dialogView.findViewById(R.id.barcode_three_btn);
        Button fourBtn = dialogView.findViewById(R.id.barcode_four_btn);
        Button fiveBtn = dialogView.findViewById(R.id.barcode_five_btn);
        Button sixBtn = dialogView.findViewById(R.id.barcode_six_btn);
        Button sevenBtn = dialogView.findViewById(R.id.barcode_seven_btn);
        Button eightBtn = dialogView.findViewById(R.id.barcode_eight_btn);
        Button nineBtn = dialogView.findViewById(R.id.barcode_nine_btn);
        Button zeroBtn = dialogView.findViewById(R.id.barcode_zero_btn);
        Button deleteNumberBtn = dialogView.findViewById(R.id.barcode_delete_nb_btn);
        Button submitBtn = dialogView.findViewById(R.id.barcode_submit_btn);
        mBarcodeTxt.setText("");

        zeroBtn.setOnClickListener(this);
        oneBtn.setOnClickListener(this);
        twoBtn.setOnClickListener(this);
        threeBtn.setOnClickListener(this);
        fourBtn.setOnClickListener(this);
        fiveBtn.setOnClickListener(this);
        sixBtn.setOnClickListener(this);
        sevenBtn.setOnClickListener(this);
        eightBtn.setOnClickListener(this);
        nineBtn.setOnClickListener(this);
        deleteNumberBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);

        if (isArabic()) {
            headlerLbl.setText(R.string.barcode_number_ar);
            submitBtn.setText(R.string.submit_ar);
        }

        mBarcodeInputDialog.setView(dialogView);

        mBarcodeInputDialog.setOnShowListener(listener -> {
            mBarcodeTxt.requestFocus();
        });

        mBarcodeInputDialog.setOnDismissListener(listener -> {
            mBarcodeTxt.clearFocus();
            mBarcodeTxt.setText("");
        });
    }

    private void prepareDeleteItemDialog(int position) {
        mDeleteItemDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.delete_product_dialog, null);
        TextView titleLbl = dialogView.findViewById(R.id.delete_item_lbl);
        TextView itemNameLbl = dialogView.findViewById(R.id.delete_item_val_lbl);
        Button yesBtn = dialogView.findViewById(R.id.delete_yes_btn);
        Button noBtn = dialogView.findViewById(R.id.delete_no_btn);

        mDeleteItemDialog.setOnShowListener(dialogInterface -> itemNameLbl.setText(mProducts.get(position).name));


        if (isArabic()) {
            RelativeLayout btnGrp = dialogView.findViewById(R.id.delete_dlg_btn_grp);
            btnGrp.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            titleLbl.setText(R.string.delete_item_quest_ar);
            yesBtn.setText(R.string.exit_yes_ar);
            noBtn.setText(R.string.exit_no_ar);
        }

        yesBtn.setOnClickListener(view -> {
            mDeleteItemDialog.dismiss();
            mProducts.remove(position);
            updateAdapter();
        });

        noBtn.setOnClickListener(view -> mDeleteItemDialog.dismiss());
        mDeleteItemDialog.setView(dialogView);

    }

    private void prepareCartErrorDialog() {
        mCartErrorDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cart_error_dialog, null);

        mCartErrorDialog.setView(dialogView);
    }
    // endregion

    // region Led Control
    @Override
    protected void lightOn(String ledName) {
        super.lightOn(ledName);
        this.enablePayBtn(false);
    }

    @Override
    protected void lightOff(String ledName) {
        super.lightOff(ledName);
        if (mProducts.size() > 0) enablePayBtn(true);
    }
    // endregion

    // region Weight Handling

    // region Weight Validation
    private Tolerance[] getWeightTolerances() {

        String toleranceString = mPreferences.getString(SharedPreferencesKey.TOLERANCE_ARRAY.name(), "");
        Gson gson = new Gson();
        try {
            return gson.fromJson(toleranceString, Tolerance[].class);
        }
        catch (Exception e) {
            logError(TAG+" => getWeightTolerances exception => "+e.getMessage());
            return null;
        }
    }

    private boolean validateProductsAgainstTolerance() {

        boolean isValid = true;

        for (int i = 0; i < mProducts.size(); i++) {
            for (int j = 0; j < mProducts.get(i).weights.size(); j++) {
                isValid &= validateIndividualProduct(mProducts.get(i), mProducts.get(i).weights.get(j));
            }
        }

        return isValid;
    }

    private boolean validateIndividualProduct(Product product, int scaledWeight) {

        boolean isValid = true;

        if(product.server_weight > 0) {

            if(isPrefixedProduct(product.originalBarcode)) {

                if (!isVegetablePrefix(product.originalBarcode)) {

                    int percentageTolerance = getPrefixTolerance(product.originalBarcode);
                    int difference = Math.abs(scaledWeight - product.server_weight);
                    double differencePercentage = (1.0 * difference / product.server_weight) * 100.0;

                    isValid = percentageTolerance >= differencePercentage;
                }
            }
            else {
                Tolerance[] tolerances = getWeightTolerances();

                for (int i = 0; i < (tolerances != null ? tolerances.length : 0); i++) {

                    if(tolerances[i].minWeight <= product.server_weight && product.server_weight <= tolerances[i].maxWeight) {

                        int difference = Math.abs(scaledWeight - product.server_weight);
                        isValid = tolerances[i].toleranceValue >= difference;

                        break;
                    }
                }
            }
        }

        return isValid;
    }

    // getting individual product's weight
    private boolean handleWeightAddition(int cartWeight, int prevWeight) {

        boolean isSuccessful = true;

        if(cartWeight > prevWeight && !mProcessingWeightAddition) {

            mProcessingWeightAddition = true;
            int additionalWeight = cartWeight - prevWeight;
            int newProductIndex = findProductWithNoWeight();

            if(newProductIndex >= 0 && newProductIndex < mProducts.size()) {

                int quantityOfNewProducts = mProducts.get(newProductIndex).quantity - mProducts.get(newProductIndex).weights.size();
                additionalWeight = additionalWeight / quantityOfNewProducts;

                if(validateIndividualProduct(mProducts.get(newProductIndex), additionalWeight)) {

                    if(isPrefixedProduct(mProducts.get(newProductIndex).originalBarcode)) {
                        mProducts.get(newProductIndex).weights.add(additionalWeight);
                        mProcessingWeightAddition = false;
                    }
                    else {
                        for (int i = 0; i < quantityOfNewProducts; i++) {
                            mProducts.get(newProductIndex).weights.add(additionalWeight);
                        }

                        JSONObject parameters = new JSONObject();
                        try{
                            parameters.put("barcode", mProducts.get(newProductIndex).barcode);
                            parameters.put("weight", additionalWeight);
                        }
                        catch (Exception e) { }

                        makeHttpPostRequest(ApiUrl.UPDATE_WEIGHT, parameters, null);
                    }
                }
                else {
                    isSuccessful = false;
                    mProcessingWeightAddition = false;
                }
            }
            else {
                mProcessingWeightAddition = false;
            }
        }

        return isSuccessful;
    }

    private int findProductWithNoWeight() {

        for (int i = 0; i < mProducts.size(); i++) {
            if(mProducts.get(i).weights.size() < mProducts.get(i).quantity) return i;
        }
        return -1;
    }

    private int IsProductScanned(String barcode) {
        for (int i = 0; i < mProducts.size(); i++) {
            if(mProducts.get(i).barcode.equals(barcode)) return i;
        }
        return -1;
    }

    private int getTotalProductsWeight() {

        int weight = 0;

        for (int i = 0; i < mProducts.size(); i++) {
            for (int j = 0; j < mProducts.get(i).weights.size(); j++) {
                weight += mProducts.get(i).weights.get(j);
            }
        }
        return weight;
    }

    private int getProductsCount() {

        int pCount = 0;
        for (int i = 0; i < mProducts.size(); i++) {
            pCount += mProducts.get(i).quantity;
        }
        return pCount;
    }

    private void validateWeights() {

        boolean isValid = true;
        int cartWeight = mStableWeight;
        int msgID = -1;
        boolean wrongWeightForProduct = false;

        int totalProductsWeight = getTotalProductsWeight();

        if(totalProductsWeight < cartWeight && Math.abs(totalProductsWeight - cartWeight) > AppConsts.SCALE_ERROR_MARGIN) {
            wrongWeightForProduct = !handleWeightAddition(cartWeight, totalProductsWeight);
        }

        totalProductsWeight = getTotalProductsWeight();
        int productsCount = getProductsCount();

        // applying weight tolerance validation for individual products
        boolean isViolatingTolerance = !validateProductsAgainstTolerance();

        // no item scanned and cart has weight
        if (productsCount == 0 && cartWeight > 0) {

            msgID = isArabic() ? R.string.msg_02_ar : R.string.msg_02;
            isValid = false;
        }
        // item scanned but wrong weight assigned
        else if(wrongWeightForProduct) {

            isValid = false;
            msgID = isArabic() ? R.string.msg_04_ar : R.string.msg_04;
        }
        // item scanned but still not added to cart (no additional weight)
        else if(findProductWithNoWeight() > -1) {

            isValid = false;
            msgID = isArabic() ? R.string.msg_01_ar : R.string.msg_01;
        }
        // there is difference in products weights and cart weight
        else if(Math.abs(totalProductsWeight - cartWeight) > AppConsts.SCALE_ERROR_MARGIN) {

            isValid = false;
            msgID = isArabic() ? R.string.msg_03_ar : R.string.msg_03;
        }

        if(!isValid || isViolatingTolerance) {

            if(msgID == -1) msgID = isArabic() ? R.string.msg_03_ar : R.string.msg_03;
            showNotification(getString(msgID), true);
        }
        else {
            showNotification(null, false);
        }
    }

    private void showNotification(String msg, boolean show) {

        if (show) {
            if (!mIsNotifying) {
                //TODO: uncomment for cart
                //mIsNotifying = true;

                runOnUiThread(() -> {
                    try {
                        mPlayer.playShortResource(R.raw.bell_put_warn);
                    } catch (Exception e) {
                        logError(TAG+" => showNotification exception => "+e.getMessage());
                    }
                });

                mStatusNotification.setText(msg);
                relativeLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                a.setParams(lp.height, convertDpToPixels(80));
                lightOn("led1");

                a.setDuration(100);
                relativeLayout.startAnimation(a);
            }
            else {
                mStatusNotification.setText(msg);
            }
        } else {
            mIsNotifying = false;
            a.setParams(lp.height, convertDpToPixels(1));
            relativeLayout.requestLayout();
            mStatusNotification.setText("");
            lightOff("led1");
            a.setDuration(100);
            relativeLayout.startAnimation(a);
        }
    }
    // endregion

    void verifyWeight(String barcode) {
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("barcode", barcode);
            makeHttpPostRequest(ApiUrl.VERIFY_WEIGHT, parameters, null);
        } catch (Exception e) {
            logError(TAG+" => verifyWeight exception => "+e.getMessage());
        }
    }

    public boolean checkSameAll(int[] weights2) {
        Set<Float> set = new HashSet<>();
        for (int i : weights2) {
            set.add((float) i);
        }
        return set.size() == 1;
    }

    private void resetAveData() {
        mAveCount = 0;
        this.mAveWeights = null;
    }

    private void resetStaData() {
        this.count = 0;
        this.mStaWeights = null;
    }

    private void clearAllArray() {
        resetStaData();
        resetAveData();
    }

    private void initAveWeight(int weight) {
        this.mAveCount++;
        this.mAveWeights[this.mAveCount - 1] = weight;
    }

    private int SortSumAve(int[] w) {
        int sum = 0;
        for (int i = 0; i < w.length - 1; i++) {
            for (int j = 0; j < (w.length - 1) - i; j++) {
                if (w[j] > w[j + 1]) {
                    int temp = w[j];
                    w[j] = w[j + 1];
                    w[j + 1] = temp;
                }
            }
        }
        for (int i2 = 9; i2 < w.length - 11; i2++) {
            sum += w[i2];
        }
        return sum / (SDKConstants.WAveSize - 20);
    }

    // endregion

    // region RecyclerView Setup and Updates
    private void setupAdapter() {
        mRecyclerView.setAdapter(mProductAdapter);
    }

    private void updateAdapter() {
        updateTotal();
        mProductAdapter.notifyDataSetChanged();
    }

    class ProductRow extends RecyclerView.ViewHolder {

        TextView productPrice;
        TextView productName;
        TextView productSubtotal;
        TextView productQuantity;
        Button incrementBtn;
        Button decrementBtn;
        ImageButton deleteBtn;
        RelativeLayout quantityGrp;

        public ProductRow(View view) {
            super(view);

            quantityGrp = view.findViewById(R.id.quantity_ctrl);
            productName = view.findViewById(R.id.product_name);
            productPrice = view.findViewById(R.id.product_price);
            productSubtotal = view.findViewById(R.id.product_subtotal);
            productQuantity = view.findViewById(R.id.quantity_lbl);

            incrementBtn = view.findViewById(R.id.increment_btn);
            decrementBtn = view.findViewById(R.id.decrement_btn);
            deleteBtn = view.findViewById(R.id.delete_btn);
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        void bindData(Product product, int position) {
            productQuantity.setText("" + product.quantity);
            String totalStr = String.format("%.3f", product.price);

            productPrice.setText(totalStr);
            productSubtotal.setText(String.format("%.3f", product.getSubtotal()));
            productName.setText(product.name);

            if(isPrefixedProduct(product.originalBarcode)) {
                incrementBtn.setEnabled(false);
                decrementBtn.setEnabled(false);
            }
            else {
                incrementBtn.setEnabled(true);
                decrementBtn.setEnabled(true);
            }

            if (isArabic()) {
                quantityGrp.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }

            incrementBtn.setOnClickListener(view -> {

                int indexOfProductWithNoWeight = findProductWithNoWeight();

                if(indexOfProductWithNoWeight > -1) {
                    if (mProducts.get(indexOfProductWithNoWeight).barcode.equals(mProducts.get(position).barcode)) {

                        mProducts.get(position).quantity += 1;
                        updateAdapter();
                    }
                }
                else if(!mIsNotifying) {

                    mProducts.get(position).quantity += 1;
                    updateAdapter();
                }
            });

            decrementBtn.setOnClickListener(view -> {

                if (mProducts.get(position).quantity > 1) {

                    if(mProducts.get(position).quantity == mProducts.get(position).weights.size()) {
                        mProducts.get(position).weights.remove(mProducts.get(position).weights.size() - 1);
                    }
                    mProducts.get(position).quantity -= 1;
                }
                updateAdapter();
            });

            deleteBtn.setOnClickListener(view -> {
                prepareDeleteItemDialog(position);
                mDeleteItemDialog.show();
                mDeleteItemDialog.getWindow().setLayout(convertDpToPixels(400), convertDpToPixels(300));
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateTotal() {
        double total = 0.0;

        enablePayBtn(mProducts.size() != 0);

        for (int i = 0; i < mProducts.size(); i++) {
            total += mProducts.get(i).price * mProducts.get(i).quantity;
        }

        String totalStr = String.format("%.3f", total);

        mTotalVal.setText(totalStr);
    }

    private double getTotal() {

        double total;

        String totalStr = mTotalVal.getText().toString();
        total = Double.parseDouble(totalStr);

        return total;
    }

    private void incrementProduct(Product product) {

        for (int i = 0; i < mProducts.size(); i++) {
            if (mProducts.get(i).barcode.equals(product.barcode)) {
                mProducts.get(i).quantity += 1;
                Product p = mProducts.get(i);
                mProducts.remove(i);
                mProducts.add(0, p);
                break;
            }
        }
    }

    private boolean isProductInCart(Product product) {

        boolean isAvailable = false;

        for (int i = 0; i < mProducts.size(); i++) {
            if (mProducts.get(i).barcode.equals(product.barcode)) {
                isAvailable = true;
                break;
            }
        }

        return isAvailable;
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductRow> {
        @NonNull
        @Override
        public ProductRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_row, parent, false);

            return new ProductRow(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductRow holder, int position) {

            final Product product = mProducts.get(position);
            holder.bindData(product, position);

        }

        @Override
        public int getItemCount() {
            Log.i("Shopping", ""+mProducts.size());
            return mProducts.size();
        }
    }
    // endregion

    // region Scanner and Scale observers
    @Override
    public void onScaleReadData(String data) {

        /*runOnUiThread(() -> {

            try {
                Thread.sleep(SDKConstants.WReadSpan);
            } catch (Exception e) {
                logError(TAG+" => onScaleReadData exception => "+e.getMessage());
            }

            if (data != null && data.startsWith(SDKConstants.WHeadST)) {
                // curWeightStr
                try {
                    if(data.length() > SDKConstants.WHeadLenght && SDKConstants.WHeadLenght < data.indexOf(SDKConstants.WTail)) {
                        String substring = data.substring(SDKConstants.WHeadLenght, data.indexOf(SDKConstants.WTail)).trim();
                        String replace1 = substring.replace(SDKConstants.WBlankSign, "").trim();
                        String replace2 = replace1.replace(SDKConstants.WAddSign, "").trim();
                        mCurWeight = Integer.parseInt(replace2);
                    }
                    else {
                        return;
                    }
                } catch (Exception e) {
                    logError(TAG+" => onScaleReadData exception => "+e.getMessage());
                    return;
                }

                count++;
                try {
                    if (mStaWeights == null) {
                        mStaWeights = new int[SDKConstants.WStableArraySize];
                    }

                    mStaWeights[count - 1] = mCurWeight;

                    if (count == SDKConstants.WRelStableArraySize) {
                        validateWeights();
                    } else if (count == SDKConstants.WRelStableArraySize + 1) {
                        validateWeights();
                    } else if (count == SDKConstants.WStableArraySize) {
                        if (!checkSameAll(mStaWeights)) {
                            resetStaData();
                        } else {
                           mStableWeight = mStaWeights[count - 1];
                            validateWeights();
                            clearAllArray();
                        }
                    }
                } catch (Exception e) {
                    logError(TAG+" => onScaleReadData exception => "+e.getMessage());
                    if (count > SDKConstants.WStableArraySize) {
                        resetStaData();
                    }
                }
            } else {
                resetStaData();
            }

            try {
                if (mAveCount == 0) {
                    mAveWeights = new int[SDKConstants.WAveSize];
                    mPreWeight = mCurWeight;
                    initAveWeight(mCurWeight);
                } else if (Math.abs(mCurWeight - mPreWeight) > SDKConstants.WAveRange) {
                    resetAveData();
                } else {
                    initAveWeight(mCurWeight);
                }
                if (mAveCount == SDKConstants.WAveSize) {
                    mStableWeight = SortSumAve(mAveWeights);
                    resetAveData();
                }
            } catch (Exception e) {
                logError(TAG+" => onScaleReadData exception => "+e.getMessage());
            }
        });*/
    }

    @Override
    public void onScannerReadData(String data) {

        /*runOnUiThread(() -> {
            try {
                mPlayer.playShortResource(R.raw.scan_success);
                String barcode = data != null ? data.replace("\n", "").replace("\r", "").trim() : "";
                verifyProduct(barcode);
            } catch (Exception e) {
                logError(TAG+" => onScannerReadData exception => "+e.getMessage());
            }

        });*/
    }
    // endregion

    // region HTTP response handler
    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);

        runOnUiThread(() -> {
            if (method == ApiUrl.VERIFY_PRODUCT) {
                try {

                    Product product = new Product();
                    product.originalBarcode = barcode;
                    double price = 0;
                    boolean isPrefix = false;

                    int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);

                    if (inventoryIndex == AppProviders.SMART_LIFE) {

                        int code = response.getInt("code");
                        String msg = response.getString("msg");

                        if (code == 0) {

                            JSONArray productsArray = response.getJSONArray("data");
                            JSONObject productObject = productsArray.getJSONObject(0);

                            product.id = productObject.getString("goods_id").trim();
                            product.name = productObject.getString("goods_name").trim();
                            product.barcode = productObject.getString("goods_barcode").trim();
                            price = productObject.getDouble("goods_price");

                        } else if(code == 1) {
                            if (isArabic()) {
                                doError(getResources().getString(R.string.error_ar), getResources().getString(R.string.item_not_found_ar));
                                return;
                            } else {
                                doError(getResources().getString(R.string.error), getResources().getString(R.string.item_not_found));
                                return;
                            }
                        }
                    } else if(inventoryIndex == AppProviders.EEMC) {

                        if(response.toString().trim().isEmpty()) {
                            if (isArabic()) {
                                doError(getResources().getString(R.string.error_ar), getResources().getString(R.string.item_not_found_ar));
                                return;
                            } else {
                                doError(getResources().getString(R.string.error), getResources().getString(R.string.item_not_found));
                                return;
                            }
                        }
                        else {
                            product.id = response.getString("recId").trim();
                            product.name = response.getString("descA").trim();
                            product.barcode = response.getString("barcode").trim();
                            price = response.getDouble("salePrice");

                            product.englishName = response.getString("descE").trim();
                            product.arabicName = response.getString("descA").trim();
                            product.storeNumber = response.getString("storeNo").trim();
                        }

                    } else if(inventoryIndex == AppProviders.INTEGRATION) {

                        if(response.toString().trim().isEmpty()) {
                            if (isArabic()) {
                                doError(getResources().getString(R.string.error_ar), getResources().getString(R.string.item_not_found_ar));
                                return;
                            } else {
                                doError(getResources().getString(R.string.error), getResources().getString(R.string.item_not_found));
                                return;
                            }
                        }
                        else {
                            product.id = response.getString("itemID").trim();
                            product.name = isArabic() ? response.getString("arabicName").trim() : response.getString("englishName").trim();
                            product.barcode = response.getString("barcode").trim();
                            price = response.getDouble("price");
                            isPrefix = response.getBoolean("isWeightedProduct");

                            product.cost = response.getDouble("cost");
                            product.storeNumber = response.getString("storeNumber").trim();
                            product.iucID = response.getString("iucID").trim();
                            product.packageID = response.getString("packageID").trim();
                            product.supplierNumber = response.getString("supplierNumber").trim();
                            product.englishName = response.getString("englishName").trim();
                            product.arabicName = response.getString("arabicName").trim();

                            if(product.storeNumber.equalsIgnoreCase("null")) product.storeNumber = null;
                            if(product.iucID.equalsIgnoreCase("null")) product.iucID = null;
                            if(product.packageID.equalsIgnoreCase("null")) product.packageID = null;
                            if(product.supplierNumber.equalsIgnoreCase("null")) product.supplierNumber = null;
                            if(product.englishName.equalsIgnoreCase("null")) product.englishName = null;
                            if(product.arabicName.equalsIgnoreCase("null")) product.arabicName = null;
                        }
                    }

                    product.isPrefix = isPrefix;

                    int indexOfProductWithNoWeight = IsProductScanned(product.barcode);
                    //Log.i(TAG, "indexOfProductWithNoWeight" + indexOfProductWithNoWeight);
                    if(indexOfProductWithNoWeight > -1) {
                        if (!isPrefixedProduct(product.originalBarcode)
                                && mProducts.get(indexOfProductWithNoWeight).barcode.equals(product.barcode)) {
                            incrementProduct(product);
                            updateAdapter();
                        }
                    }
                    else if(!mIsNotifying) {
                        Log.i(TAG, "Notifiying = " + mIsNotifying);
                        if(isPrefixedProduct(product.originalBarcode)) {

                            product.isPrefix = true;
                            double priceFromBarcode = getPrefixPrice(product.barcode);
                            if (!isPrefixInDinars) price /= 1000; // this is because prefix price is in fils and all price calculations are in dinars

                            // getting the prefix server weight from its unit price (price per kilo)
                            // the multiplication by 1000 is for converting kilos to grams
                            if(price > 0) {
                                product.server_weight = (int) ((priceFromBarcode / price) * 1000);
                            }

                            product.price = priceFromBarcode;
                            mProducts.add(0, product);
                        }
                        else {
                            product.price = price;

                            if (isProductInCart(product)) {

                                incrementProduct(product);
                            }
                            else {
                                mProducts.add(0, product);
                                //TODO: uncomment for cart
                                //verifyWeight(product.barcode);


                                Log.i(TAG, "Products count = " + mProducts.size());
                            }
                        }

                        updateAdapter();
                    }

                } catch (Exception e) {
                    logError(TAG+" => handleResponse => verifyProduct => "+e.getMessage());
                }
            }
            else if (method == ApiUrl.VERIFY_WEIGHT) {
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        JSONObject data = response.getJSONObject("data");
                        int weight = data.getInt("weight");

                        int productIndex = findProduct(data.getString("barcode").trim());
                        if(productIndex >= 0 && productIndex < mProducts.size()) mProducts.get(productIndex).server_weight = weight;
                    }
                } catch (Exception e) {
                    logError(TAG+" => handleResponse => verifyWeight => "+e.getMessage());
                }
            }
            else if (method == ApiUrl.UPDATE_WEIGHT) {
                mProcessingWeightAddition = false;
            }
            else if(method == ApiUrl.ADD_ORDER) {
                try {
                    int code = response.getInt("code");
                    String msg = response.getString("msg");

                    if (code == 0) {
                        JSONObject data = response.getJSONObject("data");

                        mCurrentOrder.orderID = data.getString("id").trim();

                        mHandler.removeCallbacks(mRunnable);
                        //TODO: turn scanner on
                        //switchHardwareOff();

                        mPreferences.edit().putInt(SharedPreferencesKey.TOTAL_CHECKOUT_WEIGHT.name(), mStableWeight).apply();

                        Intent intent = new Intent(ShoppingActivity.this, PaymentSelectionActivity.class);
                        intent.putExtra(ActivityExtraKey.ORDER.name(), mCurrentOrder);
                        startActivity(intent);
                    }
                    else {
                        mCurrentOrder = null;
                        doError(getString(isArabic() ? R.string.error_ar : R.string.error), msg);
                    }
                } catch (Exception e) {
                    logError(TAG+" => handleResponse => addOrder => "+e.getMessage());
                    mCurrentOrder = null;
                }
                finally {
                    mPayNowBtn.setEnabled(true);
                    mIsAddingOrder = false;
                }
            }
        });
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        runOnUiThread(() -> {
            if(method == ApiUrl.VERIFY_PRODUCT) {

                int statusCode = (error != null && error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                if(statusCode == 404) {
                    doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                            getString(isArabic() ? R.string.item_not_found_ar : R.string.item_not_found));
                }
                else {
                    doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                            getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
                }
            }
            if (method == ApiUrl.UPDATE_WEIGHT) {
                mProcessingWeightAddition = false;
            }
            else if (method == ApiUrl.ADD_ORDER) {
                mCurrentOrder = null;
                mPayNowBtn.setEnabled(true);
                mIsAddingOrder = false;
                doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                        getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
            }
        });
    }
    // endregion


    public String getMacAddress(){
        try{
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());

            String stringMac = "";

            for(NetworkInterface networkInterface : networkInterfaceList)
            {
                if(networkInterface.getName().equalsIgnoreCase("wlon0"));
                {
                    for(int i = 0 ;i <networkInterface.getHardwareAddress().length; i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i]& 0xFF);

                        if(stringMacByte.length() == 1)
                        {
                            stringMacByte = "0" +stringMacByte;
                        }

                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }

            }
            return stringMac;
        }catch (SocketException e)
        {
            e.printStackTrace();
        }

        return  "0";
    }

    public void init(String id) {

        try{
            URL urlObj = new URL(API+"/INIT.php?Tid="+id);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                    getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
        }
    }

    public void close(String id) {

        try{
            URL urlObj = new URL(API+"/CLOSE.php?Tid="+id);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                    getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
        }
    }


    public void transaction(String id, String ttid, String amount) {

        try{
            URL urlObj = new URL(API+"/Transaction.php?Tid="+id+"&TTid="+ttid+"&Amount="+amount);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                    getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
        }
    }
    public void event(String id, String data, String event) {

        try{
            URL urlObj = new URL(API+"/Event.php?Data="+data+"&Tid="+id+"&Event="+event);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                    getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
        }
    }

    public boolean initResponse() {
        String response = "";
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            URL urlObj = new URL(API+"/RESPONSEINIT.php");

            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);


            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                    getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
        } finally {
            try {
                reader.close();
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception ex) {
                doError(getString(isArabic() ? R.string.error_ar : R.string.error),
                        getString(isArabic() ? R.string.error_request_failed_ar : R.string.error_request_failed));
            }
        }
        return (response=="approved");
    }
}