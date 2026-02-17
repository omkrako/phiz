package com.example.phiz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.phiz.activities.GradesActivity;
import com.example.phiz.activities.QuizActivity;
import com.example.phiz.activities.StudentHomeActivity;
import com.example.phiz.helpers.NotificationHelper;

/**
 * Broadcast receiver for handling notification button actions.
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionReceiver";

    public static final String ACTION_OPEN_QUIZ = "com.example.phiz.action.OPEN_QUIZ";
    public static final String ACTION_OPEN_GRADES = "com.example.phiz.action.OPEN_GRADES";
    public static final String ACTION_OPEN_HOME = "com.example.phiz.action.OPEN_HOME";
    public static final String ACTION_DISMISS = "com.example.phiz.action.DISMISS";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        Log.d(TAG, "Received action: " + action + ", notification ID: " + notificationId);

        if (action == null) {
            return;
        }

        // Cancel the notification if we have an ID
        if (notificationId != -1) {
            NotificationHelper.getInstance(context).cancelNotification(notificationId);
        }

        switch (action) {
            case ACTION_OPEN_QUIZ:
                openActivity(context, QuizActivity.class);
                break;

            case ACTION_OPEN_GRADES:
                openActivity(context, GradesActivity.class);
                break;

            case ACTION_OPEN_HOME:
                openActivity(context, StudentHomeActivity.class);
                break;

            case ACTION_DISMISS:
                // Just dismiss the notification (already cancelled above)
                Log.d(TAG, "Notification dismissed");
                break;

            default:
                Log.w(TAG, "Unknown action: " + action);
        }
    }

    private void openActivity(Context context, Class<?> activityClass) {
        Intent intent = new Intent(context, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
