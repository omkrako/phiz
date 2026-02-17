package com.example.phiz.helpers;

import android.util.Log;

import com.example.phiz.models.NotificationPreferences;
import com.example.phiz.models.Question;
import com.example.phiz.models.QuizResult;
import com.example.phiz.models.Test;
import com.example.phiz.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class for Firebase Firestore operations.
 * Provides centralized methods for CRUD operations on all collections.
 *
 * Data Structure:
 * - users/{userId} - User documents
 * - users/{userId}/grades/{gradeId} - Grades subcollection under each user
 * - questions/{questionId} - Question documents
 * - tests/{testId} - Test documents
 */
public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";

    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_QUESTIONS = "questions";
    public static final String COLLECTION_TESTS = "tests";
    public static final String SUBCOLLECTION_GRADES = "grades";

    // Singleton instance
    private static FirestoreHelper instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Private constructor for singleton
    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    /**
     * Get Firestore instance for direct access if needed
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Get current authenticated user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ==================== CALLBACK INTERFACES ====================

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    public interface OnCompleteListener {
        void onComplete(boolean success, Exception e);
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Create a new user in Firestore
     */
    public void createUser(User user, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created successfully: " + user.getUid());
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get user by ID
     */
    public void getUser(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (onSuccess != null) onSuccess.onSuccess(user);
                    } else {
                        if (onFailure != null) onFailure.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get current authenticated user from Firestore
     */
    public void getCurrentUser(OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (onFailure != null) onFailure.onFailure(new Exception("No authenticated user"));
            return;
        }
        getUser(userId, onSuccess, onFailure);
    }

    /**
     * Update user data
     */
    public void updateUser(String userId, Map<String, Object> updates, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Update user's total score
     */
    public void updateUserScore(String userId, int scoreToAdd, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(
                        "totalScore", FieldValue.increment(scoreToAdd),
                        "testsCompleted", FieldValue.increment(1)
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User score updated: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user score", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Delete user
     */
    public void deleteUser(String userId, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get all users
     */
    public void getAllUsers(OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        users.add(doc.toObject(User.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all users", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get users by role (student/teacher)
     */
    public void getUsersByRole(String role, OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("role", role)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        users.add(doc.toObject(User.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting users by role", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get all students
     */
    public void getAllStudents(OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        getUsersByRole("student", onSuccess, onFailure);
    }

    /**
     * Get all teachers
     */
    public void getAllTeachers(OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        getUsersByRole("teacher", onSuccess, onFailure);
    }

    /**
     * Get top students by score
     */
    public void getTopStudents(int limit, OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "student")
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        users.add(doc.toObject(User.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting top students", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Check if user exists
     */
    public void userExists(String userId, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (onSuccess != null) onSuccess.onSuccess(document.exists());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user exists", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== GRADES SUBCOLLECTION OPERATIONS ====================
    // Grades are stored as: users/{userId}/grades/{gradeId}

    /**
     * Save a grade to user's grades subcollection
     */
    public void saveGrade(String userId, QuizResult grade, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .add(grade)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Grade saved for user: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving grade", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Save grade and update user score in a single batch
     */
    public void saveGradeAndUpdateScore(String userId, QuizResult grade, OnCompleteListener listener) {
        WriteBatch batch = db.batch();

        // Add grade to subcollection
        DocumentReference gradeRef = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .document();
        batch.set(gradeRef, grade);

        // Update user score
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);
        batch.update(userRef,
                "totalScore", FieldValue.increment(grade.getScore()),
                "testsCompleted", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grade saved and user score updated");
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error in batch operation", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Save grade for current user
     */
    public void saveCurrentUserGrade(QuizResult grade, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (listener != null) listener.onComplete(false, new Exception("No authenticated user"));
            return;
        }
        grade.setUserId(userId);
        saveGradeAndUpdateScore(userId, grade, listener);
    }

    /**
     * Get grades for a specific user
     */
    public void getUserGrades(String userId, OnSuccessListener<List<QuizResult>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<QuizResult> grades = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        QuizResult grade = doc.toObject(QuizResult.class);
                        grades.add(grade);
                    }
                    if (onSuccess != null) onSuccess.onSuccess(grades);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user grades", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get current user's grades
     */
    public void getCurrentUserGrades(OnSuccessListener<List<QuizResult>> onSuccess, OnFailureListener onFailure) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (onFailure != null) onFailure.onFailure(new Exception("No authenticated user"));
            return;
        }
        getUserGrades(userId, onSuccess, onFailure);
    }

    /**
     * Data class for grade with user info
     */
    public static class UserGrade {
        public String userName;
        public String oderId;
        public QuizResult grade;

        public UserGrade(String userName, String oderId, QuizResult grade) {
            this.userName = userName;
            this.oderId = oderId;
            this.grade = grade;
        }
    }

    /**
     * Get all grades from all users (for teacher view)
     */
    public void getAllGrades(OnSuccessListener<List<UserGrade>> onSuccess, OnFailureListener onFailure) {
        // First get all users
        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    List<UserGrade> allGrades = new ArrayList<>();
                    List<User> users = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : userSnapshots) {
                        users.add(doc.toObject(User.class));
                    }

                    if (users.isEmpty()) {
                        if (onSuccess != null) onSuccess.onSuccess(allGrades);
                        return;
                    }

                    // Counter to track completed queries
                    AtomicInteger completedQueries = new AtomicInteger(0);
                    int totalUsers = users.size();

                    // Get grades for each user
                    for (User user : users) {
                        db.collection(COLLECTION_USERS)
                                .document(user.getUid())
                                .collection(SUBCOLLECTION_GRADES)
                                .get()
                                .addOnSuccessListener(gradeSnapshots -> {
                                    synchronized (allGrades) {
                                        for (QueryDocumentSnapshot gradeDoc : gradeSnapshots) {
                                            QuizResult grade = gradeDoc.toObject(QuizResult.class);
                                            allGrades.add(new UserGrade(user.getName(), user.getUid(), grade));
                                        }
                                    }

                                    // Check if all queries completed
                                    if (completedQueries.incrementAndGet() == totalUsers) {
                                        // Sort by timestamp descending
                                        allGrades.sort((a, b) -> {
                                            if (a.grade.getTimestamp() == null && b.grade.getTimestamp() == null) return 0;
                                            if (a.grade.getTimestamp() == null) return 1;
                                            if (b.grade.getTimestamp() == null) return -1;
                                            return b.grade.getTimestamp().compareTo(a.grade.getTimestamp());
                                        });
                                        if (onSuccess != null) onSuccess.onSuccess(allGrades);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Still increment counter on failure to avoid hanging
                                    if (completedQueries.incrementAndGet() == totalUsers) {
                                        allGrades.sort((a, b) -> {
                                            if (a.grade.getTimestamp() == null && b.grade.getTimestamp() == null) return 0;
                                            if (a.grade.getTimestamp() == null) return 1;
                                            if (b.grade.getTimestamp() == null) return -1;
                                            return b.grade.getTimestamp().compareTo(a.grade.getTimestamp());
                                        });
                                        if (onSuccess != null) onSuccess.onSuccess(allGrades);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all grades", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Delete a grade from user's subcollection
     */
    public void deleteGrade(String userId, String gradeId, OnCompleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .document(gradeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grade deleted: " + gradeId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting grade", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get user's best score for a specific quiz
     */
    public void getUserBestScoreForQuiz(String userId, String quizId, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .whereEqualTo("quizId", quizId)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        QuizResult result = querySnapshot.getDocuments().get(0).toObject(QuizResult.class);
                        if (onSuccess != null) onSuccess.onSuccess(result != null ? result.getScore() : 0);
                    } else {
                        if (onSuccess != null) onSuccess.onSuccess(0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user best score", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get grade count for a user
     */
    public void getUserGradeCount(String userId, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_GRADES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (onSuccess != null) onSuccess.onSuccess(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting grade count", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== QUESTION OPERATIONS ====================

    /**
     * Create a new question
     */
    public void createQuestion(Question question, OnCompleteListener listener) {
        DocumentReference docRef;
        if (question.getQuestionId() != null && !question.getQuestionId().isEmpty()) {
            docRef = db.collection(COLLECTION_QUESTIONS).document(question.getQuestionId());
        } else {
            docRef = db.collection(COLLECTION_QUESTIONS).document();
            question.setQuestionId(docRef.getId());
        }

        docRef.set(question)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Question created: " + question.getQuestionId());
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating question", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get question by ID
     */
    public void getQuestion(String questionId, OnSuccessListener<Question> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_QUESTIONS)
                .document(questionId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Question question = document.toObject(Question.class);
                        if (onSuccess != null) onSuccess.onSuccess(question);
                    } else {
                        if (onFailure != null) onFailure.onFailure(new Exception("Question not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting question", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get all questions
     */
    public void getAllQuestions(OnSuccessListener<List<Question>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_QUESTIONS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Question> questions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        questions.add(doc.toObject(Question.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(questions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all questions", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get questions by difficulty
     */
    public void getQuestionsByDifficulty(String difficulty, OnSuccessListener<List<Question>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("difficulty", difficulty)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Question> questions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        questions.add(doc.toObject(Question.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(questions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting questions by difficulty", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Update question
     */
    public void updateQuestion(String questionId, Map<String, Object> updates, OnCompleteListener listener) {
        db.collection(COLLECTION_QUESTIONS)
                .document(questionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Question updated: " + questionId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating question", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Delete question
     */
    public void deleteQuestion(String questionId, OnCompleteListener listener) {
        db.collection(COLLECTION_QUESTIONS)
                .document(questionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Question deleted: " + questionId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting question", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get random questions for a quiz
     */
    public void getRandomQuestions(int count, OnSuccessListener<List<Question>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_QUESTIONS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Question> allQuestions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        allQuestions.add(doc.toObject(Question.class));
                    }

                    // Shuffle and take requested count
                    java.util.Collections.shuffle(allQuestions);
                    List<Question> randomQuestions = allQuestions.subList(0, Math.min(count, allQuestions.size()));

                    if (onSuccess != null) onSuccess.onSuccess(randomQuestions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting random questions", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== TEST OPERATIONS ====================

    /**
     * Create a new test
     */
    public void createTest(Test test, OnCompleteListener listener) {
        DocumentReference docRef;
        if (test.getTestId() != null && !test.getTestId().isEmpty()) {
            docRef = db.collection(COLLECTION_TESTS).document(test.getTestId());
        } else {
            docRef = db.collection(COLLECTION_TESTS).document();
            test.setTestId(docRef.getId());
        }

        docRef.set(test)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test created: " + test.getTestId());
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating test", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get test by ID
     */
    public void getTest(String testId, OnSuccessListener<Test> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TESTS)
                .document(testId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Test test = document.toObject(Test.class);
                        if (onSuccess != null) onSuccess.onSuccess(test);
                    } else {
                        if (onFailure != null) onFailure.onFailure(new Exception("Test not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting test", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get all tests
     */
    public void getAllTests(OnSuccessListener<List<Test>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TESTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Test> tests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        tests.add(doc.toObject(Test.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(tests);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all tests", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get active tests only
     */
    public void getActiveTests(OnSuccessListener<List<Test>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TESTS)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Test> tests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        tests.add(doc.toObject(Test.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(tests);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting active tests", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get tests by creator (teacher)
     */
    public void getTestsByCreator(String creatorId, OnSuccessListener<List<Test>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TESTS)
                .whereEqualTo("createdBy", creatorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Test> tests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        tests.add(doc.toObject(Test.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(tests);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting tests by creator", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Update test
     */
    public void updateTest(String testId, Map<String, Object> updates, OnCompleteListener listener) {
        updates.put("updatedAt", Timestamp.now());
        db.collection(COLLECTION_TESTS)
                .document(testId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test updated: " + testId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating test", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Delete test
     */
    public void deleteTest(String testId, OnCompleteListener listener) {
        db.collection(COLLECTION_TESTS)
                .document(testId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Test deleted: " + testId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting test", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Toggle test active status
     */
    public void toggleTestActive(String testId, boolean isActive, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("active", isActive);
        updateTest(testId, updates, listener);
    }

    // ==================== UTILITY OPERATIONS ====================

    /**
     * Check if a document exists in a collection
     */
    public void documentExists(String collection, String documentId, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection(collection)
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (onSuccess != null) onSuccess.onSuccess(document.exists());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking document exists", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get document count in a collection
     */
    public void getCollectionCount(String collection, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        db.collection(collection)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (onSuccess != null) onSuccess.onSuccess(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting collection count", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Delete multiple documents by IDs
     */
    public void deleteDocuments(String collection, List<String> documentIds, OnCompleteListener listener) {
        WriteBatch batch = db.batch();

        for (String docId : documentIds) {
            DocumentReference docRef = db.collection(collection).document(docId);
            batch.delete(docRef);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documents deleted from " + collection);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting documents", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Generate a new document ID
     */
    public String generateDocumentId(String collection) {
        return db.collection(collection).document().getId();
    }

    /**
     * Get statistics for dashboard
     */
    public void getDashboardStats(OnSuccessListener<Map<String, Integer>> onSuccess, OnFailureListener onFailure) {
        Map<String, Integer> stats = new HashMap<>();

        // Get student count
        db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(studentSnapshot -> {
                    stats.put("studentCount", studentSnapshot.size());

                    // Get question count
                    db.collection(COLLECTION_QUESTIONS)
                            .get()
                            .addOnSuccessListener(questionSnapshot -> {
                                stats.put("questionCount", questionSnapshot.size());
                                if (onSuccess != null) onSuccess.onSuccess(stats);
                            })
                            .addOnFailureListener(e -> {
                                if (onFailure != null) onFailure.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== FCM TOKEN OPERATIONS ====================

    /**
     * Save FCM token for a user
     */
    public void saveFCMToken(String userId, String token, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        updates.put("lastTokenUpdate", Timestamp.now());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token saved for user: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving FCM token", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Delete FCM token for a user
     */
    public void deleteFCMToken(String userId, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", FieldValue.delete());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token deleted for user: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting FCM token", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get all student FCM tokens
     */
    public void getAllStudentTokens(OnSuccessListener<List<String>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> tokens = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String token = doc.getString("fcmToken");
                        if (token != null && !token.isEmpty()) {
                            tokens.add(token);
                        }
                    }
                    if (onSuccess != null) onSuccess.onSuccess(tokens);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting student tokens", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    /**
     * Get all teacher FCM tokens
     */
    public void getTeacherTokens(OnSuccessListener<List<String>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("role", "teacher")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> tokens = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String token = doc.getString("fcmToken");
                        if (token != null && !token.isEmpty()) {
                            tokens.add(token);
                        }
                    }
                    if (onSuccess != null) onSuccess.onSuccess(tokens);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting teacher tokens", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== NOTIFICATION PREFERENCES OPERATIONS ====================

    /**
     * Save notification preferences for a user
     */
    public void saveNotificationPreferences(String userId, NotificationPreferences prefs, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("notificationPreferences", prefs);

        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification preferences saved for user: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving notification preferences", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Get notification preferences for a user
     */
    public void getNotificationPreferences(String userId, OnSuccessListener<NotificationPreferences> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        NotificationPreferences prefs = null;
                        try {
                            Map<String, Object> prefsMap = (Map<String, Object>) document.get("notificationPreferences");
                            if (prefsMap != null) {
                                prefs = document.toObject(User.class).getNotificationPreferences();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing notification preferences", e);
                        }
                        if (prefs == null) {
                            prefs = NotificationPreferences.createDefault();
                        }
                        if (onSuccess != null) onSuccess.onSuccess(prefs);
                    } else {
                        if (onSuccess != null) onSuccess.onSuccess(NotificationPreferences.createDefault());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting notification preferences", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }

    // ==================== ACTIVITY TRACKING OPERATIONS ====================

    /**
     * Update last activity timestamp for a user
     */
    public void updateLastActivity(String userId, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastActivityAt", Timestamp.now());

        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Last activity updated for user: " + userId);
                    if (listener != null) listener.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating last activity", e);
                    if (listener != null) listener.onComplete(false, e);
                });
    }

    /**
     * Update last activity for current user
     */
    public void updateCurrentUserLastActivity(OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (listener != null) listener.onComplete(false, new Exception("No authenticated user"));
            return;
        }
        updateLastActivity(userId, listener);
    }

    /**
     * Get inactive users (no activity in specified days)
     */
    public void getInactiveUsers(int daysSinceLastActivity, OnSuccessListener<List<User>> onSuccess, OnFailureListener onFailure) {
        // Calculate the cutoff timestamp
        long cutoffMillis = System.currentTimeMillis() - (daysSinceLastActivity * 24 * 60 * 60 * 1000L);
        Timestamp cutoffTimestamp = new Timestamp(cutoffMillis / 1000, 0);

        db.collection(COLLECTION_USERS)
                .whereLessThan("lastActivityAt", cutoffTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        users.add(doc.toObject(User.class));
                    }
                    if (onSuccess != null) onSuccess.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting inactive users", e);
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }
}
