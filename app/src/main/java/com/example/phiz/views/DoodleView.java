package com.example.phiz.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class DoodleView extends SurfaceView implements SurfaceHolder.Callback {
    private Paint paint;
    private List<Path> paths;
    private List<Paint> paints;
    private Path currentPath;
    private boolean isDrawingEnabled = false;
    private SurfaceHolder surfaceHolder;

    public DoodleView(Context context) {
        super(context);
        init();
    }

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DoodleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);

        paths = new ArrayList<>();
        paints = new ArrayList<>();
    }

    public void setDrawingEnabled(boolean enabled) {
        isDrawingEnabled = enabled;
    }

    public boolean isDrawingEnabled() {
        return isDrawingEnabled;
    }

    public void clearDoodle() {
        paths.clear();
        paints.clear();
        drawCanvas();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawingEnabled) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);

                // Create a new paint with current settings
                Paint pathPaint = new Paint(paint);
                paths.add(currentPath);
                paints.add(pathPaint);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                    drawCanvas();
                }
                return true;

            case MotionEvent.ACTION_UP:
                currentPath = null;
                return true;
        }

        return false;
    }

    private void drawCanvas() {
        if (surfaceHolder == null || !surfaceHolder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                // Clear the canvas with transparent
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                // Draw all paths
                for (int i = 0; i < paths.size(); i++) {
                    canvas.drawPath(paths.get(i), paints.get(i));
                }
            }
        } catch (Exception e) {
            // Ignore drawing errors
        } finally {
            if (canvas != null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    // Ignore unlock errors
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawCanvas();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawCanvas();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Clean up if needed
    }
}
