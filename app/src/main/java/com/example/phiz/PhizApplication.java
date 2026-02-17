package com.example.phiz;

import android.app.Application;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.phiz.helpers.NotificationHelper;
import com.google.firebase.FirebaseApp;

public class PhizApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Create notification channels
        NotificationHelper.getInstance(this).createNotificationChannels();

        // Initialize WorkManager with default configuration
        if (!isWorkManagerInitialized()) {
            WorkManager.initialize(this, new Configuration.Builder().build());
        }
    }

    private boolean isWorkManagerInitialized() {
        try {
            WorkManager.getInstance(this);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
