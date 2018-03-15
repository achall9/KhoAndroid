package com.kholabs.khoand.Interface;

import com.parse.ParseUser;

/**
 * Created by Aladar-PC2 on 2/18/2018.
 */

public interface FindOwnerCallBack {
    public void OnFindCompletion(boolean isFind, ParseUser user);
}
