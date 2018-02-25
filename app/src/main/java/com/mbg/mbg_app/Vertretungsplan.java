package com.mbg.mbg_app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


/**
 * Created by Linus on 08.04.2017.
 */

public class Vertretungsplan extends Fragment {

    private static Document site001, site002, currentSite;

    View view;
    TabHost host;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_vertretungsplan, container, false);

        host = (TabHost) view.findViewById(R.id.tab_host_days);

        getVertretungsplan();
        changeTab();

        return view;
    }

    public void getVertretungsplan() {

        Toast toast = new Toast(this.getActivity());

        host.setup();

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            try {


                site001 = Jsoup.parse(Ion.with(getActivity())
                        .load("https://www.mbg.wn.schule-bw.de/intern/schueler/v-plan/subst_001.htm")
                        .basicAuthentication(new String(Base64.decode(getResources().getString(R.string.encryptedUsername),Base64.DEFAULT)), new String(Base64.decode(getResources().getString(R.string.encryptedPassword),Base64.DEFAULT)))
                        .asString()
                        .get()
                        .replace("&nbsp;"," "));

                site002 = Jsoup.parse(Ion.with(getActivity())
                        .load("https://www.mbg.wn.schule-bw.de/intern/schueler/v-plan/subst_002.htm")
                        .basicAuthentication(new String(Base64.decode(getResources().getString(R.string.encryptedUsername),Base64.DEFAULT)), new String(Base64.decode(getResources().getString(R.string.encryptedPassword),Base64.DEFAULT)))
                        .asString()
                        .get()
                        .replace("&nbsp;"," "));

                String[] day1 = site001.getElementsByClass("mon_title").first().html().split(" ");
                String[] day2 = site002.getElementsByClass("mon_title").first().html().split(" ");

                TabHost.TabSpec spec_today = host.newTabSpec("today");
                spec_today.setContent(R.id.tab_today);
                spec_today.setIndicator(day1[1].substring(0, 2) + ", " + day1[0]);
                host.addTab(spec_today);

                TabHost.TabSpec spec_tomorrow = host.newTabSpec("tomorrow");
                spec_tomorrow.setContent(R.id.tab_tomorrow);
                spec_tomorrow.setIndicator(day2[1].substring(0, 2) + ", " + day2[0]);
                host.addTab(spec_tomorrow);

                host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                    @Override
                    public void onTabChanged(String tabId) {
                        final ListView list = (ListView) ((LinearLayout) host.getTabContentView().getChildAt(host.getCurrentTab())).getChildAt(0);

                        SimpleAdapter adapter;

                        List<String[]> vertretungsplan = new ArrayList<>();

                        if (host.getCurrentTab() == 0) {
                            currentSite = site001;
                        } else {
                            currentSite = site002;
                        }

                        final Elements tableRows = currentSite.getElementsByClass("mon_list").first().child(0).children();

                        for (Element item : tableRows) {
                            if (item.children().size() != 1) {
                                vertretungsplan.add(new String[]{item.child(1).html(), item.child(2).html(), item.child(3).html(), item.child(4).html(), item.child(5).html()});
                            } else {
                                vertretungsplan.add(new String[]{item.child(0).html(), "", "", "", ""});
                            }
                        }


                        ArrayList<Map<String, String>> listItems = new ArrayList<>();

                        adapter = new SimpleAdapter(getActivity(), listItems
                                , R.layout.list_item_vertretungsplan
                                , new String[]{"stunde", "vertreter", "fach", "raum", "eigentlichesFach"}
                                , new int[]{R.id.vertretungsplan_stunde, R.id.vertretungsplan_vertreter, R.id.vertretungsplan_fach
                                , R.id.vertretungsplan_raum, R.id.vertretungsplan_eigentlichesFach});

                        for (String[] item : vertretungsplan) {
                            HashMap<String, String> resultMap = new HashMap<>();
                            resultMap.put("stunde", item[0]);
                            resultMap.put("vertreter", item[1]);
                            resultMap.put("fach", item[2]);
                            resultMap.put("raum", item[3]);
                            resultMap.put("eigentlichesFach", item[4]);
                            listItems.add(resultMap);
                        }

                        list.setAdapter(adapter);

                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if(!(((TextView)view.findViewById(R.id.vertretungsplan_raum)).getText().toString().equals("") || (((TextView)view.findViewById(R.id.vertretungsplan_raum)).getText().toString().equals("Raum")))){

                                    SharedPreferences vert = getActivity().getSharedPreferences("Vert",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor edit = vert.edit();

                                    edit.putString("stunde",tableRows.get(position).child(1).html()).apply();
                                    edit.putString("vertreter",tableRows.get(position).child(2).html()).apply();
                                    edit.putString("fach",tableRows.get(position).child(3).html()).apply();
                                    edit.putString("raum",tableRows.get(position).child(4).html()).apply();
                                    edit.putString("eigentlichesFach",tableRows.get(position).child(5).html()).apply();

                                    edit.putString("art",tableRows.get(position).child(6).html()).apply();
                                    edit.putString("bemerkungen",tableRows.get(position).child(7).html()).apply();
                                    edit.putString("verlegtVon",tableRows.get(position).child(8).html()).apply();

                                    Intent intent = new Intent(view.getContext(),ShowMoreInfoActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                toast.makeText(this.getActivity(), "Bitte prüfe deine Internetverbindung!", Toast.LENGTH_SHORT).show();
            }

        } else {
            toast.makeText(this.getActivity(), "Bitte prüfe deine Internetverbindung!", Toast.LENGTH_SHORT).show();
        }

    }

    protected void changeTab() {
        if (host.getCurrentTab() == 0) {
            host.setCurrentTab(1);
            host.setCurrentTab(0);
        } else {
            host.setCurrentTab(0);
            host.setCurrentTab(1);
        }
    }

}
