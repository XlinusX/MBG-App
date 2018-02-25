package com.mbg.mbg_app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public int state;
    public boolean singedIn;
    public boolean staySingedIn;
    android.app.FragmentManager manager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
        SharedPreferences plan = this.getSharedPreferences("Plan", Context.MODE_PRIVATE);
        singedIn = sharedPref.getBoolean("singedIn", false);
        state = sharedPref.getInt("state", 0);

        if (!singedIn) {
            manager.beginTransaction().replace(R.id.contentFrame, new Login()).commit();
            this.setTitle("Login");
        } else if (state == 0) {
            manager.beginTransaction().replace(R.id.contentFrame, new Start()).commit();
            this.setTitle("Start");
        } else if (state == 1) {
            manager.beginTransaction().replace(R.id.contentFrame, new Termine()).commit();
            this.setTitle("Termine");
        } else if (state == 2) {
            manager.beginTransaction().replace(R.id.contentFrame, new Vertretungsplan()).commit();
            this.setTitle("Vertretungsplan");
        } else if (state == 3) {
            manager.beginTransaction().replace(R.id.contentFrame, new Stundenplan()).commit();
            plan.edit().putBoolean("editor_was_active", false).apply();
            this.setTitle("Mein Stundenplan");
        } else if (state == 4) {
            manager.beginTransaction().replace(R.id.contentFrame, new Klausurenplane()).commit();
            this.setTitle("Klausurenpläne");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(state).setChecked(true);

    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(sharedPref.getInt("state", 0) == 0){
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (singedIn) {
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
            } else {
                findViewById(R.id.warning).setVisibility(View.VISIBLE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        SharedPreferences plan = this.getSharedPreferences("Plan", Context.MODE_PRIVATE);

        int id = item.getItemId();
        if (singedIn) {
            if (id == R.id.nav_start) {
                state = 0;
                manager.beginTransaction().replace(R.id.contentFrame, new Start()).commit();
                this.setTitle("Start");
            } else if (id == R.id.nav_termine) {
                state = 1;
                manager.beginTransaction().replace(R.id.contentFrame, new Termine()).commit();
                this.setTitle("Termine");
            } else if (id == R.id.nav_vertretungsplan) {
                state = 2;
                manager.beginTransaction().replace(R.id.contentFrame, new Vertretungsplan()).commit();
                this.setTitle("Vertretungsplan");
            } else if (id == R.id.nav_stundenplan) {
                state = 3;
                manager.beginTransaction().replace(R.id.contentFrame, new Stundenplan()).commit();
                plan.edit().putBoolean("editor_was_active", false).apply();
                this.setTitle("Mein Stundenplan");
            } else if (id == R.id.nav_klausurenplan) {
                state = 4;
                manager.beginTransaction().replace(R.id.contentFrame, new Klausurenplane()).commit();
                this.setTitle("Klausurenpläne");
            }

            editor.putInt("state", state).apply();

        } else {
            findViewById(R.id.warning).setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void login(View view) {
        String name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        String pwd = ((EditText) findViewById(R.id.editTextPwd)).getText().toString();

        if (name.equals(new String(Base64.decode(getResources().getString(R.string.encryptedUsername),Base64.DEFAULT))) && pwd.equals(new String(Base64.decode(getResources().getString(R.string.encryptedPassword),Base64.DEFAULT)))) {
            singedIn = true;

            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean("singedIn", singedIn).apply();

            staySingedIn = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
            editor.putBoolean("staySingedIn", staySingedIn).apply();


            manager.beginTransaction().replace(R.id.contentFrame, new Start()).commit();
            this.setTitle("Start");
        } else {
            findViewById(R.id.login_failed).setVisibility(View.VISIBLE);
        }
    }

    public void makeNotification(View view) {

        NotificationCompat.Builder Nbuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notifications_black_24dp))
                .setContentTitle("Hi")
                .setContentText("wie gehts?")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));

        NotificationManagerCompat Nmngr = NotificationManagerCompat.from(this);
        Nmngr.notify(42, Nbuilder.build());
    }
}
