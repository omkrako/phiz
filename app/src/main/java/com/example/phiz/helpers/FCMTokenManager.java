package com.example.phiz.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for FCM token lifecycle.
 * Handles token retrieval, storage, and cleanup.
 */
public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static final String PREFS_NAME = "fcm_prefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private static FCMTokenManager instance;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public static synchronized FCMTokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new FCMTokenManager(context);
        }
        return instance;
    }

    /**
     * Get the current FCM token and save it to Firestore.
     * Call this after successful login.
     */
    public void initializeToken() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Cannot initialize token: no authenticated user");
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Failed to get FCM token", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM token retrieved: " + token);

                    // Save token locally
                    saveTokenLocally(token);

                    // Save token to Firestore
                    saveTokenToFirestore(token);
                });
    }

    /**
     * Save FCM token to Firestore for the current user
     */
    public void saveTokenToFirestore(String token) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Cannot save token: no authenticated user");
            return;
        }

        String userId = user.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        updates.put("lastTokenUpdate", FieldValue.serverTimestamp());

        db.collection(FirestoreHelper.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token saved to Firestore for user: " + userId);
                    saveTokenLocally(token);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving FCM token to Firestore", e);
                });
    }

    /**
     * Delete FCM token from Firestore on logout
     */
    public void deleteToken() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Just clear local token
            clearLocalToken();
            return;
        }

        String userId = user.getUid();

        // Delete from Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", FieldValue.delete());

        db.collection(FirestoreHelper.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token deleted from Firestore for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting FCM token from Firestore", e);
                });

        // Clear local token
        clearLocalToken();

        // Delete the FCM token instance
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FCM token deleted from device");
                    } else {
                        Log.w(TAG, "Failed to delete FCM token from device", task.getException());
                    }
                });
    }

    /**
     * Save token to SharedPreferences
     */
    private void saveTokenLocally(String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    /**
     * Get locally stored token
     */
    public String getLocalToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Clear locally stored token
     */
    private void clearLocalToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_FCM_TOKEN).apply();
    }

    /**
     * Subscribe to a topic for receiving topic-based messages
     */
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to topic: " + topic);
                    } else {
                        Log.w(TAG, "Failed to subscribe to topic: " + topic, task.getException());
                    }
                });
    }

    /**
     * Unsubscribe from a topic
     */
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Unsubscribed from topic: " + topic);
                    } else {
                        Log.w(TAG, "Failed to unsubscribe from topic: " + topic, task.getException());
                    }
                });
    }

    /**
     * Subscribe user to role-based topics after login
     */
    public void subscribeToRoleTopics(String role) {
        if ("student".equals(role)) {
            subscribeToTopic("all_students");
            subscribeToTopic("quiz_updates");
        } else if ("teacher".equals(role)) {
            subscribeToTopic("all_teachers");
            subscribeToTopic("student_activity");
        }
    }

    /**
     * Unsubscribe from all topics on logout
     */
    public void unsubscribeFromAllTopics() {
        unsubscribeFromTopic("all_students");
        unsubscribeFromTopic("all_teachers");
        unsubscribeFromTopic("quiz_updates");
        unsubscribeFromTopic("student_activity");
    }
}
