package com.clickntap.tap.demo;

import android.os.Bundle;

import com.clickntap.tap.TapAppActivity;
import com.clickntap.tap.TapUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoActivity extends TapAppActivity {

    public void onCreate(Bundle savedInstanceState) {
        try {
            JSONObject options = new JSONObject();
            options.put("baseUrl", "");
            options.put("appUrl", "");
            options.put("developer", "0");
            options.put("backgroundColor", "#FFFFFF");
            options.put("color", "#000000");
            options.put("projectId", "0");
            getApp().setOptions(options);
        } catch ( JSONException e) {
            TapUtils.log(e);
        }
        super.onCreate(savedInstanceState);
    }

}
