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
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

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

        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar_vertretungsplan);
        progress.setVisibility(View.INVISIBLE);

        final SharedPreferences pref = getActivity().getSharedPreferences("timestamps", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();

        final File requestedFile1 = new File(view.getContext().getFilesDir(), "vertretungsplan1.html");
        final File requestedFile2 = new File(view.getContext().getFilesDir(), "vertretungsplan2.html");


        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            final DateTime now = DateTime.now();
            DateTime timestamp = new DateTime(pref.getLong("vertretungsplan", 0));
            Log.e(TAG, timestamp.toString());

            Duration dur = new Duration(new DateTime(0), new DateTime(1990, 1, 1, 0, 0));

            if(now.getHourOfDay() < 14){
                edit.putBoolean("downloadesAfter14",false).apply();
            }

            if (timestamp.getMillis() != 0) {
                dur = new Duration(timestamp, now);
            }

            if (dur.toStandardHours().getHours() >= 2) {
                if(networkInfo != null && networkInfo.isConnected()) {
                    requestedFile1.delete();
                    requestedFile2.delete();
                }else {
                    Toast.makeText(this.getActivity(), "Vertetungsplan konnte nicht aktualisiert werden! \n Bitte prüfe deine Internetverbindung!", Toast.LENGTH_LONG).show();
                }
            } else if((now.getHourOfDay() >= 14 && (now.getDayOfYear() > timestamp.getDayOfYear() || now.getYear() >= timestamp.getYear())) && !pref.getBoolean("downloadesAfter14",false)){
                if(networkInfo != null && networkInfo.isConnected()) {
                    requestedFile1.delete();
                    requestedFile2.delete();
                    edit.putBoolean("downloadesAfter14",true);
                }else {
                    Toast.makeText(this.getActivity(), "Vertetungsplan konnte nicht aktualisiert werden! \n Bitte prüfe deine Internetverbindung!", Toast.LENGTH_LONG).show();
                }
            }

            if (!(requestedFile1.exists()) || !(requestedFile2.exists())) {

                progress.setVisibility(View.VISIBLE);

                Ion.with(view.getContext())
                        .load("https://www.mbg.wn.schule-bw.de/intern/schueler/v-plan/subst_001.htm")
                        .basicAuthentication(new String(Base64.decode(view.getContext().getResources().getString(R.string.encryptedUsername), Base64.DEFAULT)), new String(Base64.decode(view.getContext().getResources().getString(R.string.encryptedPassword), Base64.DEFAULT)))
                        .write(requestedFile1)
                        .then(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e1, File result1) {
                                result1.setExecutable(true, false);
                                result1.setReadable(true, false);

                                Ion.with(view.getContext())
                                        .load("https://www.mbg.wn.schule-bw.de/intern/schueler/v-plan/subst_002.htm")
                                        .basicAuthentication(new String(Base64.decode(view.getContext().getResources().getString(R.string.encryptedUsername), Base64.DEFAULT)), new String(Base64.decode(view.getContext().getResources().getString(R.string.encryptedPassword), Base64.DEFAULT)))
                                        .write(requestedFile2)
                                        .then(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e2, File result2) {
                                                result2.setExecutable(true, false);
                                                result2.setReadable(true, false);

                                                try {
                                                    site002 = Jsoup.parse(result2, "utf-8");
                                                    Log.e(TAG, "onCompleted: parsing..." );
                                                } catch (IOException ex) {
                                                    ex.printStackTrace();
                                                }
                                                edit.putLong("vertretungsplan", now.getMillis()).apply();
                                                showVertretungsplan(site001,site002);
                                                changeTab();
                                                Toast.makeText(getActivity(), "Vertretungsplan wurde aktualisiert!", Toast.LENGTH_SHORT).show();
                                                progress.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                try {
                                    site001 = Jsoup.parse(result1, "utf-8");
                                    Log.e(TAG, "onCompleted: parsing..." );
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
            }else{
                try {
                    site001 = Jsoup.parse(requestedFile1, "utf-8");
                    site002 = Jsoup.parse(requestedFile2, "utf-8");
                    Log.e(TAG, "onCompleted: parsing..." );
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                showVertretungsplan(site001,site002);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showVertretungsplan(final Document site001, final Document site002) {

        final SharedPreferences mbgPref = getActivity().getSharedPreferences("MBGPref", Context.MODE_PRIVATE);

        host.setup();

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

                String selectedClass;
                boolean isInBetween = false;

                vertretungsplan.add(new String[]{"Stunde","Vertreter", "Fach", "Raum", "(Fach)"});

                final List<Integer> possiblePositions = new ArrayList<>();

                for (int i = 0;i<tableRows.size();i++) {

                    Element item = tableRows.get(i);

                    if (item.children().size() == 1) {
                        if (item.child(0).html().equals("Jahrgangsstufe 1") || item.child(0).html().equals("Jahrgangsstufe 2")) {
                            selectedClass = mbgPref.getString("selected_class_level",null);
                        } else {
                            selectedClass = mbgPref.getString("selected_class_level",null) + mbgPref.getString("selected_class_letter",null);
                        }

                        if(item.child(0).html().equals(selectedClass)){
                            isInBetween = true;
                            vertretungsplan.add(new String[]{item.child(0).html(), "", "", "", ""});
                        }else {
                            isInBetween = false;
                        }

                    } else {
                        if(isInBetween) {
                            possiblePositions.add(i);
                            vertretungsplan.add(new String[]{item.child(1).html(), item.child(2).html(), item.child(3).html(), item.child(4).html(), item.child(5).html()});
                        }
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
                        if (!(((TextView) view.findViewById(R.id.vertretungsplan_raum)).getText().toString().equals("") || (((TextView) view.findViewById(R.id.vertretungsplan_raum)).getText().toString().equals("Raum")))) {

                            SharedPreferences vert = getActivity().getSharedPreferences("Vert", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = vert.edit();

                            int realPosition = (position-2)+possiblePositions.get(0);

                            edit.putString("stunde", tableRows.get(realPosition).child(1).html()).apply();
                            edit.putString("vertreter", tableRows.get(realPosition).child(2).html()).apply();
                            edit.putString("fach", tableRows.get(realPosition).child(3).html()).apply();
                            edit.putString("raum", tableRows.get(realPosition).child(4).html()).apply();
                            edit.putString("eigentlichesFach", tableRows.get(realPosition).child(5).html()).apply();

                            edit.putString("art", tableRows.get(realPosition).child(6).html().replace("&nbsp;","-")).apply();
                            edit.putString("bemerkungen", tableRows.get(realPosition).child(7).html().replace("&nbsp;","-")).apply();
                            edit.putString("verlegtVon", tableRows.get(realPosition).child(8).html().replace("&nbsp;","-")).apply();

                            Intent intent = new Intent(view.getContext(), ShowMoreInfoActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });


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
