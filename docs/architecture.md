# פרק ארכיטכטורה - אפליקציית Physics Simulator

## 1. רכיבים עיקריים שהאפליקציה משתמשת בהם

### 1.1 Firebase Services

#### Firebase Authentication
**תיאור**: מערכת אימות משתמשים המבוססת על כתובת דוא"ל וסיסמה.

**שימוש באפליקציה**:
- יצירת חשבונות משתמש חדשים
- התחברות משתמשים קיימים
- ניתוק משתמשים
- שמירת מצב התחברות

**הצדקה**: Firebase Authentication מספק פתרון אבטחה מובנה וניהול מצב משתמש ללא צורך בשרת backend נפרד.

#### Cloud Firestore
**תיאור**: מסד נתונים NoSQL בענן לשמירת נתונים בזמן אמת.

**שימוש באפליקציה**:
- שמירת פרטי משתמשים (שם, דוא"ל, תפקיד, ניקוד)
- שמירת תוצאות מבחנים
- שליפת רשימת תלמידים (עבור מורים)
- עדכון ניקוד כולל

**אוספים במסד הנתונים**:
- `users` - פרטי משתמשים
- `quizResults` - תוצאות מבחנים

**הצדקה**: Firestore מספק סנכרון אוטומטי, שאילתות מתקדמות, ויכולת גישה לנתונים מכל מקום עם חיבור לאינטרנט. מתאים לאפליקציה חינוכית שבה המשתמשים צריכים גישה לנתונים שלהם מכל מכשיר.

### 1.2 RecyclerView
**תיאור**: רכיב להצגת רשימות גדולות בצורה יעילה.

**שימוש באפליקציה**:
- תצוגת רשימת תוצאות מבחנים (`GradesActivity`)
- תצוגת רשימת תלמידים (`TeacherHomeActivity`)

**הצדקה**: RecyclerView משתמש במיחזור views לביצועים מיטביים, חיוני כשיש כמות גדולה של נתונים להציג.

### 1.3 Material Design Components
**תיאור**: ספריית רכיבי UI של Google המיישמת את עקרונות Material Design.

**רכיבים בשימוש**:
- MaterialCardView - כרטיסים עם הצללה
- MaterialToolbar - סרגל כלים עליון
- TextInputLayout - שדות טקסט עם validation
- Material Buttons - כפתורים מעוצבים

**הצדקה**: מראה מודרני ואחיד, חוויית משתמש סטנדרטית שמשתמשי Android מכירים.

### 1.4 Custom View (PhysicsSimulationView)
**תיאור**: תצוגה מותאמת אישית לציור סימולציית פיזיקה.

**שימוש באפליקציה**:
- הצגה ויזואלית של חוקי ניוטון
- ציור וקטורי כוח
- אנימציה של תנועת עצם

**הצדקה**: אין רכיב מובנה המאפשר סימולציית פיזיקה אינטראקטיבית, ולכן נדרש custom view עם ציור ידני על Canvas.

### 1.5 אנימציות
**סוג**: אנימציה ידנית באמצעות `Runnable` ו-`invalidate()`

**שימוש באפליקציה**:
- אנימציית תנועת העצם בסימולציית הפיזיקה
- קצב רענון: ~60 FPS (16 מילישניות בין פריימים)

**הצדקה**: אנימציה ידנית מאפשרת שליטה מלאה על חישובי הפיזיקה ועדכון המיקום בזמן אמת.

---

## 2. מחלקות האפליקציה ותכונותיהן

### 2.1 Application Class

#### PhizApplication
**מטרה**: אתחול Firebase בעת הפעלת האפליקציה.

**מחלקת אב**: `android.app.Application`

**תכונות**: אין

**מתודות עיקריות**:
- `onCreate()` - מאתחלת את Firebase דרך `FirebaseApp.initializeApp(this)`

**חשיבות**: חייבת להיות רשומה ב-AndroidManifest.xml כדי שהאתחול יקרה לפני כל שימוש בשירותי Firebase.

---

### 2.2 Activity Classes

#### MainActivity
**מטרה**: נקודת כניסה לאפליקציה שמעבירה מיד ל-LoginActivity.

**מחלקת אב**: `AppCompatActivity`

**תכונות**: אין

**מתודות עיקריות**:
- `onCreate()` - פותחת את LoginActivity ומסיימת את עצמה

**מסך**: אין UI, רק מסך splash קצר

---

#### LoginActivity
**מטרה**: מסך התחברות משתמשים.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private TextInputEditText emailEditText;          // שדה הזנת דוא"ל
private TextInputEditText passwordEditText;       // שדה הזנת סיסמה
private Button loginButton;                       // כפתור התחברות
private TextView registerTextView;                // קישור למסך הרשמה
private ProgressBar progressBar;                  // אינדיקטור טעינה
private FirebaseAuth mAuth;                       // מנהל אימות Firebase
private FirebaseFirestore db;                     // מנהל מסד נתונים
```

**מתודות עיקריות**:
- `onCreate()` - אתחול UI ו-Firebase
- `loginUser()` - מבצעת אימות ומעבר למסך מתאים לפי תפקיד

**ניווט**:
- הצלחה → `StudentHomeActivity` או `TeacherHomeActivity` (לפי תפקיד)
- כישלון → הצגת הודעת שגיאה

---

#### RegisterActivity
**מטרה**: מסך הרשמת משתמשים חדשים.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private TextInputEditText nameEditText;           // שדה הזנת שם
private TextInputEditText emailEditText;          // שדה הזנת דוא"ל
private TextInputEditText passwordEditText;       // שדה הזנת סיסמה
private RadioGroup roleRadioGroup;                // בחירת תפקיד (תלמיד/מורה)
private Button registerButton;                    // כפתור הרשמה
private TextView loginTextView;                   // קישור למסך התחברות
private ProgressBar progressBar;                  // אינדיקטור טעינה
private FirebaseAuth mAuth;                       // מנהל אימות Firebase
private FirebaseFirestore db;                     // מנהל מסד נתונים
private static final String TAG = "RegisterActivity"; // תג ל-logging
```

**מתודות עיקריות**:
- `onCreate()` - אתחול UI ו-Firebase
- `registerUser()` - יוצרת חשבון Firebase Auth ושומרת משתמש ב-Firestore

**תהליך הרשמה**:
1. בדיקת תקינות נתונים
2. יצירת משתמש ב-Firebase Authentication
3. שמירת אובייקט User ב-Firestore
4. מעבר למסך מתאים לפי תפקיד

---

#### StudentHomeActivity
**מטרה**: מסך בית לתלמידים עם גישה לכל התכונות.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private TextView welcomeTextView;                 // הודעת ברוכים הבאים עם שם
private TextView scoreTextView;                   // הצגת ניקוד כולל
private MaterialCardView simulationCard;          // כרטיס לסימולציית פיזיקה
private MaterialCardView quizCard;                // כרטיס למבחן
private MaterialCardView gradesCard;              // כרטיס לציונים
private Button logoutButton;                      // כפתור התנתקות
private FirebaseAuth mAuth;                       // מנהל אימות
private FirebaseFirestore db;                     // מנהל מסד נתונים
private String userId;                            // מזהה משתמש נוכחי
```

**מתודות עיקריות**:
- `onCreate()` - אתחול UI וטעינת נתוני משתמש
- `loadUserData()` - שליפת פרטי משתמש מ-Firestore והצגתם
- `logout()` - ניתוק והחזרה ל-LoginActivity

**ניווט**:
- כרטיס סימולציה → `PhysicsSimulationActivity`
- כרטיס מבחן → `QuizActivity`
- כרטיס ציונים → `GradesActivity`

---

#### TeacherHomeActivity
**מטרה**: מסך בית למורים עם תצוגת כל התלמידים וציוניהם.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private RecyclerView studentsRecyclerView;        // רשימת תלמידים
private TextView emptyTextView;                   // הודעה כשאין תלמידים
private Button logoutButton;                      // כפתור התנתקות
private StudentAdapter adapter;                   // מתאם לרשימה
private FirebaseAuth mAuth;                       // מנהל אימות
private FirebaseFirestore db;                     // מנהל מסד נתונים
private List<User> studentsList;                  // רשימת אובייקטי תלמידים
```

**מחלקות פנימיות**:
- `StudentAdapter extends RecyclerView.Adapter` - מתאם לתצוגת תלמידים
- `StudentViewHolder extends RecyclerView.ViewHolder` - מחזיק תצוגה לפריט תלמיד

**מתודות עיקריות**:
- `onCreate()` - אתחול UI וטעינת רשימת תלמידים
- `loadStudents()` - שליפת כל המשתמשים עם תפקיד "student" מ-Firestore
- `logout()` - ניתוק והחזרה ל-LoginActivity

---

#### PhysicsSimulationActivity
**מטרה**: מסך סימולציה אינטראקטיבית של חוקי ניוטון לתנועה.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private PhysicsSimulationView simulationView;     // תצוגה מותאמת לסימולציה
private SeekBar massSeekBar;                      // slider למסה
private SeekBar forceSeekBar;                     // slider לכוח
private SeekBar frictionSeekBar;                  // slider לחיכוך
private SeekBar angleSeekBar;                     // slider לזווית
private TextView massValueTextView;               // הצגת ערך מסה
private TextView forceValueTextView;              // הצגת ערך כוח
private TextView frictionValueTextView;           // הצגת ערך חיכוך
private TextView angleValueTextView;              // הצגת ערך זווית
private TextView accelerationTextView;            // הצגת תאוצה מחושבת
private TextView velocityTextView;                // הצגת מהירות מחושבת
private TextView positionTextView;                // הצגת מיקום מחושב
private TextView lawDescriptionTextView;          // תיאור חוקי ניוטון
private Button startButton;                       // כפתור התחלה
private Button resetButton;                       // כפתור איפוס
private Button backButton;                        // כפתור חזרה
private float mass = 5.0f;                        // מסה ראשונית (ק"ג)
private float appliedForce = 30.0f;               // כוח מופעל ראשוני (ניוטון)
private float frictionCoefficient = 0.2f;         // מקדם חיכוך ראשוני
private float angle = 0.0f;                       // זווית ראשונית (מעלות)
```

**טווחי פרמטרים**:
- מסה: 0.1 - 10.0 ק"ג
- כוח: 0 - 100 ניוטון
- מקדם חיכוך: 0 - 1.0
- זווית: 0 - 90 מעלות

**מתודות עיקריות**:
- `onCreate()` - אתחול UI והגדרת listeners
- `updateParametersInView()` - העברת פרמטרים ל-PhysicsSimulationView
- SeekBar listeners - עדכון בזמן אמת של פרמטרים

---

#### QuizActivity
**מטרה**: מסך מבחן אינטראקטיבי על חוקי ניוטון לתנועה.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private TextView questionNumberTextView;          // מספר שאלה נוכחית
private TextView questionTextView;                // טקסט השאלה
private TextView feedbackTextView;                // משוב על תשובה
private RadioGroup answersRadioGroup;             // קבוצת תשובות
private RadioButton option1, option2, option3, option4; // אפשרויות תשובה
private Button submitButton;                      // כפתור שליחת תשובה
private Button nextButton;                        // כפתור שאלה הבאה
private List<Question> questions;                 // רשימת שאלות
private int currentQuestionIndex = 0;             // אינדקס שאלה נוכחית
private int score = 0;                            // ניקוד צבור
private boolean answerSubmitted = false;          // האם התשובה נשלחה
private FirebaseAuth mAuth;                       // מנהל אימות
private FirebaseFirestore db;                     // מנהל מסד נתונים
```

**מחלקות פנימיות**:
- `Question` - מחלקה סטטית לייצוג שאלה
  - `String question` - טקסט השאלה
  - `String[] options` - מערך של 4 אפשרויות
  - `int correctAnswer` - אינדקס התשובה הנכונה (0-3)

**פרטי המבחן**:
- 5 שאלות סך הכל
- 20 נקודות לכל תשובה נכונה
- ניקוד מקסימלי: 100 נקודות

**מתודות עיקריות**:
- `onCreate()` - אתחול UI ושאלות
- `initializeQuestions()` - יצירת רשימת השאלות
- `displayQuestion()` - הצגת שאלה נוכחית
- `checkAnswer()` - בדיקת תשובה ומתן משוב
- `showNextQuestion()` - מעבר לשאלה הבאה
- `finishQuiz()` - שמירת תוצאות והצגת dialog סיכום
- `saveQuizResult()` - שמירת תוצאה ב-Firestore ועדכון ניקוד כולל

---

#### GradesActivity
**מטרה**: מסך הצגת היסטוריית ציונים של המשתמש.

**מחלקת אב**: `AppCompatActivity`

**תכונות**:
```java
private RecyclerView gradesRecyclerView;          // רשימת ציונים
private TextView emptyTextView;                   // הודעה כשאין ציונים
private Button backButton;                        // כפתור חזרה
private GradeAdapter adapter;                     // מתאם לרשימה
private FirebaseAuth mAuth;                       // מנהל אימות
private FirebaseFirestore db;                     // מנהל מסד נתונים
private List<QuizResult> quizResults;             // רשימת תוצאות מבחנים
```

**מחלקות פנימיות**:
- `GradeAdapter extends RecyclerView.Adapter` - מתאם לתצוגת ציונים
- `GradeViewHolder extends RecyclerView.ViewHolder` - מחזיק תצוגה לפריט ציון
  - `TextView quizNameTextView` - שם המבחן
  - `TextView dateTextView` - תאריך המבחן
  - `TextView scoreTextView` - ציון
  - `TextView percentageTextView` - אחוזים

**מתודות עיקריות**:
- `onCreate()` - אתחול UI וטעינת ציונים
- `loadGrades()` - שליפת תוצאות המבחן של המשתמש מ-Firestore
- פורמט תאריך: "MMM dd, yyyy HH:mm" (לדוגמה: "Dec 15, 2024 14:30")

---

### 2.3 Model Classes

#### User
**מטרה**: מודל נתונים לחשבון משתמש.

**תכונות**:
```java
private String uid;                               // מזהה Firebase Auth (מפתח ראשי)
private String email;                             // כתובת דוא"ל
private String name;                              // שם תצוגה
private String role;                              // תפקיד: "student" או "teacher"
private int totalScore;                           // ניקוד מצטבר ממבחנים
```

**Firestore**:
- Collection: `users`
- Document ID: `uid` (Firebase Auth UID)

**מתודות**:
- Constructor ברירת מחדל (נדרש לdeserialization של Firestore)
- Constructor מלא
- Getters ו-Setters לכל השדות

---

#### QuizResult
**מטרה**: מודל נתונים לתוצאת מבחן.

**תכונות**:
```java
private String quizId;                            // מזהה מבחן
private String userId;                            // מזהה משתמש (foreign key)
private String quizName;                          // שם המבחן להצגה
private int score;                                // ניקוד שהושג
private int totalQuestions;                       // מספר שאלות במבחן
private Timestamp timestamp;                      // תאריך ושעת השלמה
```

**Firestore**:
- Collection: `quizResults`
- Document ID: Auto-generated

**מתודות**:
- Constructor ברירת מחדל (נדרש לdeserialization של Firestore)
- Constructor מלא
- Getters ו-Setters לכל השדות
- Timestamp נוצר אוטומטית: `Timestamp.now()`

---

### 2.4 Custom View Classes

#### PhysicsSimulationView
**מטרה**: תצוגה מותאמת אישית לסימולציית פיזיקה עם ציור וקטורי כוח.

**מחלקת אב**: `android.view.View`

**תכונות**:
```java
private Paint objectPaint;                        // צבע העצם
private Paint surfacePaint;                       // צבע המשטח
private Paint vectorPaint;                        // צבע וקטורים
private Paint textPaint;                          // צבע טקסט
private float objectX, objectY;                   // מיקום העצם
private float objectSize = 80f;                   // גודל העצם בpixels
private float mass;                               // מסה (ק"ג)
private float appliedForce;                       // כוח מופעל (ניוטון)
private float frictionCoefficient;                // מקדם חיכוך
private float angle;                              // זווית המשטח (מעלות)
private boolean isAnimating;                      // האם האנימציה פועלת
private float velocity;                           // מהירות (m/s)
private float acceleration;                       // תאוצה (m/s²)
private float position;                           // מיקום (m)
private static final float GRAVITY = 9.8f;        // קבוע כבידה (m/s²)
private OnPhysicsUpdateListener listener;         // listener לעדכוני פיזיקה
```

**ממשק פנימי**:
- `OnPhysicsUpdateListener` - מאזין לעדכוני ערכי פיזיקה
  - מתודה: `onUpdate(float acceleration, float velocity, float position)`

**אנימציה**:
- מימוש: `Runnable animationRunnable` עם `postDelayed()`
- קצב פריימים: ~60 FPS (16ms delay)
- ציור ידני על Canvas עם `invalidate()`

**חישובי פיזיקה** (חוק שני של ניוטון):
```
Net Force = Applied Force - Friction Force - Gravity Component
Acceleration = Net Force / Mass
Velocity += Acceleration × ΔTime
Position += Velocity × ΔTime
```

**ציור ויזואלי**:
- מלבן כחול (העצם)
- קו חום (משטח בזווית)
- וקטורי כוח צבעוניים:
  - אדום: כוח כבידה (Fg)
  - כחול: כוח נורמלי (Fn)
  - ירוק: כוח מופעל (Fa)
  - צהוב: כוח חיכוך (Ff)
- חיצים בקצה הוקטורים
- תוויות טקסט על הוקטורים

**מתודות ציבוריות**:
- `setParameters(float mass, float force, float friction, float angle)` - הגדרת פרמטרי סימולציה
- `startAnimation()` - התחלת האנימציה
- `stopAnimation()` - עצירת האנימציה
- `reset()` - איפוס למצב התחלתי
- `setOnPhysicsUpdateListener(OnPhysicsUpdateListener listener)` - הגדרת listener

---

## 3. מבנה מסד הנתונים (Firestore)

### 3.1 Collection: users

**מטרה**: שמירת פרטי משתמשים רשומים

**מבנה Document**:
```
Document ID: {Firebase Auth UID}
{
  "uid": String,              // מזהה ייחודי
  "email": String,            // דוא"ל
  "name": String,             // שם מלא
  "role": String,             // "student" או "teacher"
  "totalScore": Number        // ניקוד מצטבר (int)
}
```

**אינדקסים**: Document ID (uid)

---

### 3.2 Collection: quizResults

**מטרה**: שמירת תוצאות מבחנים

**מבנה Document**:
```
Document ID: {Auto-generated}
{
  "quizId": String,           // מזהה מבחן
  "userId": String,           // מזהה משתמש (foreign key)
  "quizName": String,         // שם המבחן
  "score": Number,            // ניקוד (int)
  "totalQuestions": Number,   // מספר שאלות (int)
  "timestamp": Timestamp      // תאריך ושעה
}
```

**שאילתות נפוצות**:
- `whereEqualTo("userId", userId).orderBy("timestamp", DESCENDING)` - כל התוצאות של משתמש

---

## 4. הרשאות ואבטחה

### 4.1 Android Permissions

**הרשאות נדרשות** (AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**הרשאות לא בשימוש**: מצלמה, מיקום, אחסון, חיישנים, Bluetooth, NFC

---

### 4.2 Firestore Security Rules

**חוקי אבטחה נדרשים**:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // משתמש יכול לקרוא/לכתוב רק את המסמך שלו
    match /users/{userId} {
      allow read, write: if request.auth != null
                         && request.auth.uid == userId;
    }
    // משתמשים מחוברים יכולים לקרוא/לכתוב כל דבר אחר
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## 5. תצורת Build

### 5.1 גרסאות SDK
- `compileSdk`: 34
- `minSdk`: 26 (Android 8.0 Oreo)
- `targetSdk`: 34

### 5.2 Java Version
- Java 11

### 5.3 תלויות עיקריות
```gradle
// AndroidX
androidx.appcompat
androidx.constraintlayout
androidx.recyclerview
com.google.android.material

// Firebase
Firebase BOM 34.6.0
firebase-auth
firebase-firestore
firebase-analytics
```

---

## 6. זרימת ניווט

```
MainActivity (launcher)
    ↓
LoginActivity
    ├→ RegisterActivity
    │      ↓
    │   [ניתוב לפי תפקיד]
    ↓
[ניתוב לפי תפקיד]
    ├→ StudentHomeActivity
    │      ├→ PhysicsSimulationActivity
    │      ├→ QuizActivity → (dialog סיכום) → GradesActivity
    │      └→ GradesActivity
    └→ TeacherHomeActivity
           └→ (התנתקות) → LoginActivity
```

### ניתוב לפי תפקיד (Role-Based Routing)
- לאחר התחברות/הרשמה, שדה `role` ב-Firestore קובע את המסך:
  - `role: "teacher"` → TeacherHomeActivity
  - `role: "student"` → StudentHomeActivity (ברירת מחדל)

---

## 7. תבניות עיצוב (Design Patterns)

### 7.1 ViewHolder Pattern
- שימוש ב-RecyclerView.ViewHolder במחלקות:
  - `TeacherHomeActivity.StudentViewHolder`
  - `GradesActivity.GradeViewHolder`

### 7.2 Adapter Pattern
- מתאמים מותאמים אישית:
  - `TeacherHomeActivity.StudentAdapter`
  - `GradesActivity.GradeAdapter`

### 7.3 Listener Pattern
- `PhysicsSimulationView.OnPhysicsUpdateListener` - callback לעדכוני פיזיקה
- Firebase listeners: `addOnSuccessListener()`, `addOnFailureListener()`

### 7.4 Singleton Pattern
- `FirebaseAuth.getInstance()`
- `FirebaseFirestore.getInstance()`

---

## 8. רכיבים שלא בשימוש

האפליקציה **לא** משתמשת ב:
- SharedPreferences (כל הנתונים ב-Firestore)
- SQLite Database
- Content Providers
- Broadcast Receivers
- Services (Foreground/Background)
- WorkManager
- Notification API
- AlarmManager
- Camera API
- MediaPlayer / SoundPool (אין מוסיקה או צלילים)
- Vibrator (אין רטט)
- Sensors (Accelerometer, Gyroscope)
- Location Services
- Bluetooth / NFC
- External Storage
- Deep Linking
- Widgets

---

## 9. סיכום אדריכלי

**סוג ארכיטכטורה**: Traditional Android Activity-based Architecture עם Firebase Backend

**מאפיינים עיקריים**:
- מחלקת Application מותאמת לאתחול Firebase
- מערכת ניווט מבוססת תפקידים (Role-Based Navigation)
- פעולות CRUD על Firestore
- UI מבוסס Material Design
- סימולציית פיזיקה מותאמת אישית עם ציור Canvas
- תבנית RecyclerView לרשימות
- אין שמירת נתונים מקומית (הכל בענן דרך Firestore)
- אנימציה פשוטה דרך View.post(Runnable)
- תלות מלאה באינטרנט (כל התכונות דורשות חיבור רשת)

**סך מחלקות Java**: 13
- 8 מחלקות Activity
- 2 מחלקות Model
- 1 מחלקת Custom View
- 1 מחלקת Application
- 1 מחלקת Test

---

## 10. הצדקות לבחירות ארכיטקטוניות

### למה Firebase ולא SQLite?
- **יתרונות**:
  - סנכרון אוטומטי בין מכשירים
  - גיבוי אוטומטי בענן
  - אימות מובנה
  - אין צורך בשרת Backend
  - קל לשיתוף נתונים בין מורים ותלמידים
- **חסרונות**:
  - דורש חיבור לאינטרנט
  - עלות (אם יש שימוש רב)

### למה Custom View לסימולציה?
- אין ספריה מוכנה לסימולציית פיזיקה חינוכית
- שליטה מלאה על הציור והאנימציה
- יכולת להציג וקטורי כוח באופן ויזואלי
- גמישות לשינויים עתידיים

### למה אין שמירה מקומית?
- האפליקציה חינוכית ומשתמשים צריכים גישה מכל מקום
- מורים צריכים לראות את התקדמות התלמידים בזמן אמת
- פשטות - אין צורך לסנכרן בין SQLite ו-Firestore
