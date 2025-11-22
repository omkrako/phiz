# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Physics Simulator Android app (phiz) - An educational application for teaching Newton's Laws of Motion through interactive simulations and quizzes. Built with Java and Firebase.

## Build Commands

```bash
# Build the project (Android Studio)
./gradlew build

# Clean build
./gradlew clean build

# Install on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

**Note:** On Windows, use `gradlew.bat` instead of `./gradlew`. JAVA_HOME must be set in your environment.

## Architecture

### Firebase Integration
- **PhizApplication** (custom Application class): Initializes Firebase via `FirebaseApp.initializeApp()` on app startup. This is critical - Firebase MUST be initialized before any Firebase services are used.
- **Authentication**: Firebase Auth with email/password
- **Database**: Cloud Firestore for user data and quiz results
- **Configuration**: `google-services.json` (gitignored) must be present in `app/` directory

### User Flow & Role-Based Navigation
1. **MainActivity**: Entry point that immediately redirects to LoginActivity
2. **LoginActivity/RegisterActivity**: Authentication screens
3. **Role-based routing**: After authentication, users are routed based on their Firestore `role` field:
   - `role: "student"` → StudentHomeActivity
   - `role: "teacher"` → TeacherHomeActivity

### Data Model
- **User** model stored in Firestore `users` collection with document ID = Firebase Auth UID
- Required fields: `uid`, `email`, `name`, `role`, `totalScore`
- User documents must be created in Firestore during registration (not just Firebase Auth)

### Activity Structure
- **StudentHomeActivity**: Access to physics simulations, quizzes, and grades
- **TeacherHomeActivity**: View all students and their grades
- **PhysicsSimulationActivity**: Interactive Newton's Laws simulations using custom PhysicsSimulationView
- **QuizActivity**: Quiz interface with result storage
- **GradesActivity**: Display user quiz results

### Key Implementation Details

**Firestore Security Rules Required:**
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

**Registration Flow:**
1. Create Firebase Auth user via `createUserWithEmailAndPassword()`
2. Save User object to Firestore `users/{uid}` collection
3. Both steps must succeed for complete registration
4. Use logging with TAG for debugging (already implemented in RegisterActivity)

**Common Issues:**
- If Firestore operations fail with PERMISSION_DENIED, check that Cloud Firestore API is enabled in Google Cloud Console
- If CONFIGURATION_NOT_FOUND error occurs, verify PhizApplication is set in AndroidManifest.xml and FirebaseApp.initializeApp() is called
- Login will fail if user exists in Auth but not in Firestore - always create both

### UI Patterns
- Material Components used throughout (TextInputLayout, Material buttons)
- Password fields use `app:endIconMode="password_toggle"` for visibility toggle
- ProgressBar visibility controlled during async operations
- All async Firebase operations use `.addOnCompleteListener()` or `.addOnSuccessListener()/.addOnFailureListener()` patterns

## Development Notes

### Firebase Setup for New Environments
1. Obtain `google-services.json` from Firebase Console for project "phiz42"
2. Place in `app/` directory
3. Ensure Cloud Firestore API is enabled
4. Configure Firestore security rules as shown above
5. Enable Email/Password authentication in Firebase Console

### Testing User Roles
Create test users with different roles in Firestore to test role-based navigation. The `role` field determines which home activity is shown after login.
