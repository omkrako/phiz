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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherHomeActivity extends AppCompatActivity {
    private RecyclerView studentsRecyclerView;
    private TextView emptyTextView;
    private Button logoutButton;
    private StudentAdapter adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<User> studentsList = new ArrayList<>();

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
        emptyTextView = findViewById(R.id.emptyTextView);
        logoutButton = findViewById(R.id.logoutButton);

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(studentsList);
        studentsRecyclerView.setAdapter(adapter);

        loadStudents();

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherHomeActivity.this, LoginActivity.class));
            finish();
        });
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
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        studentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading students: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private static class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private List<User> students;

        public StudentAdapter(List<User> students) {
            this.students = students;
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
            holder.scoreTextView.setText("Score: " + student.getTotalScore() + " points");
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