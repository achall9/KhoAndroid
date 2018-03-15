package com.kholabs.khoand.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.kholabs.khoand.Interface.FeedNotifyListener;
import com.kholabs.khoand.Interface.NotifyListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.List;

/**
 * Created by LiCholMin on 12/17/2017.
 */

public class Feed implements Serializable, Cloneable {
    private String name;
    private String date;
    private String content;
    private String howlong;
    private String worst;
    private int nSupportCnt;
    private int nCommentCnt;
    private Bitmap avatar;
    private Bitmap postImage;
    private String postVideoUrl;
    private long id;

    public boolean isMine;
    public boolean isNotification;
    private ParseUser pUser;
    private ParseObject origData;
    private ParseObject pAnswer;

    public boolean isImageLoaded;
    public boolean isSupport;
    public boolean isPostImageLoaded;

    public void setName(String _data) { name = _data; }
    public void setDate(String _data) { date = _data; }
    public void setContent(String _data) { content = _data; }
    public void setHowlong(String _data) { howlong = _data; }
    public void setWorst(String _data) { worst = _data; }
    public void setpUser(ParseUser _data) { pUser = _data; }
    public void setnSupportCnt(int _data) { nSupportCnt = _data; }
    public void setnCommentCnt(int _data) {nCommentCnt = _data; }
    public void setOrigData(ParseObject _data) { origData = _data; }
    public void setAvatar(Bitmap _data) { avatar = _data; }
    public void setPostImage(Bitmap _data) { postImage = _data; }
    public void setPostVideoUrl(String _data) { postVideoUrl = _data; }
    public void setpAnswer(ParseObject _data) { pAnswer = _data; }
    public void setId(long _data) { id = _data;}

    public String getName() { return name; }
    public String getDate() { return date; }
    public String getContent() { return content; }
    public String getHowlong() { return howlong; }
    public String getWorst() { return worst; }
    public ParseUser getpUser() { return pUser; }
    public int getnSupportCnt() { return nSupportCnt; }
    public int getnCommentCnt() { return nCommentCnt; }
    public ParseObject getOrigData() { return origData; }
    public Bitmap getAvatar() { return avatar; }
    public Bitmap getPostImage() { return postImage; }
    public String getPostVideoUrl() { return postVideoUrl; }
    public ParseObject getpAnswer() { return  pAnswer; }
    public long getId() { return id;}

    public int updateCnt;

    public Feed()
    {
        isMine = false;
        isNotification = false;
        isSupport = false;
        isImageLoaded = false;
        nSupportCnt = -1;
        nCommentCnt = -1;
        name = "";
        date = "";
        content = "";
        howlong = "";
        worst = "";

        updateCnt = 0;
        postImage = null;
        postVideoUrl = "";
        pAnswer = null;
        avatar = null;
    }

    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }



}
