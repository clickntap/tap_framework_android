package com.clickntap.tap;

import android.support.v7.app.AppCompatActivity;

public class TapActivity extends AppCompatActivity {
    public static final String TAG = "tap";

    public TapApp getApp() {
        try {
            return (TapApp) getApplication();
        } catch (Exception e) {
            return null;
        }
    }

}

