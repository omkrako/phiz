# Physics Simulator (Phiz)

An Android educational application for teaching Newton's Laws of Motion through interactive simulations and quizzes.

[עברית](README.he.md) | English

## Overview

Physics Simulator is a mobile learning platform designed for physics education. The app provides:
- Interactive physics simulations demonstrating Newton's Laws
- Quiz system for testing knowledge
- Grade tracking and progress monitoring
- Role-based access for students and teachers

## Features

### For Students
- **Interactive Simulations**: Visualize Newton's Laws of Motion with real-time physics simulations
- **Quiz System**: Test your understanding with interactive quizzes
- **Grade Tracking**: Monitor your progress and quiz results
- **User-Friendly Interface**: Clean, Material Design-based UI with intuitive navigation

### For Teachers
- **Student Management**: View all registered students
- **Grade Monitoring**: Track student performance across quizzes
- **Progress Overview**: See comprehensive grade reports

## Technical Stack

- **Language**: Java
- **Platform**: Android (SDK 26+)
- **Architecture**: Activity-based navigation with role-based routing
- **Backend**: Firebase
  - Firebase Authentication (Email/Password)
  - Cloud Firestore (User data, quiz results)
  - Firebase Analytics
- **UI Framework**: Material Components for Android
- **Build System**: Gradle with Kotlin DSL

## Project Structure

```
phiz/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/phiz/
│   │   │   │   ├── PhizApplication.java          # App initialization
│   │   │   │   ├── MainActivity.java             # Entry point
│   │   │   │   ├── LoginActivity.java            # User login
│   │   │   │   ├── RegisterActivity.java         # User registration
│   │   │   │   ├── StudentHomeActivity.java      # Student dashboard
│   │   │   │   ├── TeacherHomeActivity.java      # Teacher dashboard
│   │   │   │   ├── PhysicsSimulationActivity.java # Simulation screen
│   │   │   │   ├── PhysicsSimulationView.java    # Custom simulation view
│   │   │   │   ├── QuizActivity.java             # Quiz interface
│   │   │   │   ├── GradesActivity.java           # Grade viewing
│   │   │   │   ├── User.java                     # User data model
│   │   │   │   └── QuizResult.java               # Quiz result model
│   │   │   ├── res/
│   │   │   │   ├── layout/                       # UI layouts
│   │   │   │   ├── values/                       # Strings, colors, themes
│   │   │   │   └── drawable/                     # Icons and graphics
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                                 # Unit tests
│   │   └── androidTest/                          # Instrumentation tests
│   ├── build.gradle.kts
│   └── google-services.json                      # Firebase config (gitignored)
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── CLAUDE.md                                     # AI assistant guidance
└── README.md
```

## Getting Started

### Prerequisites

1. **Android Studio** (latest version recommended)
2. **JDK 11** or higher
3. **Android SDK** with minimum SDK 26 (Android 8.0)
4. **Firebase Account** with a configured project

### Firebase Setup

1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project or use existing "phiz42"

2. **Enable Firebase Services**:
   - **Authentication**: Enable Email/Password sign-in method
   - **Cloud Firestore**: Create database in test mode
   - **Cloud Firestore API**: Ensure it's enabled in Google Cloud Console

3. **Configure Security Rules**:
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

4. **Download Configuration**:
   - Download `google-services.json` from Firebase Console
   - Place it in `app/` directory

### Building the Project

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd phiz
   ```

2. **Add Firebase Configuration**:
   - Obtain `google-services.json` from your Firebase project
   - Place in `app/` directory

3. **Build**:
   ```bash
   ./gradlew build
   ```

4. **Run on Device/Emulator**:
   ```bash
   ./gradlew installDebug
   ```
   Or use Android Studio's Run button

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

## User Roles

The application supports two user roles:

### Student Role
- Access to physics simulations
- Take quizzes
- View personal grades
- Track progress

### Teacher Role
- View all students
- Monitor student grades
- Access comprehensive grade reports

Role assignment happens during registration and is stored in Firestore.

## Data Models

### User
```java
{
  "uid": String,          // Firebase Auth UID
  "email": String,        // User email
  "name": String,         // Full name
  "role": String,         // "student" or "teacher"
  "totalScore": int       // Cumulative quiz score
}
```

### QuizResult
```java
{
  "userId": String,       // User UID
  "quizTitle": String,    // Quiz name
  "score": int,           // Points earned
  "totalQuestions": int,  // Total questions
  "timestamp": Date       // Completion time
}
```

## Architecture

### Application Flow

1. **App Launch**: `MainActivity` → redirects to `LoginActivity`
2. **Authentication**: User logs in or registers
3. **Role-Based Routing**:
   - Student → `StudentHomeActivity`
   - Teacher → `TeacherHomeActivity`
4. **Feature Access**: Based on role permissions

### Firebase Integration

- **PhizApplication**: Custom Application class that initializes Firebase on startup
- **Authentication**: Managed via FirebaseAuth instance
- **Data Storage**: Firestore for persistent user data and quiz results
- **Real-time Updates**: Firestore listeners for live data updates

### Key Design Patterns

- **Activity-based Navigation**: Each screen is a separate Activity
- **Role-based Access Control**: Routes and features determined by user role
- **Async Operations**: All Firebase operations use callbacks/listeners
- **Material Design**: Consistent UI using Material Components

## Common Issues & Solutions

### PERMISSION_DENIED Error
**Problem**: Firestore operations fail with permission denied
**Solution**:
- Verify Cloud Firestore API is enabled in Google Cloud Console
- Check Firestore security rules are properly configured
- Ensure user is authenticated before accessing Firestore

### CONFIGURATION_NOT_FOUND Error
**Problem**: Firebase initialization fails
**Solution**:
- Verify `google-services.json` is in `app/` directory
- Check `PhizApplication` is declared in AndroidManifest.xml
- Ensure Firebase initialization happens before any Firebase calls

### User Login Fails (User exists in Auth but not Firestore)
**Problem**: User can authenticate but app crashes when checking role
**Solution**:
- During registration, ensure both Firebase Auth user AND Firestore document are created
- The registration flow must complete both steps successfully

## Development

### Code Style
- Java naming conventions
- Material Design guidelines
- Activity naming: `<Feature>Activity.java`
- Layout naming: `activity_<feature>.xml`

### Logging
- Use Android Log with descriptive TAGs
- Include relevant context in error messages
- Log important state transitions

### UI Components
- TextInputLayout with TextInputEditText for form inputs
- Material Buttons for actions
- ProgressBar for loading states
- RecyclerView for lists

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

[Add your license here]

## Contact

[Add contact information]

## Acknowledgments

- Firebase for backend services
- Material Design for UI components
- Android developer community
