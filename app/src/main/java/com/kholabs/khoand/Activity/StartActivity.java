package com.kholabs.khoand.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.QuickStartPreferences;
import com.kholabs.khoand.Model.Notification;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Service.RegistrationIntentService;
import com.kholabs.khoand.Utils.ParseUtil.NotificationUtils;
import com.kholabs.khoand.Utils.ParseUtil.ParseUtils;
import com.kholabs.khoand.Utils.ParseUtil.PrefManager;
import com.parse.ParseUser;
import com.thunderrise.animations.PulseAnimation;


public class StartActivity extends AppCompatActivity {

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    PulseAnimation pulseAnimation;
    final Handler handler = new Handler();
    LinearLayout llAthlete, llTherapist;

    private TextView lblDisplay, lblChoose;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private PrefManager pref;

    private long oldMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        pref = new PrefManager(getApplicationContext());

        getUIObjects();

        checkTutorial();
        initEvent();

        MyApp.getInstance().setCurrentActivity(this);

    }

    private void getUIObjects()
    {
        llAthlete = (LinearLayout)findViewById(R.id.llAthlete);
        llTherapist = (LinearLayout)findViewById(R.id.llTherapist);
        lblDisplay = (TextView)findViewById(R.id.txt_label);
        lblChoose = (TextView)findViewById(R.id.txt_choose);

        ImageView img = (ImageView)findViewById(R.id.img) ;
        pulseAnimation = PulseAnimation.create().with(img)
                .setDuration(800)
                .setRepeatCount(PulseAnimation.INFINITE)
                .setRepeatMode(PulseAnimation.REVERSE);
    }

    private void initEvent()
    {
        llAthlete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, AthleteLoginActivity.class);
                startActivity(intent);
            }
        });

        llTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, TherapistLoginActivity.class);
                startActivity(intent);
            }
        });

        pulseAnimation.start();
    }

    private void gameOver() {
        pulseAnimation.start();
    }


    private void checkTutorial()
    {
        if (pref.isPreviouslyStarted() == false)
        {
            pref.setIsPrevstarted();
            Intent newIntent = new Intent(StartActivity.this, TutorialActivity.class);
            newIntent.putExtra("isAvatarLoaded", false);
            startActivity(newIntent);
            return;
        }
    }

    private void updateControlStyle()
    {

        ParseUser user = ParseUser.getCurrentUser();

        if (user != null || pref.isLoggedIn() == true) {
//            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//            if (installation.getParseUser("user") == null)
//            {
//                installation.put("user", user);
//                installation.saveInBackground();
//            }

            lblDisplay.setText("Logging");
            llAthlete.animate()
                    .alpha(0.0f)
                    .setDuration(2000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });

            llTherapist.animate()
                    .alpha(0.0f)
                    .setDuration(2000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                        }
                    });
            lblChoose.animate()
                    .alpha(0.0f)
                    .setDuration(2000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                        }
                    });
        } else
        {
            lblDisplay.setText(R.string.label_status_first);
            llAthlete.setVisibility(View.VISIBLE);
            llTherapist.setVisibility(View.VISIBLE);
            lblChoose.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        ParseUser user = ParseUser.getCurrentUser();

        if (user != null || pref.isLoggedIn() == true) {
            //NotificationUtils.sendDeviceToken(pref.getDeviceToken());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent newIntent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(newIntent);
                    finish();
                }
            }, 2000);
        }

        /*
        if (pref.getDeviceToken() == null) {
            oldMillis = System.currentTimeMillis();
            getInstanceIdToken();
            registBroadcastReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickStartPreferences.REGISTRATION_COMPLETE));
        }
        else
        {
            ParseUser user = ParseUser.getCurrentUser();

            if (user != null || pref.isLoggedIn() == true) {
                //NotificationUtils.sendDeviceToken(pref.getDeviceToken());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent newIntent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(newIntent);
                        finish();
                    }
                }, 2000);
            }
        }
        */

        updateControlStyle();
    }

    @Override
    public void onPause()
    {
        //if (pref.getDeviceToken() == null)
            //LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void registBroadcastReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(QuickStartPreferences.REGISTRATION_COMPLETE)){
                    String token = intent.getStringExtra("token");
                    pref.setDeviceToken(token);

                    ParseUser user = ParseUser.getCurrentUser();

                    if (user != null || pref.isLoggedIn() == true) {
                        NotificationUtils.sendDeviceToken(token);

                        long currMillis = System.currentTimeMillis();
                        long diffOld = currMillis - oldMillis;
                        if (diffOld >= 2000)
                        {
                            Intent newIntent = new Intent(StartActivity.this, MainActivity.class);
                            startActivity(newIntent);
                            finish();
                        } else
                        {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent newIntent = new Intent(StartActivity.this, MainActivity.class);
                                    startActivity(newIntent);
                                    finish();
                                }
                            }, diffOld);
                        }

                    }
                }
            }
        };
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }
}
