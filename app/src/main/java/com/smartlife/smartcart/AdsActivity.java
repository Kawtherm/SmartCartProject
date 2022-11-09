package com.smartlife.smartcart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.smartlife.smartcart.enums.SharedPreferencesKey;

/**This activity is for displaying add via a webview. It will move to next page by clicking on the view.*/
public class AdsActivity extends BaseActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_ads);

        WebView mAdPage = findViewById(R.id.pagetow_ad);
        mAdPage.getSettings().setJavaScriptEnabled(true);
        mAdPage.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mAdPage.getSettings().setSupportZoom(false);
        mAdPage.getSettings().setLoadWithOverviewMode(true);
        mAdPage.getSettings().setUseWideViewPort(true);
        mAdPage.getSettings().setDomStorageEnabled(true);
        mAdPage.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mAdPage.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // this is to allow redirects from http to https

        mAdPage.setWebViewClient(new WebViewClient() {

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

        String url = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_ADS_URL.name(), "");

        mAdPage.loadUrl("http://smartlifekwt2.engine.adglare.net/?353521442&iframe");

        mPreferences.edit().putInt(SharedPreferencesKey.TOTAL_CHECKOUT_WEIGHT.name(), 0).apply();
        mPreferences.edit().putBoolean(SharedPreferencesKey.CANNOT_CHECKOUT.name(), false).apply();
        lightOff("led1");

        Button nextBtn = findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(view -> {
            //TODO: uncomment
            Intent pageThreeIntent = new Intent(AdsActivity.this, LanguageSelectionActivity.class);
            startActivity(pageThreeIntent);
            finish();
        });
    }
}