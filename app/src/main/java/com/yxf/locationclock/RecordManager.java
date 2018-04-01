package com.yxf.locationclock;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jk on 2017/3/2.
 */


public class RecordManager {
    private List<RecordLocation> list;
    private static Context context;
    private static RecordManager recordManager;
    private RecordManager(){
        list=new ArrayList<RecordLocation>();
        BufferedReader reader = null;
        try {
            InputStream inputStream=context.openFileInput("data");
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder=new StringBuilder();
            String line=null;
            while ((line = reader.readLine())!=null) {
                stringBuilder.append(line);
            }
            JSONArray array= (JSONArray) new JSONTokener(stringBuilder.toString()).nextValue();
            for(int i=0;i<array.length();i++) {
                list.add(new RecordLocation(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static RecordManager instance(Context c){
        context=c;
        if (recordManager == null) {
            recordManager=new RecordManager();
        }
        return recordManager;
    }
    public List getList(){
        return list;
    }
    public RecordManager saveList(){
        JSONArray array=new JSONArray();
        for (int i=0;i<list.size();i++) {
            try {
                array.put(list.get(i).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Writer writer = null;
        try {
            OutputStream outputStream = context.openFileOutput("data", Context.MODE_PRIVATE);
            writer=new OutputStreamWriter(outputStream);
            writer.write(array.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writer!=null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return this;
    }
    public static JSONObject toJSON(RecordLocation location) {
        JSONObject js=new JSONObject();
        try {
            /*js.put("isOn", location.isOn);
            js.put("latitude", location.latitude);
            js.put("longitude", location.longitude);
            js.put("address", location.address);
            js.put("comment", location.comment);
            js.put("startDate", location.startDate);*/
            js.put("RecordLocation", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }
    public static RecordLocation getRecordLocation(JSONObject js){
        try {
            //return new RecordLocation(js.getDouble("latitude"),js.getDouble("longitude"),js.getBoolean("isOn"));
            return (RecordLocation) js.get("RecordLocation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
