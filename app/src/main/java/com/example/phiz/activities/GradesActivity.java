package com.example.phiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phiz.R;
import com.example.phiz.helpers.FirestoreHelper;
import com.example.phiz.models.QuizResult;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradesActivity extends AppCompatActivity {
    private RecyclerView gradesRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialCardView backButton;
    private Button takeQuizButton;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextView emptyTextView;
    private GradeAdapter adapter;

    private FirebaseAuth mAuth;
    private FirestoreHelper firestoreHelper;
    private List<QuizResult> quizResults = new ArrayList<>();
    private boolean isViewingOtherUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        mAuth = FirebaseAuth.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        gradesRecyclerView = findViewById(R.id.gradesRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        backButton = findViewById(R.id.backButton);
        takeQuizButton = findViewById(R.id.takeQuizButton);
        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        emptyTextView = findViewById(R.id.emptyTextView);

        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter(quizResults);
        gradesRecyclerView.setAdapter(adapter);

        // Check if viewing another user's grades (teacher viewing student)
        String userId = getIntent().getStringExtra("userId");
        String studentName = getIntent().getStringExtra("studentName");

        if (userId != null && !userId.isEmpty()) {
            // Teacher viewing a student's grades
            isViewingOtherUser = true;
            loadGrades(userId);
            if (titleTextView != null) {
                titleTextView.setText(studentName != null ? studentName + "'s Grades" : "Student Grades");
            }
            if (subtitleTextView != null) {
                subtitleTextView.setText("View student's quiz results");
            }
            if (emptyTextView != null) {
                emptyTextView.setText("This student has no quiz results yet");
            }
            // Hide "Take Quiz" button when viewing other user's grades
            takeQuizButton.setVisibility(View.GONE);
        } else {
            // User viewing their own grades
            loadGrades(currentUser.getUid());
        }

        backButton.setOnClickListener(v -> finish());

        takeQuizButton.setOnClickListener(v -> {
            startActivity(new Intent(GradesActivity.this, QuizActivity.class));
            finish();
        });
    }

    private void loadGrades(String oderId) {
        // Use FirestoreHelper to get grades from user's subcollection
        firestoreHelper.getUserGrades(oderId,
                grades -> {
                    quizResults.clear();
                    quizResults.addAll(grades);
                    adapter.notifyDataSetChanged();

                    if (quizResults.isEmpty()) {
                        gradesRecyclerView.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        gradesRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                },
                e -> {
                    Toast.makeText(this, "Error loading grades: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private static class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {
        private List<QuizResult> results;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

        public GradeAdapter(List<QuizResult> results) {
            this.results = results;
        }

        @NonNull
        @Override
        public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grade, parent, false);
            return new GradeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
            QuizResult result = results.get(position);
            holder.quizNameTextView.setText(result.getQuizName());

            Timestamp timestamp = result.getTimestamp();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                holder.dateTextView.setText(dateFormat.format(date));
            }

            int maxScore = result.getTotalQuestions() * 20;
            holder.scoreTextView.setText(String.format(Locale.US, "Score: %d/%d",
                    result.getScore(), maxScore));

            int percentage = maxScore > 0 ? (int) (((float) result.getScore() / maxScore) * 100) : 0;
            if (percentage > 100) percentage = 100; // Cap at 100%
            holder.percentageTextView.setText(percentage + "%");
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        static class GradeViewHolder extends RecyclerView.ViewHolder {
            TextView quizNameTextView, dateTextView, scoreTextView, percentageTextView;

            public GradeViewHolder(@NonNull View itemView) {
                super(itemView);
                quizNameTextView = itemView.findViewById(R.id.quizNameTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                scoreTextView = itemView.findViewById(R.id.scoreTextView);
                percentageTextView = itemView.findViewById(R.id.percentageTextView);
            }
        }
    }
}
