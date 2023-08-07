package sg.com.commute_solutions.bustracker.data;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Kyle on 8/9/16.
 */
public class Passenger {
    private String ezlinkCanId;
    private String name;
    private String gender;
    private String picUrl;
    private Boolean smsNOK;
    private String nokName;
    private String nokRelationship;
    private String nokContact;
    private String date;
//    private Boolean isDropOff;

    public Passenger(String ezlinkCanId, String name, String gender, String picUrl, Boolean smsNOK, String nokName, String nokRelationship, String nokContact) {
        this.ezlinkCanId = ezlinkCanId;
        this.name = name;
        this.gender = gender;
        this.picUrl = picUrl;
        this.smsNOK = smsNOK;
        this.nokName = nokName;
        this.nokRelationship = nokRelationship;
        this.nokContact = nokContact;
    }

    public Passenger(String ezlinkCanId, String name, String gender, String date) {
        this.ezlinkCanId = ezlinkCanId;
        this.name = name;
        this.gender = gender;
        this.date = date;
//        this.isDropOff = isDropOff;
    }

    public Passenger(String ezlinkCanId, String date) {
        this.ezlinkCanId = ezlinkCanId;
        this.date = date;
//        this.isDropOff = isDropOff;
    }

    public String getEzlinkCanId() {
        return ezlinkCanId;
    }

    public void setEzlinkCanId(String ezlinkCanId) {
        this.ezlinkCanId = ezlinkCanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Boolean getSmsNOK() {
        return smsNOK;
    }

    public void setSmsNOK(Boolean smsNOK) {
        this.smsNOK = smsNOK;
    }

    public String getNokName() {
        return nokName;
    }

    public void setNokName(String nokName) {
        this.nokName = nokName;
    }

    public String getNokRelationship() {
        return nokRelationship;
    }

    public void setNokRelationship(String nokRelationship) {
        this.nokRelationship = nokRelationship;
    }

    public String getNokContact() {
        return nokContact;
    }

    public void setNokContact(String nokContact) {
        this.nokContact = nokContact;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

//    public Boolean getDropOff() {
//        return isDropOff;
//    }
//
//    public void setDropOff(Boolean isDropOff) {
//        this.isDropOff = isDropOff;
//    }
}
