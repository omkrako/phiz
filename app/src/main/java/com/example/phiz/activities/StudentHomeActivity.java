package com.example.phiz.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.phiz.R;
import com.example.phiz.helpers.FCMTokenManager;
import com.example.phiz.helpers.FirestoreHelper;
import com.example.phiz.helpers.WorkerScheduler;
import com.example.phiz.models.NotificationPreferences;
import com.example.phiz.workers.InactivityCheckWorker;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentHomeActivity extends AppCompatActivity {
    private TextView welcomeTextView, scoreTextView;
    private MaterialCardView simulationCard, quizCard, gradesCard, logoutButton, settingsButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // Permission request launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, initialize notifications
                    initializeNotifications();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userId = currentUser.getUid();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        simulationCard = findViewById(R.id.simulationCard);
        quizCard = findViewById(R.id.quizCard);
        gradesCard = findViewById(R.id.gradesCard);
        logoutButton = findViewById(R.id.logoutButton);
        settingsButton = findViewById(R.id.settingsButton);

        loadUserData();

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Update last activity
        InactivityCheckWorker.updateLastActivity(this);
        FirestoreHelper.getInstance().updateLastActivity(userId, null);

        simulationCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, PhysicsSimulationActivity.class));
        });

        quizCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, QuizActivity.class));
        });

        gradesCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, GradesActivity.class));
        });

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                startActivity(new Intent(StudentHomeActivity.this, NotificationSettingsActivity.class));
            });
        }

        logoutButton.setOnClickListener(v -> {
            // Delete FCM token and unsubscribe from topics before logout
            FCMTokenManager tokenManager = FCMTokenManager.getInstance(this);
            tokenManager.deleteToken();
            tokenManager.unsubscribeFromAllTopics();

            mAuth.signOut();
            startActivity(new Intent(StudentHomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                initializeNotifications();
            }
        } else {
            initializeNotifications();
        }
    }

    private void initializeNotifications() {
        // Schedule notification workers with user preferences
        FirestoreHelper.getInstance().getNotificationPreferences(userId,
                prefs -> WorkerScheduler.scheduleAllWorkers(this, prefs),
                e -> WorkerScheduler.scheduleAllWorkers(this, NotificationPreferences.createDefault())
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Long totalScore = documentSnapshot.getLong("totalScore");

                        welcomeTextView.setText(name);
                        scoreTextView.setText((totalScore != null ? totalScore : 0) + " points");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
