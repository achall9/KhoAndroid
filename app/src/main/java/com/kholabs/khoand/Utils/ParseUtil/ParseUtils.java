package com.kholabs.khoand.Utils.ParseUtil;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Ravi on 01/06/15.
 */
public class ParseUtils {

    private static String TAG = ParseUtils.class.getSimpleName();

    public static void verifyParseConfiguration(Context context) {
        if (TextUtils.isEmpty(ConstValues.PARSE_APPLICATION_ID) || TextUtils.isEmpty(ConstValues.PARSE_CLIENT_KEY)) {
            Toast.makeText(context, "Please configure your Parse Application ID and Client Key in AppConfig.java", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }
    }

    public static void registerParse(Context context) {
        // initializing parse library
//        Parse.initialize(context, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);

        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId(ConstValues.PARSE_APPLICATION_ID)
                .server(ConstValues.PARSE_SERVER_URL)
                .clientKey(ConstValues.PARSE_CLIENT_KEY)
                .build()
        );

        FacebookSdk.sdkInitialize(context);
        ParseFacebookUtils.initialize(context);

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", ConstValues.GCM_SENDERID);
        installation.saveInBackground();
    }

    public static void subscribeWithEmail(String email) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("email", email);
        installation.put("GCMSenderId", ConstValues.GCM_SENDERID);
        installation.saveInBackground();

        Log.e(TAG, "Subscribed with email: " + email);
    }

    public static void subscribeWithDeviceToken(ParseUser user, String deviceToken) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        installation.put("deviceToken", deviceToken);
        installation.put("user", user);
        installation.saveInBackground();
    }

}
