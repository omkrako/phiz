package com.example.phiz.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Represents a complete test/quiz with metadata and questions.
 * This model is designed to be stored in Firestore's "tests" collection.
 */
public class Test {
    private String testId;
    private String testName;
    private String description;
    private String subject;  // e.g., "Newton's Laws", "Kinematics", "Energy"
    private List<Question> questions;
    private int totalPoints;
    private int passingScore;  // Minimum score to pass
    private int timeLimit;  // Time limit in minutes (0 = no limit)
    private String difficulty;  // "easy", "medium", "hard"
    private boolean isActive;  // Whether the test is currently available
    private String createdBy;  // Teacher/creator user ID
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /**
     * Default constructor required for Firestore deserialization
     */
    public Test() {
    }

    /**
     * Full constructor for creating a new test
     */
    public Test(String testId, String testName, String description, String subject,
                List<Question> questions, int totalPoints, int passingScore,
                int timeLimit, String difficulty, boolean isActive, String createdBy) {
        this.testId = testId;
        this.testName = testName;
        this.description = description;
        this.subject = subject;
        this.questions = questions;
        this.totalPoints = totalPoints;
        this.passingScore = passingScore;
        this.timeLimit = timeLimit;
        this.difficulty = difficulty;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    /**
     * Simplified constructor with default values
     */
    public Test(String testId, String testName, String subject, List<Question> questions, String createdBy) {
        this.testId = testId;
        this.testName = testName;
        this.description = "";
        this.subject = subject;
        this.questions = questions;
        this.totalPoints = calculateTotalPoints();
        this.passingScore = (int) (totalPoints * 0.6);  // 60% passing by default
        this.timeLimit = 0;  // No time limit by default
        this.difficulty = "medium";
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters and setters
    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(int passingScore) {
        this.passingScore = passingScore;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods

    /**
     * Calculates total points based on all questions
     */
    public int calculateTotalPoints() {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Question q : questions) {
            total += q.getPointValue();
        }
        return total;
    }

    /**
     * Gets the number of questions in the test
     */
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    /**
     * Checks if a given score is passing
     */
    public boolean isPassing(int score) {
        return score >= passingScore;
    }

    /**
     * Gets a specific question by index
     */
    public Question getQuestion(int index) {
        if (questions != null && index >= 0 && index < questions.size()) {
            return questions.get(index);
        }
        return null;
    }

    /**
     * Updates the updatedAt timestamp
     */
    public void updateTimestamp() {
        this.updatedAt = Timestamp.now();
    }
}
