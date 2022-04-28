package com.unity.purchasing.custom.util;

import android.util.Log;

public class IABLogger {

    public boolean mDebugLog = true;
    public String mDebugTag = "Purchasing";

    public void logDebug(String msg) {
        if (mDebugLog) {
            Log.d(mDebugTag, msg);
        }
    }

    public void logError(String msg) {
        Log.e(mDebugTag, "In-app billing error: " + msg);
    }

    public void logWarn(String msg) {
        Log.w(mDebugTag, "In-app billing warning: " + msg);
    }
}
