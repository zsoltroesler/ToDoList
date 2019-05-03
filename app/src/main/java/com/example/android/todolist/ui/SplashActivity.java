package com.example.android.todolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.android.todolist.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // MainActivity will start after timer is over
                startActivity(new Intent(SplashActivity.this, MainActivity.class));

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
