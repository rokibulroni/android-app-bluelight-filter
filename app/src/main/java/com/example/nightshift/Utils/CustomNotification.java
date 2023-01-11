package com.example.nightshift.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.nightshift.activity.MainActivity;
import com.example.nightshift.R;
import com.example.nightshift.Service.NightShiftService;

/**
 * Created by SonPham on 19/07/2019
 * Company : Gpaddy
 */
public class CustomNotification {
    private static RemoteViews remoteViews;
    private static NotificationCompat.Builder mBuilder;

    private static Intent mNotificationIntent;
    private static PendingIntent mContentIntent;
    private static NotificationManager mNotificationManager;

    private static boolean my_enable_shownotification;

    public static void showNotificationBar(Context context, NightShiftService service) {
        my_enable_shownotification = SharePreferencesController.getInstance(context).getBoolean(
                Const.MY_SHOW_NOTIFICATION, true);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_notificationbar);

        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.ic_notification_default);
        } else {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        mBuilder.setContent(remoteViews);
        mBuilder.setOngoing(true);

        mNotificationIntent = new Intent(context, MainActivity.class);
        mContentIntent = PendingIntent.getActivity(context, Const.REQUEST_CODE_NOTIFICATION, mNotificationIntent, 0);
        mBuilder.setContentIntent(mContentIntent);

        PendingIntent nightModeIntent = PendingIntent.getBroadcast(context, Const.REQUEST_CODE_NOTIFICATION,
                new Intent(Const.ACTION_NIGHT_MODE), 0);
        remoteViews.setOnClickPendingIntent(R.id.ll_night_mode, nightModeIntent);

        PendingIntent flashLightIntent = PendingIntent.getBroadcast(context, Const.REQUEST_CODE_NOTIFICATION,
                new Intent(Const.ACTION_FLASH_LIGHT), 0);
        remoteViews.setOnClickPendingIntent(R.id.ll_flash_light, flashLightIntent);

        PendingIntent muteIntent = PendingIntent.getBroadcast(context, Const.REQUEST_CODE_NOTIFICATION,
                new Intent(Const.ACTION_MUTE), 0);
        remoteViews.setOnClickPendingIntent(R.id.ll_mute, muteIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel",
                    "NightShift", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channel.getId());
        }

        if (my_enable_shownotification) {
            Notification notification = mBuilder.build();
            service.startForeground(Const.NOTIFICATION_ID, notification);
        } else {
            service.stopForeground(true);
        }

    }

    public static NotificationManager getmNotificationManager() {
        return mNotificationManager;
    }

    public static RemoteViews getRemoteViews() {
        return remoteViews;
    }

    public static NotificationCompat.Builder getmBuilder() {
        return mBuilder;
    }
}
