package com.example.phiz.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phiz.R;
import com.example.phiz.models.Question;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewQuestionsActivity extends AppCompatActivity {
    private static final String TAG = "ViewQuestionsActivity";

    private RecyclerView questionsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private QuestionAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Question> questionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_questions);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadQuestions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionAdapter(questionsList, this::confirmDeleteQuestion);
        questionsRecyclerView.setAdapter(adapter);
    }

    private void loadQuestions() {
        setLoading(true);
        db.collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    questionsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Question question = document.toObject(Question.class);
                        questionsList.add(question);
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error loading questions: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void confirmDeleteQuestion(Question question) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question?")
                .setPositiveButton("Delete", (dialog, which) -> deleteQuestion(question))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteQuestion(Question question) {
        db.collection("questions")
                .document(question.getQuestionId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    questionsList.remove(question);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(this, "Question deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting question: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        if (questionsList.isEmpty()) {
            questionsRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            questionsRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    // Interface for delete callback
    interface OnDeleteClickListener {
        void onDeleteClick(Question question);
    }

    // RecyclerView Adapter
    private static class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
        private final List<Question> questions;
        private final OnDeleteClickListener deleteListener;

        public QuestionAdapter(List<Question> questions, OnDeleteClickListener deleteListener) {
            this.questions = questions;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            Question question = questions.get(position);
            holder.bind(question, deleteListener);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        static class QuestionViewHolder extends RecyclerView.ViewHolder {
            private final TextView difficultyBadge;
            private final TextView pointsTextView;
            private final TextView questionTextView;
            private final TextView option1TextView;
            private final TextView option2TextView;
            private final TextView option3TextView;
            private final TextView option4TextView;
            private final TextView correctAnswerTextView;
            private final ImageButton deleteButton;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                difficultyBadge = itemView.findViewById(R.id.difficultyBadge);
                pointsTextView = itemView.findViewById(R.id.pointsTextView);
                questionTextView = itemView.findViewById(R.id.questionTextView);
                option1TextView = itemView.findViewById(R.id.option1TextView);
                option2TextView = itemView.findViewById(R.id.option2TextView);
                option3TextView = itemView.findViewById(R.id.option3TextView);
                option4TextView = itemView.findViewById(R.id.option4TextView);
                correctAnswerTextView = itemView.findViewById(R.id.correctAnswerTextView);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }

            public void bind(Question question, OnDeleteClickListener deleteListener) {
                questionTextView.setText(question.getQuestionText());
                pointsTextView.setText(question.getPointValue() + " pts");

                // Set difficulty badge
                String difficulty = question.getDifficulty();
                difficultyBadge.setText(difficulty != null ? difficulty : "medium");

                // Set badge color based on difficulty
                int badgeColor;
                if ("easy".equals(difficulty)) {
                    badgeColor = 0xFF4CAF50; // Green
                } else if ("hard".equals(difficulty)) {
                    badgeColor = 0xFFF44336; // Red
                } else {
                    badgeColor = 0xFFFF9800; // Orange for medium
                }
                difficultyBadge.getBackground().setTint(badgeColor);

                // Set options
                List<String> options = question.getOptions();
                if (options != null && options.size() >= 4) {
                    option1TextView.setText("1. " + options.get(0));
                    option2TextView.setText("2. " + options.get(1));
                    option3TextView.setText("3. " + options.get(2));
                    option4TextView.setText("4. " + options.get(3));

                    // Highlight correct answer
                    int correctIndex = question.getCorrectAnswerIndex();
                    correctAnswerTextView.setText("Correct: " + options.get(correctIndex));
                }

                deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(question));
            }
        }
    }
}
