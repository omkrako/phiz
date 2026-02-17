package com.example.phiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.phiz.R;
import com.example.phiz.helpers.FirestoreHelper;
import com.example.phiz.helpers.NotificationHelper;
import com.example.phiz.models.Question;
import com.example.phiz.models.QuizResult;
import com.example.phiz.views.DoodleView;
import com.example.phiz.workers.InactivityCheckWorker;
import com.example.phiz.workers.WeeklyProgressWorker;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private TextView questionNumberTextView, questionTextView, feedbackTextView, timerTextView;
    private RadioGroup answersRadioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button submitButton, nextButton;
    private MaterialCardView feedbackCard;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private boolean answerSubmitted = false;
    private int shuffledCorrectAnswerIndex = 0;

    // Timer
    private CountDownTimer countDownTimer;
    private static final long QUESTION_TIME_MS = 30000; // 30 seconds per question
    private static final long AUTO_PROGRESS_DELAY_MS = 2000; // 2 seconds delay before auto-progress

    // Handler for auto-progression
    private Handler autoProgressHandler = new Handler(Looper.getMainLooper());

    // Doodle
    private DoodleView doodleView;
    private FloatingActionButton doodleToggleButton, clearDoodleButton;

    // Point system constants
    private static final int POINTS_PER_CORRECT = 20;
    private static final int COMPLETION_BONUS = 10;
    private static final int PERFECT_SCORE_BONUS = 50;

    private int maxQuestions = 5; // default, overridden by Firestore setting

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        questionTextView = findViewById(R.id.questionTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        feedbackCard = findViewById(R.id.feedbackCard);
        timerTextView = findViewById(R.id.timerTextView);
        answersRadioGroup = findViewById(R.id.answersRadioGroup);
        option1 = findViewById(R.id.option1RadioButton);
        option2 = findViewById(R.id.option2RadioButton);
        option3 = findViewById(R.id.option3RadioButton);
        option4 = findViewById(R.id.option4RadioButton);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);

        // Doodle views
        doodleView = findViewById(R.id.doodleView);
        doodleToggleButton = findViewById(R.id.doodleToggleButton);
        clearDoodleButton = findViewById(R.id.clearDoodleButton);

        submitButton.setOnClickListener(v -> submitAnswer());
        nextButton.setOnClickListener(v -> nextQuestion());

        // Doodle toggle button
        if (doodleToggleButton != null) {
            doodleToggleButton.setOnClickListener(v -> toggleDoodle());
        }
        if (clearDoodleButton != null) {
            clearDoodleButton.setOnClickListener(v -> {
                if (doodleView != null) {
                    doodleView.clearDoodle();
                }
                Toast.makeText(this, "Doodle cleared", Toast.LENGTH_SHORT).show();
            });
        }

        // Load quiz settings first, then load questions
        loadQuizSettings();
    }

    private void loadQuizSettings() {
        submitButton.setEnabled(false);
        questionTextView.setText("Loading questions...");

        FirestoreHelper.getInstance().getQuizQuestionCount(
                count -> {
                    maxQuestions = count;
                    loadQuestionsFromFirebase();
                },
                e -> {
                    // Use default if settings can't be loaded
                    maxQuestions = 5;
                    loadQuestionsFromFirebase();
                }
        );
    }

    private void loadQuestionsFromFirebase() {
        db.collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            questions = new ArrayList<>();

                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Question question = new Question();
                                question.setQuestionId(doc.getString("questionId"));
                                question.setQuestionText(doc.getString("questionText"));

                                List<String> options = (List<String>) doc.get("options");
                                question.setOptions(options);

                                Long correctIndex = doc.getLong("correctAnswerIndex");
                                question.setCorrectAnswerIndex(correctIndex != null ? correctIndex.intValue() : 0);

                                question.setExplanation(doc.getString("explanation"));

                                Long pointValue = doc.getLong("pointValue");
                                question.setPointValue(pointValue != null ? pointValue.intValue() : POINTS_PER_CORRECT);

                                question.setDifficulty(doc.getString("difficulty"));

                                // Only add questions that have valid data
                                if (question.getQuestionText() != null && options != null && options.size() >= 4) {
                                    questions.add(question);
                                }
                            }

                            if (!questions.isEmpty()) {
                                // Randomize question order
                                Collections.shuffle(questions);

                                // Limit to configured number of questions
                                if (questions.size() > maxQuestions) {
                                    questions = new ArrayList<>(questions.subList(0, maxQuestions));
                                }

                                // Start the quiz
                                submitButton.setEnabled(true);
                                displayQuestion();
                            } else {
                                Toast.makeText(this, "No valid questions found", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void toggleDoodle() {
        if (doodleView == null) return;

        boolean isEnabled = doodleView.isDrawingEnabled();
        doodleView.setDrawingEnabled(!isEnabled);

        if (!isEnabled) {
            if (doodleToggleButton != null) {
                doodleToggleButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_dark)));
            }
            if (clearDoodleButton != null) {
                clearDoodleButton.setVisibility(View.VISIBLE);
            }
            Toast.makeText(this, "Doodle mode ON", Toast.LENGTH_SHORT).show();
        } else {
            if (doodleToggleButton != null) {
                doodleToggleButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_primary)));
            }
            if (clearDoodleButton != null) {
                clearDoodleButton.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Doodle mode OFF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        autoProgressHandler.removeCallbacksAndMessages(null);
    }

    private void displayQuestion() {
        if (questions == null || questions.isEmpty()) return;

        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            questionNumberTextView.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
            questionTextView.setText(q.getQuestionText());

            List<String> options = q.getOptions();
            if (options == null || options.size() < 4) {
                Toast.makeText(this, "Invalid question format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a list of indices and shuffle them
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);

            // Set options in shuffled order and track correct answer position
            option1.setText(options.get(indices.get(0)));
            option2.setText(options.get(indices.get(1)));
            option3.setText(options.get(indices.get(2)));
            option4.setText(options.get(indices.get(3)));

            // Find where the correct answer ended up after shuffle
            shuffledCorrectAnswerIndex = indices.indexOf(q.getCorrectAnswerIndex());

            answersRadioGroup.clearCheck();
            feedbackCard.setVisibility(View.GONE);
            answerSubmitted = false;

            // Enable radio buttons
            setRadioButtonsEnabled(true);

            // Show submit button, hide next button
            submitButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
            nextButton.setOnClickListener(v -> nextQuestion());

            // Clear doodle for new question
            if (doodleView != null) {
                doodleView.clearDoodle();
            }

            // Start the countdown timer
            startTimer();
        }
    }

    private void setRadioButtonsEnabled(boolean enabled) {
        option1.setEnabled(enabled);
        option2.setEnabled(enabled);
        option3.setEnabled(enabled);
        option4.setEnabled(enabled);
    }

    private void startTimer() {
        // Cancel any existing timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerTextView.setTextColor(ContextCompat.getColor(this, R.color.orange_primary));

        countDownTimer = new CountDownTimer(QUESTION_TIME_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                timerTextView.setText(String.valueOf(secondsLeft));

                // Change color to red when 10 seconds or less
                if (secondsLeft <= 10) {
                    timerTextView.setTextColor(ContextCompat.getColor(QuizActivity.this, R.color.red_primary));
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0");
                onTimeUp();
            }
        }.start();
    }

    private void onTimeUp() {
        if (answerSubmitted) return;

        Question q = questions.get(currentQuestionIndex);
        String correctAnswer = q.getCorrectAnswer();
        feedbackTextView.setText("Time's up! The correct answer is: " + correctAnswer);
        feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.red_primary));
        feedbackCard.setVisibility(View.VISIBLE);

        answerSubmitted = true;
        submitButton.setVisibility(View.GONE);
        setRadioButtonsEnabled(false);

        // Auto-progress after delay
        scheduleAutoProgress();
    }

    private void submitAnswer() {
        if (answerSubmitted) return;

        // Stop the timer when answer is submitted
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int selectedId = answersRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            // Restart timer since they didn't submit
            startTimer();
            return;
        }

        int selectedAnswer = -1;
        if (selectedId == R.id.option1RadioButton) selectedAnswer = 0;
        else if (selectedId == R.id.option2RadioButton) selectedAnswer = 1;
        else if (selectedId == R.id.option3RadioButton) selectedAnswer = 2;
        else if (selectedId == R.id.option4RadioButton) selectedAnswer = 3;

        Question q = questions.get(currentQuestionIndex);
        boolean isCorrect = (selectedAnswer == shuffledCorrectAnswerIndex);

        int pointValue = q.getPointValue() > 0 ? q.getPointValue() : POINTS_PER_CORRECT;

        if (isCorrect) {
            score += pointValue;
            correctAnswers++;
            feedbackTextView.setText("Correct! +" + pointValue + " points");
            feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
        } else {
            String correctAnswer = q.getCorrectAnswer();
            feedbackTextView.setText("Incorrect. The correct answer is: " + correctAnswer);
            feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.red_primary));
        }

        feedbackCard.setVisibility(View.VISIBLE);
        answerSubmitted = true;
        submitButton.setVisibility(View.GONE);
        setRadioButtonsEnabled(false);

        // Auto-progress after delay (for both correct and incorrect)
        scheduleAutoProgress();
    }

    private void scheduleAutoProgress() {
        // Show next button for last question only
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Go Home");
            nextButton.setVisibility(View.VISIBLE);
            // Set click listener to go home
            nextButton.setOnClickListener(v -> {
                finishQuiz();
                goHome();
            });
        } else {
            // Auto-progress to next question after delay
            autoProgressHandler.postDelayed(() -> {
                nextQuestion();
            }, AUTO_PROGRESS_DELAY_MS);
        }
    }

    private void goHome() {
        Intent intent = new Intent(this, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void nextQuestion() {
        // Cancel any pending auto-progress
        autoProgressHandler.removeCallbacksAndMessages(null);

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        }
    }

    private void finishQuiz() {
        // Calculate bonuses
        int completionBonus = COMPLETION_BONUS;
        int perfectBonus = (correctAnswers == questions.size()) ? PERFECT_SCORE_BONUS : 0;
        int totalPointsEarned = score + completionBonus + perfectBonus;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            QuizResult result = new QuizResult(
                    "physics_quiz",
                    userId,
                    "Physics Quiz",
                    totalPointsEarned,
                    questions.size()
            );

            // Use FirestoreHelper to save grade to user's grades subcollection
            FirestoreHelper.getInstance().saveGradeAndUpdateScore(userId, result, (success, e) -> {
                if (success) {
                    // Show grade posted notification to student
                    NotificationHelper.getInstance(this)
                            .showGradePostedNotification(totalPointsEarned, questions.size());

                    // Update activity tracking
                    InactivityCheckWorker.updateLastActivity(this);
                    FirestoreHelper.getInstance().updateLastActivity(userId, null);

                    // Update weekly progress tracking
                    updateProgressTracking(userId, totalPointsEarned);

                    // Check for achievements
                    checkAchievements(userId, totalPointsEarned);

                    // Notify teachers of quiz completion
                    notifyTeachersOfCompletion(userId, totalPointsEarned);
                } else if (e != null) {
                    Toast.makeText(this, "Error saving results: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProgressTracking(String userId, int score) {
        // Update the weekly progress worker with the new score
        WeeklyProgressWorker.updateCurrentScore(this, score);

        // Get and update total score for progress tracking
        FirestoreHelper.getInstance().getUser(userId,
                user -> {
                    if (user != null) {
                        WeeklyProgressWorker.updateCurrentScore(this, user.getTotalScore());
                    }
                },
                e -> { /* Ignore errors */ }
        );
    }

    private void checkAchievements(String userId, int quizScore) {
        FirestoreHelper.getInstance().getUser(userId,
                user -> {
                    if (user == null) return;

                    int totalScore = user.getTotalScore();
                    int testsCompleted = user.getTestsCompleted();
                    NotificationHelper notificationHelper = NotificationHelper.getInstance(this);

                    // Check for first quiz achievement
                    if (testsCompleted == 1) {
                        notificationHelper.showAchievementNotification(
                                "First Steps!",
                                "You completed your first physics quiz!"
                        );
                    }

                    // Check for 5 quizzes achievement
                    if (testsCompleted == 5) {
                        notificationHelper.showAchievementNotification(
                                "Quiz Enthusiast!",
                                "You've completed 5 quizzes. Keep it up!"
                        );
                    }

                    // Check for 10 quizzes achievement
                    if (testsCompleted == 10) {
                        notificationHelper.showAchievementNotification(
                                "Physics Regular!",
                                "10 quizzes completed! You're on a roll!"
                        );
                    }

                    // Check for score milestones
                    if (totalScore >= 100 && totalScore - quizScore < 100) {
                        notificationHelper.showAchievementNotification(
                                "Century Club!",
                                "You've earned 100+ points total!"
                        );
                    }

                    if (totalScore >= 500 && totalScore - quizScore < 500) {
                        notificationHelper.showAchievementNotification(
                                "High Achiever!",
                                "You've earned 500+ points! Impressive!"
                        );
                    }

                    if (totalScore >= 1000 && totalScore - quizScore < 1000) {
                        notificationHelper.showAchievementNotification(
                                "Physics Master!",
                                "1000+ points! You're a true physics expert!"
                        );
                    }

                    // Check for perfect score on quiz
                    if (correctAnswers == questions.size()) {
                        notificationHelper.showAchievementNotification(
                                "Perfect Score!",
                                "You got all questions right! Amazing!"
                        );
                    }
                },
                e -> { /* Ignore achievement check errors */ }
        );
    }

    private void notifyTeachersOfCompletion(String studentId, int score) {
        // Get student name for the notification
        FirestoreHelper.getInstance().getUser(studentId,
                student -> {
                    if (student == null) return;

                    String studentName = student.getName();
                    int percentage = (questions.size() > 0) ?
                            (correctAnswers * 100 / questions.size()) : 0;

                    // Check if low score alert is needed (below 50%)
                    if (percentage < 50) {
                        NotificationHelper.getInstance(this)
                                .showLowScoreAlertForTeacher(studentName, score, percentage);
                    }
                },
                e -> { /* Ignore errors */ }
        );
    }
}
