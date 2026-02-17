package com.example.phiz.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.phiz.helpers.NotificationHelper;

import java.util.concurrent.TimeUnit;

/**
 * Worker that checks for user inactivity and shows reminder notifications.
 * Runs periodically to check if user hasn't used the app in a while.
 */
public class InactivityCheckWorker extends Worker {
    private static final String TAG = "InactivityCheckWorker";

    public static final String WORK_NAME = "inactivity_check_work";
    private static final String PREFS_NAME = "activity_prefs";
    private static final String KEY_LAST_ACTIVITY = "last_activity_timestamp";
    private static final int INACTIVITY_THRESHOLD_DAYS = 3;

    public InactivityCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Inactivity check worker running");

        try {
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            long lastActivityTime = prefs.getLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
            long currentTime = System.currentTimeMillis();
            long daysSinceLastActivity = TimeUnit.MILLISECONDS.toDays(currentTime - lastActivityTime);

            Log.d(TAG, "Days since last activity: " + daysSinceLastActivity);

            if (daysSinceLastActivity >= INACTIVITY_THRESHOLD_DAYS) {
                // Show inactivity reminder notification
                NotificationHelper.getInstance(getApplicationContext())
                        .showInactivityReminderNotification((int) daysSinceLastActivity);

                Log.d(TAG, "Inactivity notification shown");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error checking inactivity", e);
            return Result.failure();
        }
    }

    /**
     * Update the last activity timestamp.
     * Call this whenever the user performs an action in the app.
     */
    public static void updateLastActivity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }
}
