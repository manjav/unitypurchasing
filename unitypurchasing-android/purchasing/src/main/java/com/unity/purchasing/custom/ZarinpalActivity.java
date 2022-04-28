package com.unity.purchasing.custom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.unity.purchasing.common.IStoreCallback;
import com.unity.purchasing.common.ProductType;
import com.unity.purchasing.common.PurchaseFailureDescription;
import com.unity.purchasing.common.PurchaseFailureReason;
import com.unity.purchasing.custom.util.CustomProductDefination;
import com.unity.purchasing.custom.util.HttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class ZarinpalActivity extends Activity {
    private static final String REQUEST_URI = "https://api.zarinpal.com/pg/v4/payment/request.json";
    private static final String PAYMENT_URL = "https://www.zarinpal.com/pg/StartPay/";
    private static final String VERIFICATION_URL = "https://api.zarinpal.com/pg/v4/payment/verify.json";

    private String callbackURL;
    private int amount;
    private String sku;
    private String merchantId;
    private String description;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zarinpal);

        Bundle extras = getIntent().getExtras();
        sku = extras.getString("sku");
        amount = extras.getInt("amount");
        merchantId = extras.getString("merchantId");
        callbackURL = extras.getString("callbackURL");
        description = extras.getString("description");

        requestPayment();
    }

    private void requestPayment() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("merchant_id", merchantId);
            jsonParam.put("callback_url", callbackURL);
            jsonParam.put("description", description);
            jsonParam.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpHelper.sendPost(REQUEST_URI, jsonParam.toString(), result -> {
            runOnUiThread(() -> {
                try {
                    if (result.getString("responseCode").equals("200")) {
                        JSONObject data = result.getJSONObject("data").getJSONObject("data");
                        if (data.getInt("code") == 100) {
                            String authority = data.getString("authority");
                            purchase(authority);
                        } else {
                            PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(sku, -1, "Payment Gateway not found."));
                            finish();
                        }
                    } else {
                        PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(sku, -1, "Network Problem!"));
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }


    private void purchase(String authority) {

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("purchasing", "shouldOverrideUrlLoading " + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Log.i("purchasing", "onPageFinished " + url);
                if (url.contains(callbackURL)) {
                    Uri uri = Uri.parse("http://www.chalklit.in/post.html?chapter=V-Maths-Addition%20&%20Subtraction&post=394");
                    String status = uri.getQueryParameter("Status");
                    if (status != null && status.equals("OK")) {
                        Log.d("purchasing", "Status OK trying to verify purchase...");
                        String authority = uri.getQueryParameter("Authority");
                        PurchasingBridge.unityCallback.OnPurchaseSucceeded(sku, authority, authority);
                        Log.d("purchasing", "Ignore verifying purchase cause autoVerify is set to false : ");
                        finish();
                    } else {
                        PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(sku, -1, "Purchase failed!"));
                        Log.d("purchasing", "purchase failed : " + status);
                        finish();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Error:" + description, Toast.LENGTH_SHORT).show();
            }
        });
        webView.loadUrl(PAYMENT_URL + authority);
    }


    public static void verify(Activity activity, CustomProductDefination product, String transactionID) {

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("authority", transactionID);
            jsonParam.put("amount", product.initialPrice);
            jsonParam.put("merchant_id", product.initialStoreId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpHelper.sendPost(VERIFICATION_URL, jsonParam.toString(), result -> {
            activity.runOnUiThread(() -> {
                try {
                    if (result.getString("responseCode").equals("200")) {
                        JSONObject data = result.getJSONObject("data").getJSONObject("data");
                        if (data.getInt("code") == 100) {

                        } else {
//                            PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(product.base.id, -1, "Purchase failed!"));
                        }
                    } else {
//                        PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(product.base.id, -1, "Network Problem!"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
