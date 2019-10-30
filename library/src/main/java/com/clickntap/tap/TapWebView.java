package com.clickntap.tap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class TapWebView extends FrameLayout {

    protected Map<String, Bitmap> resources;
    private List<TapAppViewComponent> viewComponents;
    private WebView webView;
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            js("appNotification", intent.getStringExtra("json"));
        }
    };
    private ViewGroup spinner;
    private int scrollTop;
    private AsyncHttpClient client;
    private int internalId;


    public TapWebView(Context context) {
        super(context);
        resources = new HashMap<String, Bitmap>();
        internalId = 99999;
        client = new AsyncHttpClient();
        scrollTop = 0;
        viewComponents = new ArrayList<TapAppViewComponent>();
        webView = new WebView(context);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.addJavascriptInterface(this, "tap");
        webView.getSettings().setJavaScriptEnabled(true);
        //if (getTapApp().optionAsInt("developer") == 1) {
        WebView.setWebContentsDebuggingEnabled(true);
        //}
        webView.getSettings().setTextZoom(100);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        try {
            client.setSSLSocketFactory(new MySSLSocketFactory(KeyStore.getInstance(KeyStore.getDefaultType())));
        } catch (Exception e) {
        }
        addView(webView);
        webView.setAlpha(0);
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        progressBar.getIndeterminateDrawable().setColorFilter(getTapApp().optionAsColor("color"), android.graphics.PorterDuff.Mode.SRC_IN);
        spinner = TapUtils.addFrame(context, this, progressBar);
        spinner.setAlpha(0);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                TapUtils.fadeIn(spinner);
                TapUtils.fadeOut(webView);
            }

            public void onPageFinished(WebView view, String url) {
                TapUtils.fadeOut(spinner);
                TapUtils.fadeIn(webView);
            }
        });
        getActivity().registerReceiver(notificationReceiver, new IntentFilter("notification"));
    }

    public static void copy(File src, File dst) throws Exception {
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

    public void close() {
        TapApp app = getTapApp();
        if (app != null) {
            app.stopNetworkServiceDiscovery();
        }
        getActivity().unregisterReceiver(notificationReceiver);
    }


    public void setEnv(String callback) throws Exception {
        int deviceId = Integer.parseInt(getValue("deviceId", "0"));
        int userId = Integer.parseInt(getValue("userId", "0"));
        JSONObject env = new JSONObject();
        Map<String, ?> keys = getSharedPreferences().getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            env.put(entry.getKey(), entry.getValue() + "");
        }
        env.put("userId", userId);
        env.put("deviceId", deviceId);
        TapUtils.log(env);
        js(callback, env.toString());
    }


    protected void setupEnv(final JSONObject json, final String callback) throws Exception {
        JSONObject env = new JSONObject();

        Map<String, ?> keys = getSharedPreferences().getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            env.put(entry.getKey(), entry.getValue() + "");
        }

        int appId = Integer.parseInt(getValue("appId", "0"));
        int userId = Integer.parseInt(getValue("userId", "0"));
        env.put("userId", userId);
        env.put("appId", appId);
        env.put("channel", "android");

        if (appId == 0) {
            AsyncHttpClient client = new AsyncHttpClient();
             client.get(json.getString("url") + "add/?channel=android", new JsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.has("id")) {
                        try {
                            setValue("appId", response.get("id").toString());
                            setupEnv(json, callback);
                        } catch (Exception e) {
                        }
                    }
                }
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    try {
                        setupEnv(json, callback);
                    } catch (Exception e) {
                    }
                }
                public void onProgress(long bytesWritten, long totalSize) {

                }
            });
        } else {
            js(callback, env);
        }
    }

    @JavascriptInterface
    public void postMessage(final String jsonAsString) {
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    final JSONObject json = new JSONObject(jsonAsString);
                    final String callback = json.has("callback") ? json.getString("callback") : "";
                    if ("env".equals(json.getString("what"))) {
                        setupEnv(json, callback);
                    } else if ("context".equals(json.getString("what"))) {

                        int deviceId = Integer.parseInt(getValue("deviceId", "0"));
                        int userId = Integer.parseInt(getValue("userId", "0"));
                        setValue("url", json.getString("url"));
                        RequestParams params = new RequestParams();
                        params.add("channel", "android");
                        params.add("deviceId", Integer.toString(deviceId));
                        params.add("userId", Integer.toString(userId));
                        client.get(json.getString("url"), params, new JsonHttpResponseHandler() {
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    setValue("deviceId", response.get("deviceId").toString());
                                    if(response.has("userId")) {
                                        setValue("userId", response.get("userId").toString());
                                    }
                                    setEnv(callback);
                                } catch (Exception e) {
                                    TapUtils.log(e);
                                }
                            }

                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                try {
                                } catch (Exception e) {
                                }
                            }
                        });
                    } else if ("wait-on".equals(json.getString("what"))) {
                        TapUtils.fadeIn(spinner);
                    } else if ("wait-off".equals(json.getString("what"))) {
                        TapUtils.fadeOut(spinner);
                    } else if ("need".equals(json.getString("what"))) {
                        if (json.has("type")) {
                            if ("bonjour".equals(json.getString("type"))) {
                                TapApp app = getTapApp();
                                if (app != null) {
                                    app.startNetworkServiceDiscovery();
                                }
                            }
                        }
                    } else if ("bonjour".equals(json.getString("what"))) {
                        TapApp app = getTapApp();
                        if (app != null) {
                            app.startNetworkServiceDiscovery();
                            js(json.getString("callback"), app.getNetworkServices());
                        }
                    } else if ("native-begin".equals(json.getString("what"))) {
                        for (TapAppViewComponent viewComponent : viewComponents) {
                            viewComponent.setState(false);
                            if (viewComponent.ci("noFade") == 0) {
                                TapUtils.fadeOut(viewComponent.getView());
                            }
                        }
                    } else if ("native".equals(json.getString("what"))) {
                        try {
                            JSONObject conf = json.getJSONObject("conf");
                            TapAppViewComponent theViewComponent = null;
                            for (TapAppViewComponent viewComponent : viewComponents) {
                                if (viewComponent.cs("component").equals(conf.getString("component"))) {
                                    if (viewComponent.ci("id") == conf.getInt("id")) {
                                        theViewComponent = viewComponent;
                                        viewComponent.setState(true);
                                    }
                                }
                            }
                            TapUtils.log("A) " + conf);

                            if (theViewComponent == null) {
                                TapUtils.log("B) " + conf);
                                ViewGroup view = null;
                                if ("web".equals(conf.getString("component"))) {
                                    view = new TapWebView(getContext());
                                    ((TapWebView) view).loadUrl(conf.getString("src"));
                                }
                                if ("map".equals(conf.getString("component"))) {
                                    view = new TapView(getContext());
                                    view.setBackgroundColor(Color.WHITE);
                                }
                                if (view != null) {
                                    addView(view);
                                    spinner.bringToFront();
                                    theViewComponent = new TapAppViewComponent(view, conf, null, true);
                                    viewComponents.add(theViewComponent);
                                    if ("map".equals(conf.getString("component"))) {
                                        buildMap((TapView) view, conf);
                                    }
                                }
                            }
                            if (theViewComponent != null) {
                                TapUtils.fadeIn(theViewComponent.getView());
                                int x = conf.getInt("x");
                                int y = conf.getInt("y");
                                int w = conf.getInt("w");
                                int h = conf.getInt("h");
                                TapUtils.setFrame(theViewComponent.getView(), x, y, w, h);
                                y = conf.getInt("y") + scrollTop;
                                conf.put("y", y);
                                theViewComponent.setConf(conf);
                            }
                        } catch (Exception e) {
                            TapUtils.log(e);
                        }
                    } else if ("native-end".equals(json.getString("what"))) {
                        for (final TapAppViewComponent viewComponent : viewComponents) {
                            if (!viewComponent.isState()) {
                                TapUtils.fadeOut(viewComponent.getView(), new TapUtils.TapCallback() {
                                    public void done() {
                                        viewComponents.remove(viewComponent);
                                        if (viewComponent.getView() instanceof TapWebView) {
                                            ((TapWebView) viewComponent.getView()).close();
                                            ViewGroup viewParent = (ViewGroup) viewComponent.getView().getParent();
                                            viewParent.removeView(viewComponent.getView());
                                        }
                                    }
                                });
                            }
                        }
                    } else if ("scroll".equals(json.getString("what"))) {
                        scrollTop = json.getInt("y");
                        try {
                            for (TapAppViewComponent viewComponent : viewComponents) {
                                JSONObject conf = viewComponent.getConf();
                                int y = conf.getInt("y") - scrollTop;
                                TapUtils.setTop(viewComponent.getView(), y);
                            }
                        } catch (Exception e) {
                            TapUtils.log(e);
                        }
                    } else if ("launch-options".equals(json.getString("what"))) {
                        JSONObject env = new JSONObject();
                        env.put("os", "android");
                        js(json.getString("callback"), env);
                    } else if ("log".equals(json.getString("what"))) {
                        TapUtils.log(json.getString("log"));
                    } else if ("http-get".equals(json.getString("what")) || "http-post".equals(json.getString("what"))) {
                        RequestParams params = new RequestParams();
                        if (json.has("params")) {
                            params = buildRequestParam(json.get("params"));
                        }
                        client.post(json.getString("url"), params, new AsyncHttpResponseHandler() {
                            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                                try {
                                    String responseAsText = new String(response, StandardCharsets.UTF_8);
                                    js(json.getString("callback"), responseAsText);
                                } catch (Exception e) {
                                    TapUtils.log(e);
                                }
                            }

                            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
                                try {
                                    js(json.getString("callback"), "{}");
                                } catch (Exception e) {
                                    TapUtils.log(e);
                                }
                            }
                        });
                    } else if ("broadcast".equals(json.getString("what"))) {
                        getTapApp().setTimeout(new TapTask.Task() {
                            public void exec() throws Exception {
                                getTapApp().getUi().broadcast(json.getJSONObject("data"));
                            }
                        }, 0);
                    } else if ("set".equals(json.getString("what")) || "save".equals(json.getString("what"))) {
                        setValue(json.getString("name"), json.getString("value"));
                    } else if ("unset".equals(json.getString("what"))) {
                        setValue(json.getString("name"), null);
                    } else if ("get".equals(json.getString("what")) || "load".equals(json.getString("what"))) {
                        String value = getValue(json.getString("name"), "");
                        try {
                            js(callback, new JSONObject(value).toString());
                        } catch (Exception e) {
                            js(callback, "{}");
                        }
                    } else if ("offline".equals(json.getString("what"))) {
                        checkOffline(json);
                    } else if ("options".equals(json.getString("what"))) {
                        options(json);
                    } else if ("open-url".equals(json.getString("what"))) {
                        String url = json.getString("url");
                        if (url.startsWith("http")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            getActivity().startActivity(intent);
                        }
                    } else if ("alert".equals(json.getString("what"))) {
                        alert(json.getString("message"));
                    } else if ("resource-image".equals(json.getString("what"))) {
                        byte[] base64Byte = Base64.decode(json.getString("base64"), Base64.NO_WRAP);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(base64Byte, 0, base64Byte.length);
                        if (json.has("scale")) {
                            double scale = json.getDouble("scale");
                            int width = Math.round((float) scale * bitmap.getWidth()) * 2;
                            int height = Math.round((float) scale * bitmap.getHeight()) * 2;
                            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        }
                        resources.put(json.getString("name"), bitmap);
                    } else if ("map-poi".equals(json.getString("what"))) {
                        setupMapPoi(json);
                    } else {
                        TapUtils.log(jsonAsString);
                    }
                } catch (Exception e) {
                    TapUtils.log(e);
                }
            }
        });
    }

    protected void setupMapPoi(final JSONObject json) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    final String id = "map" + json.getString("mapId");

                    TapAppViewComponent theViewComponent = null;
                    for (TapAppViewComponent viewComponent : viewComponents) {
                        if (viewComponent.cs("component").equals("map")) {
                            if (viewComponent.ci("id") == json.getInt("mapId")) {
                                theViewComponent = viewComponent;
                                viewComponent.setState(true);
                            }
                        }
                    }
                    if (theViewComponent != null) {
                        TapView element = (TapView) theViewComponent.getView();
                        GoogleMap map = (GoogleMap) element.getValue("map");
                        double lat = Double.parseDouble(getVal(json, "lat", "0"));
                        double lng = Double.parseDouble(getVal(json, "lng", "0"));
                        String title = getVal(json, "title", "");
                        String subtitle = getVal(json, "subtitle", "");
                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).title(title).snippet(subtitle);
                        if (json.has("icon")) {
                            try {
                                Bitmap bitmap = resources.get(json.getString("icon"));
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                            } catch (Exception e) {
                            }
                        }
                        Marker marker = map.addMarker(markerOptions);
                        marker.setTag(json);
                    }

                } catch (Exception e) {
                }
            }
        });
    }

    private void buildMap(final TapView element, final JSONObject conf) throws Exception {
        final int mapId = conf.getInt("id");

        //TapUtils.log(conf.toString());

        element.setId((internalId++) + mapId);

        final int w = conf.getInt("w");
        final int h = conf.getInt("h");

        final String didChangeCallback = getVal(conf, "didChangeCallback", "");
        final String didLoadCallback = getVal(conf, "didLoadCallback", "");
        final String didSelectCallback = getVal(conf, "didSelectCallback", "");


        addMap(element.getId(), new OnMapReadyCallback() {
            public void onMapReady(final GoogleMap googleMap) {

                element.setValue("map", googleMap);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }

                if (!"".equals(didChangeCallback)) mapChanged(googleMap, didChangeCallback, mapId);
                if (!"".equals(didSelectCallback)) mapSelect(googleMap, didSelectCallback);
                if (!"".equals(didLoadCallback)) js(didLoadCallback, conf);

                try {
                    if (conf.has("center")) {
                        moveMap(googleMap, conf.getJSONObject("center"));
                    } else {
                        // ONLY UBIK APP
                        JSONObject center = new JSONObject();
                        center.put("lat", 43.333148);
                        center.put("lng", 11.5401635);
                        center.put("radius", 705451.5313829471);
                        center.put("animated", 0);
                        moveMap(googleMap, center);
                    }
                } catch (Exception e) {
                    TapUtils.log(e);
                }
            }
        });
    }

    private void moveMap(GoogleMap map, JSONObject data) {
        final double lat = Float.parseFloat(getVal(data, "lat", "0"));
        final double lng = Float.parseFloat(getVal(data, "lng", "0"));
        final double radius = Float.parseFloat(getVal(data, "radius", "0"));
        final float animated = Float.parseFloat(getVal(data, "animated", "0"));
        LatLng latLng = new LatLng(lat, lng);

        if (data.has("radius")) {
            if (animated == 1)
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(toBounds(latLng, radius), 0), 1000, null);
            else map.moveCamera(CameraUpdateFactory.newLatLngBounds(toBounds(latLng, radius), 0));
        } else {
            if (animated == 1) map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000, null);
            else map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private void mapChanged(final GoogleMap googleMap, final String callback, final int mapId) {
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            public void onCameraMove() {
                try {
                    LatLng target = googleMap.getCameraPosition().target;
                    JSONObject o = new JSONObject();
                    o.put("lat", target.latitude);
                    o.put("lng", target.longitude);
                    js(callback + "(" + o.toString() + ", " + mapId + ")");
                } catch (Exception e) {
                    TapUtils.log(e);
                }
            }
        });
    }

    private void mapSelect(final GoogleMap googleMap, final String callback) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                JSONObject o = new JSONObject();
                try {
                    if (marker.getTag() != null) {
                        o = (JSONObject) marker.getTag();
                    } else {
                        o.put("lat", marker.getPosition().latitude);
                        o.put("lng", marker.getPosition().longitude);
                    }
                } catch (Exception e) {
                }
                js(callback, o);
                return false;
            }
        });
    }

    public void addMap(final int viewGroupId, final OnMapReadyCallback onload) {
        FragmentManager fm = ((TapAppActivity) getActivity()).getSupportFragmentManager();
        FragmentTransaction mTransaction = fm.beginTransaction();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(onload);
        mTransaction.add(viewGroupId, mapFragment).commit();
        fm.executePendingTransactions();
    }

    private void options(final JSONObject json) {
        try {
            final JSONArray optionsData = json.getJSONArray("options");
            String[] options = new String[optionsData.length()];
            for (int i = 0; i < optionsData.length(); i++) {
                JSONObject option = optionsData.getJSONObject(i);
                options[i] = option.getString("text");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            String message = "";
            if (json.has("title")) message += json.getString("title");
            if (json.has("message")) message += " " + json.getString("message");
            if (!"".equals(message)) builder.setTitle(message);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int index) {
                    try {
                        js(json.getString("callback"), index + "");
                    } catch (Exception e) {
                    }
                }
            });
            builder.show();
        } catch (Exception e) {
        }
    }

    private void alert(final String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void checkOffline(final JSONObject json) {
        try {
            final String url = json.getString("url");
            if ("".equals(url)) return;

            String urlEncode = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP).trim().replace("=", "");
            String filename = ("resources/" + urlEncode + "." + json.getString("extension")).trim();
            final File fileCache = getFileCache(filename, true);
            if (!fileCache.getParentFile().exists()) fileCache.getParentFile().mkdirs();
            if (fileCache.exists()) {
                setupOffline(fileCache, json);
            } else {
                client.get(url, new FileAsyncHttpResponseHandler(getContext()) {
                    public void onSuccess(int statusCode, Header[] headers, File file) {
                        try {
                            copy(file, fileCache);
                            setupOffline(fileCache, json);
                        } catch (Exception e) {
                            TapUtils.log(e);
                        }
                    }

                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    }
                });
            }
        } catch (Exception e) {
            TapUtils.log(e);
        }
    }

    private void save(String name, String text) {
        writeToFile(getFileCache("data/" + name, true), text);
    }

    private String load(String name) {
        return readFromFile(getFileCache("data/" + name, true));
    }

    public String getFileCachePath(String name) {
        return getFileCachePath(name, false);
    }

    public String getFileCachePath(String name, boolean isPrivate) {
        if (isPrivate) {
            return getApp().getCacheDir().getAbsolutePath() + "/" + name;
        }
        return getApp().getExternalCacheDir().getAbsolutePath() + "/" + name;
    }

    public File getFileCache(String name) {
        return new File(getFileCachePath(name));
    }

    public File getFileCache(String name, boolean isPrivate) {
        return new File(getFileCachePath(name, isPrivate));
    }

    private void writeToFile(File file, String data) {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (file.exists()) file.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (Exception e) {
        }
    }

    private String readFromFile(File file) {
        String data = "";
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (file.exists()) {
                InputStream in = new FileInputStream(file);
                if (in != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    in.close();
                    data = stringBuilder.toString();
                }
            }
        } catch (Exception e) {
        }
        return data;
    }

    private void setupOffline(File file, JSONObject data) {
        //Log.d(TAG, "setupOffline " + file.getAbsolutePath());
        try {
            String id = data.getString("id");
            String attr = String.format(Locale.ITALIAN, "$('[id=%s%s%s]')", '"', id, '"');
            if ("background".equals(data.getString("attribute"))) {
                js(attr + ".css('background-image','url(file://" + file.getAbsolutePath() + ")')");
            } else if ("src".equals(data.getString("attribute"))) {
                js(attr + ".attr('attr','file://" + file.getAbsolutePath() + "')");
            }
            js("appImage('" + id + "')");
        } catch (Exception e) {
        }
    }

    public String getValue(String key, String defVal) {
        String value = getSharedPreferences().getString(key, defVal);
        if ("null".equals(value)) value = defVal;
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
        return getApp().getSharedPreferences(getApp().getPackageName(), Application.MODE_PRIVATE);
    }

    protected RequestParams buildRequestParam(Object params) {
        RequestParams requestParams = new RequestParams();
        try {
            if (params instanceof JSONObject) {
                return buildRequestParam((JSONObject) params);
            } else if (params instanceof JSONArray) {
                return buildRequestParam((JSONArray) params);
            }
        } catch (Exception e) {
        }
        return requestParams;
    }

    protected RequestParams buildRequestParam(JSONObject params) {
        RequestParams requestParams = new RequestParams();
        try {
            Iterator<String> keys = params.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = params.get(key);
                requestParams.put(key, value);
            }

        } catch (Exception e) {
        }
        return requestParams;
    }

    protected RequestParams buildRequestParam(JSONArray params) {
        RequestParams requestParams = new RequestParams();
        try {
            for (int i = 0; i < params.length(); i++) {
                JSONObject param = params.getJSONObject(i);
                requestParams.put(param.get("name").toString(), param.get("value"));
            }
        } catch (Exception e) {
        }
        return requestParams;
    }


    public void js(String callback, JSONObject json) {
        if (!"".equals(callback)) {
            js(callback + "(" + json.toString() + ")");
        }
    }

    public void js(String callback, String code) {
        if (!"".equals(callback)) {
            js(callback + "(" + code + ")");
        }
    }

    public void js(final String code) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                webView.evaluateJavascript(code, null);
                //TapUtils.log(webView.getOriginalUrl() + "/" + code);
            }
        });
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private Application getApp() {
        return getActivity().getApplication();
    }

    private TapAppActivity getTapAppActivity() {
        try {
            return (TapAppActivity) getActivity();
        } catch (Exception e) {
            return null;
        }
    }

    private TapApp getTapApp() {
        try {
            return (TapApp) getActivity().getApplication();
        } catch (Exception e) {
            return null;
        }
    }

    public void loadUrl(String url) {
        webView.loadUrl(url);
    }

}
