package com.example.phiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private TextView questionNumberTextView, questionTextView, feedbackTextView;
    private RadioGroup answersRadioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button submitButton, nextButton;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSubmitted = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static class Question {
        String question;
        String[] options;
        int correctAnswer;

        Question(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        questionTextView = findViewById(R.id.questionTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        answersRadioGroup = findViewById(R.id.answersRadioGroup);
        option1 = findViewById(R.id.option1RadioButton);
        option2 = findViewById(R.id.option2RadioButton);
        option3 = findViewById(R.id.option3RadioButton);
        option4 = findViewById(R.id.option4RadioButton);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);

        initializeQuestions();
        displayQuestion();

        submitButton.setOnClickListener(v -> submitAnswer());
        nextButton.setOnClickListener(v -> nextQuestion());
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();

        questions.add(new Question(
                "What is Newton's First Law of Motion?",
                new String[]{
                        "F = ma",
                        "An object at rest stays at rest unless acted upon by a force",
                        "For every action there is an equal and opposite reaction",
                        "Energy cannot be created or destroyed"
                },
                1
        ));

        questions.add(new Question(
                "According to Newton's Second Law, if you double the force on an object, what happens to its acceleration?",
                new String[]{
                        "It stays the same",
                        "It doubles",
                        "It halves",
                        "It quadruples"
                },
                1
        ));

        questions.add(new Question(
                "What is the formula for Newton's Second Law?",
                new String[]{
                        "E = mc²",
                        "F = ma",
                        "P = mv",
                        "W = Fd"
                },
                1
        ));

        questions.add(new Question(
                "Which is an example of Newton's Third Law?",
                new String[]{
                        "A ball rolling down a hill",
                        "A car accelerating forward",
                        "A rocket launching by expelling gas downward",
                        "A book sitting on a table"
                },
                2
        ));

        questions.add(new Question(
                "If the mass of an object is 10 kg and the net force is 50 N, what is its acceleration?",
                new String[]{
                        "5 m/s²",
                        "500 m/s²",
                        "0.2 m/s²",
                        "60 m/s²"
                },
                0
        ));
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            questionNumberTextView.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
            questionTextView.setText(q.question);
            option1.setText(q.options[0]);
            option2.setText(q.options[1]);
            option3.setText(q.options[2]);
            option4.setText(q.options[3]);
            answersRadioGroup.clearCheck();
            feedbackTextView.setVisibility(View.GONE);
            answerSubmitted = false;
        }
    }

    private void submitAnswer() {
        if (answerSubmitted) return;

        int selectedId = answersRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedAnswer = -1;
        if (selectedId == R.id.option1RadioButton) selectedAnswer = 0;
        else if (selectedId == R.id.option2RadioButton) selectedAnswer = 1;
        else if (selectedId == R.id.option3RadioButton) selectedAnswer = 2;
        else if (selectedId == R.id.option4RadioButton) selectedAnswer = 3;

        Question q = questions.get(currentQuestionIndex);
        if (selectedAnswer == q.correctAnswer) {
            score += 20;
            feedbackTextView.setText("Correct! +20 points");
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            feedbackTextView.setText("Incorrect. The correct answer is: " + q.options[q.correctAnswer]);
            feedbackTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        feedbackTextView.setVisibility(View.VISIBLE);
        answerSubmitted = true;
        submitButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
            submitButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            QuizResult result = new QuizResult(
                    "newton_laws_quiz",
                    userId,
                    "Newton's Laws Quiz",
                    score,
                    questions.size()
            );

            db.collection("quizResults")
                    .add(result)
                    .addOnSuccessListener(documentReference -> {
                        db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Long currentScore = documentSnapshot.getLong("totalScore");
                                    long newScore = (currentScore != null ? currentScore : 0) + score;

                                    db.collection("users").document(userId)
                                            .update("totalScore", newScore)
                                            .addOnSuccessListener(aVoid -> {
                                                showResultDialog();
                                            });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving results: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showResultDialog();
                    });
        }
    }

    private void showResultDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quiz Completed!")
                .setMessage("Your score: " + score + " out of " + (questions.size() * 20))
                .setPositiveButton("View Grades", (dialog, which) -> {
                    startActivity(new Intent(QuizActivity.this, GradesActivity.class));
                    finish();
                })
                .setNegativeButton("Back to Home", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}