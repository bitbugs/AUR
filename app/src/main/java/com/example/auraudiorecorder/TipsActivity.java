package com.example.auraudiorecorder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class TipsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppTheme_DarkTheme);
        }else{
            setTheme(R.style.AppTheme_Dialog);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips);
    }
}
