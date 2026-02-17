package com.example.phiz.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.phiz.helpers.NotificationHelper;

/**
 * Worker that checks weekly score progress and shows notifications.
 * Runs weekly to report on score improvements.
 */
public class WeeklyProgressWorker extends Worker {
    private static final String TAG = "WeeklyProgressWorker";

    public static final String WORK_NAME = "weekly_progress_work";
    private static final String PREFS_NAME = "progress_prefs";
    private static final String KEY_LAST_WEEK_SCORE = "last_week_score";
    private static final String KEY_CURRENT_SCORE = "current_score";

    public WeeklyProgressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Weekly progress worker running");

        try {
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            int lastWeekScore = prefs.getInt(KEY_LAST_WEEK_SCORE, 0);
            int currentScore = prefs.getInt(KEY_CURRENT_SCORE, 0);
            int improvement = currentScore - lastWeekScore;

            Log.d(TAG, "Last week score: " + lastWeekScore + ", Current score: " + currentScore);

            // Show weekly progress notification
            NotificationHelper.getInstance(getApplicationContext())
                    .showWeeklyProgressNotification(improvement, currentScore);

            // Update last week's score for next comparison
            prefs.edit().putInt(KEY_LAST_WEEK_SCORE, currentScore).apply();

            Log.d(TAG, "Weekly progress notification shown");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error showing weekly progress", e);
            return Result.failure();
        }
    }

    /**
     * Update the current score.
     * Call this after a quiz is completed.
     */
    public static void updateCurrentScore(Context context, int score) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_CURRENT_SCORE, score).apply();
    }
}
