package com.kholabs.khoand.Utils.ParseUtil;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.R;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;

    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public void showNotificationMessage(String title, String message, Intent intent) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // notification icon
        int icon = R.mipmap.ic_launcher;

        int smallIcon = R.drawable.ic_push;

        int mNotificationId = ConstValues.NOTIFICATION_ID;

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);
        Notification notification = mBuilder.setSmallIcon(smallIcon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(inboxStyle)
                .setContentIntent(resultPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, notification);
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static void sendPush(String message, ParseUser user, String strClass, ParseObject object)
    {
        if (message.length() == 0 || ParseUser.getCurrentUser() == null || user == null)
            return;
        Date currDate = Calendar.getInstance().getTime();
        String dateString          = (String) DateFormat.format("MMM d, h:mm a",  currDate); // 20
        String targetuserid = user.getObjectId();
        String badge = "Increment";

        HashMap<String, String> data = new HashMap<>();
        data.put("badge", badge);
        data.put("alert", message);
        data.put("sound", "default");
        data.put("class", strClass);
        data.put("objectid", object.getObjectId());

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid", targetuserid);
        requestParams.put("data", data);
        requestParams.put("n_class", strClass);
        requestParams.put("n_date", dateString);
        requestParams.put("n_objid", object.getObjectId());

        ParseCloud.callFunctionInBackground("iOSPush", requestParams, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {

            }
        });
    }

    public static void sendPush(String message, ParseUser user, String strClass, ParseObject post, ParseObject comment)
    {
        if (message.length() == 0 || ParseUser.getCurrentUser() == null || user == null)
            return;
        Date currDate = Calendar.getInstance().getTime();
        String dateString          = (String) DateFormat.format("MMM d, h:mm a",  currDate); // 20
        String targetuserid = user.getObjectId();
        String badge = "Increment";

        HashMap<String, String> data = new HashMap<>();
        data.put("badge", badge);
        data.put("alert", message);
        data.put("sound", "default");
        data.put("class", strClass);
        data.put("objectid", post.getObjectId());
        data.put("commentid", comment.getObjectId());

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid", targetuserid);
        requestParams.put("data", data);
        requestParams.put("n_class", strClass);
        requestParams.put("n_date", dateString);
        requestParams.put("n_objid", post.getObjectId());
        requestParams.put("n_commentid", comment.getObjectId());

        ParseCloud.callFunctionInBackground("iOSPushWithComment", requestParams, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {

            }
        });
    }

    public static  void sendDeviceToken(String registrationId) {
        final ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();

        if (registrationId != null) {
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("installationId", currentInstallation.getObjectId());
            params.put("deviceToken", registrationId);

            ParseCloud.callFunctionInBackground("androidSetToken", params, new FunctionCallback<Boolean>() {
                @Override
                public void done(java.lang.Boolean success, ParseException e) {
                    if (e != null) {
                        // logException(e);
                    }
                }
            });
        }
    }
}
