package com.yxf.locationclock;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jk on 2017/3/2.
 */
public class RecordLocation implements Serializable{
    public double latitude,longitude;
    public boolean isOn;
    public Date startDate;
    public Date endDate;
    public String comment="未备注";
    public String address = "暂无地址信息";
    public double limits=0.00300;

    public static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    public RecordLocation(LatLng latLng, boolean isOn) {
        this(latLng.latitude, latLng.longitude, isOn);
    }

    public RecordLocation(double latitude, double longitude, boolean isOn, String comment, String address) {
        this(latitude, longitude, isOn);
        if (comment != null) {
            this.comment=comment;
        }
        if (address != null) {
            this.address=address;
        }
    }

    public RecordLocation(JSONObject jsonObject) throws JSONException, ParseException {
        this.isOn = jsonObject.getBoolean("isOn");
        this.address = jsonObject.getString("address");
        this.comment = jsonObject.getString("comment");
        this.endDate = format.parse(jsonObject.getString("endDate"));
        this.startDate = format.parse(jsonObject.getString("startDate"));
        this.latitude = jsonObject.getDouble("latitude");
        this.longitude = jsonObject.getDouble("longitude");
        this.limits = jsonObject.getDouble("limits");
    }
    public RecordLocation(LatLng latLng, boolean isOn, String comment, String address) {
        this(latLng,isOn);
        this.comment=comment;
        this.address=address;
    }

    public RecordLocation(double latitude, double longitude, boolean isOn) {
        this.latitude=latitude;
        this.longitude=longitude;
        this.isOn=isOn;
        try {
            startDate = format.parse("00:00");
            endDate = format.parse("23:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public JSONObject toJSON() throws JSONException {
        JSONObject js = new JSONObject();
        js.put("latitude", this.latitude);
        js.put("longitude", this.longitude);
        js.put("isOn", this.isOn);
        js.put("address", this.address);
        js.put("startDate", format.format(this.startDate));
        js.put("endDate", format.format(this.endDate));
        js.put("limits", this.limits);
        js.put("comment", this.comment);
        return js;
    }

    public RecordLocation setStartDate(int hour, int minute) {
        try {
            startDate = format.parse(hour+ ":" + minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public RecordLocation setEndDate(int hour, int minute) {
        try {
            endDate = format.parse(hour + ":" + minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public static Date getNowDate() {
        Date date=new Date(System.currentTimeMillis());
        String str = format.format(date);
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Date dateFormat(String str){
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return null;
    }
    public boolean isClockOn(){
        if (isOn) {
            if (startDate.getTime() < getNowDate().getTime() && endDate.getTime() > getNowDate().getTime()) {
                return true;
            } else if (startDate.getTime() > endDate.getTime()) {
                if (startDate.getTime() < getNowDate().getTime() && getNowDate().getTime() < dateFormat("24:00").getTime()) {
                    return true;
                }
                if (dateFormat("00:00").getTime() < getNowDate().getTime() && getNowDate().getTime() < endDate.getTime()) {
                    return true;
                }
            }
        }
        return false;
    }
}
