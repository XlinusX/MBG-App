package com.mbg.mbg_app;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;

public class Klausurenplane extends Fragment {


    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_klausurenplane, container, false);

        final SharedPreferences pref = getActivity().getSharedPreferences("timestamps", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();

        ListView list = (ListView) view.findViewById(R.id.list_kausurenplaene);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1);

        adapter.add("J1 Halbjahr 1");
        adapter.add("J1 Halbjahr 2");
        adapter.add("J2 Halbjahr 1");
        adapter.add("J2 Halbjahr 2");

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String halbjahr = "";

                switch (position) {
                    case 0:
                        halbjahr = "1_1";
                        break;
                    case 1:
                        halbjahr = "1_2";
                        break;
                    case 2:
                        halbjahr = "2_1";
                        break;
                    case 3:
                        halbjahr = "2_2";
                        break;
                }

                File requestedFile = new File(view.getContext().getFilesDir(), "klausurenplan_" + halbjahr + ".pdf");

                DateTime now = DateTime.now();
                final DateTime timestamp = new DateTime(pref.getLong("klausurenplan_" + halbjahr, 0));

                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                Log.i("linus", timestamp.toString());

                Duration dur = new Duration(new DateTime(0), new DateTime(1990, 1, 1, 0, 0));

                if (timestamp.getMillis() != 0) {
                    dur = new Duration(timestamp, now);
                }

                if (dur.toStandardHours().getHours() >= 12) {
                    if(networkInfo != null && networkInfo.isConnected()) {
                        requestedFile.delete();
                    }else {
                        Toast.makeText(getActivity(), "Klausurenplan konnte nicht aktualisiert werden! \n Bitte prüfe deine Internetverbindung!", Toast.LENGTH_LONG).show();
                    }
                }

                if (!(requestedFile.exists())) {
                    final String finalHalbjahr = halbjahr;

                    Ion.with(view.getContext())
                            .load("http://www.mbg.wn.schule-bw.de/files/klausurenplan_j" + halbjahr + ".pdf")
                            .write(requestedFile)
                            .then(new FutureCallback<File>() {
                                @Override
                                public void onCompleted(Exception e, File result) {
                                    result.setExecutable(true, false);
                                    result.setReadable(true, false);

                                    edit.putLong("klausurenplan_" + finalHalbjahr,timestamp.getMillis()).apply();

                                    showKlausrenplan(result);
                                }
                            });
                }else{
                    showKlausrenplan(requestedFile);
                }
            }
        });

        return view;

    }

    private void showKlausrenplan(File klausurenplan) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(klausurenplan), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
