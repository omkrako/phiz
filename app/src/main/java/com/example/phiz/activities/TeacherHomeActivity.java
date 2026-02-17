package com.example.phiz.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phiz.R;
import com.example.phiz.helpers.FCMTokenManager;
import com.example.phiz.helpers.FirestoreHelper;
import com.example.phiz.helpers.WorkerScheduler;
import com.example.phiz.models.NotificationPreferences;
import com.example.phiz.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherHomeActivity extends AppCompatActivity {
    private RecyclerView studentsRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialCardView logoutButton;
    private MaterialCardView settingsButton;
    private MaterialCardView createQuestionButton;
    private MaterialCardView viewQuestionsButton;
    private MaterialCardView allGradesButton;
    private StudentAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<User> studentsList = new ArrayList<>();

    // Permission request launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initializeNotifications();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        logoutButton = findViewById(R.id.logoutButton);
        settingsButton = findViewById(R.id.settingsButton);
        createQuestionButton = findViewById(R.id.createQuestionButton);
        viewQuestionsButton = findViewById(R.id.viewQuestionsButton);
        allGradesButton = findViewById(R.id.allGradesButton);

        // Request notification permission for Android 13+
        requestNotificationPermission();

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(studentsList, student -> {
            Intent intent = new Intent(TeacherHomeActivity.this, GradesActivity.class);
            intent.putExtra("userId", student.getUid());
            intent.putExtra("studentName", student.getName());
            startActivity(intent);
        });
        studentsRecyclerView.setAdapter(adapter);

        loadStudents();

        createQuestionButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherHomeActivity.this, CreateQuestionActivity.class));
        });

        viewQuestionsButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherHomeActivity.this, ViewQuestionsActivity.class));
        });

        allGradesButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherHomeActivity.this, AllGradesActivity.class));
        });

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent settingsIntent = new Intent(TeacherHomeActivity.this, NotificationSettingsActivity.class);
                settingsIntent.putExtra(NotificationSettingsActivity.EXTRA_USER_ROLE, "teacher");
                startActivity(settingsIntent);
            });
        }

        logoutButton.setOnClickListener(v -> {
            // Delete FCM token and unsubscribe from topics before logout
            FCMTokenManager tokenManager = FCMTokenManager.getInstance(this);
            tokenManager.deleteToken();
            tokenManager.unsubscribeFromAllTopics();

            mAuth.signOut();
            startActivity(new Intent(TeacherHomeActivity.this, LoginActivity.class));
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
        // Teachers don't need study reminders, inactivity checks, or weekly progress workers.
        // Those are student-only features. No workers to schedule for teachers.
    }

    private void loadStudents() {
        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        studentsList.add(user);
                    }
                    adapter.notifyDataSetChanged();

                    if (studentsList.isEmpty()) {
                        studentsRecyclerView.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        studentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading students: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private static class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private List<User> students;
        private OnStudentClickListener listener;

        public interface OnStudentClickListener {
            void onStudentClick(User student);
        }

        public StudentAdapter(List<User> students, OnStudentClickListener listener) {
            this.students = students;
            this.listener = listener;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_student, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            User student = students.get(position);
            holder.nameTextView.setText(student.getName());
            holder.emailTextView.setText(student.getEmail());
            holder.scoreTextView.setText(student.getTotalScore() + " points");

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(student);
                }
            });
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        static class StudentViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, emailTextView, scoreTextView;

            public StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.studentNameTextView);
                emailTextView = itemView.findViewById(R.id.studentEmailTextView);
                scoreTextView = itemView.findViewById(R.id.studentScoreTextView);
            }
        }
    }
}
