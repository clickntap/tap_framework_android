package com.clickntap.tap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.msebera.android.httpclient.Header;
import id.zelory.compressor.Compressor;

public class TapActivity extends AppCompatActivity {
    public static final String TAG = "tap";

    private Handler handler;
    protected Map<String, Object> nativeItems;
    protected Map<String, Bitmap> resources;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeItems = new HashMap<String, Object>();
        resources = new HashMap<String, Bitmap>();
    }

    public Handler getHandler(){
        if(handler == null){
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public void setSize(int resourceId, int w, int h) {
        setSize((ViewGroup) findViewById(resourceId), w, h);
    }

    public void setSize(ViewGroup viewGroup, int w, int h) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.LayoutParams params = viewGroup.getLayoutParams();
        params.width = (int) (w * metrics.density);
        params.height = (int) (h * metrics.density);
        viewGroup.requestLayout();
    }

    public void setMargin(int resourceId, int left, int top, int right, int bottom) {
        setMargin((ViewGroup) findViewById(resourceId), left, top, right, bottom);
    }

    public int getDeviceWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) (metrics.widthPixels / metrics.density);
    }

    public int getDeviceHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        boolean fullScreen = (window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        int statusBarHeight = fullScreen ? 0 : getStatusBarHeight();

        return (int) ((metrics.heightPixels-statusBarHeight) / metrics.density);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public DisplayMetrics getDisplayMetrics(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public void setMargin(ViewGroup viewGroup, int left, int top, int right, int bottom) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        left = (int) (left * metrics.density);
        top = (int) (top * metrics.density);
        right = (int) (right * metrics.density);
        bottom = (int) (bottom * metrics.density);
        if (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams()).setMargins(left, top, right, bottom);
        }
        viewGroup.requestLayout();
    }

    public void setFrame(int resourceId, int left, int top, int w, int h) {
        ViewGroup viewGroup = (ViewGroup) findViewById(resourceId);
        setSize(viewGroup, w, h);
        setMargin(viewGroup, left, top, 0, 0);
    }

    public void setFrame(ViewGroup viewGroup, int left, int top, int w, int h) {
        setSize(viewGroup, w, h);
        setMargin(viewGroup, left, top, 0, 0);
    }

    public static  String getVal(JSONObject json, String key, String defaultValue){
        try{
            if(json != null && json.has(key)){
                return getVal(json.get(key).toString(), defaultValue);
            }else{
                return defaultValue;
            }
        }catch (Exception e){
            return "";
        }
    }

    public static String getVal(String value, String defaultValue){
        if(value != null && !"".equals(value)){
            return value;
        }else{
            return defaultValue;
        }
    }

    public String getValue(String key, String defVal) {
        String value = getSharedPreferences().getString(key, defVal);
        if("null".equals(value)) value = defVal;
        return value;
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

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(getPackageName(), Application.MODE_PRIVATE);
    }

    public void fadeOut(final View view) {
        fadeOut(view, null);
    }

    public void fadeIn(final View view) {
        fadeIn(view, null);
    }

    public void setTimeout(final Runnable runnable, int time){
        getHandler().postDelayed(runnable, time);
    }

    public void clearTimeout(final Runnable runnable){
        getHandler().removeCallbacks(runnable);
    }

    public void fadeOut(final View view, final FadeOut callback){
        view.animate()
                .setDuration(500)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                        if(callback != null) callback.run(animation);
                    }
                });
    }

    public void fadeIn(final View view, final FadeIn callback){
        view.animate()
                .setDuration(500)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.VISIBLE);
                        if(callback != null) callback.run(animation);
                    }
                });
    }

    public interface FadeIn{
        public void run(Animator animation);
    }
    public interface FadeOut{
        public void run(Animator animation);
    }
    public static void copy(File src, File dst) throws Exception{
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public String getFilePath(String name){
        return getFilePath(name, false);
    }
    public String getFilePath(String name, boolean isPrivate){
        if(isPrivate){
            return getFilesDir().getAbsolutePath()+"/"+name;
        }
        return getExternalFilesDir(null).getAbsolutePath()+"/"+name;
    }

    public File getFile(String name){
        return new File(getFilePath(name));
    }
    public File getFile(String name, boolean isPrivate){
        return new File(getFilePath(name, isPrivate));
    }



    public String getFileCachePath(String name){
        return getFileCachePath(name, false);
    }
    public String getFileCachePath(String name, boolean isPrivate){
        if(isPrivate){
            return getCacheDir().getAbsolutePath()+"/"+name;
        }
        return getExternalCacheDir().getAbsolutePath()+"/"+name;
    }
    public File getFileCache(String name){
        return new File(getFileCachePath(name));
    }
    public File getFileCache(String name, boolean isPrivate){
        return new File(getFileCachePath(name, isPrivate));
    }

    protected void unzip(File zip, String folder){
        try {
            ZipInputStream entries = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
            ZipEntry entry;
            int count;
            byte[] buffer = new byte[8192];
            while ((entry = entries.getNextEntry()) != null) {
                File file = getFile("/"+folder+"/"+entry.getName(), true);
                if(!file.getParentFile().getName().contains(getPackageName())){
                    file.getParentFile().mkdirs();
                }
                FileOutputStream out = new FileOutputStream(file);
                try {
                    while ((count = entries.read(buffer)) != -1) out.write(buffer, 0, count);
                } finally {
                    out.close();
                }
            }
        } catch (Exception e) {
        }
    }

    protected void imageCompress(File file, String destinationPath){
        try{
            new Compressor(this)
                    .setMaxWidth(600)
                    .setMaxHeight(600)
                    .setQuality(60)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(destinationPath)
                    .compressToFile(file);
        }catch (Exception e){
            Log.d(TAG,"compress "+e.toString());
        }
    }

    public static boolean addPermission(Context ctx, List<String> list, String permission) {
        if (ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
            list.add(permission);
            return true;
        }
        return false;
    }

    protected RequestParams buildRequestParam(Object params){
        RequestParams requestParams = new RequestParams();
        try{
            if(params instanceof JSONObject){
                return buildRequestParam((JSONObject) params);
            }else if(params instanceof JSONArray){
                return buildRequestParam((JSONArray) params);
            }
        }catch(Exception e) {
        }
        return requestParams;
    }

    protected RequestParams buildRequestParam(JSONObject params){
        RequestParams requestParams = new RequestParams();
        try{
            Iterator<String> keys = params.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = params.get(key).toString();
                requestParams.put(key, value);
            }

        }catch(Exception e) {
        }
        return requestParams;
    }

    protected RequestParams buildRequestParam(JSONArray params){
        RequestParams requestParams = new RequestParams();
        try{
            for(int i=0; i<params.length(); i++){
                JSONObject param = params.getJSONObject(i);
                requestParams.put(param.get("name").toString(), param.get("value").toString());
            }
        }catch(Exception e) {
        }
        return requestParams;
    }


    public interface RequestCallback{
        public void success(JSONObject json);
        public void failed(String error);
    }
}

