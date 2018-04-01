package com.yxf.locationclock;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.List;

/**
 * Created by jk on 2017/3/5.
 */
public class DetectLocationService extends Service implements BDLocationListener {
    private static boolean isServiceOn=false;

    LocationClient mClient;

    FloatMessageView floatView;
    private Handler handler = new Handler();
    private MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceOn=true;
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,i,0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("LocationClock")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Service 运行中...")
                .setContentIntent(pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification notification=builder.build();
            startForeground(1,notification);
        }
        showMessage("Service Start");
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setScanSpan(4000);
        mClient = new LocationClient(getApplicationContext());
        mClient.setLocOption(option);
        mClient.registerLocationListener(this);
        mClient.start();
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Double latitude = bdLocation.getLatitude(), longitude = bdLocation.getLongitude();
        List list = RecordManager.instance(getApplicationContext()).getList();
        for (int i = 0; i < list.size(); i++) {
            RecordLocation location = (RecordLocation) list.get(i);
            if (location.isClockOn()) {
                Double mLatitude=location.latitude;
                Double mLongitude=location.longitude;
                Double limits=location.limits;
                double result=Math.sqrt((mLatitude-latitude)*(mLatitude-latitude)*1.21+(mLongitude-longitude)*(mLongitude-longitude));
                boolean isMatch=result<limits;
                if (isMatch) {
                    showFloatMessage(location);
                    location.isOn=false;
                    Intent in = new Intent("com.yxf.location.notify");
                    sendBroadcast(in);
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        showMessage("Service Destroy");
        isServiceOn=false;
        if (mClient != null&&mClient.isStarted()) {
            mClient.stop();
        }
        if (floatView != null) {
            removeFloatMessage();
        }
    }
    private void showMessage(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    public static boolean getServiceState(){
        return isServiceOn;
    }
    public void showFloatMessage(RecordLocation location) {
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int screenW=manager.getDefaultDisplay().getWidth();
        int screenH=manager.getDefaultDisplay().getHeight();
        if (floatView == null) {
            floatView = new FloatMessageView(getApplicationContext());
            WindowManager.LayoutParams params=new WindowManager.LayoutParams();
            //params.x=screenW/2-FloatMessageView.viewWidth/2;
            //params.y=screenH/2-FloatMessageView.viewHeight/2;
            params.type=Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                    WindowManager.LayoutParams.TYPE_TOAST:WindowManager . LayoutParams.TYPE_PHONE;
            params.gravity= Gravity.CENTER;
            params.width=FloatMessageView.viewWidth;
            params.height=FloatMessageView.viewHeight;
            manager.addView(floatView,params);
            initView(location);
            playMusic();
        }
    }

    private void initView(RecordLocation location) {
        Button confirmBtn = (Button) floatView.findViewById(R.id.float_message_confirm);
        TextView comment = (TextView) floatView.findViewById(R.id.float_message_comment);
        TextView address = (TextView) floatView.findViewById(R.id.float_message_address);
        comment.setText(location.comment);
        address.setText(location.address);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFloatMessage();
            }
        });
    }

    public void removeFloatMessage() {
        if (floatView != null) {
            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            manager.removeView(floatView);
            floatView=null;
            stopMusic();
        }
    }
    public void playMusic() {
        stopMusic();
        player = MediaPlayer.create(getApplicationContext(), R.raw.faruxue);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopMusic();
            }
        });
    }
    public void stopMusic() {
        if (player != null) {
            player.release();
            player=null;
        }
    }
}
