package com.unity.purchasing.custom.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Prefs {
    private SharedPreferences sharedPref;

    static private Prefs instance;
    static public final String KEY_USERNAME = "username";

    static public Prefs getInstance() {
        return instance;
    }

    static public void setInstance(Context context) {
        instance = new Prefs();
        instance.sharedPref = context.getSharedPreferences("purchasing", Context.MODE_PRIVATE);
    }

    public boolean contains(String key) {
        return sharedPref.contains(key);
    }

    public int getInt(String key, int defValue) {
        return sharedPref.getInt(key, defValue);
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public long getLong(String key, long defValue) {
        return sharedPref.getLong(key, defValue);
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String getString(String key, String defValue) {
        return sharedPref.getString(key, defValue);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public Object getObject(String key, Object defValue) {
        byte[] data = Base64.decode(sharedPref.getString(key, null), Base64.DEFAULT);
        Object o = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    public void setObject(String key, Object value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPref.getBoolean(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}