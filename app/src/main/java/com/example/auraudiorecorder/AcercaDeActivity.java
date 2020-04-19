package com.example.auraudiorecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;

public class AcercaDeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_DarkTheme);
        }else{
            setTheme(R.style.AppTheme_Dialog);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.acercade);
    }
}
