package com.kholabs.khoand.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Utils.ParseUtil.NotificationUtils;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aladar-PC2 on 2/10/2018.
 */

public class CustomPushReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = CustomPushReceiver.class.getSimpleName();
    public static final String intentAction = "com.parse.push.intent.RECEIVE";

    private Intent parseIntent;
    private NotificationUtils notificationUtils;

    @Override
    protected void onPushReceive(Context context, Intent intent)
    {
        super.onPushReceive(context, intent);

        if (intent == null)
            return;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            parseIntent = intent;
            parsePushJson(context, json);
        } catch (JSONException e) {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    private void parsePushJson(Context context, JSONObject json) {
        try {
            String message = json.getString("alert");
            Activity currActivity = MyApp.getInstance().getCurrentActivity();

            broadCast(context, json);
            Intent resultIntent = new Intent(context, currActivity.getClass());
            showNotificationMessage(context, "Kho App", message, resultIntent);
        } catch (JSONException e)
        {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }

    private void showNotificationMessage(Context context, String title, String message, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.putExtras(parseIntent.getExtras());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, intent);
    }

    private void broadCast(Context context, JSONObject json) {
        Intent intent = new Intent(intentAction);
        try {
            String alert = json.getString("alert");
            String notiClass = json.getString("class");

            if (notiClass.equals("Threads"))
            {
                String objectId = json.getString("objectid");
                intent.setAction("android.intent.action.IM_NEW_MSG");
                intent.putExtra("alert", alert);
                intent.putExtra("message", alert.split(":")[1]);
                intent.putExtra("objectId", objectId);
                intent.putExtra("class", notiClass);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
            else if (notiClass.equals("Comments"))
            {
                String objectId = json.getString("objectid");
                String commentId = json.getString("commentId");
                intent.setAction("android.intent.action.IM_NEW_COMMENT");
                intent.putExtra("alert", alert);
                intent.putExtra("comment", alert.split(":")[1]);
                intent.putExtra("objectId", objectId);
                intent.putExtra("commentId", objectId);
                intent.putExtra("class", notiClass);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
            else if (notiClass.equals("Posts"))
            {
                String objectId = json.getString("objectid");
                intent.setAction("android.intent.action.IM_NEW_POST");
                intent.putExtra("alert", alert);
                intent.putExtra("class", notiClass);
                intent.putExtra("objectId", objectId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        } catch (JSONException ex)
        {

        }
    }

}
