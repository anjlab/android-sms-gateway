package com.anjlab.android.smsgateway.gcm;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_SENDER_ID = "sender_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	static final String TAG = "SMS Gateway";

	GoogleCloudMessaging gcm;
	String regid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, "Your Android SMS Gateway Registration ID");
				intent.putExtra(Intent.EXTRA_TEXT, regid);
				startActivity(Intent.createChooser(intent, "Send Email"));
			}
		});
		findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView view = (TextView) findViewById(R.id.senderIdInputView);
				storeSenderId(view.getText().toString());
				registerInBackground();
			}
		});

		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(getApplicationContext());

			if (regid.length() == 0)
			{
				findViewById(R.id.send).setVisibility(View.INVISIBLE);
				setStatusText("");
			}
			else
				findViewById(R.id.senderIdPanel).setVisibility(View.GONE);
		} 
		else {
			Log.i(TAG, "No valid Google Play Services APK found.");
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences();
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private String getSenderId() {
		return getGcmPreferences().getString(PROPERTY_SENDER_ID, "");
	}

	private void storeSenderId(String senderId) {
		final SharedPreferences prefs = getGcmPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_SENDER_ID, senderId);
		editor.commit();
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null)
						gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
					regid = gcm.register(getSenderId());
					msg = "Device registered, Registration ID=" + regid;
					storeRegistrationId(getApplicationContext(), regid);
				} 
				catch (IOException ex) {
					msg = "Error: " + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(final String msg) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setStatusText(msg);
						if (regid != null && regid.length() > 0) {
							findViewById(R.id.send).setVisibility(View.VISIBLE);
							findViewById(R.id.senderIdPanel).setVisibility(View.GONE);
						}
					}
				});
				
			}
		}.execute(null, null, null);
	}

	private void setStatusText(String msg) {
		TextView textView = (TextView) findViewById(R.id.display);
		textView.setText(msg);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private SharedPreferences getGcmPreferences() {
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}
}
