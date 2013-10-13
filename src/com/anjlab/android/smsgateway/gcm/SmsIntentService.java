package com.anjlab.android.smsgateway.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;


public class SmsIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public SmsIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "SMS Gateway";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	String number = extras.getString("number");
            	String message = extras.getString("message");
            	if (number != null && number.length() > 0 && message != null && message.length() > 0) {
//            		SmsManager smsManager = SmsManager.getDefault();
//            		smsManager.sendTextMessage(number, null, message, null, null);
            		String result = number + ": " + message;
            		Log.i(TAG, result);
            		sendNotification(result);
            		
            		ContentValues values = new ContentValues();
            		values.put("address", number);
            		values.put("body", message); 
            		getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
            	}
            }
        }
        SmsBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_gcm)
        .setContentTitle(getText(R.string.app_name))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg).setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
