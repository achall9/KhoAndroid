package com.kholabs.khoand.Model;

/**
 * Created by Aladar-PC2 on 1/27/2018.
 */

public class ParseMedia {
    private String imageKey;
    private String videoKey;
    private byte[] byteImage;
    private byte[] byteVideo;

    public void setImageKey(String _key) {imageKey = _key;}
    public void setVideoKey(String _key) {videoKey = _key;}
    public void setByteImage(byte[] _data) {byteImage = _data;}
    public void setByteVideo(byte[] _data) {byteVideo = _data;}

    public String getImageKey() {return imageKey;}
    public String getVideoKey() {return videoKey;}
    public byte[] getByteImage() {return byteImage;}
    public byte[] getByteVideo() {return byteVideo;}
}
