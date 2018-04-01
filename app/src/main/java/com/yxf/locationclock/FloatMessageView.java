package com.yxf.locationclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by jk on 2017/3/5.
 */
public class FloatMessageView extends LinearLayout {
    public static int viewWidth,viewHeight;
    public FloatMessageView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_message, this);
        View v = findViewById(R.id.float_message);
        viewWidth=v.getLayoutParams().width;
        viewHeight=v.getLayoutParams().height;
    }
}
