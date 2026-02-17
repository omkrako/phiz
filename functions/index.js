/**
 * Cloud Functions for Phiz Push Notifications
 *
 * These functions handle server-triggered notifications for:
 * - New question created (notify all students)
 * - Quiz completed (notify teachers)
 * - Low score alerts (notify teachers)
 * - Weekly digest (scheduled)
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

// Initialize Firebase Admin
initializeApp();

const db = getFirestore();
const messaging = getMessaging();

/**
 * Triggered when a new question is created.
 * Sends a notification to all students via the "all_students" topic.
 */
exports.onQuestionCreated = onDocumentCreated("questions/{questionId}", async (event) => {
  const questionData = event.data.data();
  const questionId = event.params.questionId;

  console.log(`New question created: ${questionId}`);

  // Build the notification message
  const message = {
    topic: "all_students",
    notification: {
      title: "New Question Available!",
      body: `A new ${questionData.difficulty || "physics"} question has been added. Test your knowledge!`,
    },
    data: {
      type: "new_content",
      content_type: "question",
      question_id: questionId,
    },
    android: {
      notification: {
        channelId: "quiz_notifications",
        priority: "high",
        clickAction: "OPEN_QUIZ",
      },
    },
  };

  try {
    const response = await messaging.send(message);
    console.log("Successfully sent notification to all_students topic:", response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error("Error sending notification:", error);
    return { success: false, error: error.message };
  }
});

/**
 * Triggered when a new grade is created in a user's grades subcollection.
 * Notifies teachers when a student completes a quiz.
 */
exports.onGradeCreated = onDocumentCreated("users/{userId}/grades/{gradeId}", async (event) => {
  const gradeData = event.data.data();
  const userId = event.params.userId;

  console.log(`New grade created for user: ${userId}`);

  // Get user info for the notification
  const userDoc = await db.collection("users").doc(userId).get();
  if (!userDoc.exists) {
    console.log("User not found:", userId);
    return null;
  }

  const userData = userDoc.data();

  // Only notify for students
  if (userData.role !== "student") {
    return null;
  }

  const studentName = userData.name || "A student";
  const score = gradeData.score || 0;
  const totalQuestions = gradeData.totalQuestions || 5;
  const percentage = Math.round((score / (totalQuestions * 20)) * 100);

  // Build notification for teachers
  const message = {
    topic: "all_teachers",
    notification: {
      title: "Quiz Completed",
      body: `${studentName} completed a quiz with ${score} points (${percentage}%)`,
    },
    data: {
      type: "quiz_completed",
      student_id: userId,
      student_name: studentName,
      score: String(score),
      percentage: String(percentage),
    },
    android: {
      notification: {
        channelId: "teacher_notifications",
        priority: "high",
      },
    },
  };

  try {
    const response = await messaging.send(message);
    console.log("Successfully notified teachers:", response);

    // Check for low score alert (below 50%)
    if (percentage < 50) {
      await sendLowScoreAlert(studentName, score, percentage);
    }

    return { success: true, messageId: response };
  } catch (error) {
    console.error("Error sending notification:", error);
    return { success: false, error: error.message };
  }
});

/**
 * Send a low score alert to teachers
 */
async function sendLowScoreAlert(studentName, score, percentage) {
  const message = {
    topic: "all_teachers",
    notification: {
      title: "Low Score Alert",
      body: `${studentName} scored ${percentage}% on their quiz. They may need additional help.`,
    },
    data: {
      type: "low_score_alert",
      student_name: studentName,
      score: String(score),
      percentage: String(percentage),
    },
    android: {
      notification: {
        channelId: "teacher_notifications",
        priority: "high",
      },
    },
  };

  try {
    const response = await messaging.send(message);
    console.log("Low score alert sent:", response);
    return response;
  } catch (error) {
    console.error("Error sending low score alert:", error);
    throw error;
  }
}

/**
 * Weekly digest for teachers - runs every Sunday at 10:00 AM
 * Summarizes student activity for the week
 */
exports.weeklyTeacherDigest = onSchedule("every sunday 10:00", async (event) => {
  console.log("Running weekly teacher digest...");

  // Get all students
  const studentsSnapshot = await db.collection("users")
    .where("role", "==", "student")
    .get();

  if (studentsSnapshot.empty) {
    console.log("No students found");
    return null;
  }

  // Calculate weekly stats
  const oneWeekAgo = new Date();
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

  let totalQuizzes = 0;
  let totalScore = 0;
  let activeStudents = 0;

  for (const studentDoc of studentsSnapshot.docs) {
    const gradesSnapshot = await db.collection("users")
      .doc(studentDoc.id)
      .collection("grades")
      .where("timestamp", ">=", oneWeekAgo)
      .get();

    if (!gradesSnapshot.empty) {
      activeStudents++;
      totalQuizzes += gradesSnapshot.size;
      gradesSnapshot.forEach((gradeDoc) => {
        totalScore += gradeDoc.data().score || 0;
      });
    }
  }

  const avgScore = totalQuizzes > 0 ? Math.round(totalScore / totalQuizzes) : 0;

  // Send digest to teachers
  const message = {
    topic: "all_teachers",
    notification: {
      title: "Weekly Class Summary",
      body: `${activeStudents} students completed ${totalQuizzes} quizzes. Average score: ${avgScore} points.`,
    },
    data: {
      type: "weekly_digest",
      active_students: String(activeStudents),
      total_quizzes: String(totalQuizzes),
      average_score: String(avgScore),
    },
    android: {
      notification: {
        channelId: "teacher_notifications",
        priority: "default",
      },
    },
  };

  try {
    const response = await messaging.send(message);
    console.log("Weekly digest sent:", response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error("Error sending weekly digest:", error);
    return { success: false, error: error.message };
  }
});

/**
 * Inactivity reminder - runs daily at 6 PM
 * Checks for students who haven't used the app in 3+ days
 */
exports.inactivityReminder = onSchedule("every day 18:00", async (event) => {
  console.log("Running inactivity check...");

  const threeDaysAgo = new Date();
  threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);

  // Find inactive students
  const inactiveSnapshot = await db.collection("users")
    .where("role", "==", "student")
    .where("lastActivityAt", "<", threeDaysAgo)
    .get();

  console.log(`Found ${inactiveSnapshot.size} inactive students`);

  // Send personalized reminders to each inactive student
  const promises = inactiveSnapshot.docs.map(async (studentDoc) => {
    const studentData = studentDoc.data();
    const token = studentData.fcmToken;

    if (!token) {
      console.log(`No FCM token for user: ${studentDoc.id}`);
      return null;
    }

    // Calculate days since last activity
    const lastActivity = studentData.lastActivityAt ? studentData.lastActivityAt.toDate() : new Date(0);
    const daysSince = Math.floor((Date.now() - lastActivity.getTime()) / (1000 * 60 * 60 * 24));

    const message = {
      token: token,
      notification: {
        title: "We Miss You!",
        body: `It's been ${daysSince} days since your last visit. Come back and keep learning physics!`,
      },
      data: {
        type: "inactivity_reminder",
        days_inactive: String(daysSince),
      },
      android: {
        notification: {
          channelId: "reminder_notifications",
          priority: "default",
        },
      },
    };

    try {
      const response = await messaging.send(message);
      console.log(`Reminder sent to ${studentDoc.id}:`, response);
      return response;
    } catch (error) {
      console.error(`Error sending reminder to ${studentDoc.id}:`, error);
      return null;
    }
  });

  const results = await Promise.all(promises);
  const successCount = results.filter((r) => r !== null).length;

  console.log(`Sent ${successCount} inactivity reminders`);
  return { success: true, sentCount: successCount };
});

/**
 * Send notification to a specific user by their FCM token
 * This can be called from client-side via HTTPS callable function
 */
exports.sendNotificationToUser = require("firebase-functions/v2/https").onCall(async (request) => {
  const { userId, title, body, data } = request.data;

  if (!userId || !title || !body) {
    throw new Error("Missing required parameters: userId, title, body");
  }

  // Get user's FCM token
  const userDoc = await db.collection("users").doc(userId).get();
  if (!userDoc.exists) {
    throw new Error("User not found");
  }

  const token = userDoc.data().fcmToken;
  if (!token) {
    throw new Error("User has no FCM token");
  }

  const message = {
    token: token,
    notification: {
      title: title,
      body: body,
    },
    data: data || {},
    android: {
      notification: {
        channelId: data?.channelId || "quiz_notifications",
        priority: "high",
      },
    },
  };

  try {
    const response = await messaging.send(message);
    return { success: true, messageId: response };
  } catch (error) {
    console.error("Error sending notification:", error);
    throw new Error(`Failed to send notification: ${error.message}`);
  }
});
