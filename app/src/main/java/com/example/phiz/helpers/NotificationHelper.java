package com.example.phiz.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.phiz.R;
import com.example.phiz.activities.GradesActivity;
import com.example.phiz.activities.QuizActivity;
import com.example.phiz.activities.StudentHomeActivity;

/**
 * Singleton helper class for managing notifications.
 * Handles notification channel creation and displaying notifications.
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    // Notification Channel IDs
    public static final String CHANNEL_QUIZ = "quiz_notifications";
    public static final String CHANNEL_GRADE = "grade_notifications";
    public static final String CHANNEL_ACHIEVEMENT = "achievement_notifications";
    public static final String CHANNEL_REMINDER = "reminder_notifications";
    public static final String CHANNEL_TEACHER = "teacher_notifications";

    // Notification IDs
    public static final int NOTIFICATION_ID_QUIZ = 1001;
    public static final int NOTIFICATION_ID_GRADE = 1002;
    public static final int NOTIFICATION_ID_ACHIEVEMENT = 1003;
    public static final int NOTIFICATION_ID_REMINDER = 1004;
    public static final int NOTIFICATION_ID_TEACHER = 1005;
    public static final int NOTIFICATION_ID_NEW_CONTENT = 1006;
    public static final int NOTIFICATION_ID_INACTIVITY = 1007;
    public static final int NOTIFICATION_ID_WEEKLY_PROGRESS = 1008;

    // Deep link actions
    public static final String ACTION_OPEN_QUIZ = "com.example.phiz.OPEN_QUIZ";
    public static final String ACTION_OPEN_GRADES = "com.example.phiz.OPEN_GRADES";
    public static final String ACTION_OPEN_HOME = "com.example.phiz.OPEN_HOME";

    // Singleton instance
    private static NotificationHelper instance;
    private final Context context;

    private NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized NotificationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
        return instance;
    }

    /**
     * Create all notification channels. Call this in Application.onCreate()
     */
    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            // Quiz notifications channel (HIGH importance)
            NotificationChannel quizChannel = new NotificationChannel(
                    CHANNEL_QUIZ,
                    "Quiz Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            quizChannel.setDescription("Notifications about new quizzes and quiz deadlines");
            quizChannel.enableVibration(true);
            notificationManager.createNotificationChannel(quizChannel);

            // Grade notifications channel (HIGH importance)
            NotificationChannel gradeChannel = new NotificationChannel(
                    CHANNEL_GRADE,
                    "Grade Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            gradeChannel.setDescription("Notifications about grades and score updates");
            gradeChannel.enableVibration(true);
            notificationManager.createNotificationChannel(gradeChannel);

            // Achievement notifications channel (DEFAULT importance)
            NotificationChannel achievementChannel = new NotificationChannel(
                    CHANNEL_ACHIEVEMENT,
                    "Achievements",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            achievementChannel.setDescription("Notifications about achievements and milestones");
            notificationManager.createNotificationChannel(achievementChannel);

            // Reminder notifications channel (DEFAULT importance)
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Daily study reminders and inactivity notifications");
            notificationManager.createNotificationChannel(reminderChannel);

            // Teacher notifications channel (HIGH importance)
            NotificationChannel teacherChannel = new NotificationChannel(
                    CHANNEL_TEACHER,
                    "Teacher Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            teacherChannel.setDescription("Notifications for teachers about student activity");
            teacherChannel.enableVibration(true);
            notificationManager.createNotificationChannel(teacherChannel);
        }
    }

    // ==================== STUDENT NOTIFICATIONS ====================

    /**
     * Show notification when a new quiz is available
     */
    public void showNewQuizNotification(String quizTitle) {
        Intent intent = new Intent(context, QuizActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_QUIZ)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Quiz Available!")
                .setContentText(quizTitle != null ? quizTitle : "A new physics quiz is ready for you")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_QUIZ, builder);
    }

    /**
     * Show notification when grade is posted (after quiz completion)
     */
    public void showGradePostedNotification(int score, int totalQuestions) {
        Intent intent = new Intent(context, GradesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message = "You scored " + score + " points!";
        if (score >= totalQuestions * 20) { // Perfect score check
            message += " Perfect score!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GRADE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Quiz Complete!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_GRADE, builder);
    }

    /**
     * Show achievement unlocked notification
     */
    public void showAchievementNotification(String achievementTitle, String achievementDescription) {
        Intent intent = new Intent(context, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENT)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Achievement Unlocked!")
                .setContentText(achievementTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(achievementTitle + "\n" + achievementDescription))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_ACHIEVEMENT, builder);
    }

    /**
     * Show daily study reminder notification
     */
    public void showStudyReminderNotification() {
        Intent intent = new Intent(context, QuizActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to Study!")
                .setContentText("Take a quick physics quiz to keep your skills sharp")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_REMINDER, builder);
    }

    /**
     * Show inactivity reminder notification
     */
    public void showInactivityReminderNotification(int daysSinceLastActivity) {
        Intent intent = new Intent(context, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("We Miss You!")
                .setContentText("It's been " + daysSinceLastActivity + " days since your last visit. Come back and learn some physics!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_INACTIVITY, builder);
    }

    /**
     * Show weekly progress notification
     */
    public void showWeeklyProgressNotification(int scoreImprovement, int currentScore) {
        Intent intent = new Intent(context, GradesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message;
        if (scoreImprovement > 0) {
            message = "You improved by " + scoreImprovement + " points this week! Current score: " + currentScore;
        } else {
            message = "Your current score is " + currentScore + " points. Keep practicing!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GRADE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Weekly Progress Report")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_WEEKLY_PROGRESS, builder);
    }

    /**
     * Show new content available notification
     */
    public void showNewContentNotification(String contentType) {
        Intent intent = new Intent(context, QuizActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_QUIZ)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Content Available!")
                .setContentText("Your teacher added new " + contentType + ". Check it out!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_NEW_CONTENT, builder);
    }

    // ==================== TEACHER NOTIFICATIONS ====================

    /**
     * Show notification to teacher when a student completes a quiz
     */
    public void showQuizCompletionNotificationForTeacher(String studentName, int score) {
        Intent intent = new Intent(context, GradesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TEACHER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Quiz Completed")
                .setContentText(studentName + " completed a quiz with " + score + " points")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_TEACHER, builder);
    }

    /**
     * Show low score alert notification to teacher
     */
    public void showLowScoreAlertForTeacher(String studentName, int score, int percentage) {
        Intent intent = new Intent(context, GradesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TEACHER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Low Score Alert")
                .setContentText(studentName + " scored " + percentage + "% on their quiz. They may need help.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        showNotification(NOTIFICATION_ID_TEACHER + 1, builder);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Show a notification using NotificationManagerCompat
     */
    private void showNotification(int notificationId, NotificationCompat.Builder builder) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Permission not granted - ignore
        }
    }

    /**
     * Cancel a specific notification
     */
    public void cancelNotification(int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    /**
     * Check if notifications are enabled for the app
     */
    public boolean areNotificationsEnabled() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        return notificationManager.areNotificationsEnabled();
    }

    /**
     * Check if a specific channel is enabled (Android O+)
     */
    public boolean isChannelEnabled(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        return true;
    }
}
