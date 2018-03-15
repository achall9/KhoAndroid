package com.kholabs.khoand.Service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Aladar-PC2 on 2/22/2018.
 */

public class MyInstanceIDListenerSErvice extends InstanceIDListenerService {
    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
