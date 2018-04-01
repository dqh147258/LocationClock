package com.yxf.locationclock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by jk on 2017/2/5.
 */
public class MyOrientationChangeListener implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;

    private float lastX=0;

    public MyOrientationChangeListener(Context context){
        this.mContext=context;
    }

    public void start(){
        mSensorManager=(SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!=null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    public void stop(){
        mSensorManager.unregisterListener(this);

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX)>1.0) {
                if (mOnOrientationListener!=null){
                    mOnOrientationListener.onOrientationChanged(x);

                }
            }
            lastX=x;
        }
    }

    public void setmSensorManager(SensorManager mSensorManager) {
        this.mSensorManager=mSensorManager;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private  OnOrientationListener mOnOrientationListener;

    public void setOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener=mOnOrientationListener;
    }

    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }
}
