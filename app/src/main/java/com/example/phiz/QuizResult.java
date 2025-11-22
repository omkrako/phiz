package com.example.phiz;

import com.google.firebase.Timestamp;

public class QuizResult {
    private String quizId;
    private String userId;
    private String quizName;
    private int score;
    private int totalQuestions;
    private Timestamp timestamp;

    public QuizResult() {
        // Default constructor required for Firestore
    }

    public QuizResult(String quizId, String userId, String quizName, int score, int totalQuestions) {
        this.quizId = quizId;
        this.userId = userId;
        this.quizName = quizName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = Timestamp.now();
    }

    // Getters and setters
    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}