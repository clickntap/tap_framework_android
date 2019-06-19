package com.clickntap.tap;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.msebera.android.httpclient.Header;

public class TapUi {
    private TapApp app;
    private TapWebView webApp;
    private AsyncHttpClient client;

    public TapUi(TapApp app, TapWebView webApp) {
        this.app = app;
        this.webApp = webApp;
        this.client = new AsyncHttpClient();
        app.setUi(this);
    }

    public void check() {
        if (!TapConnectivity.isConnected(app)) {
            show();
        } else {
            String appUrl = app.optionAsString("appUrl");
            TapUtils.log("appUrl = " + appUrl);
            client.get(appUrl, new JsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    TapUtils.log("version checked succesfully");
                    try {
                        final String url = response.getString("archive");
                        if (url.equals(app.getValue("archive", ""))) show();
                        else download(url);
                    } catch (Exception e) {
                    }
                }

                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    TapUtils.log("error checking version");
                    if (!"".equals(app.getValue("archive", ""))) show();
                }
            });
        }
    }

    private void download(final String url) {
        TapUtils.log("Download Archive: " + url);
        client.get(url, new FileAsyncHttpResponseHandler(app) {
            public void onSuccess(int statusCode, Header[] headers, File file) {
                unzip(file, "app");
                app.setValue("archive", url);
                show();
                TapUtils.log("downloaded succesfully");
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                TapUtils.log("download error");
            }
        });
    }

    private void unzip(File zip, String folder) {
        try {
            ZipInputStream entries = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
            ZipEntry entry;
            int count;
            byte[] buffer = new byte[8192];
            while ((entry = entries.getNextEntry()) != null) {
                File file = app.getFile("/" + folder + "/" + entry.getName(), true);
                if (!file.getParentFile().getName().contains(app.getPackageName())) {
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
            TapUtils.log(e);
        }
    }

    public void show() {
        String filename = app.getFilePath("app/app.html", true);
        webApp.loadUrl("file:///" + filename + "?view=app");
        TapUtils.log("webapp init");
    }

    public void broadcast(JSONObject data) {
        final String dataAsString = data.toString();
        app.setTimeout(new TapTask.Task() {
            public void exec() throws Exception {
                webApp.js("appBroadcast", dataAsString);
            }
        }, 0);
    }

    public void init() {
    }
}
