package com.kholabs.khoand.Extension;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.kholabs.khoand.Common.ResponseCallBack;
import com.kholabs.khoand.Model.ParseMedia;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Aladar-PC2 on 1/27/2018.
 */

public class ParseMediaUpload extends ParseObject {

    private ParseMedia pMedia;
    private Context mContext;

    public ParseMediaUpload() {}

    public ParseMediaUpload(Context context, Uri _uri1, Uri _uri2, String _imgKey, String _vidKey)
    {
        pMedia = new ParseMedia();
        this.mContext = context;
        pMedia.setImageKey(_imgKey);
        pMedia.setVideoKey(_vidKey);
        pMedia.setByteImage(convertImageToBytes(_uri1));
        pMedia.setByteVideo(convertVideoToBytes(_uri2));
    }

    private byte[] convertImageToBytes(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    private byte[] convertVideoToBytes(Uri uri){
        byte[] videoBytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(mContext, uri)));

            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoBytes;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Video.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void saveInBackgroundWithMedia() {
        if (pMedia.getByteImage() != null)
        {
            final ParseFile imageFile = new ParseFile(pMedia.getByteImage());
            imageFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // Handle success or failure here ...
                    if (e == null)
                    {
                        put(pMedia.getImageKey(), imageFile);
                        if (pMedia.getByteVideo() == null)
                        {
                            saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    return;
                                }
                            });

                        }
                        else
                        {
                            final ParseFile videoFile = new ParseFile("video.mp4", pMedia.getByteVideo(), "video/mp4");
                            videoFile.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // Handle success or failure here ...
                                    if (e == null) {
                                        put(pMedia.getVideoKey(), videoFile);
                                        saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                            }
                                        });
                                    }
                                }
                            }, new ProgressCallback() {
                                @Override
                                public void done(Integer percentDone) {

                                }
                            });
                        }

                    }

                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    // Update your progress spinner here. percentDone will be between 0 and 100.
                }
            });
        }

        if (pMedia.getByteVideo() != null)
        {
            final ParseFile videoFile = new ParseFile("video.mp4", pMedia.getByteVideo(), "video/mp4");
            videoFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // Handle success or failure here ...
                    if (e == null) {
                        put(pMedia.getVideoKey(), videoFile);
                        if (pMedia.getByteImage() != null)
                        {
                            final ParseFile imageFile = new ParseFile(pMedia.getByteImage());
                            imageFile.saveInBackground(new SaveCallback() {
                                public void done(ParseException e) {
                                    // Handle success or failure here ...
                                    if (e == null) {
                                        put(pMedia.getImageKey(), imageFile);
                                        saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                }
                            });
                        }
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {

                }
            });
        }


    }
}
