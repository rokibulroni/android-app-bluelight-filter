package com.example.nightshift.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.nightshift.Adapter.ViewPagerAdapter;
import com.example.nightshift.R;

public class SflatActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private RelativeLayout rlHelp;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ImageView imgCancel;
    private ImageView imgIndex1, imgIndex2, imgIndex3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initViews();
    }

    private void initViews() {
        imgIndex1 = findViewById(R.id.iv_pager_index_0);
        imgIndex2 = findViewById(R.id.iv_pager_index_1);
        imgIndex3 = findViewById(R.id.iv_pager_index_2);
        imgCancel = findViewById(R.id.img_cancel);
        imgCancel.setOnClickListener(this);

        rlHelp = findViewById(R.id.rl_help);
        rlHelp.setVisibility(View.GONE);
        sharedPreferences = getSharedPreferences("MySharedPreference", MODE_PRIVATE);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //     Log.e("adsfadfadfe", i + "   s   " + i1);
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    imgIndex1.setImageResource(R.drawable.ic_pager_index_checked);
                    imgIndex2.setImageResource(R.drawable.ic_pager_index);
                    imgIndex3.setImageResource(R.drawable.ic_pager_index);
                } else if (i == 1) {
                    imgIndex2.setImageResource(R.drawable.ic_pager_index_checked);
                    imgIndex1.setImageResource(R.drawable.ic_pager_index);
                    imgIndex3.setImageResource(R.drawable.ic_pager_index);
                } else if (i == 2) {
                    imgIndex3.setImageResource(R.drawable.ic_pager_index_checked);
                    imgIndex1.setImageResource(R.drawable.ic_pager_index);
                    imgIndex2.setImageResource(R.drawable.ic_pager_index);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // Log.e("adsfadfadfe", i + "   a   ");
            }
        });
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int isfirst = sharedPreferences.getInt("first", 0);
                if (isfirst == 0) {
                    rlHelp.setVisibility(View.VISIBLE);
                } else {
                    startActivity(new Intent(SflatActivity.this, MainActivity.class));
                }
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                SharedPreferences.Editor editor = getSharedPreferences("MySharedPreference", MODE_PRIVATE).edit();
                editor.putInt("first", 1);
                editor.apply();
                startActivity(new Intent(this, WecomeActivity.class));
                break;
        }
    }
}