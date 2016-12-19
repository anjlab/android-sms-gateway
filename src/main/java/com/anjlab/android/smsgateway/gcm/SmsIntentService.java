package com.anjlab.android.smsgateway.gcm;

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
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;

public class SmsIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public SmsIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "SMS Gateway";

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                String number = extras.getString("number");
                String message = extras.getString("message");
                if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(message))
                {
                    try
                    {
                        if (!number.startsWith("+"))
                        {
                            number = "+" + number;
                        }
                        SmsManager smsManager = SmsManager.getDefault();
                        ArrayList<String> parts = smsManager.divideMessage(message);
                        if (parts.size() > 1)
                        {
                            smsManager.sendMultipartTextMessage(number, null, parts, null, null);
                        }
                        else
                        {
                            smsManager.sendTextMessage(number, null, message, null, null);
                        }

                        String result = number + ": " + message;
                        Log.i(TAG, result);

                        sendNotification(result);

                        ContentValues values = new ContentValues();
                        values.put("address", number);
                        values.put("body", message);
                        getApplicationContext().getContentResolver()
                                               .insert(Uri.parse("content://sms/sent"), values);
                    }
                    catch (Exception ex)
                    {
                        Log.e(TAG, ex.toString());
                    }
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
