package com.example.phiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.phiz.R;
import com.example.phiz.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private MaterialCardView studentCard, teacherCard;
    private String selectedRole = "student";
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        studentCard = findViewById(R.id.studentCard);
        teacherCard = findViewById(R.id.teacherCard);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);

        // Set up role selection card clicks
        studentCard.setOnClickListener(v -> selectRole("student"));
        teacherCard.setOnClickListener(v -> selectRole("teacher"));

        registerButton.setOnClickListener(v -> registerUser());

        loginTextView.setOnClickListener(v -> {
            finish();
        });
    }

    private void selectRole(String role) {
        selectedRole = role;
        if ("student".equals(role)) {
            studentCard.setStrokeColor(ContextCompat.getColor(this, R.color.green_dark));
            studentCard.setStrokeWidth(6);
            studentCard.setCardElevation(8);
            teacherCard.setStrokeColor(ContextCompat.getColor(this, R.color.divider));
            teacherCard.setStrokeWidth(2);
            teacherCard.setCardElevation(2);
        } else {
            teacherCard.setStrokeColor(ContextCompat.getColor(this, R.color.blue_dark));
            teacherCard.setStrokeWidth(6);
            teacherCard.setCardElevation(8);
            studentCard.setStrokeColor(ContextCompat.getColor(this, R.color.divider));
            studentCard.setStrokeWidth(2);
            studentCard.setCardElevation(2);
        }
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = selectedRole;

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        Log.d(TAG, "Starting user registration for email: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Creating user object for UID: " + firebaseUser.getUid());
                            User user = new User(firebaseUser.getUid(), email, name, role);
                            saveUserToFirestore(user);
                        } else {
                            Log.e(TAG, "Firebase user is null after successful authentication");
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this,
                                    "Registration error: User is null",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "User authentication failed", task.getException());
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        Log.d(TAG, "Saving user to Firestore: " + user.getEmail() + " with role: " + user.getRole());

        db.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore successfully");
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful!", Toast.LENGTH_SHORT).show();

                    Intent intent;
                    if ("teacher".equals(user.getRole())) {
                        Log.d(TAG, "Navigating to TeacherHomeActivity");
                        intent = new Intent(RegisterActivity.this, TeacherHomeActivity.class);
                    } else {
                        Log.d(TAG, "Navigating to StudentHomeActivity");
                        intent = new Intent(RegisterActivity.this, StudentHomeActivity.class);
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data to Firestore", e);
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Error saving user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
