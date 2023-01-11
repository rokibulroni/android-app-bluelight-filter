package com.example.nightshift.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.example.nightshift.Listener.IClickCheckCamera;
import com.example.nightshift.Listener.IClickSetTimer;
import com.example.nightshift.Listener.IShowNotification;
import com.example.nightshift.Model.NightMode;
import com.example.nightshift.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SonPham on 19/07/2019
 * Company : Gpaddy
 */
public class Commom {
    public static List<NightMode> arrNightModes;
    public static IClickCheckCamera iClickCheckCamera;
    public static IShowNotification iShowNotification;
    public static IClickSetTimer iClickSetTimer;
    public static Context mContext;

    public static int getHeightDevice(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId) + height;
        }
        return height;
    }

    public static void setiClickCheckCamera(IClickCheckCamera iClickCheckCamera) {
        Commom.iClickCheckCamera = iClickCheckCamera;
    }

    public static void setData() {
        arrNightModes = new ArrayList<>();
        arrNightModes.add(new NightMode("Night Shift", R.drawable.ic_color_def_normal, R.drawable.ic_color_def_pressed, 3200, false, 255, 187, 120));
        arrNightModes.add(new NightMode("Candlelight", R.drawable.ic_color_1_normal, R.drawable.ic_color_1_pressed, 1800, false, 255, 126, 0));
        arrNightModes.add(new NightMode("Dawn", R.drawable.ic_color_2_normal, R.drawable.ic_color_2_pressed, 2000, false, 255, 138, 18));
        arrNightModes.add(new NightMode("Incandescent Lamp", R.drawable.ic_color_3_normal, R.drawable.ic_color_3_pressed, 2700, false, 255, 169, 87));
        arrNightModes.add(new NightMode("Fluorescent Lamp", R.drawable.ic_color_4_normal, R.drawable.ic_color_4_pressed, 3400, false, 255, 193, 132));
        arrNightModes.add(new NightMode("Sunlight", R.drawable.ic_color_eclipse_normal, R.drawable.ic_color_eclipse_pressed, 4500, false, 255, 219, 186));
        arrNightModes.add(new NightMode("Eclipse", R.drawable.ic_color_forest_normal, R.drawable.ic_color_forest_pressed, 500, false, 217, 217, 217));
        arrNightModes.add(new NightMode("Forest", R.drawable.ic_color_5_normal,R.drawable.ic_color_5_pressed, 3300, false, 255, 190, 126));
        arrNightModes.get(0).setChoose(true);
    }

    public static boolean isFlashlightAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
