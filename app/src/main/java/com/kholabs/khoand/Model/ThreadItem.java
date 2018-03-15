package com.kholabs.khoand.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Adapter.MessageAdapter;
import com.kholabs.khoand.Interface.FetchMsgCallBack;
import com.kholabs.khoand.Interface.FindOwnerCallBack;
import com.kholabs.khoand.Interface.NotifyListener;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LiCholMin on 1/6/2018.
 */

public class ThreadItem {
    private Context mContext;
    private String name;
    private Date createdAt;
    private String timeAgo;
    private String latestMsg;
    private Bitmap avatar;
    private ParseUser pUser;
    private ParseObject thread;
    private ParseObject message;
    private long id;
    private Object lockObj;
    private int pos;
    private NotifyListener listener;

    public boolean isAvatarLoaded;

    public void setName(String _data) { name = _data; }
    public void setTimeAgo(String _data) { timeAgo = _data; }
    public void setCreatedAt(Date _data) { createdAt = _data; }
    public void setLatestMsg(String _data) { latestMsg = _data; }
    public void setAvatar(Bitmap _data) { avatar = _data; }
    public void setpUser(ParseUser _data) {pUser = _data; }
    public void setId(long _data) {id = _data;}
    public void setThread(ParseObject _data) { thread = _data; }
    public void setMessage(ParseObject _data) { message = _data; }
    public void setPos(int _data) { pos = _data; }

    public String getName() { return name; }
    public String getLatestMsg() { return latestMsg; }
    public Date getCreatedAt() { return createdAt; }
    public String getTimeAgo() { return timeAgo; }
    public Bitmap getAvatar() { return avatar; }
    public ParseUser getpUser() { return pUser; }
    public ParseObject getThread() { return thread; }
    public ParseObject getMessage() { return message; }
    public long getId() { return id; }
    public int getPos() { return pos; }
    public Date getDate()
    {
        if (timeAgo != null)
            return MyUtils.convertDateFromString(timeAgo);

        return null;
    }

    public ThreadItem()
    {
        name = "";
        timeAgo = "";
        latestMsg = "";
        avatar = null;
        pUser = null;
        thread = null;
        message = null;
        lockObj = new Object();
        isAvatarLoaded = false;
    }

    public void initThreadItem()
    {
        findOwnerWithCompletion(thread, new FindOwnerCallBack() {
            @Override
            public void OnFindCompletion(boolean isFind, ParseUser user) {
                if (isFind) {
                    pUser = user;
                    loadThread();
                }
            }
        });
    }

    public synchronized void loadThread()
    {
        if (pUser == null)
            return;

        synchronized (lockObj)
        {
            name = pUser.getString("name");
            listener.OnAddItem(this, pos);

            GetAvatarPhotoFromData(pUser);
            if (message == null)
            {
                fetchLatestMessageWithCompletion(new FetchMsgCallBack() {
                    @Override
                    public void OnFetchCompletion(boolean isFetch) {
                        if (isFetch)
                            updateMessageContents();
                    }
                });
            }

            if (message != null)
                updateMessageContents();
        }
    }

    public synchronized void updateMessageContents()
    {
        synchronized (lockObj)
        {
            if (message == null) return;
            long createTime = message.getCreatedAt().getTime();
            setCreatedAt(message.getCreatedAt());
            String timeago = TimeAgo.using(createTime);
            timeago = timeago.replace("about an hour ago", "1h");
            timeago = timeago.replace(" days ago", "d");
            timeago = timeago.replace(" hours ago", "h");
            timeago = timeago.replace(" minutes ago", "m");
            setTimeAgo(timeago);
            setLatestMsg(message.getString("message"));

            listener.OnUpdatedItem(ThreadItem.this, pos);
        }
    }

    public void fetchLatestMessageWithCompletion(FetchMsgCallBack msgCallBack) {
        if (message != null)
            msgCallBack.OnFetchCompletion(true);

        if (pUser == null) {
            findOwnerWithCompletion(thread, new FindOwnerCallBack() {
                @Override
                public void OnFindCompletion(boolean isFind, ParseUser user) {

                }
            });
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.setLimit(1);
        query.whereEqualTo("thread", thread);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0) {
                    msgCallBack.OnFetchCompletion(false);
                    return;
                }

                ParseObject msg = objects.get(0);
                message = msg;
                msgCallBack.OnFetchCompletion(true);
                return;
            }
        });

    }

    public void findOwnerWithCompletion(ParseObject result, FindOwnerCallBack mCallBack) {
        ParseUser threadOwner = result.getParseUser("threadOwner");
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (!threadOwner.getObjectId().equals(currentUser.getObjectId())) {
            mCallBack.OnFindCompletion(true, threadOwner);
            return;
        }
        else
        {
            List<String> people = result.getList("people");
            if (people == null || people.size() == 0) {
                mCallBack.OnFindCompletion(false, null);
                return;
            }

            String objId = people.get(0);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.setLimit(1);
            query.whereEqualTo("objectId", objId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        mCallBack.OnFindCompletion(false, null);
                        return;
                    }

                    mCallBack.OnFindCompletion(true, objects.get(0));
                    return;

                }
            });
        }
    }

    public void GetAvatarPhotoFromData(ParseUser user)
    {
        if (getAvatar() != null)
            return;

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null || object == null)
                    return;
                ParseFile pFile = object.getParseFile("avatar");

                if (pFile == null)
                    return;

                pFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null || data == null)
                            return;

                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        setAvatar(bmp);
                        bmp = null;

                        listener.OnUpdatedItem(ThreadItem.this, pos);
                    }
                });

            }
        });
    }

}
