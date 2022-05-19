package com.unity.purchasing.custom;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.unity.purchasing.common.IStoreCallback;
import com.unity.purchasing.common.IUnityCallback;
import com.unity.purchasing.common.InitializationFailureReason;
import com.unity.purchasing.common.ProductDescription;
import com.unity.purchasing.common.ProductMetadata;
import com.unity.purchasing.common.ProductType;
import com.unity.purchasing.common.PurchaseFailureDescription;
import com.unity.purchasing.common.PurchaseFailureReason;
import com.unity.purchasing.common.UnityPurchasing;
import com.unity.purchasing.custom.util.CustomProductDefination;
import com.unity.purchasing.custom.util.IabResult;
import com.unity.purchasing.custom.util.Inventory;
import com.unity.purchasing.custom.util.Purchase;
import com.unity.purchasing.custom.util.SkuDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasingBridge {

    public static String TAG = "Purchasing";
    public static String ZARINPAL = "zarinpal";

    public static IStoreCallback unityCallback;
    public static Activity testActivity;
    private static PurchasingBridge instance;

    private IabHelper helper;
    private String pendingJsonProducts = null;
    private final String storePackageName;
    private HashMap<String, Purchase> purchases;
    private Map<String, CustomProductDefination> definedProducts;
    private String lastPurchasingSKU;

    public static void log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static PurchasingBridge instance(IUnityCallback bridge, String storePackageName, String bindURL) {
        if (instance == null) {
            instance = new PurchasingBridge(new UnityPurchasing(bridge), storePackageName, bindURL);
        }
        return instance;
    }

    public Activity getActivity() {
        if (testActivity != null) {
            return testActivity;
        }
        try {
            // Using reflection to remove reference to Unity library.
            Class<?> mUnityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            Field mUnityPlayerActivityField = mUnityPlayerClass.getField("currentActivity");
            return (Activity) mUnityPlayerActivityField.get(mUnityPlayerClass);
        } catch (ClassNotFoundException e) {
            log("Could not find UnityPlayer class: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            log("Could not find currentActivity field: " + e.getMessage());
        } catch (Exception e) {
            log("Unknown exception occurred finding getActivity: " + e.getMessage());
        }
        return null;
    }

    public PurchasingBridge(IStoreCallback callback, String packageName, String bindURL) {
        unityCallback = callback;
        storePackageName = packageName;
        log("Setup start for " + storePackageName + " " + bindURL);
        if (storePackageName.equals(ZARINPAL)) {
            return;
        }

        helper = new IabHelper(getActivity(), storePackageName, bindURL);
        helper.enableDebugLogging(true);
        helper.startSetup(result -> {

            if (result.isFailure()) {
                // Oh noes, there was a problem.
                log("Problem setting up in-app billing: " + result);
                unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable);
                return;
            }
            log("Setup Succeed.");

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            if (pendingJsonProducts != null)
                RetrieveProducts(pendingJsonProducts);
        });
    }

    public void RetrieveProducts(String json) {
        log("RetrieveProducts " + json);
        pendingJsonProducts = json;

        if (!isConnected()) {
            return;
        }
        if (definedProducts != null) {
            fillProductDescription(null);
            return;
        }

        // Create defined products
        List<String> skusList = new ArrayList<>();
        definedProducts = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject value = jsonArray.getJSONObject(i);
                CustomProductDefination product = new CustomProductDefination(value);
                definedProducts.put(product.base.id, product);
                skusList.add(product.base.id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            unityCallback.OnSetupFailed(InitializationFailureReason.NoProductsAvailable);
        }
        pendingJsonProducts = null;

        if (storePackageName.equals(ZARINPAL)) {
            fillProductDescription(null);
            return;
        }

        // Query SkuDetails
        helper.queryInventoryAsync(true, skusList, mGotInventoryListener);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            log("Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (!isConnected()) {
                unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable);
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                log("Failed to query inventory: " + result);
                unityCallback.OnSetupFailed(InitializationFailureReason.PurchasingUnavailable);
                return;
            }

            log("Query inventory was successful.");
            fillProductDescription(inv);
        }
    };

    private void fillProductDescription(Inventory inv) {
        purchases = new HashMap<>();

        List<ProductDescription> productDescriptions = new ArrayList<>();
        for (CustomProductDefination product : definedProducts.values()) {
            String sku = product.base.id;
            SkuDetails skuDetails = null;
            if (inv != null) {
                skuDetails = inv.getSkuDetails(sku);
            }

            String price = skuDetails != null ? uniformPrices(skuDetails.getPrice()) : String.valueOf(product.initialPrice);
            ProductMetadata metadata = new ProductMetadata(
                    price,
                    skuDetails != null ? skuDetails.getTitle() : "",
                    skuDetails != null ? skuDetails.getDescription() : product.description,
                    "IRR",
                    skuDetails != null ? new BigDecimal(parsePrice(price)) : BigDecimal.valueOf(product.initialPrice)
            );
            String receipt = "";
            String transactionId = "";

            // Bind purchase and Consume consumable one
            if (inv != null) {
                Purchase purchase = inv.getPurchase(sku);
                if (purchase != null) {
                    purchases.put(sku, purchase);
                    receipt = purchase.getToken();
                    transactionId = purchase.getOrderId();
                    if (definedProducts.get(sku).base.type.equals(ProductType.Consumable)) {
                        unityCallback.OnPurchaseSucceeded(sku, receipt, transactionId);
                    }
                }
            }
            productDescriptions.add(new ProductDescription(sku, metadata, receipt, transactionId));
        }

        unityCallback.OnProductsRetrieved(productDescriptions);
    }

    public void Purchase(String productJSON, String developerPayload) {
        log("Purchase " + productJSON);

        lastPurchasingSKU = getSKUFromJson(productJSON);
        CustomProductDefination product = definedProducts.get(lastPurchasingSKU);
        if (product == null) {
            PurchaseFailureDescription description = new PurchaseFailureDescription(lastPurchasingSKU, PurchaseFailureReason.BillingUnavailable, "Json is invalid.", "");
            PurchasingBridge.unityCallback.OnPurchaseFailed(description);
            return;
        }

        if (storePackageName.equals(ZARINPAL)) {
            Intent i = new Intent(getActivity(), ZarinpalActivity.class);
            i.putExtra("sku", product.base.id);
            i.putExtra("amount", product.initialPrice);
            i.putExtra("merchantId", product.initialStoreId);
            i.putExtra("description", product.description);
            i.putExtra("callbackURL", "zarin://" + lastPurchasingSKU);
            getActivity().startActivity(i);
            return;
        }

        if (!isConnected()) {
            PurchaseFailureDescription description = new PurchaseFailureDescription(lastPurchasingSKU, PurchaseFailureReason.BillingUnavailable, "Helper not Found!", "");
            PurchasingBridge.unityCallback.OnPurchaseFailed(description);
            return;
        }

        helper.launchPurchaseFlow(getActivity(), product.base.id, mPurchaseFinishedListener, developerPayload);
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            log("Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (!isConnected()) {
                PurchaseFailureDescription description = new PurchaseFailureDescription(purchase.getSku(), PurchaseFailureReason.BillingUnavailable, "Helper not Found!", "");
                PurchasingBridge.unityCallback.OnPurchaseFailed(description);
                return;
            }

            if (result.isFailure()) {
                PurchaseFailureDescription description = getProperDescription(lastPurchasingSKU, result.getResponse(), result.getMessage());
                PurchasingBridge.unityCallback.OnPurchaseFailed(description);
                return;
            }

            log("Purchase successful. " + purchase.getToken());
            purchases.put(purchase.getSku(), purchase);
//            if (!Security.verifyPurchase(purchase.getOriginalJson(), purchase.getSignature())) {
//                Log.e(TAG, "Invalid signature on purchase. Check to make " +
//                        "sure your public key is correct.");
//                continue;
//            }
            unityCallback.OnPurchaseSucceeded(purchase.getSku(), purchase.getToken(), purchase.getOrderId());
        }
    };

    public void FinishTransaction(String productJSON, String transactionID) {
        log("Finishing transaction " + productJSON + " - " + transactionID);
        CustomProductDefination product = definedProducts.get(getSKUFromJson(productJSON));

        if (storePackageName.equals(ZARINPAL)) {
            if (product.autoVerification) {
                ZarinpalActivity.verify(getActivity(), product, transactionID);
            }
            return;
        }

        if (product == null || !product.base.type.equals(ProductType.Consumable)) {
            return;
        }

        if (isConnected()) {
            Purchase purchase = purchases.get(product.base.id);
            helper.consumeAsync(purchase, mConsumeFinishedListener);
        }
    }

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            log("Consumption finished. Purchase: " + purchase + ", result: " + result);

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                log("Consumption successful. Provisioning.");
            } else {
                log("Error while consuming: " + result);
            }
            log("End consumption flow.");
        }
    };


    public static PurchaseFailureDescription getProperDescription(String sku, int response, String message) {
        PurchaseFailureReason reason = PurchaseFailureReason.Unknown;
        if (response == IabHelper.IABHELPER_BAD_RESPONSE) {
            reason = PurchaseFailureReason.UserCancelled;
        }
        return new PurchaseFailureDescription(sku, reason, message, "Needs to detect!");
    }

    private String uniformPrices(String price) {
        if (price.contains("تومان")) {
            price = price.replace(" تومان", "۰ ریال");
        }
        return price;
    }

    private String parsePrice(String price) {
        if (price.contains("صفر") || price.toLowerCase().contains("zero")) {
            return "0";
        }

        String[] pre = price.split(" ");
        String _price = pre[0]
                .replace(",", "")
                .replace('٠', '0')
                .replace('١', '1')
                .replace('٢', '2')
                .replace('٣', '3')
                .replace('۴', '4')
                .replace('۵', '5')
                .replace('۶', '6')
                .replace('٧', '7')
                .replace('٨', '8')
                .replace('٩', '9');

        return _price;
    }

    public boolean isConnected() {
        if (storePackageName.equals(ZARINPAL)) return true;
        if (helper == null) return false;
        if (helper.mDisposed) return false;
        if (helper.iabConnection == null || !helper.iabConnection.mSetupDone) return false;
        return true;
    }

    private String getSKUFromJson(String productJSON) {
        try {
            JSONObject json = new JSONObject(productJSON);
            return json.getString("storeSpecificId");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
