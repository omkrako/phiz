package com.example.phiz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.phiz.helpers.WorkerScheduler;
import com.example.phiz.models.NotificationPreferences;

/**
 * Broadcast receiver that reschedules notification workers after device reboot.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed, rescheduling notification workers");

            // Reschedule workers with default preferences
            // The actual preferences will be loaded when the user opens the app
            WorkerScheduler.scheduleAllWorkers(context, NotificationPreferences.createDefault());
        }
    }
}
