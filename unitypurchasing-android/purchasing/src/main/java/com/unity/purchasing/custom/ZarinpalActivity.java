package com.unity.purchasing.custom;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.unity.purchasing.custom.util.CustomProductDefination;
import com.unity.purchasing.custom.util.HttpHelper;
import com.unity.purchasing.custom.util.Prefs;

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
    private WebView webView;
    private View registerView;
    private EditText inputText;
    private ProgressBar progressBar;
    private boolean paymentProceed = false;

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
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        registerView = findViewById(R.id.registerView);
        inputText = findViewById(R.id.inputText);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        startProcess();
    }

    private void startProcess() {
        if (paymentProceed) {
            return;
        }
        paymentProceed = true;

        TextView titleText = findViewById(R.id.titleText);
        titleText.setText(description);

        Prefs.setInstance(this);
        String username = Prefs.getInstance().getString(Prefs.KEY_USERNAME, null);
        if (username != null && !username.isEmpty()) {
            inputText.setText(username);
        }
    }

    public void registerContact(View view) {

        if (!inputText.getText().equals("")) {
            Prefs.getInstance().setString(Prefs.KEY_USERNAME, inputText.getText().toString());
        }
        hideKeyboard();
        registerView.setVisibility(View.GONE);
        requestPayment();
    }

    private void requestPayment() {
        String username = Prefs.getInstance().getString(Prefs.KEY_USERNAME, null);

        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("merchant_id", merchantId);
            jsonParam.put("callback_url", callbackURL);
            jsonParam.put("description", description);
            jsonParam.put("amount", amount);
            if (username != null) {
                if (username.contains("@")) {
                    jsonParam.put("email", username);
                } else {
                    jsonParam.put("mobile", username);
                }
            }
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
                            close();
                        }
                    } else {
                        PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(sku, -1, "Network Problem!"));
                        close();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void purchase(String authority) {

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
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("shaparak")) {
                    webView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }

                PurchasingBridge.log("onPageFinished " + url);
                if (url.contains(callbackURL)) {
                    Uri uri = Uri.parse(url);
                    String status = uri.getQueryParameter("Status");
                    if (status != null && status.equals("OK")) {
                        PurchasingBridge.log("Status OK trying to verify purchase...");
                        String authority = uri.getQueryParameter("Authority");
                        PurchasingBridge.unityCallback.OnPurchaseSucceeded(sku, authority, authority);
                        PurchasingBridge.log("Ignore verifying purchase cause autoVerify is set to false : ");
                        close();
                    } else {
                        PurchasingBridge.unityCallback.OnPurchaseFailed(PurchasingBridge.getProperDescription(sku, -1, "Purchase failed!"));
                        PurchasingBridge.log("purchase failed : " + status);
                        close();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Toast.makeText(getApplicationContext(), "Error:" + description, Toast.LENGTH_SHORT).show();
            }
        });
        webView.loadUrl(PAYMENT_URL + authority);
    }

    private void close() {
        paymentProceed = false;
        webView.setVisibility(View.INVISIBLE);
        finish();
    }

    public static void verify(Activity activity, CustomProductDefination product, String transactionID) {
        PurchasingBridge.log("verify " + product.initialStoreId + " " + transactionID);
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

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (registerView.getVisibility() == View.VISIBLE) {
            registerContact(null);
            return;
        }
        super.onBackPressed();
    }
}
