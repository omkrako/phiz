# Firestore Tests Database Structure

This guide explains the data structure for storing and retrieving tests/quizzes from Firestore.

## Data Models

### Question.java
Represents a single quiz question with the following fields:
- `questionId` (String): Unique identifier for the question
- `questionText` (String): The question text
- `options` (List<String>): List of answer options (typically 4 options)
- `correctAnswerIndex` (int): 0-based index of the correct answer
- `explanation` (String): Optional explanation for the correct answer
- `pointValue` (int): Points awarded for correct answer (default: 20)
- `difficulty` (String): "easy", "medium", or "hard"

### Test.java
Represents a complete test/quiz with the following fields:
- `testId` (String): Unique identifier for the test
- `testName` (String): Display name of the test
- `description` (String): Test description
- `subject` (String): Subject area (e.g., "Newton's Laws", "Kinematics")
- `questions` (List<Question>): List of Question objects
- `totalPoints` (int): Total possible points
- `passingScore` (int): Minimum score to pass
- `timeLimit` (int): Time limit in minutes (0 = no limit)
- `difficulty` (String): Overall difficulty level
- `isActive` (boolean): Whether the test is currently available
- `createdBy` (String): Teacher/creator user ID
- `createdAt` (Timestamp): Creation timestamp
- `updatedAt` (Timestamp): Last update timestamp

## Firestore Collection Structure

```
tests/
  {testId}/
    testId: "newton_laws_quiz_1"
    testName: "Newton's Laws Quiz"
    subject: "Newton's Laws"
    questions: [
      {
        questionId: "q1",
        questionText: "What is Newton's First Law?",
        options: ["Law of Inertia", "Law of Acceleration", "Law of Action-Reaction", "Law of Gravity"],
        correctAnswerIndex: 0,
        explanation: "Newton's First Law states that an object at rest stays at rest...",
        pointValue: 20,
        difficulty: "easy"
      },
      // ... more questions
    ]
    totalPoints: 100
    passingScore: 60
    timeLimit: 30
    difficulty: "medium"
    isActive: true
    createdBy: "teacher_uid"
    createdAt: Timestamp
    updatedAt: Timestamp
```

## Code Examples

### 1. Creating and Saving a Test to Firestore

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();
FirebaseAuth mAuth = FirebaseAuth.getInstance();

// Create questions
List<Question> questions = new ArrayList<>();

Question q1 = new Question(
    "q1",
    "What is Newton's First Law?",
    Arrays.asList(
        "Law of Inertia",
        "Law of Acceleration",
        "Law of Action-Reaction",
        "Law of Gravity"
    ),
    0,
    "Newton's First Law states that an object at rest stays at rest and an object in motion stays in motion unless acted upon by an external force.",
    20,
    "easy"
);
questions.add(q1);

Question q2 = new Question(
    "q2",
    "Newton's Second Law is expressed as:",
    Arrays.asList("F = ma", "E = mc²", "v = u + at", "s = ut + ½at²"),
    0,
    "F = ma represents Force equals mass times acceleration.",
    20,
    "medium"
);
questions.add(q2);

// Create test
Test test = new Test(
    "newton_laws_quiz_1",
    "Newton's Laws Quiz",
    "Basic quiz on Newton's Three Laws of Motion",
    "Newton's Laws",
    questions,
    100,
    60,
    30,
    "medium",
    true,
    mAuth.getCurrentUser().getUid()
);

// Save to Firestore
db.collection("tests")
    .document(test.getTestId())
    .set(test)
    .addOnSuccessListener(aVoid -> {
        Log.d("Firestore", "Test saved successfully");
    })
    .addOnFailureListener(e -> {
        Log.e("Firestore", "Error saving test", e);
    });
```

### 2. Retrieving a Single Test from Firestore

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();
String testId = "newton_laws_quiz_1";

db.collection("tests")
    .document(testId)
    .get()
    .addOnSuccessListener(documentSnapshot -> {
        if (documentSnapshot.exists()) {
            Test test = documentSnapshot.toObject(Test.class);
            if (test != null) {
                // Use the test
                Log.d("Firestore", "Test loaded: " + test.getTestName());
                Log.d("Firestore", "Questions: " + test.getQuestionCount());
            }
        } else {
            Log.d("Firestore", "Test not found");
        }
    })
    .addOnFailureListener(e -> {
        Log.e("Firestore", "Error loading test", e);
    });
```

### 3. Retrieving All Active Tests

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();

db.collection("tests")
    .whereEqualTo("isActive", true)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .addOnSuccessListener(queryDocumentSnapshots -> {
        List<Test> tests = new ArrayList<>();
        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
            Test test = doc.toObject(Test.class);
            if (test != null) {
                tests.add(test);
            }
        }
        Log.d("Firestore", "Loaded " + tests.size() + " active tests");
        // Use the tests list (e.g., display in RecyclerView)
    })
    .addOnFailureListener(e -> {
        Log.e("Firestore", "Error loading tests", e);
    });
```

### 4. Retrieving Tests by Subject

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();

db.collection("tests")
    .whereEqualTo("subject", "Newton's Laws")
    .whereEqualTo("isActive", true)
    .get()
    .addOnSuccessListener(queryDocumentSnapshots -> {
        List<Test> tests = queryDocumentSnapshots.toObjects(Test.class);
        Log.d("Firestore", "Found " + tests.size() + " Newton's Laws tests");
    })
    .addOnFailureListener(e -> {
        Log.e("Firestore", "Error loading tests", e);
    });
```

### 5. Updating a Test

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();
String testId = "newton_laws_quiz_1";

// Update specific fields
db.collection("tests")
    .document(testId)
    .update(
        "isActive", false,
        "updatedAt", Timestamp.now()
    )
    .addOnSuccessListener(aVoid -> {
        Log.d("Firestore", "Test updated");
    })
    .addOnFailureListener(e -> {
        Log.e("Firestore", "Error updating test", e);
    });
```

### 6. Using Test Data in QuizActivity

```java
public class QuizActivity extends AppCompatActivity {
    private Test currentTest;
    private int currentQuestionIndex = 0;

    private void loadTestFromFirestore(String testId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tests")
            .document(testId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                currentTest = documentSnapshot.toObject(Test.class);
                if (currentTest != null) {
                    displayQuestion(currentQuestionIndex);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading test", Toast.LENGTH_SHORT).show();
            });
    }

    private void displayQuestion(int index) {
        Question question = currentTest.getQuestion(index);
        if (question != null) {
            questionTextView.setText(question.getQuestionText());
            List<String> options = question.getOptions();
            option1.setText(options.get(0));
            option2.setText(options.get(1));
            option3.setText(options.get(2));
            option4.setText(options.get(3));
        }
    }

    private void checkAnswer(int selectedIndex) {
        Question question = currentTest.getQuestion(currentQuestionIndex);
        if (question.isCorrect(selectedIndex)) {
            score += question.getPointValue();
            feedbackTextView.setText("Correct! " + question.getExplanation());
        } else {
            feedbackTextView.setText("Incorrect. " + question.getExplanation());
        }
    }
}
```

## Firestore Security Rules

Add these rules to allow teachers to create/edit tests and students to read them:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Existing user rules
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Test rules
    match /tests/{testId} {
      // Anyone authenticated can read active tests
      allow read: if request.auth != null;

      // Only teachers can create/update/delete tests
      allow create, update, delete: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "teacher";
    }

    // Existing quiz results rules
    match /quizResults/{resultId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Migration from Hardcoded Questions

To migrate from the current hardcoded questions in QuizActivity:

1. Create a one-time migration script or admin function to convert existing questions to Test objects
2. Save them to Firestore
3. Update QuizActivity to load tests from Firestore instead of hardcoded list
4. Add test selection UI in StudentHomeActivity

## Best Practices

1. **Test IDs**: Use descriptive IDs like `"newton_laws_quiz_1"` instead of auto-generated IDs for easier reference
2. **Question IDs**: Use simple IDs like `"q1"`, `"q2"` within each test
3. **Point Values**: Keep consistent (e.g., 20 points per question) or vary by difficulty
4. **Active Flag**: Use `isActive` to hide tests without deleting them
5. **Timestamps**: Always update `updatedAt` when modifying tests
6. **Explanations**: Include explanations for educational value
7. **Validation**: Validate that `correctAnswerIndex` is within options array bounds

## Teacher Test Management UI (Future Enhancement)

Consider adding:
- TeacherCreateTestActivity: UI for teachers to create new tests
- TeacherManageTestsActivity: List and edit existing tests
- Test preview functionality
- Question bank/library for reusing questions
- Test analytics (average scores, question difficulty analysis)