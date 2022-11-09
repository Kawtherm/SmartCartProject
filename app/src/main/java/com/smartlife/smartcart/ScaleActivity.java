package com.smartlife.smartcart;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.android.volley.VolleyError;
import com.smartlife.smartcart.classes.AppConsts;
import com.smartlife.smartcart.classes.Device;
import com.smartlife.smartcart.classes.SDKConstants;
import com.smartlife.smartcart.classes.ScannerSerialPortManager;
import com.smartlife.smartcart.classes.SoundPoolPlayer;
import com.smartlife.smartcart.enums.ApiUrl;
import com.smartlife.smartcart.interfaces.IScaleReadCallback;
import com.smartlife.smartcart.interfaces.IScannerReadCallback;

import org.json.JSONObject;

import static java.lang.Thread.sleep;

/**this activity is responsible for scaling items and sending its barcode and weight to server*/
public class ScaleActivity extends BaseActivity implements IScannerReadCallback, IScaleReadCallback {

    private final String TAG = "ScaleActivity";
    private EditText mBarcodeTxt;
    private TextView mWeightLbl;
    private TextView mErrorLbl;

    // Scanning/Scaling
    private SoundPoolPlayer mPlayer;
    boolean mScanOn = true;
    Handler mHandler = new Handler();
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);

        Button mBackBtn = findViewById(R.id.scale_back_btn);
        mBackBtn.setOnClickListener(view -> finish());

        Button mSaveBtn = findViewById(R.id.scale_save_btn);
        Button mResetBarcodeBtn = findViewById(R.id.barcode_reset_btn);
        Button mRebaseScaleBtn = findViewById(R.id.scale_reset_btn);
        mBarcodeTxt = findViewById(R.id.scale_barcode_nb_txt);
        mWeightLbl = findViewById(R.id.scale_weight_val_lbl);
        mErrorLbl = findViewById(R.id.scale_error_lbl);
        mBarcodeTxt.setShowSoftInputOnFocus(false);

        mSaveBtn.setOnClickListener(view -> {
            String barcode  = mBarcodeTxt.getText().toString().trim();
            String weight  = mWeightLbl.getText().toString().trim();

            if(barcode.isEmpty() || barcode.equals("") || weight.isEmpty() || weight.equals("") || weight.equals("0")) {
                mErrorLbl.setText("Barcode or weight is not correct");
                return;
            }

            JSONObject params = new JSONObject();
            try {
                params.put("barcode", barcode);
                params.put("weight", weight);
            }
            catch (Exception e) { }
            showActivityIndicator(getResources().getString(R.string.saving));
            makeHttpPostRequest(ApiUrl.SCALE, params, null);
        });

        mResetBarcodeBtn.setOnClickListener(view -> {
            mBarcodeTxt.setText("");
            mErrorLbl.setText("");
            mDevice = new Device("/dev/ttySAC2", "9600");
        });

        mRebaseScaleBtn.setOnClickListener(view -> {
            rebaseScale();
        });

        // Pre-load the sound file
        mPlayer = new SoundPoolPlayer(this);
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        // Turn scanner on
        turnScannerHardwareOn();
        switchToScanMode(this);
    }

    @Override
    protected void onResume() {

        mHandler.postDelayed(mRunnable = () -> {
            mHandler.postDelayed(mRunnable, AppConsts.SERIAL_PORT_DELAY);
            if (mScanOn) {
                mScanOn = false;
                ScannerSerialPortManager scannerSerialPortManager = ScannerSerialPortManager.instance();
                scannerSerialPortManager.setScannerReadCallback(this);
                scannerSerialPortManager.open(new Device("/dev/ttySAC3", "19200"));

            } else {
                mScanOn = true;
                switchToScaleMode(this);
            }
        }, AppConsts.SERIAL_PORT_DELAY);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        switchSerialOff();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        switchHardwareOff();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onScannerReadData(String data) {
        runOnUiThread(() -> {
            try {

                String barcode = data != null ? data.replace("\n", "").replace("\r", "").trim() : "";

                if(barcode.length() != 0) {
                    mPlayer.playShortResource(R.raw.scan_success);
                    mBarcodeTxt.setText(barcode);
                    mErrorLbl.setText("");
                }

            } catch (Exception e) {
                logError(TAG+" => onScannerReadData => "+e.getMessage());
                mErrorLbl.setText(e.getMessage());
            }
        });
    }

    @Override
    public void onScaleReadData(String data) {

        runOnUiThread(() -> {

            try {
                sleep(SDKConstants.WReadSpan);
            } catch (Exception e) {
                logError(TAG+" => onScaleReadData => "+e.getMessage());
            }

            try{
                if (data != null && data.startsWith(SDKConstants.WHeadST)) {

                    int weight = 0;
                    try {
                        if(data.length() > SDKConstants.WHeadLenght && SDKConstants.WHeadLenght < data.indexOf(SDKConstants.WTail)) {
                            String substring = data.substring(SDKConstants.WHeadLenght, data.indexOf(SDKConstants.WTail)).trim();
                            String replace1 = substring.replace(SDKConstants.WBlankSign, "").trim();
                            String replace2 = replace1.replace(SDKConstants.WAddSign, "").trim();
                            weight = Integer.parseInt(replace2);
                        }
                    } catch (Exception e) {
                        logError(TAG+" => onScaleReadData => "+e.getMessage());
                    }

                    mWeightLbl.setText("" + weight);
                }
            }
            catch (Exception e) {
                logError(TAG+" => onScaleReadData => "+e.getMessage());
                mErrorLbl.setText(e.getMessage());
            }
        });
    }

    @Override
    protected void handleResponse(JSONObject response, ApiUrl method, String barcode) {
        super.handleResponse(response, method, barcode);

        runOnUiThread(() -> {
            hideActivityIndicator();

            if(method == ApiUrl.SCALE) {

                try {
                    int code = response.getInt("code");
                    if(code == 0) {
                        String message = response.getString("msg");
                        mDevice = new Device("/dev/ttySAC2", "9600");

                        doSuccess(getString(R.string.title_success), message);
                    } else {
                        doError(getString(R.string.error), getString(R.string.error_request_failed));
                    }
                } catch (Exception e) {
                    logError(TAG+" => handleResponse => scale => "+e.getMessage());
                    doError(getString(R.string.error), getString(R.string.error_request_failed));
                    mErrorLbl.setText(e.getMessage());
                }
            }
        });
    }

    @Override
    protected void handleError(VolleyError error, ApiUrl method) {
        super.handleError(error, method);

        if(method == ApiUrl.SCALE){
            runOnUiThread(() -> {
                try {
                    if(error != null) mErrorLbl.setText(error.getMessage());
                }
                catch (Exception e) {
                    logError(TAG+" => handleError => scale => "+e.getMessage());
                }
            });
        }
    }
}