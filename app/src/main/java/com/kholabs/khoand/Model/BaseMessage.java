package com.kholabs.khoand.Model;

import android.graphics.Bitmap;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by LiCholMin on 1/6/2018.
 */

public class BaseMessage {
    public boolean isTherapist;
    public boolean verifedTherapist;
    public boolean isAvatarLoaded;

    private ParseUser sendUser;
    private ParseObject msgObject;
    private String name;
    private String message;
    private String timeAt;
    private Date createAt;
    private Bitmap avatar;
    private long id;

    public void setSendUser(ParseUser _data) { sendUser = _data; }
    public void setMsgObject(ParseObject _data) { msgObject = _data; }
    public void setAvatar(Bitmap _data) { avatar = _data; }
    public void setName(String _data) {name = _data;}
    public void setMessage(String _data) {message = _data; }
    public void setId(long _data) {id = _data;}
    public void setTimeAt(String _data) { timeAt = _data; }
    public void setCreateAt(Date _data) { createAt = _data; }

    public ParseUser getSendUser() { return sendUser; }
    public ParseObject getMsgObject() { return msgObject; }
    public Bitmap getAvatar() { return avatar; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public long getId() { return id; }
    public String getTimeAt() { return timeAt; }
    public Date getCreateAt() { return createAt; }

    public BaseMessage()
    {
        isTherapist = false;
        verifedTherapist = false;
        isAvatarLoaded = false;
        sendUser = null;
        msgObject = null;
        timeAt = "";
        createAt = null;
        name = "";
        message = "";
        avatar = null;
    }
}
