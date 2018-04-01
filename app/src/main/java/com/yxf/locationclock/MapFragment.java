package com.yxf.locationclock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

/**
 * Created by jk on 2017/2/28.
 */
public class MapFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, BaiduMap.OnMapClickListener, BDLocationListener, View.OnClickListener {
    BaiduMap baiduMap;
    MapView mapView;
    Marker mMarker;
    LocationClient mLocationClient;
    MyOrientationChangeListener myOrientationChangeListener;
    BitmapDescriptor mLocationBitMap;

    CheckBox satellite;
    CheckBox traffic;

    TextView myLocationView;
    Button addMyLocation;
    Button addMarkLocation;
    EditText searchEdit;

    private Handler handler = new Handler();

    private LatLng markLatLng;
    private LatLng myLatlng;

    private float mIconX;
    private boolean isSetMyLocation = true;

    private String myAddress = null, markAddress = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        SDKInitializer.initialize(getActivity().getApplicationContext());
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) v.findViewById(R.id.baidu_mapView);
        baiduMap = mapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomBy(3.f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMyLocationEnabled(true);//设置显示自己的位置
        //设置图标定位....
        mLocationBitMap = BitmapDescriptorFactory.fromResource(R.mipmap.arrow);
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, mLocationBitMap);
        baiduMap.setMyLocationConfigeration(config);
        baiduMap.setOnMapClickListener(this);
        mLocationClient = new LocationClient(getContext());
        mLocationClient.registerLocationListener(this);

        satellite = (CheckBox) v.findViewById(R.id.satellite);
        traffic = (CheckBox) v.findViewById(R.id.traffic);
        myLocationView = (TextView) v.findViewById(R.id.my_location);
        addMyLocation = (Button) v.findViewById(R.id.add_my_location);
        addMarkLocation = (Button) v.findViewById(R.id.add_mark_location);
        searchEdit = (EditText) v.findViewById(R.id.fragment_map_search);
        Button searchBtn = (Button) v.findViewById(R.id.fragment_map_search_button);
        myLocationView.setOnClickListener(this);
        addMarkLocation.setOnClickListener(this);
        addMyLocation.setOnClickListener(this);
        satellite.setOnCheckedChangeListener(this);
        traffic.setOnCheckedChangeListener(this);
        searchBtn.setOnClickListener(this);
        initLocation();
        return v;
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);


        //初始化方向传感器监听器
        if (myOrientationChangeListener == null) {
            myOrientationChangeListener = new MyOrientationChangeListener(getContext());
        }
        myOrientationChangeListener.setOnOrientationListener(new MyOrientationChangeListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mIconX = x;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        if (myOrientationChangeListener != null) {
            myOrientationChangeListener.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (mLocationClient != null && !mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        if (myOrientationChangeListener != null) {//方向传感器
            myOrientationChangeListener.start();
        } else {
            myOrientationChangeListener = new MyOrientationChangeListener(getContext());
            myOrientationChangeListener.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setReenterTransition(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.satellite:
                baiduMap.setMapType(isChecked ? BaiduMap.MAP_TYPE_SATELLITE : BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.traffic:
                baiduMap.setTrafficEnabled(isChecked);
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markAddress = null;
        markLatLng = latLng;
        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_gcoding);
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);
        mMarker = (Marker) baiduMap.addOverlay(option);
        GeoCoder coder = GeoCoder.newInstance();
        coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult != null) {
                    if (reverseGeoCodeResult.getAddress() != null && reverseGeoCodeResult.getAddress().length() > 0) {
                        markAddress = reverseGeoCodeResult.getAddress();
                        if (markAddress != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), markAddress, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }
        });
        coder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        myLatlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        myAddress = bdLocation.getAddrStr();
        MyLocationData data = new MyLocationData.Builder()
                .direction(mIconX)
                .accuracy(bdLocation.getRadius())
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();
        baiduMap.setMyLocationData(data);//设置自己的位置信息
        if (isSetMyLocation) {
            //设置自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mLocationBitMap);
            baiduMap.setMyLocationConfigeration(config);
            LatLng latlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
            baiduMap.animateMapStatus(msu);
            isSetMyLocation = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location:
                isSetMyLocation = true;
                break;
            case R.id.add_my_location:
                if (myLatlng != null) {
                    showDialog(RecordMode.MY_LOCATION);
                }
                break;
            case R.id.add_mark_location:
                if (markLatLng != null) {
                    showDialog(RecordMode.MARK_LOCATION);
                }
                break;
            case R.id.fragment_map_search_button:
                Toast.makeText(getContext(),"点击搜索",Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void showDialog(final int mode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final EditText edit = new EditText(getContext());
                builder.setTitle("请设置备注")
                        .setView(edit)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (mode) {
                                    case RecordMode.MY_LOCATION:
                                        if (edit.getText().toString() != null && edit.getText().toString().length() > 0) {
                                            RecordManager.instance(getActivity()).getList().add(new RecordLocation(myLatlng, false, edit.getText().toString(), myAddress));
                                            RecordManager.instance(getActivity()).saveList();
                                        }
                                        break;
                                    case RecordMode.MARK_LOCATION:
                                        if (edit.getText().toString() != null && edit.getText().toString().length() > 0) {
                                            RecordManager.instance(getActivity()).getList().add(new RecordLocation(markLatLng, true, edit.getText().toString(), markAddress));
                                            RecordManager.instance(getActivity()).saveList();
                                        }
                                        break;
                                }

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    public void initMapSetting(int position) {
        addMyLocation.setVisibility(View.GONE);
        addMarkLocation.setVisibility(View.GONE);
        satellite.setVisibility(View.GONE);
        traffic.setVisibility(View.GONE);
        RecordLocation rl = (RecordLocation) RecordManager.instance(getActivity()).getList().get(position);
        LatLng latLng = new LatLng(rl.latitude, rl.longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_gcoding);
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);
        mMarker = (Marker) baiduMap.addOverlay(option);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.animateMapStatus(msu);
        isSetMyLocation=false;
    }

    public void saveChangedLocation(int position) {
        RecordLocation rl = (RecordLocation) RecordManager.instance(getActivity()).getList().get(position);
        if (markLatLng != null) {
            rl.latitude = markLatLng.latitude;
            rl.longitude = markLatLng.longitude;
            if (markAddress != null||markAddress.length()>0) {
                rl.address=markAddress;
            }else{
                rl.address = "未知位置";
            }
            RecordManager.instance(getActivity()).saveList();
        }

    }


    class RecordMode {
        public static final int MY_LOCATION = 0;
        public static final int MARK_LOCATION = 1;
    }

}
