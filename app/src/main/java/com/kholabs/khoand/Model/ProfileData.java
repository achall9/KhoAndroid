package com.kholabs.khoand.Model;

import java.util.Date;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/9/2018.
 */

public class ProfileData {
    private String name;
    private String phone;
    private Date dob;
    private String bio;
    private List<String> sportArr;

    public void setName(String _data) {name = _data;}
    public void setPhone(String _data) {phone = _data;}
    public void setDob(Date _data) {dob = _data;}
    public void setBio(String _data) {bio = _data;}
    public void setSportArr(List<String> _data) {sportArr = _data;}

    public String getName() {return name;}
    public String getPhone() {return phone;}
    public Date getDob() {return dob;}
    public String getBio() {return bio;}
    public List<String> getSportArr() {return sportArr;}
}
