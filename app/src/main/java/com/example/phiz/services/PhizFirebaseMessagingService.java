package com.example.phiz.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.phiz.helpers.FCMTokenManager;
import com.example.phiz.helpers.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Firebase Cloud Messaging service for handling push notifications.
 * Handles both token refresh events and incoming messages.
 */
public class PhizFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "PhizFCMService";

    // Notification types from FCM data payload
    public static final String TYPE_NEW_QUIZ = "new_quiz";
    public static final String TYPE_QUIZ_DEADLINE = "quiz_deadline";
    public static final String TYPE_NEW_CONTENT = "new_content";
    public static final String TYPE_QUIZ_COMPLETED = "quiz_completed";
    public static final String TYPE_LOW_SCORE_ALERT = "low_score_alert";
    public static final String TYPE_CLASS_PROGRESS = "class_progress";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Save the new token to Firestore
        FCMTokenManager.getInstance(this).saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());

        // Check if message contains a notification payload (shown automatically when app in background)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification payload: " + remoteMessage.getNotification().getBody());
            handleNotificationPayload(remoteMessage.getNotification());
        }

        // Check if message contains a data payload (always handled by us)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }

    /**
     * Handle notification payload from FCM.
     * This is typically used for simple notifications that show automatically.
     */
    private void handleNotificationPayload(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();

        // Show notification using our helper for consistent styling
        NotificationHelper helper = NotificationHelper.getInstance(this);

        // Default to quiz channel if we can't determine type
        NotificationHelper.getInstance(this).showNewQuizNotification(body);
    }

    /**
     * Handle data payload from FCM.
     * This allows for more complex notification logic.
     */
    private void handleDataPayload(Map<String, String> data) {
        String type = data.get("type");
        NotificationHelper helper = NotificationHelper.getInstance(this);

        if (type == null) {
            Log.w(TAG, "No notification type in data payload");
            return;
        }

        switch (type) {
            case TYPE_NEW_QUIZ:
                handleNewQuizNotification(data, helper);
                break;

            case TYPE_QUIZ_DEADLINE:
                handleQuizDeadlineNotification(data, helper);
                break;

            case TYPE_NEW_CONTENT:
                handleNewContentNotification(data, helper);
                break;

            case TYPE_QUIZ_COMPLETED:
                handleQuizCompletedNotification(data, helper);
                break;

            case TYPE_LOW_SCORE_ALERT:
                handleLowScoreAlertNotification(data, helper);
                break;

            case TYPE_CLASS_PROGRESS:
                handleClassProgressNotification(data, helper);
                break;

            default:
                Log.w(TAG, "Unknown notification type: " + type);
        }
    }

    private void handleNewQuizNotification(Map<String, String> data, NotificationHelper helper) {
        String quizTitle = data.get("quiz_title");
        helper.showNewQuizNotification(quizTitle != null ? quizTitle : "New Physics Quiz");
    }

    private void handleQuizDeadlineNotification(Map<String, String> data, NotificationHelper helper) {
        String quizTitle = data.get("quiz_title");
        String deadline = data.get("deadline");
        // For now, reuse the new quiz notification
        helper.showNewQuizNotification("Deadline approaching: " + (quizTitle != null ? quizTitle : "Quiz"));
    }

    private void handleNewContentNotification(Map<String, String> data, NotificationHelper helper) {
        String contentType = data.get("content_type");
        helper.showNewContentNotification(contentType != null ? contentType : "questions");
    }

    private void handleQuizCompletedNotification(Map<String, String> data, NotificationHelper helper) {
        String studentName = data.get("student_name");
        String scoreStr = data.get("score");
        int score = 0;
        try {
            score = Integer.parseInt(scoreStr != null ? scoreStr : "0");
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid score value: " + scoreStr);
        }
        helper.showQuizCompletionNotificationForTeacher(
                studentName != null ? studentName : "A student",
                score
        );
    }

    private void handleLowScoreAlertNotification(Map<String, String> data, NotificationHelper helper) {
        String studentName = data.get("student_name");
        String scoreStr = data.get("score");
        String percentageStr = data.get("percentage");
        int score = 0;
        int percentage = 0;
        try {
            score = Integer.parseInt(scoreStr != null ? scoreStr : "0");
            percentage = Integer.parseInt(percentageStr != null ? percentageStr : "0");
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid score/percentage value");
        }
        helper.showLowScoreAlertForTeacher(
                studentName != null ? studentName : "A student",
                score,
                percentage
        );
    }

    private void handleClassProgressNotification(Map<String, String> data, NotificationHelper helper) {
        // This would typically be a weekly summary for teachers
        // For now, we'll just show a simple notification
        String summary = data.get("summary");
        if (summary != null) {
            helper.showQuizCompletionNotificationForTeacher("Class Progress", 0);
        }
    }
}
