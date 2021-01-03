package com.github.Tuner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setActivityBackgroundColor(0xFF212121); //setarea culorii fundalului pe negru
    }

    public void setActivityBackgroundColor(int color) {
        //metoda pentru schimbarea culorii de background al unei activitati
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }
}