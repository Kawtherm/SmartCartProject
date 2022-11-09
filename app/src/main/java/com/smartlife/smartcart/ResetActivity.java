package com.smartlife.smartcart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartlife.smartcart.enums.SharedPreferencesKey;

import java.util.HashMap;

/**this activity displays a webview that gives the user the ability to reset orders*/
public class ResetActivity extends BaseActivity {

    private WebView mWebView;
    private ProgressBar mProgressBar;

    boolean mLoginShown = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        Button backBtn = findViewById(R.id.orders_back_btn);
        backBtn.setOnClickListener(v -> finish());

        Button printBtn = findViewById(R.id.print_btn);
        printBtn.setOnClickListener(v -> {
            createWebPrintJob(mWebView);
        });

        mProgressBar = findViewById(R.id.progress_bar);

        mWebView = findViewById(R.id.orders_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // this is to allow redirects from http to https
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                HashMap<String, String> noCacheHeaders = new HashMap<>();
                noCacheHeaders.put("Pragma", "no-cache");
                noCacheHeaders.put("Cache-Control", "no-cache");

                mWebView.loadUrl(url, noCacheHeaders);

                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                handler.proceed(); // this is to accept all ssl certificates even the failed ones
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.clearCache(true);

                mProgressBar.setVisibility(View.GONE);

                if(url.toLowerCase().contains("account/login")) mLoginShown = true;

                // todo uncomment this when you know why the Print Spooler is stopping unexpectedly
//                if(mLoginShown && url.toLowerCase().contains("content/reset")) printBtn.setVisibility(View.VISIBLE);
//                else printBtn.setVisibility(View.GONE);
            }
        });

        String companyId = mPreferences.getString(SharedPreferencesKey.COMPANY_ID.name(), "");
        String cartNum = mPreferences.getString(SharedPreferencesKey.CART_NUMBER.name(), "");

        clearWebView();

        String url = mPreferences.getString(SharedPreferencesKey.SMART_LIFE_CMS_URL.name(), "")+"/Content/Reset?CompanyId="+companyId+"&cartNumber="+cartNum;
        mWebView.loadUrl(url);
    }

    private void clearWebView() {

        WebStorage.getInstance().deleteAllData();

        // Clear all the cookies
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        mWebView.clearCache(true);
        mWebView.clearFormData();
        mWebView.clearHistory();
        mWebView.clearSslPreferences();
    }

    private void createWebPrintJob(WebView webView) {

        try {

            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = getString(R.string.app_name) + "Reset Document";
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        }
        catch (Exception e) { }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearWebView();
    }
}