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
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                SmsMessage message = messages[0];
                String text;
                if (messages.length == 1 || message.isReplace())
                {
                    text = message.getDisplayMessageBody();
                }
                else
                {
                    StringBuilder textBuilder = new StringBuilder();
                    for (SmsMessage msg : messages)
                    {
                        textBuilder.append(msg.getMessageBody());
                    }
                    text = textBuilder.toString();
                }

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
