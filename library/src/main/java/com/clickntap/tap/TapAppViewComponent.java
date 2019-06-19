package com.clickntap.tap;

import android.view.ViewGroup;

import org.json.JSONObject;

public class TapAppViewComponent {

    JSONObject conf;
    JSONObject animateConf;
    private boolean state;
    private ViewGroup view;

    public TapAppViewComponent(ViewGroup view, JSONObject conf, JSONObject animateConf, boolean state) {
        this.view = view;
        this.conf = conf;
        this.animateConf = animateConf;
        this.state = state;
    }

    public void setup() {

    }

    public void setupAnimate() {

    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public ViewGroup getView() {
        return view;
    }

    public void setView(ViewGroup view) {
        this.view = view;
    }

    public JSONObject getConf() {
        return conf;
    }

    public void setConf(JSONObject conf) {
        this.conf = conf;
    }

    public JSONObject getAnimateConf() {
        return animateConf;
    }

    public void setAnimateConf(JSONObject animateConf) {
        this.animateConf = animateConf;
    }

    public int ci(String key) {
        try {
            return conf.getInt(key);
        } catch (Exception e) {
            return 0;
        }
    }

    public String cs(String key) {
        try {
            return conf.getString(key);
        } catch (Exception e) {
            return "";
        }
    }

}
