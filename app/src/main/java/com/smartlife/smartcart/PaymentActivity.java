package com.smartlife.smartcart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.smartlife.smartcart.classes.SDKConstants;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;
import com.smartlife.smartcart.model.Order;
import com.smartlife.smartcart.enums.ActivityExtraKey;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**This activity is a web view that shows KNet page.
 * it checks the payment status for the order and behaves accordingly.
 * if paid successfully, goes to page eight*/
public class PaymentActivity extends BaseActivity implements IScaleReadCallback {

    private final String TAG = "PageSeven";

    private ImageView mPaymentQR;

    private Order mOrder;
    final private Timer mTimer = new Timer();
    private boolean mIsTwoLights = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mOrder = (Order)getIntent().getSerializableExtra(ActivityExtraKey.ORDER.name());
        boolean isKNet = getIntent().getBooleanExtra(ActivityExtraKey.IS_KNET.name(), true);

        String language = mPreferences.getString(SharedPreferencesKey.LANGUAGE.name(), "En").toUpperCase();
        String kNetUrl = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), "")+"/Content/Knet?lang="+language+"&orderid="+mOrder.orderID;
        String visaUrl = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), "")+"/Content/Credit?lang="+language+"&orderid="+mOrder.orderID;

        mIsTwoLights = mPreferences.getBoolean(SharedPreferencesKey.IS_TWO_LIGHTS.name(), false);

        String url = isKNet ? kNetUrl : visaUrl;

        mPaymentQR = findViewById(R.id.iv_payment_qr);
        createCode(url);

        Button backBtn = findViewById(R.id.btn_back);
        backBtn.setText(isArabic() ? R.string.btn_cancel_order_ar : R.string.btn_cancel_order);
        backBtn.setOnClickListener(view -> {

            Intent intent = new Intent(PaymentActivity.this, AdsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        TextView lblDescription = findViewById(R.id.tv_lbl_desc);
        lblDescription.setText(isArabic() ? R.string.txt_payment_qr_ar : R.string.txt_payment_qr);

        switchToScaleMode(this);

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkPayment();
            }
        }, 5000, 5000);
    }

    private void createCode(String message) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            int width = convertDpToPixels(1400);
            int height = convertDpToPixels(1400);
            BitMatrix bitMatrix = multiFormatWriter.encode(message, BarcodeFormat.QR_CODE, width, height);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            mPaymentQR.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
            logError(TAG+" => control(path, data) exception => "+e.getMessage());
        }
    }

    private void checkPayment() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("id", mOrder.orderID);

        }
        catch (Exception e) { }
        makeHttpPostRequest(ApiUrl.CHECK_PAYMENT_STATUS, parameters, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimer.purge();
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);

        if(method == ApiUrl.CHECK_PAYMENT_STATUS) {

            runOnUiThread(() -> {
                try {
                    int code = response.getInt("code");
                    JSONObject data = response.getJSONObject("data");

                    if(code == 0) {
                        if(data.getInt("paymentStatusId") == 1) { // not paid
//                            mError.setVisibility(View.VISIBLE);
                        }
                        else { // paid
                            Intent i = new Intent(PaymentActivity.this, InvoiceActivity.class);
                            mOrder.invoiceNo = data.getString("invoiceNumber").trim();
                            mOrder.clearRecid = data.getString("customerRecordId").trim();
                            mOrder.shiftNo = data.getString("shiftNumber").trim();
                            mOrder.isKNet = data.getBoolean("isKnet");
                            mOrder.isVisa = data.getBoolean("isVisa");
                            mOrder.isMasterCard = data.getBoolean("isMasterCard");
                            mOrder.invoiceDate = data.getString("invoiceDate").trim();
                            mOrder.recid = data.getString("customerRecordId2").trim();
                            i.putExtra(ActivityExtraKey.ORDER.name(), mOrder);
                            startActivity(i);
                            finish();
                        }
                    }
                    else {
//                        mError.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    logError(TAG+" => handleResponse => checkPaymentStatus => "+e.getMessage());
                    e.printStackTrace();

//                    mError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        if (method == ApiUrl.CHECK_PAYMENT_STATUS) {
            runOnUiThread(() -> {
//                mError.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void onScaleReadData(String data) {
        runOnUiThread(() -> {

            try {
                sleep(SDKConstants.WReadSpan);
            } catch (Exception e) {
                logError(TAG+" => onScaleReadData => "+e.getMessage());
            }

            if (data != null && data.startsWith(SDKConstants.WHeadST)) {
                try {
                    int weight = 0;
                    if(data.length() > SDKConstants.WHeadLenght && SDKConstants.WHeadLenght < data.indexOf(SDKConstants.WTail)) {
                        String substring = data.substring(SDKConstants.WHeadLenght, data.indexOf(SDKConstants.WTail)).trim();
                        String replace1 = substring.replace(SDKConstants.WBlankSign, "").trim();
                        String replace2 = replace1.replace(SDKConstants.WAddSign, "").trim();
                        weight = Integer.parseInt(replace2);
                    }

                    if(!mIsTwoLights) checkWeightViolation(weight);
                } catch (Exception e) {
                    logError(TAG+" => onScaleReadData => "+e.getMessage());
                }
            }
        });
    }
}