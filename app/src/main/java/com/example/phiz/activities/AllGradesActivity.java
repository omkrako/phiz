package com.example.phiz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class AllGradesActivity extends AppCompatActivity {
    private RecyclerView allGradesRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialCardView backButton;
    private ProgressBar progressBar;
    private AllGradesAdapter adapter;

    private FirebaseAuth mAuth;
    private FirestoreHelper firestoreHelper;
    private List<UserGrade> userGradesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_grades);

        mAuth = FirebaseAuth.getInstance();
        firestoreHelper = FirestoreHelper.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        allGradesRecyclerView = findViewById(R.id.allGradesRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        allGradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllGradesAdapter(userGradesList);
        allGradesRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        loadAllGrades();
    }

    private void loadAllGrades() {
        progressBar.setVisibility(View.VISIBLE);

        // Use FirestoreHelper to get all grades from all users' subcollections
        firestoreHelper.getAllGrades(
                grades -> {
                    userGradesList.clear();
                    for (FirestoreHelper.UserGrade userGrade : grades) {
                        userGradesList.add(new UserGrade(userGrade.userName, userGrade.grade));
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (userGradesList.isEmpty()) {
                        allGradesRecyclerView.setVisibility(View.GONE);
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        allGradesRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                },
                e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading grades: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Data class to hold user name with quiz result
    private static class UserGrade {
        String userName;
        QuizResult quizResult;

        UserGrade(String userName, QuizResult quizResult) {
            this.userName = userName;
            this.quizResult = quizResult;
        }
    }

    private static class AllGradesAdapter extends RecyclerView.Adapter<AllGradesAdapter.GradeViewHolder> {
        private List<UserGrade> userGrades;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

        public AllGradesAdapter(List<UserGrade> userGrades) {
            this.userGrades = userGrades;
        }

        @NonNull
        @Override
        public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_all_grades, parent, false);
            return new GradeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
            UserGrade userGrade = userGrades.get(position);
            QuizResult result = userGrade.quizResult;

            holder.userNameTextView.setText(userGrade.userName);
            holder.quizNameTextView.setText(result.getQuizName());

            Timestamp timestamp = result.getTimestamp();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                holder.dateTextView.setText(dateFormat.format(date));
            } else {
                holder.dateTextView.setText("");
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
            return userGrades.size();
        }

        static class GradeViewHolder extends RecyclerView.ViewHolder {
            TextView userNameTextView, quizNameTextView, dateTextView, scoreTextView, percentageTextView;

            public GradeViewHolder(@NonNull View itemView) {
                super(itemView);
                userNameTextView = itemView.findViewById(R.id.userNameTextView);
                quizNameTextView = itemView.findViewById(R.id.quizNameTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                scoreTextView = itemView.findViewById(R.id.scoreTextView);
                percentageTextView = itemView.findViewById(R.id.percentageTextView);
            }
        }
    }
}
