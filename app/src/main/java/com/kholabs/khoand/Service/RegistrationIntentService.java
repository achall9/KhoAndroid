package com.kholabs.khoand.Service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kholabs.khoand.Common.QuickStartPreferences;
import com.kholabs.khoand.Context.ConstValues;

import java.io.IOException;

/**
 * Created by Aladar-PC2 on 2/22/2018.
 */

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                token = instanceID.getToken(ConstValues.GCM_SENDERID, scope, null);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent registrationComplete = new Intent(QuickStartPreferences.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
