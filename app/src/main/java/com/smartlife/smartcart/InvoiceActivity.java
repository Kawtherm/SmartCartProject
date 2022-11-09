package com.smartlife.smartcart;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.print.PrintHelper;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.PrefUtils;
import com.printer.sdk.utils.Utils;
import com.smartlife.smartcart.classes.AppProviders;
import com.smartlife.smartcart.classes.AppConsts;
import com.smartlife.smartcart.classes.SDKConstants;
import com.smartlife.smartcart.classes.TimeUtil;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.enums.SharedPreferencesKey;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;
import com.smartlife.smartcart.model.Order;
import com.smartlife.smartcart.enums.ActivityExtraKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

import java.util.HashMap;

/**this activity is the final screen in the app. it shows a summary for the bill and its barcode.
 * this activity is responsible for sending bill's details to the coop's servers (EEMC).
 * when the user removes his items from the cart and the weight becomes less than 4,
 * the selling operation is complete and cart will go to ads page ready for next customer*/
public class InvoiceActivity extends BaseActivity implements IScaleReadCallback {

    private final String TAG = "PageEight";

    private ImageView mQRCodeImg;
    private Button mBackHomeBtn;
    private Context mContext;
    private Button mPrintInvoiceBtn;
    private String mInvoiceUrl = "";

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.smartlife.smartcart";
    private static PrinterInstance mPrinter;

    private  final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        //收到消息
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(device != null){
                    //call method to set up device communication
                    mPrinter.openConnection();
                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        mContext = this;

        Order order = (Order)getIntent().getSerializableExtra(ActivityExtraKey.ORDER.name());

        if (order != null) {
            TextView invoiceNumber = findViewById(R.id.invoice_nb_lbl);
            invoiceNumber.setText(String.format("%5s", order.invoiceNo).replace(' ', '0'));

            int inventoryIndex = mPreferences.getInt(SharedPreferencesKey.INVENTORY_INDEX.name(), 0);
            boolean isTwoLights = mPreferences.getBoolean(SharedPreferencesKey.IS_TWO_LIGHTS.name(), false);

            if(isTwoLights) switchBothLights(false);

            if (inventoryIndex == AppProviders.SMART_LIFE) {

                JSONObject parameters = new JSONObject();
                try {
                    parameters.put("company_id", order.companyId);
                    parameters.put("store_number", order.storeNo);
                    parameters.put("shift_number", order.shiftNo);
                    parameters.put("cart_number", order.cartNo);
                    parameters.put("invoice_number", order.invoiceNo);
                    parameters.put("invoice_detail", new JSONArray(order.invoiceDetail));
                    parameters.put("total_amount", order.totalAmount);

                    if(order.customerNumber != null && !order.customerNumber.isEmpty()) {
                        parameters.put("user_id", order.customerNumber);
                        parameters.put("current_balance", order.customerBalance);
                        parameters.put("new_balance", (order.totalAmount + order.customerBalance));
                    }
                }
                catch (Exception e) { }

                makeHttpPostRequest(ApiUrl.PLACE_ORDER, parameters, null);
            }
            else if (inventoryIndex == AppProviders.EEMC) {

                JSONObject parameters = new JSONObject();

                try {
                    // custCardId: new Bilal
                    parameters.put("custCardId", null);
                    parameters.put("invoiceType", 1);
                    parameters.put("fmonth", 1);
                    parameters.put("custCardType", 1);
                    parameters.put("totalItemDiscountAmount", 0);
                    parameters.put("invoiceNo", order.invoiceNo);
                    parameters.put("userRecid", AppConsts.USER_REC_ID);
                    parameters.put("recid", order.recid);
                    parameters.put("storeNo", order.storeNo);
                    parameters.put("shiftNo", order.shiftNo);
                    parameters.put("clearRecid", order.clearRecid);
                    parameters.put("status", AppConsts.ORDER_STATUS);
                    parameters.put("machineRecid", mPreferences.getString(SharedPreferencesKey.CUSTOMER_CART_NUMBER.name(), ""));
                    parameters.put("totalAmount", order.totalAmount);
                    parameters.put("payAmount", order.totalAmount);
                    parameters.put("netAmount", order.totalAmount);
                    parameters.put("totalPayAmount", order.totalAmount);
                    parameters.put("invoiceDate", TimeUtil.getDateEEMC(order.invoiceDate));
                    parameters.put("lastUpdateDate", TimeUtil.getDateEEMC(order.invoiceDate));
                    parameters.put("fy", TimeUtil.getDateEEMC(order.invoiceDate).length() >= 4 ? TimeUtil.getDateEEMC(order.invoiceDate).substring(0, 4) : TimeUtil.getDateEEMC().substring(0, 4));
                    parameters.put("posSaleDetails", new JSONArray(order.getInvoiceDetailsForEEMC()));
                    // balcancePosted: new Bilal
                    parameters.put("balcancePosted", order.customerBalance + order.totalAmount);
                    if(order.customerNumber != null && !order.customerNumber.isEmpty()) {
                        parameters.put("custCode", order.customerNumber);
                        parameters.put("customerRecid", order.customerID);
                        parameters.put("customerBalance", (order.totalAmount + order.customerBalance));
                    }
                } catch (JSONException e) { }

                makeHttpPostRequest(ApiUrl.PLACE_ORDER, parameters, null);
            }
            else if (inventoryIndex == AppProviders.INTEGRATION) {

                JSONObject parameters = new JSONObject();

                try {
                    parameters.put("storeNumber", order.storeNo);
                    parameters.put("cartNumber", mPreferences.getString(SharedPreferencesKey.CUSTOMER_CART_NUMBER.name(), ""));
                    parameters.put("shiftNumber", order.shiftNo);
                    parameters.put("invoiceNumber", order.invoiceNo);
                    parameters.put("invoiceDate", order.invoiceDate);
                    parameters.put("isKnet", order.isKNet);
                    parameters.put("isVisa", order.isVisa);
                    parameters.put("isMasterCard", order.isMasterCard);
                    parameters.put("orderDetails", new JSONArray(order.getInvoiceDetailsForIntegration()));

                    if(order.customerNumber != null && !order.customerNumber.isEmpty()) {
                        parameters.put("customerNumber", order.customerNumber);
                        parameters.put("customerID", order.customerID);
                    }
                } catch (JSONException e) { }

                makeHttpPostRequest(ApiUrl.PLACE_ORDER, parameters, null);
            }

            mQRCodeImg = findViewById(R.id.invoice_qr_code_img);
            String url = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), "")+"/Content/Invoice?orderid="+order.orderID;
            mInvoiceUrl = url;
            createCode(url);
        }

        mBackHomeBtn = findViewById(R.id.bill_back_home);
        if(isArabic()) {
            mBackHomeBtn.setText("الصفحة الرئيسية");
        } else {
            mBackHomeBtn.setText("Home Page");
        }
        mBackHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InvoiceActivity.this, AdsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();



            }
        });

        mPrintInvoiceBtn = findViewById(R.id.print_invoice);
        if(isArabic()) {
            mPrintInvoiceBtn.setText("طباعة الفاتورة");
        } else {
            mPrintInvoiceBtn.setText("Print Invoice");
        }
        mPrintInvoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    int width = convertDpToPixels(1400);
                    int height = convertDpToPixels(1400);
                    BitMatrix bitMatrix = multiFormatWriter.encode(mInvoiceUrl, BarcodeFormat.QR_CODE, width, height);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                    Bitmap bCode = Utils.zoomImage(bitmap, PrinterConstants.paperWidth-140, PrefUtils.getInt(mContext, "", 0));



                    if(mUsbManager.hasPermission(mDevice)) {
                        PrinterInstance.mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
                        mPrinter.printText("Scan Qrcode For Invoice");
                        mPrinter.printImage(bCode, PrinterConstants.PAlign.CENTER, 0, 128);
                        mPrinter.printText("\r\n");
                        mPrinter.printText("\r\n");
                        mPrinter.printText("\r\n");
                        mPrinter.printText("\r\n");
                        mPrinter.printText("\r\n");
                        mPrinter.printText("\r\n");

                        new Thread(new Runnable() {
                            public void run() {

                                mPrinter.cutPaper(65, 50);

                            }
                        }).start();
                    } else {

                    }
                } catch (Exception e) {

                }
            }
        });

        loadImageURL(findViewById(R.id.iv_logo_eight));

        ImageButton ratingBtn01 = findViewById(R.id.rating_btn_01);
        ImageButton ratingBtn02 = findViewById(R.id.rating_btn_02);
        ImageButton ratingBtn03 = findViewById(R.id.rating_btn_03);

        ratingBtn01.setOnClickListener(view -> {
            //todo send the rating to the server
            ImageViewCompat.setImageTintList(ratingBtn01, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.sl_green)));
            ImageViewCompat.setImageTintList(ratingBtn02, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
            ImageViewCompat.setImageTintList(ratingBtn03, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
        });
        ratingBtn02.setOnClickListener(view -> {
            //todo send the rating to the server
            ImageViewCompat.setImageTintList(ratingBtn02, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.sl_orange)));
            ImageViewCompat.setImageTintList(ratingBtn01, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
            ImageViewCompat.setImageTintList(ratingBtn03, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
        });
        ratingBtn03.setOnClickListener(view -> {
            //todo send the rating to the server
            ImageViewCompat.setImageTintList(ratingBtn03, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.sl_red)));
            ImageViewCompat.setImageTintList(ratingBtn02, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
            ImageViewCompat.setImageTintList(ratingBtn01, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), android.R.color.darker_gray)));
        });

        TextView thankU = findViewById(R.id.pageeight_thank_lbl);
        TextView forShopping = findViewById(R.id.pageeight_forshop_lbl);
        TextView rateExp = findViewById(R.id.pageeight_rate_lbl);
        TextView invoiceNo = findViewById(R.id.pageeight_receipt_nb_lbl);
        TextView scanQr = findViewById(R.id.scan_qr_inst_lbl);

        if(isArabic()) {
            thankU.setText(R.string.thank_you_ar);
            forShopping.setText(R.string.for_shopping_ar);
            rateExp.setText(R.string.rate_ar);
            invoiceNo.setText(R.string.invoice_no_ar);
            scanQr.setText(R.string.scan_code_ar);
        }

        //switchToScaleMode(this);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);// 启动服务进程
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        registerReceiver(mUsbReceiver, filter);

        HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
        for (UsbDevice device : devices.values()) {

            if (device.getVendorId() == 1155) {

                mDevice = device;
            }
        }
        mUsbManager.requestPermission(mDevice, mPermissionIntent);
        boolean hasPermission = mUsbManager.hasPermission(mDevice);
        UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);

        mPrinter = PrinterInstance.getPrinterInstance(this, mDevice, null);
        mPrinter.openConnection();
    }

    private void createCode(String message) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            int width = convertDpToPixels(1400);
            int height = convertDpToPixels(1400);
            BitMatrix bitMatrix = multiFormatWriter.encode(message, BarcodeFormat.QR_CODE, width, height);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            mQRCodeImg.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
            logError(TAG+" => control(path, data) exception => "+e.getMessage());
        }
    }

    /*



     */

    @Override
    public void onScaleReadData(String data) {
        /*runOnUiThread(() -> {

            try {
                sleep(SDKConstants.WReadSpan);
            } catch (Exception e) {
                logError(TAG+" => nScaleReadData exception => "+e.getMessage());
            }

            if (data != null && data.startsWith(SDKConstants.WHeadST)) {
                try {
                    if(data.length() > SDKConstants.WHeadLenght && SDKConstants.WHeadLenght < data.indexOf(SDKConstants.WTail)) {

                        String substring = data.substring(SDKConstants.WHeadLenght, data.indexOf(SDKConstants.WTail)).trim();
                        String replace1 = substring.replace(SDKConstants.WBlankSign, "").trim();
                        String replace2 = replace1.replace(SDKConstants.WAddSign, "").trim();
                        int weight = Integer.parseInt(replace2);

                        checkWeightViolation(weight);

                        if(canCheckout()) {
                            lightOn("led2");
                        }
                        else {
                            lightOff("led2");

                            if(weight <= AppConsts.SCALE_ERROR_MARGIN) {
                                switchSerialOff();
                                Intent intent = new Intent(InvoiceActivity.this, AdsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                } catch (Exception e) {
                    logError(TAG+" => onScaleReadData exception => "+e.getMessage());
                }
            }
        });*/
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);
        lightOn("led2");
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);
        lightOn("led2");
    }

    @Override
    protected void onDestroy() {

        lightOff("led2");
        switchSerialOff();

        super.onDestroy();
    }
}