package com.clickntap.tap;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

public class TapView extends FrameLayout {
    private int top = 0;
    private int left = 0;
    private int width = 0;
    private int height = 0;
    private DisplayMetrics metrics;
    private Map<String, Object> data;

    public TapView(Context context, int top, int left, int width, int height) {
        super(context);
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        init();
    }

    protected void init(){
        this.data = new HashMap<String, Object>();
        this.metrics = getContext().getResources().getDisplayMetrics();
        LayoutParams params = new LayoutParams(getRealWidth(true), getRealHeight(true));
        params.setMargins(getRealLeft(true), getRealTop(true), 0, 0);
        this.setLayoutParams(params);
    }

    protected void loadUi(){

    }

    public void resizeUi(){

    }

    protected void setupUi(){

    }

    protected void setupUiAnimated(){

    }

    protected void unloadUi(){

    }
    public int getRealTop(){
        return getRealTop(false);
    }

    public int getRealLeft(){
        return getRealLeft(false);
    }

    public int getRealWidth(){
        return getRealWidth(false);
    }

    public int getRealHeight(){
        return getRealHeight(false);
    }

    public int getRealTop(boolean dp){
        int top = this.top;
        if(dp){
            top = (int) (top*metrics.density);
        }
        return top;
    }

    public int getRealLeft(boolean dp){
        int left = this.left;
        if(dp){
            left = (int) (left*metrics.density);
        }
        return left;
    }
    public int getRealWidth(boolean dp){
        int width = this.width;
        if(dp){
            width = (int) (width*metrics.density);
        }
        return width;
    }

    public int getRealHeight(boolean dp){
        int height = this.height;
        if(dp){
            height = (int) (height*metrics.density);
        }
        return height;
    }

    protected DisplayMetrics getDisplayMetrics(){
        return metrics;
    }

    public void setMargin(int top, int left, int right, int bottom) {
        this.top = top;
        this.left = left;
        top = (int) (top * metrics.density);
        left = (int) (left * metrics.density);
        right = (int) (right * metrics.density);
        bottom = (int) (bottom * metrics.density);
        if (this.getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) this.getLayoutParams()).setMargins(left, top, right, bottom);
        }
        this.requestLayout();
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = (int) (w * metrics.density);
        params.height = (int) (h * metrics.density);
        this.requestLayout();
        resizeUi();
    }

    public void setFrame(int top, int left, int w, int h) {
        setSize(w, h);
        setMargin(top, left, 0, 0);
    }

    public void setValue(String key, Object value){
        if(key != null){
            if(value != null){
                data.put(key, value);
            }else{
                if(data.containsKey(key)){
                    data.remove(key);
                }
            }
        }
    }
    public String getValueAsString(String key){
        String defaultValue = "";
        if(data.containsKey(key)) defaultValue = (String) data.get(key);
        return defaultValue;
    }
    public Object getValue(String key){
        if(data.containsKey(key)) return data.get(key);
        return null;
    }
    public Map<String, Object> getData(){
        return data;
    }
}
