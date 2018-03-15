package com.kholabs.khoand.Model;

import android.graphics.Bitmap;

import com.parse.ParseUser;

import java.io.Serializable;

/**
 * Created by Aladar-PC2 on 1/29/2018.
 */

public class SearchUser  implements Serializable{
    private String name;
    private String type;
    private Bitmap avatar;
    private ParseUser pUser;
    public boolean isAvatarLoaded;

    public void setName(String _data) { this.name = _data; }
    public void setType(String _data) { this.type = _data; }
    public void setAvatar(Bitmap _data) { this.avatar = _data; }
    public void setpUser(ParseUser _data) { this.pUser = _data; }

    public String getName() { return name; }
    public String getType() { return type; }
    public Bitmap getAvatar() { return avatar; }
    public ParseUser getpUser() { return pUser; }

    public SearchUser()
    {
        isAvatarLoaded = false;
        name = "";
        type = "";
        avatar = null;
        pUser = null;
    }
}
