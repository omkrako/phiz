package com.example.phiz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class PhysicsSimulationView extends View {
    private Paint objectPaint, surfacePaint, vectorPaint, textPaint;
    private float objectX, objectY;
    private float objectSize = 80f;
    private float mass = 5.0f;
    private float appliedForce = 30.0f;
    private float frictionCoefficient = 0.2f;
    private float angle = 0.0f;
    private boolean isAnimating = false;

    private float velocity = 0.0f;
    private float acceleration = 0.0f;
    private float position = 0.0f;
    private static final float GRAVITY = 9.8f;

    private OnPhysicsUpdateListener listener;

    public interface OnPhysicsUpdateListener {
        void onUpdate(float acceleration, float velocity, float position);
    }

    public PhysicsSimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        objectPaint = new Paint();
        objectPaint.setColor(Color.parseColor("#2196F3"));
        objectPaint.setStyle(Paint.Style.FILL);

        surfacePaint = new Paint();
        surfacePaint.setColor(Color.parseColor("#795548"));
        surfacePaint.setStrokeWidth(5f);

        vectorPaint = new Paint();
        vectorPaint.setStrokeWidth(4f);
        vectorPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setTextSize(30f);
        textPaint.setAntiAlias(true);
    }

    public void setParameters(float mass, float force, float friction, float angle) {
        this.mass = mass;
        this.appliedForce = force;
        this.frictionCoefficient = friction;
        this.angle = angle;
        calculatePhysics();
        invalidate();
    }

    public void startAnimation() {
        isAnimating = true;
        velocity = 0.0f;
        position = 0.0f;
        calculatePhysics();
        post(animationRunnable);
    }

    public void stopAnimation() {
        isAnimating = false;
        removeCallbacks(animationRunnable);
    }

    public void reset() {
        stopAnimation();
        velocity = 0.0f;
        position = 0.0f;
        acceleration = 0.0f;
        objectX = 100f;
        if (listener != null) {
            listener.onUpdate(acceleration, velocity, position);
        }
        invalidate();
    }

    public void setOnPhysicsUpdateListener(OnPhysicsUpdateListener listener) {
        this.listener = listener;
    }

    private void calculatePhysics() {
        double angleRad = Math.toRadians(angle);
        float gravityComponent = (float) (mass * GRAVITY * Math.sin(angleRad));
        float normalForce = (float) (mass * GRAVITY * Math.cos(angleRad));
        float frictionForce = frictionCoefficient * normalForce;

        float netForce = appliedForce - frictionForce - gravityComponent;
        acceleration = netForce / mass;

        if (listener != null) {
            listener.onUpdate(acceleration, velocity, position);
        }
    }

    private Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAnimating) {
                float deltaTime = 0.016f; // ~60 FPS
                velocity += acceleration * deltaTime;
                position += velocity * deltaTime;

                objectX = 100f + position * 20f; // Scale for visualization

                if (objectX > getWidth() - objectSize) {
                    objectX = getWidth() - objectSize;
                    velocity = 0;
                }

                if (objectX < 100f) {
                    objectX = 100f;
                    velocity = 0;
                }

                if (listener != null) {
                    listener.onUpdate(acceleration, velocity, position);
                }

                invalidate();
                postDelayed(this, 16);
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float surfaceY = height * 0.7f;
        double angleRad = Math.toRadians(angle);

        canvas.drawLine(0, surfaceY, width, (float) (surfaceY - width * Math.tan(angleRad)),
                surfacePaint);

        if (objectX == 0) {
            objectX = 100f;
        }
        objectY = (float) (surfaceY - objectSize - (objectX * Math.tan(angleRad)));

        canvas.drawRect(objectX, objectY, objectX + objectSize, objectY + objectSize, objectPaint);

        float centerX = objectX + objectSize / 2;
        float centerY = objectY + objectSize / 2;

        float gravityForce = mass * GRAVITY;
        drawVector(canvas, centerX, centerY, 0, gravityForce * 2, Color.RED, "Fg");

        float normalForce = (float) (mass * GRAVITY * Math.cos(angleRad));
        drawVector(canvas, centerX, centerY, 0, -normalForce * 2, Color.BLUE, "Fn");

        if (appliedForce > 0) {
            float forceX = (float) (appliedForce * Math.cos(angleRad)) * 3;
            drawVector(canvas, centerX, centerY, forceX, 0, Color.GREEN, "Fa");
        }

        if (frictionCoefficient > 0 && velocity != 0) {
            float frictionForce = frictionCoefficient * normalForce;
            float frictionX = -(frictionForce * 3);
            drawVector(canvas, centerX, centerY, frictionX, 0, Color.YELLOW, "Ff");
        }
    }

    private void drawVector(Canvas canvas, float startX, float startY, float dx, float dy,
                            int color, String label) {
        vectorPaint.setColor(color);
        textPaint.setColor(color);

        float endX = startX + dx;
        float endY = startY + dy;

        canvas.drawLine(startX, startY, endX, endY, vectorPaint);

        double angle = Math.atan2(dy, dx);
        float arrowSize = 20f;
        float arrowX1 = (float) (endX - arrowSize * Math.cos(angle - Math.PI / 6));
        float arrowY1 = (float) (endY - arrowSize * Math.sin(angle - Math.PI / 6));
        float arrowX2 = (float) (endX - arrowSize * Math.cos(angle + Math.PI / 6));
        float arrowY2 = (float) (endY - arrowSize * Math.sin(angle + Math.PI / 6));

        Path arrowPath = new Path();
        arrowPath.moveTo(endX, endY);
        arrowPath.lineTo(arrowX1, arrowY1);
        arrowPath.moveTo(endX, endY);
        arrowPath.lineTo(arrowX2, arrowY2);
        canvas.drawPath(arrowPath, vectorPaint);

        canvas.drawText(label, endX + 10, endY - 10, textPaint);
    }
}