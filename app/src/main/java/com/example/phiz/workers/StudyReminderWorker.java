package com.example.phiz.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.phiz.helpers.NotificationHelper;

/**
 * Worker that shows daily study reminder notifications.
 * Scheduled via WorkManager based on user's notification preferences.
 */
public class StudyReminderWorker extends Worker {
    private static final String TAG = "StudyReminderWorker";

    public static final String WORK_NAME = "study_reminder_work";

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Study reminder worker running");

        try {
            // Show the study reminder notification
            NotificationHelper.getInstance(getApplicationContext())
                    .showStudyReminderNotification();

            Log.d(TAG, "Study reminder notification shown");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error showing study reminder", e);
            return Result.failure();
        }
    }
}
