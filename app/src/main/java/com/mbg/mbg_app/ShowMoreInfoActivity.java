package com.mbg.mbg_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowMoreInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_more_info);

        SharedPreferences vert = this.getSharedPreferences("Vert",Context.MODE_PRIVATE);

        ((TextView)findViewById(R.id.more_info_dynamic_text_stunde)).setText(vert.getString("stunde",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_vertreter)).setText(vert.getString("vertreter",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_fach)).setText(vert.getString("fach",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_raum)).setText(vert.getString("raum",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_eigentlichesFach)).setText(vert.getString("eigentlichesFach",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_art)).setText(vert.getString("art",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_bemerkungen)).setText(vert.getString("bemerkungen",""));
        ((TextView)findViewById(R.id.more_info_dynamic_text_verlegtVon)).setText(vert.getString("verlegtVon",""));

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
}