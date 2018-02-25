package com.mbg.mbg_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class EditScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        Spinner fach = (Spinner) findViewById(R.id.spinner_fach);
        Spinner raum = (Spinner) findViewById(R.id.spinner_raum);
        Spinner lehrkraft = (Spinner) findViewById(R.id.spinner_lehrkraft);

        List<String> fachList = Arrays.asList(getResources().getStringArray(R.array.selector_fach));
        List<String> raumList = Arrays.asList(getResources().getStringArray(R.array.selector_raum));
        List<String> lehrkraftList = Arrays.asList(getResources().getStringArray(R.array.selector_lehrkraft));

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fachList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fach.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, raumList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raum.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lehrkraftList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lehrkraft.setAdapter(adapter);

        SharedPreferences pref = this.getSharedPreferences("Plan", Context.MODE_PRIVATE);

        String day = pref.getString("edit_tag", "");
        int stunde = pref.getInt("edit_stunde", 0);

        String prefPrefix = day + "_" + stunde + "_";

        String selFach = pref.getString(prefPrefix + "fach", "");
        String selRaum = pref.getString(prefPrefix + "raum", "");
        String selLehrkraft = pref.getString(prefPrefix + "lehrkraft", "");

        if (fachList.contains(selFach)) {
            fach.setSelection(fachList.indexOf(selFach));
        }
        if (raumList.contains(selRaum)) {
            raum.setSelection(raumList.indexOf(selRaum));
        }
        if (lehrkraftList.contains(selLehrkraft)) {
            lehrkraft.setSelection(lehrkraftList.indexOf(selLehrkraft));
        }

        ((TextView) findViewById(R.id.text_header)).setText(day.substring(0, 1).toUpperCase() + day.substring(1) + ", " + stunde + ". Stunde");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveDay(View view) {
        SharedPreferences pref = this.getSharedPreferences("Plan", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();

        String day = pref.getString("edit_tag", "");
        int stunde = pref.getInt("edit_stunde", 0);

        Spinner fach = (Spinner) findViewById(R.id.spinner_fach);
        Spinner raum = (Spinner) findViewById(R.id.spinner_raum);
        Spinner lehrkraft = (Spinner) findViewById(R.id.spinner_lehrkraft);

        String prefPrefix = day + "_" + stunde + "_";

        edit.putString(prefPrefix + "fach", fach.getSelectedItem().toString()).apply();
        edit.putString(prefPrefix + "raum", raum.getSelectedItem().toString()).apply();
        edit.putString(prefPrefix + "lehrkraft", lehrkraft.getSelectedItem().toString()).apply();

        this.finish();
    }
    
    public void deleteEntry(View view){
        SharedPreferences pref = this.getSharedPreferences("Plan", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();

        String day = pref.getString("edit_tag", "");
        int stunde = pref.getInt("edit_stunde", 0);

        String prefPrefix = day + "_" + stunde + "_";

        edit.putString(prefPrefix + "fach", "").apply();
        edit.putString(prefPrefix + "raum", "").apply();
        edit.putString(prefPrefix + "lehrkraft", "").apply();

        this.finish();
    }
}
