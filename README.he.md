# סימולטור פיזיקה (Phiz)

אפליקציית אנדרויד חינוכית להוראת חוקי התנועה של ניוטון באמצעות סימולציות אינטראקטיביות ומבחנים.

עברית | [English](README.md)

## סקירה כללית

סימולטור הפיזיקה הוא פלטפורמת למידה ניידת המיועדת לחינוך פיזיקלי. האפליקציה מספקת:
- סימולציות פיזיקליות אינטראקטיביות המדגימות את חוקי ניוטון
- מערכת מבחנים לבחינת הידע
- מעקב אחר ציונים והתקדמות
- גישה מבוססת תפקידים לתלמידים ומורים

## תכונות

### לתלמידים
- **סימולציות אינטראקטיביות**: הצגה ויזואלית של חוקי התנועה של ניוטון עם סימולציות פיזיקליות בזמן אמת
- **מערכת מבחנים**: בדקו את הבנתכם עם מבחנים אינטראקטיביים
- **מעקב ציונים**: עקבו אחר ההתקדמות ותוצאות המבחנים שלכם
- **ממשק ידידותי למשתמש**: עיצוב נקי ואינטואיטיבי מבוסס Material Design

### למורים
- **ניהול תלמידים**: צפייה בכל התלמידים הרשומים
- **מעקב ציונים**: מעקב אחר ביצועי התלמידים במבחנים
- **סקירת התקדמות**: צפייה בדוחות ציונים מקיפים

## מחסנית טכנולוגית

- **שפה**: Java
- **פלטפורמה**: Android (SDK 26+)
- **ארכיטקטורה**: ניווט מבוסס Activity עם ניתוב מבוסס תפקידים
- **Backend**: Firebase
  - Firebase Authentication (אימייל/סיסמה)
  - Cloud Firestore (נתוני משתמשים, תוצאות מבחנים)
  - Firebase Analytics
- **מסגרת UI**: Material Components עבור Android
- **מערכת Build**: Gradle עם Kotlin DSL

## מבנה הפרויקט

```
phiz/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/phiz/
│   │   │   │   ├── PhizApplication.java          # אתחול האפליקציה
│   │   │   │   ├── MainActivity.java             # נקודת כניסה
│   │   │   │   ├── LoginActivity.java            # התחברות משתמש
│   │   │   │   ├── RegisterActivity.java         # הרשמת משתמש
│   │   │   │   ├── StudentHomeActivity.java      # לוח בקרה תלמיד
│   │   │   │   ├── TeacherHomeActivity.java      # לוח בקרה מורה
│   │   │   │   ├── PhysicsSimulationActivity.java # מסך סימולציה
│   │   │   │   ├── PhysicsSimulationView.java    # תצוגת סימולציה מותאמת
│   │   │   │   ├── QuizActivity.java             # ממשק מבחן
│   │   │   │   ├── GradesActivity.java           # צפייה בציונים
│   │   │   │   ├── User.java                     # מודל נתוני משתמש
│   │   │   │   └── QuizResult.java               # מודל תוצאת מבחן
│   │   │   ├── res/
│   │   │   │   ├── layout/                       # פריסות UI
│   │   │   │   ├── values/                       # מחרוזות, צבעים, ערכות נושא
│   │   │   │   └── drawable/                     # אייקונים וגרפיקה
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                                 # בדיקות יחידה
│   │   └── androidTest/                          # בדיקות אינסטרומנטציה
│   ├── build.gradle.kts
│   └── google-services.json                      # תצורת Firebase (gitignored)
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── CLAUDE.md                                     # הנחיות לעוזר AI
└── README.md
```

## תחילת העבודה

### דרישות מוקדמות

1. **Android Studio** (מומלץ גרסה אחרונה)
2. **JDK 11** או גבוה יותר
3. **Android SDK** עם SDK מינימלי 26 (Android 8.0)
4. **חשבון Firebase** עם פרויקט מוגדר

### הגדרת Firebase

1. **יצירת פרויקט Firebase**:
   - עברו ל-[Firebase Console](https://console.firebase.google.com)
   - צרו פרויקט חדש או השתמשו בפרויקט קיים "phiz42"

2. **הפעלת שירותי Firebase**:
   - **Authentication**: הפעילו שיטת התחברות אימייל/סיסמה
   - **Cloud Firestore**: צרו מסד נתונים במצב בדיקה
   - **Cloud Firestore API**: ודאו שהוא מופעל ב-Google Cloud Console

3. **הגדרת חוקי אבטחה**:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

4. **הורדת תצורה**:
   - הורידו את `google-services.json` מ-Firebase Console
   - מקמו אותו בתיקיית `app/`

### בניית הפרויקט

1. **שכפול המאגר**:
   ```bash
   git clone <repository-url>
   cd phiz
   ```

2. **הוספת תצורת Firebase**:
   - קבלו את `google-services.json` מפרויקט Firebase שלכם
   - מקמו בתיקיית `app/`

3. **בנייה**:
   ```bash
   ./gradlew build
   ```

4. **הרצה על מכשיר/אמולטור**:
   ```bash
   ./gradlew installDebug
   ```
   או השתמשו בכפתור Run ב-Android Studio

### הרצת בדיקות

```bash
# בדיקות יחידה
./gradlew test

# בדיקות אינסטרומנטציה
./gradlew connectedAndroidTest
```

## תפקידי משתמש

האפליקציה תומכת בשני תפקידי משתמש:

### תפקיד תלמיד
- גישה לסימולציות פיזיקליות
- ביצוע מבחנים
- צפייה בציונים אישיים
- מעקב אחר התקדמות

### תפקיד מורה
- צפייה בכל התלמידים
- מעקב אחר ציוני תלמידים
- גישה לדוחות ציונים מקיפים

הקצאת תפקיד מתבצעת במהלך ההרשמה ונשמרת ב-Firestore.

## מודלים של נתונים

### משתמש (User)
```java
{
  "uid": String,          // Firebase Auth UID
  "email": String,        // אימייל משתמש
  "name": String,         // שם מלא
  "role": String,         // "student" או "teacher"
  "totalScore": int       // ציון מצטבר במבחנים
}
```

### תוצאת מבחן (QuizResult)
```java
{
  "userId": String,       // User UID
  "quizTitle": String,    // שם מבחן
  "score": int,           // נקודות שהושגו
  "totalQuestions": int,  // סה"כ שאלות
  "timestamp": Date       // זמן השלמה
}
```

## ארכיטקטורה

### זרימת האפליקציה

1. **הפעלת אפליקציה**: `MainActivity` → הפניה ל-`LoginActivity`
2. **אימות**: משתמש מתחבר או נרשם
3. **ניתוב מבוסס תפקיד**:
   - תלמיד → `StudentHomeActivity`
   - מורה → `TeacherHomeActivity`
4. **גישה לתכונות**: מבוסס על הרשאות תפקיד

### אינטגרציית Firebase

- **PhizApplication**: מחלקת Application מותאמת אישית שמאתחלת את Firebase בהפעלה
- **אימות**: מנוהל באמצעות מופע FirebaseAuth
- **אחסון נתונים**: Firestore לנתוני משתמשים קבועים ותוצאות מבחנים
- **עדכונים בזמן אמת**: מאזינים של Firestore לעדכוני נתונים חיים

### דפוסי עיצוב מרכזיים

- **ניווט מבוסס Activity**: כל מסך הוא Activity נפרד
- **בקרת גישה מבוססת תפקיד**: נתיבים ותכונות נקבעים לפי תפקיד משתמש
- **פעולות אסינכרוניות**: כל פעולות Firebase משתמשות ב-callbacks/listeners
- **Material Design**: UI עקבי באמצעות Material Components

## בעיות נפוצות ופתרונות

### שגיאת PERMISSION_DENIED
**בעיה**: פעולות Firestore נכשלות עם permission denied
**פתרון**:
- ודאו שה-Cloud Firestore API מופעל ב-Google Cloud Console
- בדקו שחוקי האבטחה של Firestore מוגדרים כראוי
- ודאו שהמשתמש מאומת לפני גישה ל-Firestore

### שגיאת CONFIGURATION_NOT_FOUND
**בעיה**: אתחול Firebase נכשל
**פתרון**:
- ודאו ש-`google-services.json` נמצא בתיקיית `app/`
- בדקו ש-`PhizApplication` מוצהר ב-AndroidManifest.xml
- ודאו שאתחול Firebase מתרחש לפני כל קריאות Firebase

### התחברות משתמש נכשלת (משתמש קיים ב-Auth אך לא ב-Firestore)
**בעיה**: משתמש יכול להתאמת אבל האפליקציה קורסת בבדיקת תפקיד
**פתרון**:
- במהלך הרשמה, ודאו שגם משתמש Firebase Auth וגם מסמך Firestore נוצרים
- זרימת ההרשמה חייבת להשלים את שני השלבים בהצלחה

## פיתוח

### סגנון קוד
- מוסכמות שמות Java
- הנחיות Material Design
- שמות Activity: `<Feature>Activity.java`
- שמות Layout: `activity_<feature>.xml`

### Logging
- השתמשו ב-Android Log עם TAGs תיאוריים
- כללו הקשר רלוונטי בהודעות שגיאה
- רשמו מעברי מצב חשובים

### רכיבי UI
- TextInputLayout עם TextInputEditText עבור קלטי טופס
- Material Buttons עבור פעולות
- ProgressBar עבור מצבי טעינה
- RecyclerView עבור רשימות

## תרומה

1. עשו Fork למאגר
2. צרו ענף תכונה (feature branch)
3. בצעו את השינויים שלכם
4. בדקו ביסודיות
5. שלחו pull request

## רישיון

[הוסיפו את הרישיון שלכם כאן]

## יצירת קשר

[הוסיפו מידע ליצירת קשר]

## תודות

- Firebase עבור שירותי backend
- Material Design עבור רכיבי UI
- קהילת מפתחי Android
