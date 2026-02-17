package com.example.phiz.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phiz.R;
import com.example.phiz.helpers.NotificationHelper;
import com.example.phiz.models.Question;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateQuestionActivity extends AppCompatActivity {
    private static final String TAG = "CreateQuestionActivity";

    private TextInputEditText questionTextInput;
    private TextInputEditText option1Input, option2Input, option3Input, option4Input;
    private RadioGroup correctAnswerRadioGroup;
    private Spinner difficultySpinner;
    private TextInputEditText explanationInput;
    private TextInputEditText pointValueInput;
    private Button saveQuestionButton;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        initializeViews();
        setupDifficultySpinner();
        setupToolbar();
        setupSaveButton();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        questionTextInput = findViewById(R.id.questionTextInput);
        option1Input = findViewById(R.id.option1Input);
        option2Input = findViewById(R.id.option2Input);
        option3Input = findViewById(R.id.option3Input);
        option4Input = findViewById(R.id.option4Input);
        correctAnswerRadioGroup = findViewById(R.id.correctAnswerRadioGroup);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        explanationInput = findViewById(R.id.explanationInput);
        pointValueInput = findViewById(R.id.pointValueInput);
        saveQuestionButton = findViewById(R.id.saveQuestionButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDifficultySpinner() {
        String[] difficulties = {"easy", "medium", "hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                difficulties
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        difficultySpinner.setSelection(1); // Default to "medium"
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        saveQuestionButton.setOnClickListener(v -> saveQuestion());
    }

    private void saveQuestion() {
        String questionText = getTextFromInput(questionTextInput);
        String option1 = getTextFromInput(option1Input);
        String option2 = getTextFromInput(option2Input);
        String option3 = getTextFromInput(option3Input);
        String option4 = getTextFromInput(option4Input);
        String explanation = getTextFromInput(explanationInput);
        String pointValueStr = getTextFromInput(pointValueInput);
        String difficulty = difficultySpinner.getSelectedItem().toString();

        // Validate inputs
        if (questionText.isEmpty()) {
            questionTextInput.setError("Question text is required");
            return;
        }
        if (option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty()) {
            Toast.makeText(this, "All four options are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int correctAnswerIndex = getCorrectAnswerIndex();
        if (correctAnswerIndex == -1) {
            Toast.makeText(this, "Please select the correct answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int pointValue = 20; // Default
        if (!pointValueStr.isEmpty()) {
            try {
                pointValue = Integer.parseInt(pointValueStr);
            } catch (NumberFormatException e) {
                pointValueInput.setError("Invalid point value");
                return;
            }
        }

        // Create question object
        String questionId = UUID.randomUUID().toString();
        List<String> options = Arrays.asList(option1, option2, option3, option4);
        Question question = new Question(
                questionId,
                questionText,
                options,
                correctAnswerIndex,
                explanation,
                pointValue,
                difficulty
        );

        // Show progress
        setLoading(true);

        // Save to Firestore
        db.collection("questions")
                .document(questionId)
                .set(question)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Question saved successfully!", Toast.LENGTH_SHORT).show();

                    // Notify students of new content via FCM topic
                    notifyStudentsOfNewContent();

                    clearForm();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error saving question: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void notifyStudentsOfNewContent() {
        // Send notification to all students topic
        // Note: For server-triggered notifications, you would typically use Cloud Functions
        // Here we show a local notification as a fallback/demo
        NotificationHelper.getInstance(this).showNewContentNotification("questions");
    }

    private String getTextFromInput(TextInputEditText input) {
        if (input.getText() != null) {
            return input.getText().toString().trim();
        }
        return "";
    }

    private int getCorrectAnswerIndex() {
        int checkedId = correctAnswerRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioOption1) return 0;
        if (checkedId == R.id.radioOption2) return 1;
        if (checkedId == R.id.radioOption3) return 2;
        if (checkedId == R.id.radioOption4) return 3;
        return -1;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveQuestionButton.setEnabled(!isLoading);
    }

    private void clearForm() {
        questionTextInput.setText("");
        option1Input.setText("");
        option2Input.setText("");
        option3Input.setText("");
        option4Input.setText("");
        correctAnswerRadioGroup.clearCheck();
        explanationInput.setText("");
        pointValueInput.setText("20");
        difficultySpinner.setSelection(1);
    }
}
