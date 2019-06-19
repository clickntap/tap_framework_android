package com.clickntap.tap;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class TapApp extends Application {

    private Handler handler;
    private TapAppBonjour bonjour;
    private TapUi ui;

    private JSONObject options;

    public TapApp() {
        handler = new Handler();
        bonjour = new TapAppBonjour(this);

    }

    public static String getVal(JSONObject json, String key, String defaultValue) {
        try {
            if (json != null && json.has(key)) {
                return getVal(json.get(key).toString(), defaultValue);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String getVal(String value, String defaultValue) {
        if (value != null && !"".equals(value)) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public TapUi getUi() {
        return ui;
    }

    public void setUi(TapUi ui) {
        this.ui = ui;
    }

    public void setTimeout(final TapTask.Task task, int delayMillis) {
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    task.exec();
                } catch (Exception e) {
                    TapUtils.log(e);
                }
            }
        }, delayMillis);
    }

    public void notification(String what, JSONObject json) {
        Intent intent = new Intent();
        intent.setAction("notification");
        intent.putExtra("what", what);
        intent.putExtra("json", json.toString());
        sendBroadcast(intent);
    }

    public void onCreate() {
        super.onCreate();
        //TapUtils.log("TapApp::onCreate");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //TapUtils.log("TapApp::onConfigurationChanged");
    }

    public void onLowMemory() {
        super.onLowMemory();
        //TapUtils.log("TapApp::onLowMemory");
    }

    public void startNetworkServiceDiscovery() {
        bonjour.startDiscovery();
    }

    public void stopNetworkServiceDiscovery() {
        bonjour.stopDiscovery();
    }

    public JSONObject getNetworkServices() {
        return bonjour.getNetworkServices();
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public String optionAsString(String option) {
        try {
            return getOptions().getString(option);
        } catch (JSONException e) {
            return "";
        }
    }

    public int optionAsColor(String option) {
        return Color.parseColor(optionAsString(option));
    }

    public int optionAsInt(String option) {
        return Integer.parseInt(optionAsString(option));
    }

    public String getFilePath(String name) {
        return getFilePath(name, false);
    }

    public String getFilePath(String name, boolean isPrivate) {
        if (isPrivate) {
            return getFilesDir().getAbsolutePath() + "/" + name;
        }
        return getExternalFilesDir(null).getAbsolutePath() + "/" + name;
    }

    public File getFile(String name) {
        return new File(getFilePath(name));
    }

    public File getFile(String name, boolean isPrivate) {
        return new File(getFilePath(name, isPrivate));
    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(getPackageName(), Application.MODE_PRIVATE);
    }

    public String getValue(String key, String defVal) {
        return getSharedPreferences().getString(key, defVal);
    }

    public void setValue(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }
        editor.commit();
    }
}