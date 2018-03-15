package com.kholabs.khoand.Common;

/**
 * Created by Aladar-PC2 on 1/27/2018.
 */

public interface ResponseCallBack <T> {
    public void onCompleted(boolean isSuccess);

    public static final ResponseCallBack<Void> DoNothing = new   ResponseCallBack<Void>(){

        @Override
        public void onCompleted(boolean isSuccess) {

        }

    };
}