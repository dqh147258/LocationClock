package com.yxf.locationclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    //key:W7qHoPmjmiGikaXim9eThHaF4TXtMMIn
    private MapFragment mapFragment=null;
    private ClockFragment clockFragment=null;
    private FragmentManager fm=null;
    private MyReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBroadCast();
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        fm=getSupportFragmentManager();

        if (savedInstanceState != null) {
            mapFragment = (MapFragment) fm.findFragmentByTag("mapFragment");
            clockFragment = (ClockFragment) fm.findFragmentByTag("clockFragment");
        }
        if(mapFragment==null){
            mapFragment =new MapFragment();
            fm.beginTransaction().add(R.id.activity_main_fragment, mapFragment).commit();
        }
        if (clockFragment == null) {
            clockFragment=new ClockFragment();
            fm.beginTransaction().add(R.id.activity_main_fragment,clockFragment).hide(clockFragment).commit();
        }
        RadioGroup radioGroup= (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(this);

    }
    private void initBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.yxf.location.notify");
        myReceiver=new MyReceiver();
        registerReceiver(myReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecordManager.instance(this).saveList();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_map:
                showFragment(mapFragment);
                break;
            case R.id.radio_clock:
                showFragment(clockFragment);
                clockFragment.notifyList();
                break;
        }
    }

    private void showFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        } else if (fragment == mapFragment) {
            fm.beginTransaction().hide(clockFragment).commit();
            fm.beginTransaction().show(mapFragment).commit();
        }else if(fragment==clockFragment){
            fm.beginTransaction().hide(mapFragment).commit();
            fm.beginTransaction().show(clockFragment).commit();
        }
    }




    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            clockFragment.notifyList();
        }
    }


}
