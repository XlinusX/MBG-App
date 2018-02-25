package com.mbg.mbg_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_filename),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        boolean staySingedIn = sharedPref.getBoolean("staySingedIn",false);
        if (!staySingedIn) {
            editor.putBoolean("singedIn", false).apply();
        }

        editor.putInt("state",0).apply();

        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
}
