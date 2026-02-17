package com.example.phiz.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phiz.R;
import com.example.phiz.views.PhysicsSimulationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class PhysicsSimulationActivity extends AppCompatActivity {
    private PhysicsSimulationView simulationView;
    private SeekBar massSeekBar, forceSeekBar, frictionSeekBar, angleSeekBar;
    private TextView massValueTextView, forceValueTextView, frictionValueTextView,
            angleValueTextView, accelerationTextView, velocityTextView, positionTextView,
            lawDescriptionTextView;
    private Button startButton, resetButton;
    private MaterialCardView backButton;

    private float mass = 5.0f;
    private float appliedForce = 30.0f;
    private float frictionCoefficient = 0.2f;
    private float angle = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physics_simulation);

        simulationView = findViewById(R.id.simulationView);
        massSeekBar = findViewById(R.id.massSeekBar);
        forceSeekBar = findViewById(R.id.forceSeekBar);
        frictionSeekBar = findViewById(R.id.frictionSeekBar);
        angleSeekBar = findViewById(R.id.angleSeekBar);
        massValueTextView = findViewById(R.id.massValueTextView);
        forceValueTextView = findViewById(R.id.forceValueTextView);
        frictionValueTextView = findViewById(R.id.frictionValueTextView);
        angleValueTextView = findViewById(R.id.angleValueTextView);
        accelerationTextView = findViewById(R.id.accelerationTextView);
        velocityTextView = findViewById(R.id.velocityTextView);
        positionTextView = findViewById(R.id.positionTextView);
        lawDescriptionTextView = findViewById(R.id.lawDescriptionTextView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        backButton = findViewById(R.id.backButton);

        updateLawDescription();

        simulationView.setOnPhysicsUpdateListener((acceleration, velocity, position) -> {
            runOnUiThread(() -> {
                accelerationTextView.setText(String.format(Locale.US, "%.2f m/s²", acceleration));
                velocityTextView.setText(String.format(Locale.US, "%.2f m/s", velocity));
                positionTextView.setText(String.format(Locale.US, "%.2f m", position));
            });
        });

        massSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mass = (progress + 1) / 10.0f;
                massValueTextView.setText(String.format(Locale.US, "%.1f kg", mass));
                simulationView.setParameters(mass, appliedForce, frictionCoefficient, angle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        forceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                appliedForce = progress;
                forceValueTextView.setText(String.format(Locale.US, "%.1f N", appliedForce));
                simulationView.setParameters(mass, appliedForce, frictionCoefficient, angle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        frictionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frictionCoefficient = progress / 100.0f;
                frictionValueTextView.setText(String.format(Locale.US, "%.2f", frictionCoefficient));
                simulationView.setParameters(mass, appliedForce, frictionCoefficient, angle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle = progress;
                angleValueTextView.setText(String.format(Locale.US, "%d°", (int) angle));
                simulationView.setParameters(mass, appliedForce, frictionCoefficient, angle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startButton.setOnClickListener(v -> {
            simulationView.startAnimation();
        });

        resetButton.setOnClickListener(v -> {
            simulationView.reset();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        simulationView.setParameters(mass, appliedForce, frictionCoefficient, angle);
    }

    private void updateLawDescription() {
        String description = "Newton's Laws:\n\n" +
                "1st Law: An object remains at rest or in uniform motion unless acted upon by a force.\n\n" +
                "2nd Law: F = ma (Force equals mass times acceleration)\n\n" +
                "3rd Law: For every action, there is an equal and opposite reaction.\n\n" +
                "Adjust the parameters to see how forces affect motion!";
        lawDescriptionTextView.setText(description);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simulationView.stopAnimation();
    }
}
