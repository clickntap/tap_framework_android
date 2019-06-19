package com.clickntap.tap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class TapAppActivity extends TapActivity {
    private TapWebView webApp;
    private TapUi ui;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TapUtils.log("TapAppActivity::onCreate");
        RelativeLayout main = new RelativeLayout(this);
        main.setBackgroundColor(getApp().optionAsColor("backgroundColor"));
        webApp = new TapWebView(this);
        webApp.setBackgroundColor(Color.TRANSPARENT);
        webApp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ui = new TapUi(getApp(), webApp);
        if ("1".equals(getApp().optionAsString("developer"))) {
            webApp.loadUrl(getApp().optionAsString("baseUrl") + "?id=" + getApp().optionAsString("projectId") + "&view=app");
        } else {
            ui.check();
        }
        main.addView(webApp);
        setContentView(main);
        ui.init();
    }

    public void onStart() {
        super.onStart();
        //TapUtils.log("TapAppActivity::onStart");
    }

    public void onRestart() {
        super.onRestart();
        //TapUtils.log("TapAppActivity::onRestart");
    }

    public void onResume() {
        super.onResume();
        //TapUtils.log("TapAppActivity::onResume");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void onPause() {
        super.onPause();
        //TapUtils.log("TapAppActivity::onPause");
    }

    public void onStop() {
        super.onStop();
        //TapUtils.log("TapAppActivity::onStop");
    }

    public void onDestroy() {
        super.onDestroy();
        //TapUtils.log("TapAppActivity::onDestroy");
        webApp.close();
    }

}
