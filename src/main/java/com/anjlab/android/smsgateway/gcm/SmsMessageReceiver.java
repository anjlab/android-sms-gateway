package com.anjlab.android.smsgateway.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;

public class SmsMessageReceiver extends BroadcastReceiver
{
    private static final String PDUS = "pdus";

    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences preferences =
                context.getSharedPreferences(context.getApplicationContext().getPackageName(),
                                             Context.MODE_PRIVATE);
        if (preferences.getBoolean(MainActivity.PROPERTY_FORWARD_ENABLED, false))
        {
            Object[] pdus = (Object[]) intent.getExtras().get(PDUS);
            if (pdus != null && pdus.length > 0)
            {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
                String text = message.getMessageBody();
                String number = message.getDisplayOriginatingAddress();

                Intent i = new Intent(context, HttpHookIntentService.class);
                i.putExtra(HttpHookIntentService.URL,
                           preferences.getString(MainActivity.PROPERTY_FORWARD_URL, ""));
                i.putExtra(HttpHookIntentService.TEXT, text);
                i.putExtra(HttpHookIntentService.SENDER, number);
                context.startService(i);
            }
        }
    }
}
