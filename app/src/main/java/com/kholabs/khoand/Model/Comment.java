package com.kholabs.khoand.Model;

import android.graphics.Bitmap;
import android.net.Uri;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;

public class Comment implements Serializable{
    public boolean isTherapist;
    public boolean verifedTherapist;
    public boolean isEditingMode;
    public boolean isImageLoaded;
    public boolean isPostImageLoaded;

    private String name;
    private String content;
    private String date;
    private ParseUser pUser;
    private ParseObject origData;
    private Bitmap avatar;
    private Bitmap postImage;
    private String postVideoUri;

    public void setName(String _data) { name = _data; }
    public void setContent(String _data) { content = _data; }
    public void setDate(String _data) { date = _data; }
    public void setpUser(ParseUser _data) { pUser = _data; }
    public void setOrigData(ParseObject _data) { origData = _data; }
    public void setAvatar(Bitmap _data) { avatar = _data; }
    public void setPostImage(Bitmap _data) { postImage = _data; }
    public void setPostVideoPath(String _data) { postVideoUri = _data; }

    public String getName() { return name; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public ParseUser getpUser() { return pUser; }
    public ParseObject getOrigData() { return origData; }
    public Bitmap getAvatar() { return avatar; }
    public Bitmap getPostImage() { return postImage; }
    public String getPostVideoPath() { return postVideoUri; }

    public Comment()
    {
        name = "";
        content = "";
        date = "";
        isTherapist = false;
        verifedTherapist = false;
        isEditingMode = false;
        isImageLoaded = false;
        isPostImageLoaded = false;
        content = "";
        date = "";
        postImage = null;
        postVideoUri = "";
        avatar = null;
    }
}
