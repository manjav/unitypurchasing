package com.unity.purchasing.custom.util;

import android.annotation.TargetApi;
import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHelper {
    @TargetApi(Build.VERSION_CODES.N)
    public static void sendPost(String address, String data, IHttpCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.write(data.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject json = new JSONObject();
                json.put("responseCode", String.valueOf(conn.getResponseCode()));
                json.put("responseMessage", conn.getResponseMessage());
                json.put("data", new JSONObject(response.toString()));
                callback.callbackCall(json);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}
