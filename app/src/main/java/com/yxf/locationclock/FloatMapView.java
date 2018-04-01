package com.yxf.locationclock;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by jk on 2017/3/12.
 */
public class FloatMapView extends RelativeLayout {
    public static int viewWidth,viewHeight;
    public FloatMapView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_map_setting, this);
        View view = findViewById(R.id.setting_map);
        viewWidth= view.getLayoutParams().width;
        viewHeight=view.getLayoutParams().height;
    }
}
