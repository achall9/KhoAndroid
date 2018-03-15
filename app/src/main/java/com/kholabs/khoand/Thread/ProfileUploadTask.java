package com.kholabs.khoand.Thread;

import android.content.Context;
import android.os.AsyncTask;

import com.kholabs.khoand.Interface.UploadProfileCallBack;
import com.kholabs.khoand.Model.ProfileData;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/9/2018.
 */

public class ProfileUploadTask extends AsyncTask<ProfileData, Void, ProfileData> {
    private UploadProfileCallBack mCallBack;
    public Exception mException;

    public ProfileUploadTask(UploadProfileCallBack _callBack)
    {
        mCallBack = _callBack;
    }



    @Override
    protected ProfileData doInBackground(ProfileData... profileData) {
        ProfileData result = profileData[0];
        return result;
    }

    @Override
    protected void onPostExecute(ProfileData result) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", result.getName());
        user.put("phone", result.getPhone());
        user.put("dob", result.getDob());
        user.put("bio", result.getBio());

    /*  check sports items:     */
        List<String> sportsArr = result.getSportArr();

        if (sportsArr != null)
            user.put("sports", sportsArr);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    mCallBack.onFailure(e);
                    return;
                }

                mCallBack.onSuccess();

            }
        });
    }
}
