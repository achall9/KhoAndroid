package com.kholabs.khoand.Interface;

/**
 * Created by Aladar-PC2 on 1/29/2018.
 */

public interface SaveMediaCallBack<T> {
    public void onSuccess(T object);
    public void onFailure(Exception e);
}
