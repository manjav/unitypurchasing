package com.manjav.trivialdrivesample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.custom.PurchasingBridge;

import java.lang.reflect.Array;

public class MainActivity extends Activity {

    private PurchasingBridge purchasing;
    private String transactionID = null;
    private String[] testItems = {
            "{\"id\":\"gas\",\"storeSpecificId\":\"gas\",\"type\":\"Consumable\",\"enabled\":true,\"payouts\":[], \"zarinpalConfig\":{\"price\":50000, \"merchantId\":\"bc671e64-f8c8-11e6-b953-000c295eb8fc\", \"description\":\"بنزین مصرفی\"}}",
            "{\"id\":\"premium\",\"storeSpecificId\":\"premium\",\"type\":\"NonConsumable\",\"enabled\":true,\"payouts\":[]}",
            "{\"id\":\"infinite_gas\",\"storeSpecificId\":\"infinite_gas\",\"type\":\"Subscription\",\"enabled\":true,\"payouts\":[]}"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PurchasingBridge.testActivity = this;
    }

    public void init(View view) {
        purchasing = PurchasingBridge.instance(new IUnityCallback() {

            @Override
            public void OnSetupFailed(String message) {
                PurchasingBridge.log("OnSetupFailed: " + message);
            }

            @Override
            public void OnProductsRetrieved(String message) {
                PurchasingBridge.log("OnProductsRetrieved: " + message);
            }

            @Override
            public void OnPurchaseSucceeded(String sku, String developerPayload, String transactionId) {
                PurchasingBridge.log("OnPurchaseSucceeded: " + sku + ", " + transactionID + ", " + developerPayload);
                transactionID = transactionId;
            }

            @Override
            public void OnPurchaseFailed(String message) {
                PurchasingBridge.log("OnPurchaseFailed: " + message);
            }
        },"com.farsitel.bazaar", "ir.cafebazaar.pardakht.InAppBillingService.BIND");
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void retrieveProducts(View view) {
        purchasing.RetrieveProducts("[" + String.join(", ", testItems) + "]");
    }

    public void purchase(View view) {
        purchasing.Purchase(testItems[0], "payload");
    }

    public void finishTransaction(View view) {
        purchasing.FinishTransaction(testItems[0], transactionID);
    }
}