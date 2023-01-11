package com.example.nightshift.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nightshift.R;

import java.util.Random;

public class WecomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStartApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initViews();
    }

    private void initViews() {
        btnStartApp = findViewById(R.id.btn_switch);
        btnStartApp.setOnClickListener(this);
        Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.app_wall);
        loadAnimation.reset();
        loadAnimation.setFillAfter(true);
        findViewById(R.id.iv_star).startAnimation(loadAnimation);
        Animation loadAnimation2 = AnimationUtils.loadAnimation(this, R.anim.app_wall_star);
        loadAnimation2.setDuration((long) (new Random().nextInt(600) + 500));
        loadAnimation2.reset();
        ((ImageView) findViewById(R.id.app_wall_02)).startAnimation(loadAnimation2);
        Animation loadAnimation3 = AnimationUtils.loadAnimation(this, R.anim.app_wall_star);
        loadAnimation3.setDuration((long) (new Random().nextInt(400) + 500));
        loadAnimation3.reset();
        ((ImageView) findViewById(R.id.app_wall_03)).startAnimation(loadAnimation3);
        Animation loadAnimation4 = AnimationUtils.loadAnimation(this, R.anim.app_wall_star);
        loadAnimation4.setDuration((long) (new Random().nextInt(500) + 400));
        loadAnimation4.reset();
        ((ImageView) findViewById(R.id.app_wall_04)).startAnimation(loadAnimation4);
        Animation loadAnimation5 = AnimationUtils.loadAnimation(this, R.anim.app_wall_star);
        loadAnimation5.setDuration((long) (new Random().nextInt(400) + 600));
        loadAnimation5.reset();
        ((ImageView) findViewById(R.id.app_wall_05)).startAnimation(loadAnimation5);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}
