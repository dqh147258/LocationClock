package com.yxf.locationclock;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.List;

public class ClockSettingActivity extends AppCompatActivity implements View.OnClickListener {
    FragmentManager fm = null;
    EditText comment = null;
    TextView address = null;
    EditText limits = null;
    TimePicker startTimePicker = null;
    TimePicker endTimePicker = null;
    Button startMap = null;

    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = this.getIntent();
        position = i.getIntExtra("position", 0);
        setContentView(R.layout.activity_clock_setting);
        fm = getSupportFragmentManager();
        initView();
    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();

    }

    private void initView() {
        comment = (EditText) findViewById(R.id.setting_comment);
        address = (TextView) findViewById(R.id.setting_address);
        limits = (EditText) findViewById(R.id.setting_limits);
        startTimePicker = (TimePicker) findViewById(R.id.setting_start_time);
        endTimePicker = (TimePicker) findViewById(R.id.setting_end_time);
        startMap = (Button) findViewById(R.id.setting_start_map);
        RecordLocation rl = (RecordLocation) RecordManager.instance(getApplicationContext()).getList().get(position);
        comment.setText(rl.comment);
        address.setText(rl.address);
        limits.setText((int)(rl.limits / 0.00001) + "");
        int hour = rl.startDate.getHours();
        int minute = rl.startDate.getMinutes();
        startTimePicker.setCurrentHour(hour);
        startTimePicker.setCurrentMinute(minute);
        hour = rl.endDate.getHours();
        minute = rl.endDate.getMinutes();
        endTimePicker.setCurrentHour(hour);
        endTimePicker.setCurrentMinute(minute);


        startMap.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("是否保存?");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List list = RecordManager.instance(ClockSettingActivity.this).getList();
                        RecordLocation location = (RecordLocation) list.get(position);
                        location.comment = comment.getText().toString();
                        if (limits.getText().toString() != null)
                            location.limits = ((int)(double)Double.valueOf(limits.getText().toString())+0.5) * 0.00001;
                        location.setStartDate(startTimePicker.getCurrentHour(), startTimePicker.getCurrentMinute());
                        location.setEndDate(endTimePicker.getCurrentHour(), endTimePicker.getCurrentMinute());
                        RecordManager.instance(ClockSettingActivity.this).saveList();
                        finish();
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
                return false;
            //break;
            default:
                return super.onKeyDown(keyCode, event);
            //break;
        }

    }

    @Override
    public void onClick(View v) {
        MapSettingFragment dialog = MapSettingFragment.newInstance(position);
        dialog.show(fm, "map_setting");

    }
}
