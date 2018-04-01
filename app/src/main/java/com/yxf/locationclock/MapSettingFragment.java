package com.yxf.locationclock;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jk on 2017/3/12.
 */
public class MapSettingFragment extends DialogFragment {
    View view=null;
    private int position=0;
    MapFragment mapFragment=null;
    @Override
    public void onStart() {
        super.onStart();
        mapFragment.initMapSetting(position);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.position = getArguments().getInt("position");
        return super.onCreateDialog(savedInstanceState);
    }
    public static MapSettingFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt("position",position);
        MapSettingFragment fragment = new MapSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = new FloatMapView(getContext());
        FragmentManager fm=getChildFragmentManager();
        mapFragment=new MapFragment();
        fm.beginTransaction().add(R.id.setting_map, mapFragment).commit();
        //mapFragment.initMapSetting(position);
        TextView confirm = (TextView) view.findViewById(R.id.float_map_setting_confirm);
        TextView cancel = (TextView) view.findViewById(R.id.float_map_setting_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.saveChangedLocation(position);
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });



        return view;
    }
}
