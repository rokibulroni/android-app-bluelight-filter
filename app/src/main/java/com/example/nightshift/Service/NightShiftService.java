package com.example.nightshift.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.nightshift.Broadcast.NightModeReceiver;
import com.example.nightshift.Listener.IClickSetTimer;
import com.example.nightshift.Listener.IShowNotification;
import com.example.nightshift.activity.MainActivity;
import com.example.nightshift.Model.NightMode;
import com.example.nightshift.R;
import com.example.nightshift.Utils.Commom;
import com.example.nightshift.Utils.Const;
import com.example.nightshift.Utils.CustomNotification;
import com.example.nightshift.Utils.SharePreferencesController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by SonPham on 18/07/2019
 * Company : Gpaddy
 */
public class NightShiftService extends Service implements IShowNotification, IClickSetTimer {
    public static View view;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private Context context;
    private List<NightMode> arrNightModes;

    private int current_alpha;
    private int red;
    private int green;
    private int blue;

    private AudioManager audioManager;
    public static AlarmManager alarmManager;
    private MenuItem menuItem;

    private PendingIntent pendingIntentOn, pendingIntentOff;

    public static boolean isCheckedSwitch;

    private IntentFilter intentFilter = new IntentFilter();
    private BroadcastNightShift broadcastNightShift = new BroadcastNightShift();

    //Flashlight
    private Camera camera;
    private Camera.Parameters parameters;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        Commom.iShowNotification = this;
        Commom.iClickSetTimer = this;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        intentFilter.addAction(Const.ACTION_NIGHT_MODE);
        intentFilter.addAction(Const.ACTION_FLASH_LIGHT);
        intentFilter.addAction(Const.ACTION_TURN_ON);
        intentFilter.addAction(Const.ACTION_TURN_OFF);
        intentFilter.addAction(Const.ACTION_MUTE);

        registerReceiver(broadcastNightShift, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getIntArrayExtra("rgb") != null) {
                int[] rgb = intent.getIntArrayExtra("rgb");
                red = rgb[0];
                green = rgb[1];
                blue = rgb[2];
            } else {
                arrNightModes = Commom.arrNightModes;
                red = arrNightModes.get(SharePreferencesController.getInstance(context).getInt(Const.KEY_MODE, 0)).getRed();
                green = arrNightModes.get(SharePreferencesController.getInstance(context).getInt(Const.KEY_MODE, 0)).getGreen();
                blue = arrNightModes.get(SharePreferencesController.getInstance(context).getInt(Const.KEY_MODE, 0)).getBlue();
            }
            updateMyView();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateMyView() {
        params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = Commom.getHeightDevice(context);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        params.format = PixelFormat.TRANSLUCENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        current_alpha = SharePreferencesController.getInstance(context).getInt(Const.MY_OPACITY, 100);

        CustomNotification.showNotificationBar(getApplicationContext(), this);

        if (view != null) {
            view.setBackgroundColor(Color.argb(current_alpha,
                    red, green, blue));
            view.invalidate();
        } else {
            view = new View(this);
            view.setBackgroundColor(Color.argb(current_alpha,
                    red, green, blue));
            windowManager.addView(view, params);
            isCheckedSwitch = true;
            setColorItem(R.id.tv_night_mode, R.id.img_night_mode, R.color.color_yellow);
        }

        //Menuitem
        Menu menu = MainActivity.navigationView.getMenu();
        menuItem = menu.findItem(R.id.nav_on_off);
    }

    @Override
    public void onShowNotification() {
        CustomNotification.showNotificationBar(getApplicationContext(), this);
        Log.d("son.pt", "clickonshownotification");
    }

    @Override
    public void setTimer(int hour, int minute, String on_off, int request, PendingIntent pendingIntent) {
        if (pendingIntentOn == null && pendingIntentOff == null) {
            pendingIntentOn = pendingIntent;
        } else {
            pendingIntentOff = pendingIntent;
        }
        SharePreferencesController.getInstance(context).putBoolean(Const.ALARM_APP, true);
        Date date = new Date();

        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();

        cal_now.setTime(date);
        cal_alarm.setTime(date);

        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE, minute);
        cal_alarm.set(Calendar.SECOND, 0);

        if (cal_alarm.before(cal_now)) {
            cal_alarm.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, NightModeReceiver.class);
        intent.putExtra(on_off, 1);
        pendingIntent = PendingIntent.getBroadcast(context, request, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
    }

    class BroadcastNightShift extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Const.ACTION_NIGHT_MODE:
                    boolean isOnePending = pendingIntentOff == null || pendingIntentOn == null;
                    int count = SharePreferencesController.getInstance(context).getInt(Const.ALARM_COUNT, 0);
                    if (isOnePending && count == 1) {
                        SharePreferencesController.getInstance(context).putInt(Const.ALARM_COUNT, 0);
                        SharePreferencesController.getInstance(context).putBoolean(Const.ALARM_APP, false);
                    }

                    if (count == 2) {
                        SharePreferencesController.getInstance(context).putInt(Const.ALARM_COUNT, 0);
                        SharePreferencesController.getInstance(context).putBoolean(Const.ALARM_APP, false);
                    }
                    if (!MainActivity.isAlive) {
                        if (view.getWindowToken() != null) {
                            turnOff();
                        } else {
                            turnOn();
                        }
                    }
                    break;
                case Const.ACTION_FLASH_LIGHT:
                    if (!Commom.isFlashlightAvailable(context)) {
                        Toast.makeText(context, "Đèn pin không khả dụng", Toast.LENGTH_SHORT).show();
                    } else {
                        if (SharePreferencesController.getInstance(context).getBoolean(Const.KEY_PERMISSION_CAMERA, false)) {
                            actionFlashlight(context);
                        } else {
                            Commom.iClickCheckCamera.checkPermissionCamera();
                        }
                    }
                    break;
                case Const.ACTION_MUTE:
                    changedRing();
                    break;
                case Const.ACTION_TURN_ON:
                    turnOn();
                    break;
                case Const.ACTION_TURN_OFF:
                    turnOff();
                    break;
            }
        }
    }

    private void changedRing() {
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                CustomNotification.getRemoteViews().setImageViewResource(R.id.img_mute, R.drawable.ic_volume_off_black_24dp);
                CustomNotification.getRemoteViews().setTextViewText(R.id.tv_mute, "Mute");
                startForeground(Const.NOTIFICATION_ID, CustomNotification.getmBuilder().build());
                audioManager.setRingerMode(0);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                CustomNotification.getRemoteViews().setImageViewResource(R.id.img_mute, R.drawable.ic_volume_up_black_24dp);
                CustomNotification.getRemoteViews().setTextViewText(R.id.tv_mute, "Ring");
                startForeground(Const.NOTIFICATION_ID, CustomNotification.getmBuilder().build());
                audioManager.setRingerMode(2);
                break;
            case AudioManager.RINGER_MODE_SILENT:
                CustomNotification.getRemoteViews().setImageViewResource(R.id.img_mute, R.drawable.ic_vibration_black_24dp);
                CustomNotification.getRemoteViews().setTextViewText(R.id.tv_mute, "Vibration");
                startForeground(Const.NOTIFICATION_ID, CustomNotification.getmBuilder().build());
                audioManager.setRingerMode(1);
                break;
        }
    }

    public void turnOn() {
        menuItem.setTitle(getResources().getString(R.string.status_off));
        SharePreferencesController.getInstance(context).putBoolean(Const.KEY_NIGHT_MODE, true);
        windowManager.addView(view, params);
        setColorItem(R.id.tv_night_mode, R.id.img_night_mode, R.color.color_yellow);
        isCheckedSwitch = true;
    }

    public void turnOff() {
        menuItem.setTitle(getResources().getString(R.string.status_on));
        SharePreferencesController.getInstance(context).putBoolean(Const.KEY_NIGHT_MODE, false);
        windowManager.removeView(view);
        setColorItem(R.id.tv_night_mode, R.id.img_night_mode, android.R.color.white);
        isCheckedSwitch = false;
    }

    private void setColorItem(int tv, int img, int color) {
        if (SharePreferencesController.getInstance(this).getBoolean(Const.MY_SHOW_NOTIFICATION, true)) {
            CustomNotification.getRemoteViews().setTextColor(tv, getResources().getColor(color));
            CustomNotification.getRemoteViews().setInt(img, "setColorFilter", getResources().getColor(color));
            startForeground(Const.NOTIFICATION_ID, CustomNotification.getmBuilder().build());
        }
    }

    private void actionFlashlight(Context context) {
        boolean isTurnOn = SharePreferencesController.getInstance(context).getBoolean(Const.KEY_FLASH_LIGHT, false);
        camera = Camera.open();
        if (isTurnOn) {
            setColorItem(R.id.tv_flash_light, R.id.img_flash_light, android.R.color.white);
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            camera.release();
            SharePreferencesController.getInstance(context).putBoolean(Const.KEY_FLASH_LIGHT, false);
        } else {
            setColorItem(R.id.tv_flash_light, R.id.img_flash_light, R.color.color_yellow);
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            SharePreferencesController.getInstance(context).putBoolean(Const.KEY_FLASH_LIGHT, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("son.pt", "onDestroy service");
        unregisterReceiver(broadcastNightShift);
    }
}
