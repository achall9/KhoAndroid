package com.kholabs.khoand.Utils;

import android.os.Environment;

import com.kholabs.khoand.App.MyApp;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/9/2018.
 */

public class MyUtils {
    public static String JoinString(List<String> array)
    {
        if (array.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();

            for (String n : array) {
                nameBuilder.append(n).append(',');
            }

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            return nameBuilder.toString();
        } else {
            return "";
        }
    }

    public static List<String> getArrayString(String str)
    {
        List<String> items = Arrays.asList(str.split(","));
        return items;
    }

    public static Date convertDateFromString(String dateString)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return convertedDate;
    }

    public static Date convertDateFromStringMM(String dateString)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return convertedDate;
    }

    public static String getFormatStringFromDate(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a");
        String result = dateFormat.format(date);
        return result;
    }

    public static String getAppDataFolder(String subFolder){
        String strFilesDirectory =  Environment.getExternalStorageDirectory().getAbsolutePath();

        File appDirectory = null;
        boolean bExternalStorageAvailable = iSExternalStorageAvailable();
        if(bExternalStorageAvailable)
        {
            appDirectory  = new File(strFilesDirectory + "/" + MyApp.APP_NAME);
        }
        else
        {
            appDirectory = new File( MyApp.getContext().getFilesDir() , MyApp.APP_NAME);
        }

        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }
        String dir = appDirectory.getAbsolutePath();
        if (!dir.endsWith(File.separator)){
            dir +=File.separator;
        }

        dir+=subFolder;
        File f = new File(dir);
        if(!f.exists())
        {
            f.mkdir();
        }
        if (!dir.endsWith(File.separator)){
            dir +=File.separator;
        }

        return dir;
    }
    public static boolean iSExternalStorageAvailable()
    {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable;
    }
}
