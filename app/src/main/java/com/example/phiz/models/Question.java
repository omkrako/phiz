package com.example.phiz.models;

import java.util.List;

/**
 * Represents a single quiz/test question.
 * This model is designed to be stored in and retrieved from Firestore.
 */
public class Question {
    private String questionId;
    private String questionText;
    private List<String> options;  // Using List for Firestore compatibility
    private int correctAnswerIndex;  // 0-based index of correct answer
    private String explanation;  // Optional explanation for the correct answer
    private int pointValue;  // Points awarded for correct answer
    private String difficulty;  // "easy", "medium", "hard"

    /**
     * Default constructor required for Firestore deserialization
     */
    public Question() {
    }

    /**
     * Constructor for creating a new question
     */
    public Question(String questionId, String questionText, List<String> options,
                   int correctAnswerIndex, String explanation, int pointValue, String difficulty) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
        this.pointValue = pointValue;
        this.difficulty = difficulty;
    }

    /**
     * Simplified constructor with default values
     */
    public Question(String questionId, String questionText, List<String> options, int correctAnswerIndex) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = "";
        this.pointValue = 20;  // Default point value
        this.difficulty = "medium";
    }

    // Getters and setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Checks if a given answer index is correct
     */
    public boolean isCorrect(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }

    /**
     * Gets the correct answer text
     */
    public String getCorrectAnswer() {
        if (options != null && correctAnswerIndex >= 0 && correctAnswerIndex < options.size()) {
            return options.get(correctAnswerIndex);
        }
        return null;
    }
}
