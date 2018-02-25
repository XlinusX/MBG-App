package com.mbg.mbg_app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Start extends Fragment {


    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_start, container, false);

        ListView list = (ListView) view.findViewById(R.id.list_start);

        final ArrayAdapter adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1);

        adapter.add("Termine");
        adapter.add("Vertretungsplan");
        adapter.add("Mein Stundenplan");
        adapter.add("Klausurenpläne");
        adapter.add("Einstelleungen");

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager manager = getActivity().getFragmentManager();

                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                NavigationView nav = ((NavigationView) getActivity().findViewById(R.id.nav_view));

                switch (position) {
                    case 0:
                        manager.beginTransaction().replace(R.id.contentFrame, new Termine()).commit();
                        getActivity().setTitle("Termine");
                        break;
                    case 1:
                        manager.beginTransaction().replace(R.id.contentFrame, new Vertretungsplan()).commit();
                        getActivity().setTitle("Vertretungsplan");
                        break;
                    case 2:
                        manager.beginTransaction().replace(R.id.contentFrame, new Stundenplan()).commit();
                        getActivity().setTitle("Stundenplan");
                        break;
                    case 3:
                        manager.beginTransaction().replace(R.id.contentFrame, new Klausurenplane()).commit();
                        getActivity().setTitle("Klausurenpläne");
                        break;
                    case 4:
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        startActivity(intent);
                        break;
                }

                if (position != 4) {
                    Log.e("linus", "onItemClick: navigation");
                    editor.putInt("state", position+1).apply();
                    nav.getMenu().getItem(position+1).setChecked(true);
                }


            }
        });

        return view;

    }
}
