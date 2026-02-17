package com.example.phiz.helpers;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.phiz.models.NotificationPreferences;
import com.example.phiz.workers.InactivityCheckWorker;
import com.example.phiz.workers.StudyReminderWorker;
import com.example.phiz.workers.WeeklyProgressWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for scheduling WorkManager workers for notifications.
 */
public class WorkerScheduler {
    private static final String TAG = "WorkerScheduler";

    /**
     * Schedule all notification workers based on user preferences.
     */
    public static void scheduleAllWorkers(Context context, NotificationPreferences prefs) {
        if (prefs == null) {
            prefs = NotificationPreferences.createDefault();
        }

        if (prefs.isStudyReminders()) {
            scheduleStudyReminder(context, prefs);
        } else {
            cancelStudyReminder(context);
        }

        // Always schedule inactivity check
        scheduleInactivityCheck(context);

        if (prefs.isWeeklyProgress()) {
            scheduleWeeklyProgress(context);
        } else {
            cancelWeeklyProgress(context);
        }

        Log.d(TAG, "All workers scheduled");
    }

    /**
     * Schedule daily study reminder at user's preferred time.
     */
    public static void scheduleStudyReminder(Context context, NotificationPreferences prefs) {
        // Calculate initial delay to the next reminder time
        long initialDelay = calculateInitialDelay(prefs.getReminderHour(), prefs.getReminderMinute());

        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                StudyReminderWorker.class,
                1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(StudyReminderWorker.WORK_NAME)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        StudyReminderWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        reminderRequest
                );

        Log.d(TAG, "Study reminder scheduled with initial delay: " + initialDelay + "ms");
    }

    /**
     * Cancel study reminder worker.
     */
    public static void cancelStudyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(StudyReminderWorker.WORK_NAME);
        Log.d(TAG, "Study reminder cancelled");
    }

    /**
     * Schedule inactivity check worker to run daily.
     */
    public static void scheduleInactivityCheck(Context context) {
        PeriodicWorkRequest inactivityRequest = new PeriodicWorkRequest.Builder(
                InactivityCheckWorker.class,
                1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS) // Start after 1 hour
                .addTag(InactivityCheckWorker.WORK_NAME)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        InactivityCheckWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        inactivityRequest
                );

        Log.d(TAG, "Inactivity check scheduled");
    }

    /**
     * Cancel inactivity check worker.
     */
    public static void cancelInactivityCheck(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(InactivityCheckWorker.WORK_NAME);
        Log.d(TAG, "Inactivity check cancelled");
    }

    /**
     * Schedule weekly progress worker to run once a week.
     */
    public static void scheduleWeeklyProgress(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest progressRequest = new PeriodicWorkRequest.Builder(
                WeeklyProgressWorker.class,
                7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(calculateDelayToSunday(), TimeUnit.MILLISECONDS)
                .addTag(WeeklyProgressWorker.WORK_NAME)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        WeeklyProgressWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        progressRequest
                );

        Log.d(TAG, "Weekly progress scheduled");
    }

    /**
     * Cancel weekly progress worker.
     */
    public static void cancelWeeklyProgress(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WeeklyProgressWorker.WORK_NAME);
        Log.d(TAG, "Weekly progress cancelled");
    }

    /**
     * Cancel all notification workers.
     */
    public static void cancelAllWorkers(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelUniqueWork(StudyReminderWorker.WORK_NAME);
        workManager.cancelUniqueWork(InactivityCheckWorker.WORK_NAME);
        workManager.cancelUniqueWork(WeeklyProgressWorker.WORK_NAME);
        Log.d(TAG, "All workers cancelled");
    }

    /**
     * Calculate the initial delay until the specified time.
     */
    private static long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // If the target time has passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    /**
     * Calculate delay until next Sunday at 10:00 AM.
     */
    private static long calculateDelayToSunday() {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        target.set(Calendar.HOUR_OF_DAY, 10);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // If it's already past Sunday 10 AM this week, go to next week
        if (target.before(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}
