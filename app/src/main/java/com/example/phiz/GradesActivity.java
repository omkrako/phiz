package com.example.phiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradesActivity extends AppCompatActivity {
    private RecyclerView gradesRecyclerView;
    private TextView emptyTextView;
    private Button backButton;
    private GradeAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<QuizResult> quizResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        gradesRecyclerView = findViewById(R.id.gradesRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        backButton = findViewById(R.id.backButton);

        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter(quizResults);
        gradesRecyclerView.setAdapter(adapter);

        loadGrades(currentUser.getUid());

        backButton.setOnClickListener(v -> finish());
    }

    private void loadGrades(String userId) {
        db.collection("quizResults")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizResults.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        QuizResult result = document.toObject(QuizResult.class);
                        quizResults.add(result);
                    }
                    adapter.notifyDataSetChanged();

                    if (quizResults.isEmpty()) {
                        gradesRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        gradesRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
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

            int percentage = (int) (((float) result.getScore() / maxScore) * 100);
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