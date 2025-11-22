package com.example.phiz;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role; // "student" or "teacher"
    private int totalScore;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.totalScore = 0;
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
}