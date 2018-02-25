package com.mbg.mbg_app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

/**
 * Created by Linus on 08.04.2017.
 */

public class Termine extends Fragment {

    private static Document site;

    Element current;
    String date, data;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_termine, container, false);

        getTermine();

        return view;
    }

    public void getTermine() {

        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar_termine);
        progress.setVisibility(View.INVISIBLE);

        final SharedPreferences pref = getActivity().getSharedPreferences("timestamps", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();

        final File requestedFile = new File(view.getContext().getFilesDir(), "termine.html");

        try {

            ConnectivityManager connMgr = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            final DateTime now = DateTime.now();
            DateTime timestamp = new DateTime(pref.getLong("termine", 0));

            Log.e("linus", timestamp.toString());

            Duration dur = new Duration(new DateTime(0), new DateTime(1990, 1, 1, 0, 0));

            if (timestamp.getMillis() != 0) {
                dur = new Duration(timestamp, now);
            }

            if (dur.toStandardHours().getHours() >= 12) {
                if(networkInfo != null && networkInfo.isConnected()) {
                    requestedFile.delete();
                }else {
                    Toast.makeText(this.getActivity(), "Termine konnten nicht aktualisiert werden! \n Bitte prüfe deine Internetverbindung!", Toast.LENGTH_LONG).show();
                }
            }

            if (!(requestedFile.exists())) {

                progress.setVisibility(View.VISIBLE);

                Ion.with(view.getContext())
                        .load("http://www.mbg.wn.schule-bw.de")
                        .write(requestedFile)
                        .then(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File result) {
                                result.setExecutable(true, false);
                                result.setReadable(true, false);

                                try {
                                    site = Jsoup.parse(result, "utf-8");
                                    Log.e(TAG, "onCompleted: parsing..." );
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                edit.putLong("termine", now.getMillis()).apply();
                                parseStuff(site);
                                Toast.makeText(getActivity(), "Temine wurden aktualisiert!", Toast.LENGTH_SHORT).show();
                                progress.setVisibility(View.INVISIBLE);
                            }
                        });
            }else{
                try {
                    site = Jsoup.parse(requestedFile, "utf-8");
                    Log.e(TAG, "onCompleted: parsing..." );
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                parseStuff(site);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseStuff(Document site){
        ListView list = (ListView) view.findViewById(R.id.list_termine);
        SimpleAdapter adapter;

        Elements table;

        try {
            table = site.body().getElementsByClass("ohne_rahmen")
                    .first().getElementsByTag("tbody").first().getElementsByTag("tr");
        }catch (Exception e){

            return;
        }

        List<String[]> termine = new ArrayList<>();

        for (int i = 0; i < table.size(); i++) {

            current = table.get(i).getElementsByTag("td").first();

            if (current.hasAttr("colspan")) {
                date = current.getElementsByTag("div").first().html();
                data = current.getElementsByTag("div").last().html();
            } else {
                date = current.html();
                data = current.nextElementSibling().html();
            }

            date = date.replaceAll("&nbsp;", " ");
            date += ":";

            data = data.replaceAll("&nbsp;", " ");
            data = data.replace("\n", " ");
            data = data.replaceAll("<br>", ",\n");

            termine.add(new String[]{date, data});
        }

        List<HashMap<String, String>> listItems = new ArrayList<>();

        adapter = new SimpleAdapter(view.getContext(), listItems, R.layout.list_item_termine, new String[]{"main", "sub"}, new int[]{R.id.text_main, R.id.text_sub});

        for (String[] termin : termine) {
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("main", termin[0]);
            resultMap.put("sub", termin[1]);
            listItems.add(resultMap);
        }

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String date = ((TextView) view.findViewById(R.id.text_main)).getText().toString();
                String data = ((TextView) view.findViewById(R.id.text_sub)).getText().toString();

                date = date.replaceAll(Pattern.quote("."), "p");
                date = date.replaceAll(Pattern.quote("–"), "m");
                date = date.replaceAll(Pattern.quote("-"), "m");


                int dayBegin = 1;
                int dayEnd = 1;
                int monthBegin = 0;
                int monthEnd = 0;
                int yearBegin = 2017;
                int yearEnd = 2017;
                boolean fullDay = false;

                int hourBegin;
                int minsBegin;
                int hourEnd;
                int minsEnd;

                if (date.split("\\s")[0].length() == 2) {
                    dayBegin = dayEnd = parseInt(date.substring(4).split("p")[0]);
                    monthBegin = monthEnd = parseInt(date.substring(4).split("p")[1]);
                    yearBegin = yearEnd = parseInt(date.substring(4).split("p")[2].replace(":", ""));
                } else if (date.split("\\s")[0].length() == 5) {
                    dayBegin = parseInt(date.substring(7).split("m")[0].split("p")[0]);
                    monthBegin = parseInt(date.substring(7).split("m")[0].split("p")[1]);
                    yearBegin = parseInt(date.substring(7).split("m")[0].split("p")[2]);

                    dayEnd = parseInt(date.substring(7).split("m")[1].split("p")[0]);
                    monthEnd = parseInt(date.substring(7).split("m")[1].split("p")[1]);
                    yearEnd = parseInt(date.substring(7).split("m")[1].split("p")[2].replace(":", ""));

                    fullDay = true;
                }

                if (yearBegin < 100) {
                    yearBegin += 2000;
                }
                if (yearEnd < 100) {
                    yearEnd += 2000;
                }

                Calendar beginTime = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();

                String com1 = "ttfttfttftt"; //z.B. 13:15-15:00
                String com2 = "tfttfttftt"; //z.B. 8:80-12:00
                String com3 = "tfttftftt"; //z.B. 7:45-9:30
                String com4 = "ttftt"; //z.B. 12:30
                String com5 = "tftt"; //z.B. 9:45


                String dataArr = "";
                int timeIndex;

                for (int i = 0; i < data.length(); i++) {
                    if (Character.isDigit(data.toCharArray()[i])) {
                        dataArr += "t";
                    }else if(!(Character.isSpaceChar(data.toCharArray()[i]))){
                        dataArr += "f";
                    }
                }
                if(dataArr.contains(com1)) {
                    timeIndex = dataArr.indexOf(com1);
                    String spaceless = data.replace(" ", "");

                    hourBegin = parseInt(spaceless.substring(timeIndex, timeIndex + 2));
                    minsBegin = parseInt(spaceless.substring(timeIndex + 3, timeIndex + 5));

                    hourEnd = parseInt(spaceless.substring(timeIndex + 6, timeIndex + 8));
                    minsEnd = parseInt(spaceless.substring(timeIndex + 9, timeIndex + 11));

                    beginTime.set(yearBegin, monthBegin - 1, dayBegin, hourBegin, minsBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd, hourEnd, minsEnd);

                    if(timeIndex == 0){
                        data = data.substring(data.indexOf(" ",10)+1);
                    }

                }else if(dataArr.contains(com2)) {
                    timeIndex = dataArr.indexOf(com2);
                    String spaceless = data.replace(" ", "");

                    hourBegin = parseInt(spaceless.substring(timeIndex, timeIndex + 1));
                    minsBegin = parseInt(spaceless.substring(timeIndex + 2, timeIndex + 4));

                    hourEnd = parseInt(spaceless.substring(timeIndex + 5, timeIndex + 7));
                    minsEnd = parseInt(spaceless.substring(timeIndex + 8, timeIndex + 10));

                    beginTime.set(yearBegin, monthBegin - 1, dayBegin, hourBegin, minsBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd, hourEnd, minsEnd);

                    if (timeIndex == 0) {
                        data = data.substring(data.indexOf(" ", 9) + 1);
                    }
                }else if(dataArr.contains(com3)){
                    timeIndex = dataArr.indexOf(com3);
                    String spaceless = data.replace(" ", "");

                    hourBegin = parseInt(spaceless.substring(timeIndex, timeIndex + 1));
                    minsBegin = parseInt(spaceless.substring(timeIndex + 2, timeIndex + 4));

                    hourEnd = parseInt(spaceless.substring(timeIndex + 5, timeIndex + 6));
                    minsEnd = parseInt(spaceless.substring(timeIndex + 7, timeIndex + 9));

                    beginTime.set(yearBegin, monthBegin - 1, dayBegin, hourBegin, minsBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd, hourEnd, minsEnd);

                    if (timeIndex == 0) {
                        data = data.substring(data.indexOf(" ", 8) + 1);
                    }
                }else if (dataArr.contains(com4)) {
                    timeIndex = dataArr.indexOf(com4);
                    String spaceless = data.replace(" ", "");

                    hourBegin = parseInt(spaceless.substring(timeIndex, timeIndex + 2));
                    minsBegin = parseInt(spaceless.substring(timeIndex + 3, timeIndex + 5));

                    beginTime.set(yearBegin, monthBegin - 1, dayBegin, hourBegin, minsBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd, hourBegin + 1, minsBegin);

                    if (timeIndex == 0) {
                        data = data.substring(data.indexOf(" ",4)+1);
                    }

                } else if (dataArr.contains(com5)) {
                    timeIndex = dataArr.indexOf(com5);
                    String spaceless = data.replace(" ", "");

                    hourBegin = parseInt(spaceless.substring(timeIndex, timeIndex + 1));
                    minsBegin = parseInt(spaceless.substring(timeIndex + 2, timeIndex + 4));

                    beginTime.set(yearBegin, monthBegin - 1, dayBegin, hourBegin, minsBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd, hourBegin + 1, minsBegin);

                    if (timeIndex == 0) {
                        data = data.substring(data.indexOf(" ",3)+1);
                    }

                }else {
                    fullDay = true;
                    beginTime.set(yearBegin, monthBegin - 1, dayBegin);
                    endTime.set(yearEnd, monthEnd - 1, dayEnd);
                }

                Intent calendarIntent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, fullDay)
                        .putExtra(CalendarContract.Events.TITLE, data);
                startActivity(calendarIntent);
            }
        });
    }

}
