package com.kholabs.khoand.App;

import com.facebook.FacebookSdk;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Activity.StartActivity;
import com.kholabs.khoand.KhoAPI.KhoAPI;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.Model.ThreadItem;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ParseUtil.ParseUtils;
import com.kholabs.khoand.Utils.ParseUtil.PrefManager;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.leakcanary.LeakCanary;
import com.taplytics.sdk.Taplytics;
import com.wonderkiln.blurkit.BlurKit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aladar-PC2 on 1/12/2018.
 */

public class MyApp extends Application {
    public static final String APP_NAME = "KhoAnd";

    private static MyApp me;
    public static Context appContext;
    private Feed postFeed;
    private ParseObject msgThread;
    private ParseUser shareUser;
    private int fromParent = 0;
    private String ncObjectId = "";

    public PrefManager pref;
    private Activity mCurrentActivity = null;
    public static MyApp getInstance() {
        return me;
    }
    public static Context getContext()
    {
        return MyApp.appContext;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        //if (LeakCanary.isInAnalyzerProcess(this))
            //return;

        //LeakCanary.install(this);


        ParseUtils.registerParse(this);
        pref = new PrefManager(getApplicationContext());

        Taplytics.startTaplytics(this, getString(R.string.taplytics_sdk_key));

        BlurKit.init(this);

        me = this;
        MyApp.appContext = getApplicationContext();
        KhoAPI.initialize(getApplicationContext());

        postFeed = null;
        shareUser = null;
        msgThread = null;
        fromParent = 0;
    }

    public void setPostFeed(Feed _item) { this.postFeed = _item; }
    public Feed getPostFeed() { return postFeed; }

    public void setMsgThread(ParseObject _item) { this.msgThread = _item; }
    public ParseObject getMsgThread() { return msgThread; }

    public void setShareUser(ParseUser _user) { this.shareUser = _user; }
    public ParseUser getShareUser() { return shareUser; }

    public void setFromParent(int _data) { this.fromParent = _data; }
    public int getFromParent() { return fromParent; }

    public void setNcObjectId(String _data) { this.ncObjectId = _data; }
    public String getNcObjectId() { return this.ncObjectId; }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void goToStartActivity()
    {
        if (mCurrentActivity == null)
            return;

        Intent intent = new Intent(mCurrentActivity, StartActivity.class);
        mCurrentActivity.startActivity(intent);
        mCurrentActivity.finish();
    }

    public void clearBadges()
    {
        if (!isNetworkAvailable())
            return;
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put("badge", 0);
        currentInstallation.saveInBackground();

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassname());
        intent.putExtra("badge_count", 0);
        sendBroadcast(intent);
    }

    private String getLauncherClassname() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(intent.CATEGORY_LAUNCHER);
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }

        return null;
    }

    public void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity(), AlertDialog.THEME_HOLO_LIGHT );
        builder.setTitle(title);
        TextView myMsg = new TextView(this);
        myMsg.setText(message);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        /*

        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(Color.BLUE);
        txtTitle.setTextSize(20);
        builder.setCustomTitle(txtTitle);
        */
        builder.setCancelable(false);


        builder.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void alertDisplayer(String title, String message, String btnString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity(), AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(title);
        TextView myMsg = new TextView(this);
        myMsg.setText(message);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        /*

        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(Color.BLUE);
        txtTitle.setTextSize(20);
        builder.setCustomTitle(txtTitle);
        */
        builder.setCancelable(false);


        builder.setNeutralButton(btnString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
