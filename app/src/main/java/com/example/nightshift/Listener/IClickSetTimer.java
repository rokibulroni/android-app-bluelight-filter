package com.example.nightshift.Listener;

import android.app.PendingIntent;

/**
 * Created by SonPham on 25/07/2019
 * Company : Gpaddy
 */
public interface IClickSetTimer {
    void setTimer(int hour, int minute, String on_off, int request, PendingIntent pendingIntent);
}
