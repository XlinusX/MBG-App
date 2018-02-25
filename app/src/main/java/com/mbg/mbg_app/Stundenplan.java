package com.mbg.mbg_app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

/**
 * Created by Linus on 08.04.2017.
 */

public class Stundenplan extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_stundenplan, container, false);

        createStundenplan();
        changeTab();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeTab();
    }

    public void createStundenplan() {

        final TabHost host = (TabHost) view.findViewById(R.id.tab_host_stundenplan);
        host.setup();

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                final SharedPreferences plan = getActivity().getSharedPreferences("Plan", Context.MODE_PRIVATE);
                final SharedPreferences.Editor edit = plan.edit();

                ListView list = (ListView) ((LinearLayout)host.getTabContentView().getChildAt(host.getCurrentTab())).getChildAt(0);
                SimpleAdapter adapter;

                List<String[]> stunden = new ArrayList<>();
                String prefPrefix;

                for(int i = 0; i<11;i++){

                    prefPrefix = host.getCurrentTabTag()+"_"+(i+1)+"_";

                    stunden.add(new String[]{(i+1+". Stunde"),
                            plan.getString(prefPrefix+"fach",""),
                            plan.getString(prefPrefix+"raum",""),
                            plan.getString(prefPrefix+"lehrkraft","")});
                }

                List<Map<String, String>> listItems = new ArrayList<>();

                adapter = new SimpleAdapter(view.getContext(), listItems, R.layout.list_item_stundenplan, new String[]{"stunde", "fach","raum","lehrkraft"}, new int[]{R.id.text_stunde, R.id.text_fach,R.id.text_raum,R.id.text_lehrkraft});

                for(String[] stunde:stunden){
                    HashMap<String, String> resultMap = new HashMap<>();
                    resultMap.put("stunde",stunde[0]);
                    resultMap.put("fach",stunde[1]);
                    resultMap.put("raum",stunde[2]);
                    resultMap.put("lehrkraft",stunde[3]);
                    listItems.add(resultMap);
                }

                list.setAdapter(adapter);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        edit.putString("edit_tag",host.getCurrentTabTag()).apply();
                        edit.putInt("edit_stunde",position+1).apply();
                        edit.putInt("last_edited_day",host.getCurrentTab()).apply();
                        edit.putBoolean("editor_was_active",true).apply();

                        Intent intent = new Intent(view.getContext(),EditScheduleActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        TabHost.TabSpec spec;

        spec = host.newTabSpec("montag");
        spec.setContent(R.id.tab_montag);
        spec.setIndicator("Mo");
        host.addTab(spec);

        spec = host.newTabSpec("dienstag");
        spec.setContent(R.id.tab_dienstag);
        spec.setIndicator("Di");
        host.addTab(spec);

        spec = host.newTabSpec("mittwoch");
        spec.setContent(R.id.tab_mittwoch);
        spec.setIndicator("Mi");
        host.addTab(spec);

        spec = host.newTabSpec("donnerstag");
        spec.setContent(R.id.tab_donnerstag);
        spec.setIndicator("Do");
        host.addTab(spec);

        spec = host.newTabSpec("freitag");
        spec.setContent(R.id.tab_freitag);
        spec.setIndicator("Fr");
        host.addTab(spec);

    }

    protected void changeTab(){
        SharedPreferences plan = getActivity().getSharedPreferences("Plan",Context.MODE_PRIVATE);
        final TabHost host = (TabHost) view.findViewById(R.id.tab_host_stundenplan);

        if(plan.getBoolean("editor_was_active",false)){

            int index = plan.getInt("last_edited_day",0);

            if(index+1>4){
                host.setCurrentTab(index-1);
            }else{
                host.setCurrentTab(index+1);
            }


            host.setCurrentTab(index);
        }else {
            int tag = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
            if (tag > 5) {
                tag = 0;
            }
            host.setCurrentTab(tag);
        }
    }

}
