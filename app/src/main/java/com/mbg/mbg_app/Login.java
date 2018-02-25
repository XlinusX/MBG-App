package com.mbg.mbg_app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by Linus on 08.04.2017.
 */

public class Login extends Fragment{

    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.login,container,false);

        return myView;
    }
}
