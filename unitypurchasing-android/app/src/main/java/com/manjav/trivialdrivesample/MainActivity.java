package com.manjav.trivialdrivesample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.custom.PurchasingBridge;

public class MainActivity extends Activity {

    private PurchasingBridge purchasing;
    private String transactionID = null;

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
                Log.i(PurchasingBridge.TAG, "OnSetupFailed: " + message);
            }

            @Override
            public void OnProductsRetrieved(String message) {
                Log.i(PurchasingBridge.TAG, "OnProductsRetrieved: " + message);
            }

            @Override
            public void OnPurchaseSucceeded(String sku, String developerPayload, String transactionId) {
                Log.i(PurchasingBridge.TAG, "OnPurchaseSucceeded: " + sku + ", " + transactionID + ", " + developerPayload);
                transactionID = transactionId;
            }

            @Override
            public void OnPurchaseFailed(String message) {
                Log.i(PurchasingBridge.TAG, "OnPurchaseFailed: " + message);
            }
        },"ir.mservices.market", "ir.mservices.market.InAppBillingService.BIND");
    }

    public void retrieveProducts(View view) {
        purchasing.RetrieveProducts("[{\"id\":\"gas\",\"storeSpecificId\":\"gas\",\"type\":\"Consumable\",\"enabled\":true,\"payouts\":[]},{\"id\":\"premium\",\"storeSpecificId\":\"premium\",\"type\":\"NonConsumable\",\"enabled\":true,\"payouts\":[]},{\"id\":\"infinite_gas\",\"storeSpecificId\":\"infinite_gas\",\"type\":\"Subscription\",\"enabled\":true,\"payouts\":[]}]");
    }

    public void purchase(View view) {
        purchasing.Purchase("{\"id\":\"gas\",\"storeSpecificId\":\"gas\",\"type\":\"Consumable\",\"enabled\":true,\"payouts\":[]}", "payload");
    }

    public void finishTransaction(View view) {
        purchasing.FinishTransaction("{\"id\":\"gas\",\"storeSpecificId\":\"gas\",\"type\":\"Consumable\",\"enabled\":true,\"payouts\":[]}", transactionID);
    }
}