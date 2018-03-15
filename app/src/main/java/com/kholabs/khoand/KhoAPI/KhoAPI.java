package com.kholabs.khoand.KhoAPI;

import android.content.Context;
import android.os.AsyncTask;

import com.kholabs.khoand.Interface.FindThreadCallBack;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/19/2018.
 */

public class KhoAPI {
    public static Context mContext = null;

    public static void initialize(Context _context)
    {
        mContext = _context;
    }

    public static AsyncTask<ParseUser, Void, ParseUser> findOwnerWithCompletion(FindThreadCallBack callback) {
        return new findThreadFromUser(mContext, callback).execute();
    }

    public static class findThreadFromUser extends AsyncTask<ParseUser, Void, ParseUser> {

        private FindThreadCallBack mCallBack;
        private Context mContext;
        public Exception mException;


        public findThreadFromUser(Context context, FindThreadCallBack callback) {
            mCallBack = callback;
            mContext = context;
        }

        @Override
        protected ParseUser doInBackground(ParseUser... users) {
            ParseUser result = users[0];
            return result;
        }

        @Override
        protected void onPostExecute(ParseUser result) {
            ParseQuery<ParseObject> owner_us = ParseQuery.getQuery("Threads");
            owner_us.whereEqualTo("threadOwner", ParseUser.getCurrentUser());
            owner_us.whereContains("people", result.getObjectId());

            ParseQuery<ParseObject> owner_them = ParseQuery.getQuery("Threads");
            owner_them.whereEqualTo("threadOwner", result);
            owner_them.whereContains("people", ParseUser.getCurrentUser().getObjectId());

            List<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(owner_us);
            queries.add(owner_them);

            ParseQuery<ParseObject> query = ParseQuery.or(queries);
            query.orderByDescending("updatedAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        mCallBack.onCompletition(null);
                        return;
                    }

                    ParseObject thread = objects.get(0);
                    mCallBack.onCompletition(thread);
                }
            });
        }
    }

}
