package com.clickntap.tap.demo;

import android.os.Bundle;
import android.view.View;

import com.clickntap.tap.TapAppActivity;
import com.clickntap.tap.TapUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoActivity extends TapAppActivity {

    public void onCreate(Bundle savedInstanceState) {
        try {
            JSONObject options = new JSONObject();
            options.put("baseUrl", "");
            options.put("developer", "1");
            options.put("backgroundColor", "#000000");
            options.put("color", "#F0F0F0");
            options.put("projectId", "0");
            getApp().setOptions(options);
        } catch (JSONException e) {
            TapUtils.log(e);
        }
        super.onCreate(savedInstanceState);
    }

}
