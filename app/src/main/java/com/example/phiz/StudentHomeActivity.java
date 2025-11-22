package com.example.phiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentHomeActivity extends AppCompatActivity {
    private TextView welcomeTextView, scoreTextView;
    private MaterialCardView simulationCard, quizCard, gradesCard;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

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

        loadUserData();

        simulationCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, PhysicsSimulationActivity.class));
        });

        quizCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, QuizActivity.class));
        });

        gradesCard.setOnClickListener(v -> {
            startActivity(new Intent(StudentHomeActivity.this, GradesActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentHomeActivity.this, LoginActivity.class));
            finish();
        });
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

                        welcomeTextView.setText("Welcome, " + name);
                        scoreTextView.setText((totalScore != null ? totalScore : 0) + " points");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}