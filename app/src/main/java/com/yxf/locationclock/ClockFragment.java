package com.yxf.locationclock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by jk on 2017/2/28.
 */
public class ClockFragment extends Fragment {

    ListView listView=null;
    ClockAdapter adapter=null;
    SwitchCompat serviceSwitch=null;

    private boolean isServiceOn=false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setReenterTransition(true);
        //setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clock, container, false);
        listView = (ListView) v.findViewById(R.id.clock_list);
        //registerForContextMenu(listView);
        adapter=new ClockAdapter(getContext(),R.layout.clock_list,RecordManager.instance(getActivity()).getList());
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ClockSettingActivity.class);
                i.putExtra("position", position);
                startActivityForResult(i,0);
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.clock_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_clock_delete:
                        int count=adapter.getCount();
                        for (int i=count-1;i>=0;i--){
                            if (listView.isItemChecked(i)) {
                                RecordManager.instance(getActivity()).getList().remove(i);
                            }
                        }
                        mode.finish();
                        adapter.notifyDataSetChanged();
                        return true;
                        //break;
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        serviceSwitch = (SwitchCompat) v.findViewById(R.id.clock_start_service);
        serviceSwitch.setChecked(DetectLocationService.getServiceState());
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent i = new Intent(getActivity(), DetectLocationService.class);
                if (isChecked) {
                    isServiceOn=isChecked;
                    getActivity().startService(i);
                }else{
                    getActivity().stopService(i);
                }
            }
        });
        super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }
    public void notifyList(){
        if (listView != null && adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.clock_delete, menu);
    }

    class ClockAdapter extends ArrayAdapter<RecordLocation> {

        public ClockAdapter(Context context, int resource, List<RecordLocation> list) {
            super(context, resource, list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.clock_list, null);
            }
            if (position % 4 == 0) {
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.clock_list_first));
            }else if(position%3==1){
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.clock_list_second));
            }else if(position%3==2){
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.clock_list_third));
            }else{
                convertView.setBackgroundDrawable(getResources().getDrawable(R.drawable.clock_list_forth));
            }
            final RecordLocation record=getItem(position);
            TextView title = (TextView) convertView.findViewById(R.id.clock_list_title);
            TextView time = (TextView) convertView.findViewById(R.id.clock_list_time);
            TextView address = (TextView) convertView.findViewById(R.id.clock_list_address);
            SwitchCompat switchCompat = (SwitchCompat) convertView.findViewById(R.id.clock_list_switch);
            title.setText(record.comment);
            String startTime,endTime;
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat("HH;mm");
            startTime = simpleDateFormat.format(record.startDate);
            endTime = simpleDateFormat.format(record.endDate);
            time.setText(startTime+"-"+endTime);
            address.setText(record.address);
            switchCompat.setChecked(record.isOn);
            switchCompat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*List list=RecordManager.instance(getActivity()).getList();
                    RecordLocation location = (RecordLocation) list.get(position);
                    location.isOn=!location.isOn;*/
                    record.isOn=!record.isOn;
                }
            });
            return convertView;
        }
    }
}
