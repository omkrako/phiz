package com.example.phiz.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model class for user notification preferences.
 * Stores settings for different notification types and reminder schedules.
 */
public class NotificationPreferences {
    private boolean quizNotifications;
    private boolean gradeNotifications;
    private boolean achievementNotifications;
    private boolean studyReminders;
    private boolean weeklyProgress;
    private String reminderTime; // Format: "HH:mm" (e.g., "18:00")
    private List<String> reminderDays; // Days of week: "MONDAY", "TUESDAY", etc.

    // Default constructor for Firestore
    public NotificationPreferences() {
        // Set defaults - all enabled
        this.quizNotifications = true;
        this.gradeNotifications = true;
        this.achievementNotifications = true;
        this.studyReminders = true;
        this.weeklyProgress = true;
        this.reminderTime = "18:00";
        this.reminderDays = new ArrayList<>(Arrays.asList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
        ));
    }

    /**
     * Create preferences with all notifications enabled (default)
     */
    public static NotificationPreferences createDefault() {
        return new NotificationPreferences();
    }

    /**
     * Create preferences with all notifications disabled
     */
    public static NotificationPreferences createDisabled() {
        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setQuizNotifications(false);
        prefs.setGradeNotifications(false);
        prefs.setAchievementNotifications(false);
        prefs.setStudyReminders(false);
        prefs.setWeeklyProgress(false);
        return prefs;
    }

    // Getters and setters
    public boolean isQuizNotifications() {
        return quizNotifications;
    }

    public void setQuizNotifications(boolean quizNotifications) {
        this.quizNotifications = quizNotifications;
    }

    public boolean isGradeNotifications() {
        return gradeNotifications;
    }

    public void setGradeNotifications(boolean gradeNotifications) {
        this.gradeNotifications = gradeNotifications;
    }

    public boolean isAchievementNotifications() {
        return achievementNotifications;
    }

    public void setAchievementNotifications(boolean achievementNotifications) {
        this.achievementNotifications = achievementNotifications;
    }

    public boolean isStudyReminders() {
        return studyReminders;
    }

    public void setStudyReminders(boolean studyReminders) {
        this.studyReminders = studyReminders;
    }

    public boolean isWeeklyProgress() {
        return weeklyProgress;
    }

    public void setWeeklyProgress(boolean weeklyProgress) {
        this.weeklyProgress = weeklyProgress;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public List<String> getReminderDays() {
        return reminderDays;
    }

    public void setReminderDays(List<String> reminderDays) {
        this.reminderDays = reminderDays;
    }

    /**
     * Get reminder hour from reminderTime
     */
    public int getReminderHour() {
        if (reminderTime == null || !reminderTime.contains(":")) {
            return 18; // Default to 6 PM
        }
        try {
            return Integer.parseInt(reminderTime.split(":")[0]);
        } catch (NumberFormatException e) {
            return 18;
        }
    }

    /**
     * Get reminder minute from reminderTime
     */
    public int getReminderMinute() {
        if (reminderTime == null || !reminderTime.contains(":")) {
            return 0;
        }
        try {
            return Integer.parseInt(reminderTime.split(":")[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if reminders are enabled for a specific day
     */
    public boolean isReminderEnabledForDay(String dayOfWeek) {
        if (reminderDays == null) {
            return false;
        }
        return reminderDays.contains(dayOfWeek.toUpperCase());
    }

    /**
     * Check if any notification type is enabled
     */
    public boolean hasAnyNotificationEnabled() {
        return quizNotifications || gradeNotifications || achievementNotifications
                || studyReminders || weeklyProgress;
    }
}
