package com.kholabs.khoand.Utils.ParseUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Ravi on 01/06/15.
 */
public class PrefManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared pref file name
    private static final String PREF_NAME = "KhoAndroid";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Badge Count of Notifications
    public static final String BADGE_CNT = "badgeCnt";

    public static final String IS_PREVSTARTED = "prevStarted";

    public static final String DEVICE_TOKEN = "deviceToken";

    // Constructor
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String email) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();
    }

    public void logOutSession() {
        editor.putBoolean(IS_LOGIN, false);
        editor.putString(KEY_EMAIL, "");
        editor.commit();
    }

    public void setDeviceToken(String token)
    {
        editor.putString(DEVICE_TOKEN, token);
        editor.commit();
    }

    public String getDeviceToken() { return pref.getString(DEVICE_TOKEN, null); }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void setNotificationBadges(int nCount) {
        editor.putInt(BADGE_CNT, nCount);
        editor.commit();
    }

    public int getNotificationbadges() { return pref.getInt(BADGE_CNT, 0); }

    public void logout() {
        editor.putBoolean(IS_LOGIN, false);
        editor.commit();
    }

    public boolean isPreviouslyStarted() {return pref.getBoolean(IS_PREVSTARTED, false);}

    public void setIsPrevstarted()
    {
        editor.putBoolean(IS_PREVSTARTED, true);
        editor.commit();
    }
}