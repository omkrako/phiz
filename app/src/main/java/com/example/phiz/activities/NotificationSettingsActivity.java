package com.example.phiz.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phiz.R;
import com.example.phiz.helpers.FirestoreHelper;
import com.example.phiz.helpers.WorkerScheduler;
import com.example.phiz.models.NotificationPreferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/**
 * Activity for managing notification preferences.
 */
public class NotificationSettingsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationSettings";
    public static final String EXTRA_USER_ROLE = "user_role";

    private MaterialToolbar toolbar;
    private SwitchMaterial switchQuizNotifications;
    private SwitchMaterial switchGradeNotifications;
    private SwitchMaterial switchAchievementNotifications;
    private SwitchMaterial switchStudyReminders;
    private SwitchMaterial switchWeeklyProgress;
    private MaterialCardView reminderTimeCard;
    private TextView reminderTimeText;
    private Button saveButton;
    private ProgressBar progressBar;

    // Reminder section views (hidden for teachers)
    private View studyRemindersRow;
    private View weeklyProgressRow;
    private View dividerStudyReminders;
    private View dividerWeeklyProgress;

    private FirebaseAuth mAuth;
    private NotificationPreferences currentPreferences;
    private int selectedHour = 18;
    private int selectedMinute = 0;
    private boolean isTeacher = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        // Check if user is a teacher
        isTeacher = "teacher".equals(getIntent().getStringExtra(EXTRA_USER_ROLE));

        initializeViews();
        setupToolbar();

        // Hide reminder-related settings for teachers
        if (isTeacher) {
            hideStudentOnlySettings();
        }

        loadPreferences();

        reminderTimeCard.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> savePreferences());

        // Update reminder time card visibility based on switch state
        switchStudyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isTeacher) {
                reminderTimeCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        switchQuizNotifications = findViewById(R.id.switchQuizNotifications);
        switchGradeNotifications = findViewById(R.id.switchGradeNotifications);
        switchAchievementNotifications = findViewById(R.id.switchAchievementNotifications);
        switchStudyReminders = findViewById(R.id.switchStudyReminders);
        switchWeeklyProgress = findViewById(R.id.switchWeeklyProgress);
        reminderTimeCard = findViewById(R.id.reminderTimeCard);
        reminderTimeText = findViewById(R.id.reminderTimeText);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        studyRemindersRow = findViewById(R.id.studyRemindersRow);
        weeklyProgressRow = findViewById(R.id.weeklyProgressRow);
        dividerStudyReminders = findViewById(R.id.dividerStudyReminders);
        dividerWeeklyProgress = findViewById(R.id.dividerWeeklyProgress);
    }

    private void hideStudentOnlySettings() {
        studyRemindersRow.setVisibility(View.GONE);
        weeklyProgressRow.setVisibility(View.GONE);
        dividerStudyReminders.setVisibility(View.GONE);
        dividerWeeklyProgress.setVisibility(View.GONE);
        reminderTimeCard.setVisibility(View.GONE);

        // Force these off for teachers
        switchStudyReminders.setChecked(false);
        switchWeeklyProgress.setChecked(false);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadPreferences() {
        setLoading(true);

        String userId = mAuth.getCurrentUser().getUid();
        FirestoreHelper.getInstance().getNotificationPreferences(userId,
                prefs -> {
                    setLoading(false);
                    currentPreferences = prefs;
                    updateUI(prefs);
                },
                e -> {
                    setLoading(false);
                    // Use defaults if loading fails
                    currentPreferences = NotificationPreferences.createDefault();
                    updateUI(currentPreferences);
                    Toast.makeText(this, "Using default settings", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void updateUI(NotificationPreferences prefs) {
        switchQuizNotifications.setChecked(prefs.isQuizNotifications());
        switchGradeNotifications.setChecked(prefs.isGradeNotifications());
        switchAchievementNotifications.setChecked(prefs.isAchievementNotifications());
        switchStudyReminders.setChecked(prefs.isStudyReminders());
        switchWeeklyProgress.setChecked(prefs.isWeeklyProgress());

        selectedHour = prefs.getReminderHour();
        selectedMinute = prefs.getReminderMinute();
        updateReminderTimeText();

        // Show/hide reminder time card based on study reminders toggle (never show for teachers)
        if (!isTeacher) {
            reminderTimeCard.setVisibility(prefs.isStudyReminders() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateReminderTimeText() {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int displayHour = selectedHour > 12 ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
        String timeString = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, selectedMinute, amPm);
        reminderTimeText.setText(timeString);
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateReminderTimeText();
                },
                selectedHour,
                selectedMinute,
                false
        );
        timePicker.setTitle("Select Reminder Time");
        timePicker.show();
    }

    private void savePreferences() {
        setLoading(true);

        // Build preferences from UI state
        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setQuizNotifications(switchQuizNotifications.isChecked());
        prefs.setGradeNotifications(switchGradeNotifications.isChecked());
        prefs.setAchievementNotifications(switchAchievementNotifications.isChecked());
        prefs.setStudyReminders(switchStudyReminders.isChecked());
        prefs.setWeeklyProgress(switchWeeklyProgress.isChecked());
        prefs.setReminderTime(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

        String userId = mAuth.getCurrentUser().getUid();
        FirestoreHelper.getInstance().saveNotificationPreferences(userId, prefs,
                (success, e) -> {
                    setLoading(false);
                    if (success) {
                        // Only schedule reminder workers for students
                        if (!isTeacher) {
                            WorkerScheduler.scheduleAllWorkers(this, prefs);
                        }

                        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving settings: " +
                                (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }
}
