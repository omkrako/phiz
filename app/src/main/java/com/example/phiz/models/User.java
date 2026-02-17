package com.example.phiz.models;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role; // "student" or "teacher"
    private int totalScore;
    private int testsCompleted;

    // Notification-related fields
    private String fcmToken;
    private Timestamp lastActivityAt;
    private NotificationPreferences notificationPreferences;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.totalScore = 0;
        this.testsCompleted = 0;
        this.notificationPreferences = NotificationPreferences.createDefault();
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTestsCompleted() {
        return testsCompleted;
    }

    public void setTestsCompleted(int testsCompleted) {
        this.testsCompleted = testsCompleted;
    }

    // Get user level based on total score
    public int getLevel() {
        if (totalScore >= 1000) return 5;
        if (totalScore >= 500) return 4;
        if (totalScore >= 250) return 3;
        if (totalScore >= 100) return 2;
        return 1;
    }

    // Get level title based on score
    public String getLevelTitle() {
        int level = getLevel();
        switch (level) {
            case 5: return "Physics Master";
            case 4: return "Expert";
            case 3: return "Advanced";
            case 2: return "Intermediate";
            default: return "Beginner";
        }
    }

    // FCM Token
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // Last Activity
    public Timestamp getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Timestamp lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    // Notification Preferences
    public NotificationPreferences getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(NotificationPreferences notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
}
