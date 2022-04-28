package com.unity.purchasing.custom.util;

import org.json.JSONException;
import org.json.JSONObject;

public interface IHttpCallback {
    void callbackCall(JSONObject result) throws JSONException;
}
