package com.example.nightshift.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nightshift.Adapter.NightModeAdapter;
import com.example.nightshift.BuildConfig;
import com.example.nightshift.Listener.IClickCheckCamera;
import com.example.nightshift.Listener.IOnClickRecylerView;
import com.example.nightshift.Model.NightMode;
import com.example.nightshift.R;
import com.example.nightshift.Service.NightShiftService;
import com.example.nightshift.Slide.ModeInfomation;
import com.example.nightshift.Utils.Commom;
import com.example.nightshift.Utils.Const;
import com.example.nightshift.Utils.SharePreferencesController;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IOnClickRecylerView,
        NavigationView.OnNavigationItemSelectedListener, IClickCheckCamera {
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private static final int KEY_OVERLAY = 1000;
    private static final int ON_DO_NOT_DISTURB_CALLBACK_CODE = 9;
    public static boolean isAlive;

    private RecyclerView rvMain;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    @SuppressLint("StaticFieldLeak")
    public static NavigationView navigationView;
    private NightModeAdapter adapter;
    private List<NightMode> arrNightModes;

    private SeekBar seekBarOpacity, seekBarBrightness;
    private TextView tvOpacity, tvTemperatureColor, tvBrightness, tvTurnOffTime, tvOnTimePicker, tvOffTimePicker, tvOnOffNoti;
    private LinearLayout llTurnOnTime;
    private ImageView imgSlide;
    private Switch aSwitch, switchTime, switchNoti;

    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    //Hen gio
    private PendingIntent pendingIntentOn, pendingIntentOff;

    private ContentResolver contentResolver;
    private int currentBrightnessValue;

    private IntentFilter intentFilter = new IntentFilter();
    private BroadcastNightShift broadcastNightShift = new BroadcastNightShift();

    class BroadcastNightShift extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("son.pt", "mainactivity receiver");
            boolean isOnePending = pendingIntentOff == null || pendingIntentOn == null;
            int count = SharePreferencesController.getInstance(context).getInt(Const.ALARM_COUNT, 0);
            if (isOnePending && count == 1) {
                SharePreferencesController.getInstance(context).putInt(Const.ALARM_COUNT, 0);
                switchTime.setChecked(false);
                SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.ALARM_APP, false);
            }

            if (count == 2) {
                SharePreferencesController.getInstance(context).putInt(Const.ALARM_COUNT, 0);
                switchTime.setChecked(false);
                SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.ALARM_APP, false);
            }

            if (NightShiftService.isCheckedSwitch) {
                aSwitch.setChecked(false);
            } else {
                aSwitch.setChecked(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Commom.mContext = this;
        calendar = Calendar.getInstance();

        isAlive = true;

        intentFilter.addAction(Const.ACTION_NIGHT_MODE);
        registerReceiver(broadcastNightShift, intentFilter);
        initAdsFull();
        initViews();

        checkPermissionApp();

        setData();

        Commom.setiClickCheckCamera(this);

    }

    private void checkPermissionApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                //kiem tra quyen ve tren ung dung khac chua
                startNightModeService();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, KEY_OVERLAY);
            }

            if (Settings.System.canWrite(this)) {
                // Do stuff here
                Log.d("son,pt", "Can't write");
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp() {
        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!n.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, MainActivity.ON_DO_NOT_DISTURB_CALLBACK_CODE);
        }
    }

    private void startNightModeService() {
        Intent intent = new Intent(this, NightShiftService.class);
        startService(intent);
    }

    private void setData() {
        Commom.setData();
        arrNightModes = Commom.arrNightModes;

        arrNightModes.get(SharePreferencesController.getInstance(this).getInt(Const.KEY_MODE, 0)).setChoose(true);
        adapter = new NightModeAdapter(this, arrNightModes, this);

        rvMain.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvMain.setLayoutManager(layoutManager);

        rvMain.setAdapter(adapter);
    }

    @SuppressLint({"RestrictedApi", "NewApi"})
    private void initViews() {
        MobileAds.initialize(this, getResources().getString(R.string.mobile_id));
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
       // adView.loadAd(adRequest);

        tvOnOffNoti = findViewById(R.id.tv_on_off);
        switchNoti = findViewById(R.id.switchNotification);
        tvOnTimePicker = findViewById(R.id.tv_on_timepicker);
        tvOffTimePicker = findViewById(R.id.tv_off_timepicker);
        tvTurnOffTime = findViewById(R.id.tv_turn_off_time);
        llTurnOnTime = findViewById(R.id.ll_turn_on_time);
        switchTime = findViewById(R.id.switchTime);
        tvBrightness = findViewById(R.id.tv_brightness);
        seekBarBrightness = findViewById(R.id.seekbar_brightness);
        imgSlide = findViewById(R.id.img_slide);
        tvOpacity = findViewById(R.id.tv_opacity);
        tvTemperatureColor = findViewById(R.id.tv_temperature_color);
        seekBarOpacity = findViewById(R.id.seekbar_opacity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        rvMain = findViewById(R.id.rv_main);

        rvMain.setNestedScrollingEnabled(false);

        simpleDateFormat = new SimpleDateFormat("HH:mm");
        int hourOn = SharePreferencesController.getInstance(this).getInt(Const.ON_TIME_HOURS, 0);
        int hourOff = SharePreferencesController.getInstance(this).getInt(Const.OFF_TIME_HOURS, 0);
        int minuteOn = SharePreferencesController.getInstance(this).getInt(Const.ON_TIME_MINUTE, 0);
        int minuteOff = SharePreferencesController.getInstance(this).getInt(Const.OFF_TIME_MINUTE, 0);
        calendar.set(0, 0, 0, hourOn, minuteOn);
        tvOnTimePicker.setText(getResources().getString(R.string.status_on) + " " + simpleDateFormat.format(calendar.getTime()));

        calendar.set(0, 0, 0, hourOff, minuteOff);
        tvOffTimePicker.setText(getResources().getString(R.string.status_off) + " " + simpleDateFormat.format(calendar.getTime()));
        imgSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ModeInfomation.class));
            }
        });

        setSupportActionBar(toolbar);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.mo, R.string.dong) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setTitle(getString(R.string.title_toolbar));

        //Seekbar opacity
        int progress = SharePreferencesController.getInstance(this).getInt(Const.MY_OPACITY, 0);
        seekBarOpacity.setProgress(progress);
        tvOpacity.setText(progress * 100 / 204 + "%");
        seekBarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvOpacity.setText(progress * 100 / 204 + "%");
                SharePreferencesController.getInstance(getApplicationContext()).putInt(Const.MY_OPACITY, progress);
                startService(new Intent(MainActivity.this,
                        NightShiftService.class));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Seekbar brightness
        contentResolver = getContentResolver();
        try {
            currentBrightnessValue = Settings.System.getInt(contentResolver
                    , Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        seekBarBrightness.setProgress(currentBrightnessValue);
        tvBrightness.setText(currentBrightnessValue * 100 / 255 + "%");
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBrightness.setText(progress * 100 / 255 + "%");
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Switch Time
        if (SharePreferencesController.getInstance(MainActivity.this).getBoolean(Const.ALARM_APP, false)) {
            switchTime.setChecked(true);
            llTurnOnTime.setVisibility(View.VISIBLE);
            tvTurnOffTime.setVisibility(View.INVISIBLE);
        } else {
            switchTime.setChecked(false);
            llTurnOnTime.setVisibility(View.INVISIBLE);
            tvTurnOffTime.setVisibility(View.VISIBLE);
        }

        switchTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llTurnOnTime.setVisibility(View.VISIBLE);
                    tvTurnOffTime.setVisibility(View.INVISIBLE);
                } else {
                    if (pendingIntentOn != null) {
                        NightShiftService.alarmManager.cancel(pendingIntentOn);
                    }
                    if (pendingIntentOff != null) {
                        NightShiftService.alarmManager.cancel(pendingIntentOff);
                    }
                    SharePreferencesController.getInstance(MainActivity.this).putInt(Const.ALARM_COUNT, 0);
                    SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.ALARM_APP, false);
                    llTurnOnTime.setVisibility(View.INVISIBLE);
                    tvTurnOffTime.setVisibility(View.VISIBLE);
                }
            }
        });

        //Timepicker
        tvOnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerTime(tvOnTimePicker, getResources().getString(R.string.status_on), pendingIntentOn, 1);
            }
        });

        tvOffTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerTime(tvOffTimePicker, getResources().getString(R.string.status_off), pendingIntentOff, 2);
            }
        });

        //Notification
        if (SharePreferencesController.getInstance(this).getBoolean(Const.MY_SHOW_NOTIFICATION, true)) {
            switchNoti.setChecked(true);
            tvOnOffNoti.setText(getResources().getString(R.string.status_on));
        } else {
            switchNoti.setChecked(false);
            tvOnOffNoti.setText(getResources().getString(R.string.status_off));
        }

        switchNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvOnOffNoti.setText(getResources().getString(R.string.status_on));
                    SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.MY_SHOW_NOTIFICATION, true);
                    Commom.iShowNotification.onShowNotification();
                } else {
                    tvOnOffNoti.setText(getResources().getString(R.string.status_off));
                    SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.MY_SHOW_NOTIFICATION, false);
                    Commom.iShowNotification.onShowNotification();
                }
            }
        });

    }

    private void pickerTime(final TextView tv, final String on_off, final PendingIntent pendingIntent, final int request) {
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (on_off.equals(getResources().getString(R.string.status_on))) {
                    SharePreferencesController.getInstance(MainActivity.this).putInt(Const.ON_TIME_HOURS, hourOfDay);
                    SharePreferencesController.getInstance(MainActivity.this).putInt(Const.ON_TIME_MINUTE, minute);
                } else {
                    SharePreferencesController.getInstance(MainActivity.this).putInt(Const.OFF_TIME_HOURS, hourOfDay);
                    SharePreferencesController.getInstance(MainActivity.this).putInt(Const.OFF_TIME_MINUTE, minute);
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                calendar.set(0, 0, 0, hourOfDay, minute);
                tv.setText(on_off + " " + simpleDateFormat.format(calendar.getTime()));
                Commom.iClickSetTimer.setTimer(hourOfDay, minute, on_off, request, pendingIntent);
                Log.d("son.pt", hourOfDay + " " + minute);
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(R.layout.switch_layout);

        aSwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        aSwitch.setChecked(NightShiftService.isCheckedSwitch);
        Log.d("son.pt", "aswitch ");

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(Const.ACTION_TURN_ON);
                    switchNoti.setChecked(true);
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(Const.ACTION_TURN_OFF);
                    sendBroadcast(intent);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_info:
                startActivity(new Intent(MainActivity.this, ModeInfomation.class));
                break;
            case R.id.nav_stop:
                if (NightShiftService.view.getWindowToken() != null) {
                    Intent intent = new Intent(Const.ACTION_TURN_OFF);
                    sendBroadcast(intent);
                }
                SharePreferencesController.getInstance(MainActivity.this).putBoolean(Const.ALARM_APP, false);
                if (pendingIntentOff != null) {
                    NightShiftService.alarmManager.cancel(pendingIntentOff);
                }
                if (pendingIntentOn != null) {
                    NightShiftService.alarmManager.cancel(pendingIntentOn);
                }
//                NightShiftService.alarmManager.cancel(pendingIntentOn);
//                NightShiftService.alarmManager.cancel(pendingIntentOff);
                finish();
                stopService(new Intent(MainActivity.this, NightShiftService.class));
                System.exit(0);
                break;
            case R.id.nav_on_off:
                Intent intent = new Intent(Const.ACTION_NIGHT_MODE);
                sendBroadcast(intent);
                break;
            case R.id.nav_share:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "NightShift");
                    String shareMessage = "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(int position) {
        NightMode nightMode = arrNightModes.get(position);
        SharePreferencesController.getInstance(this).putInt(Const.KEY_MODE, position);
        tvTemperatureColor.setText(nightMode.getColorTemperature() + "K");
        Intent intent = new Intent(MainActivity.this,
                NightShiftService.class);
        intent.putExtra("rgb", new int[]{nightMode.getRed(), nightMode.getGreen(), nightMode.getBlue()});
        startService(intent);
        Toast.makeText(this, nightMode.getName() + "", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KEY_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                checkPermissionApp();
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_permistion), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MainActivity.ON_DO_NOT_DISTURB_CALLBACK_CODE) {
            this.requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp();
        }
    }

    @Override
    public void checkPermissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                SharePreferencesController.getInstance(this).putBoolean(Const.KEY_PERMISSION_CAMERA, true);
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            boolean result = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!result) {
                Toast.makeText(this, getResources().getString(R.string.no_flat), Toast.LENGTH_SHORT).show();
            } else {
                SharePreferencesController.getInstance(this).putBoolean(Const.KEY_PERMISSION_CAMERA, true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
    }

    private void initAdsFull() {
        mInterstitialAd = new InterstitialAd(this);
        MobileAds.initialize(this,
                getResources().getString(R.string.mobile_id));
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ads_full));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mInterstitialAd != null) {

                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        });

    }
}
