package com.unity.purchasing.custom.util;

import com.unity.purchasing.common.ProductDefinition;
import com.unity.purchasing.common.ProductType;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomProductDefination {
    public ProductDefinition base;
    public int initialPrice;
    public String initialStoreId;
    public String description;

    public CustomProductDefination(JSONObject json) {
        try {
            base = new ProductDefinition(json.getString("storeSpecificId"),
                    ProductType.valueOf(json.getString("type")));

            if (json.has("zarinpalConfig")) {
                JSONObject zarinpal = json.getJSONObject("zarinpalConfig");
                initialPrice = zarinpal.getInt("price");
                initialStoreId = zarinpal.getString("merchantId");
                description = zarinpal.getString("description");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
