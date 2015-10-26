package com.anjlab.android.smsgateway.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpHookIntentService extends IntentService {
    public static final String URL = "url";
    public static final String TEXT = "text";
    public static final String SENDER = "sender";

    public HttpHookIntentService() {
        super("HttpHookIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        try
        {
            java.net.URL url = new URL(extras.getString(URL));
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(String.format("content=%s&from_number=%s",
                                       extras.getString(TEXT),
                                       extras.getString(SENDER)));
            writer.flush();
            writer.close();
            os.close();
            conn.getInputStream().close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
