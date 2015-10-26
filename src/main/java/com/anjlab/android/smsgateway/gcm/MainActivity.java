package com.anjlab.android.smsgateway.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity
{

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_SENDER_ID = "sender_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    public static final String PROPERTY_FORWARD_ENABLED = "forwarding_enabled";
    public static final String PROPERTY_FORWARD_URL = "forwarding_url";
    static final String TAG = "SMS Gateway";

    @InjectView(R.id.send)
    Button send;

    @InjectView(R.id.saveProjectId)
    Button saveProjectId;

    @InjectView(R.id.saveForwarning)
    Button saveForwarning;

    @InjectView(R.id.display)
    TextView display;

    @InjectView(R.id.senderIdInputView)
    EditText senderId;

    @InjectView(R.id.forwardingUrlInputView)
    EditText forwardingUrl;

    @InjectView(R.id.forwardingEnabled)
    CheckBox forwardingEnabled;

    @InjectView(R.id.senderIdPanel)
    View senderIdPanel;

    GoogleCloudMessaging gcm;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButterKnife.inject(this);

        send.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Your Android SMS Gateway Registration ID");
                intent.putExtra(Intent.EXTRA_TEXT, getRegistrationId());
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        saveProjectId.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                storeSenderId(senderId.getText().toString());
                registerInBackground();
            }
        });
        saveForwarning.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getGcmPreferences()
                        .edit()
                        .putString(PROPERTY_FORWARD_URL, forwardingUrl.getText().toString())
                        .putBoolean(PROPERTY_FORWARD_ENABLED, forwardingEnabled.isChecked())
                        .apply();
            }
        });

        forwardingUrl.setText(getForwardingUrl());
        forwardingEnabled.setChecked(getForwardingEnabled());

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            if (TextUtils.isEmpty(getRegistrationId()))
            {
                send.setVisibility(View.INVISIBLE);
                display.setText("");
            }
            else
                senderIdPanel.setVisibility(View.GONE);
        }
        else
        {
            display.setText(R.string.error_no_play_services);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        checkPlayServices();
    }

    private boolean checkPlayServices()
    {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

    private void storeRegistrationId(String regId)
    {
        getGcmPreferences()
                .edit()
                .putString(PROPERTY_REG_ID, regId)
                .putInt(PROPERTY_APP_VERSION, getAppVersion())
                .apply();
    }

    private String getRegistrationId()
    {
        SharedPreferences prefs = getGcmPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0)
        {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        if (registeredVersion != getAppVersion())
        {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private String getSenderId()
    {
        return getGcmPreferences().getString(PROPERTY_SENDER_ID, "");
    }

    private void storeSenderId(String senderId)
    {
        getGcmPreferences()
                .edit()
                .putString(PROPERTY_SENDER_ID, senderId)
                .apply();
    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                try
                {
                    if (gcm == null)
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    String id = gcm.register(getSenderId());
                    storeRegistrationId(id);
                    return "Device registered, Registration ID = " + id;
                }
                catch (IOException ex)
                {
                    return "Error: " + ex.getMessage();
                }
            }

            @Override
            protected void onPostExecute(final String msg)
            {
                runOnUiThread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        display.setText(msg);
                        if (!TextUtils.isEmpty(getRegistrationId()))
                        {
                            send.setVisibility(View.VISIBLE);
                            senderIdPanel.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }.execute(null, null, null);
    }

    private int getAppVersion()
    {
        try
        {
            return getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0)
                    .versionCode;
        }
        catch (NameNotFoundException e)
        {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGcmPreferences()
    {
        return getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
    }

    private String getForwardingUrl()
    {
        return getGcmPreferences().getString(PROPERTY_FORWARD_URL, "");
    }

    private boolean getForwardingEnabled()
    {
        return getGcmPreferences().getBoolean(PROPERTY_FORWARD_ENABLED, false);
    }
}
