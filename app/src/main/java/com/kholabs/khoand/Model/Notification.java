package com.kholabs.khoand.Model;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Aladar-PC2 on 2/21/2018.
 */

public class Notification {
    private String strTitle;
    private String strContent;
    private String strDate;
    private Date currDate;
    private HashMap<String, String> data;

    public void setStrTitle(String _data) { strTitle = _data; }
    public void setStrContent(String _data) { strContent = _data; }
    public void setStrDate(String _data) { strDate = _data; }
    public void setCurrDate(Date _data) { currDate = _data; }
    public void setData(HashMap<String, String> _data) { data = _data; }

    public String getStrTitle() { return strTitle; }
    public String getStrContent() { return strContent; }
    public String getStrDate() { return strDate; }
    public Date getCurrDate() { return currDate; }
    public HashMap<String, String> getData() { return data; }
}
