package com.daille.zonadepescajava_app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class TideParticlesView extends View {
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final int BASE_COLOR = 0xB3E5FC;
    private static final int BASE_ALPHA = 170;
    private static final int WAVE_BAND_COUNT = 4;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Direction> pendingDirections = new ArrayList<>();

    private ValueAnimator animator;
    private Direction activeDirection;
    private Runnable completionCallback;
    private float currentProgress;

    public TideParticlesView(Context context) {
        super(context);
        init();
    }

    public TideParticlesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TideParticlesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(BASE_COLOR);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void playSequence(List<Direction> directions, Runnable onComplete) {
        if (directions == null || directions.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (onComplete != null) {
            if (completionCallback == null) {
                completionCallback = onComplete;
            } else {
                Runnable previous = completionCallback;
                completionCallback = () -> {
                    previous.run();
                    onComplete.run();
                };
            }
        }
        pendingDirections.addAll(directions);
        if (!isRunning()) {
            startNextAnimation();
        }
    }

    private void startNextAnimation() {
        if (pendingDirections.isEmpty()) {
            if (completionCallback != null) {
                Runnable callback = completionCallback;
                completionCallback = null;
                callback.run();
            }
            return;
        }
        activeDirection = pendingDirections.remove(0);
        startAnimation();
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        currentProgress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000L);
        animator.setInterpolator(new DecelerateInterpolator(1.2f));
        animator.addUpdateListener(animation -> {
            currentProgress = (float) animation.getAnimatedFraction();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentProgress = 0f;
                invalidate();
                startNextAnimation();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (activeDirection == null || currentProgress <= 0f) {
            return;
        }
        drawWaveBands(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        pendingDirections.clear();
        completionCallback = null;
    }

    private void drawWaveBands(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float amplitude = dpToPx(10f);
        float wavelength = dpToPx(110f);
        float stroke = dpToPx(3.2f);
        float spacing = dpToPx(26f);
        float travelPadding = dpToPx(40f);

        paint.setStrokeWidth(stroke);

        for (int i = 0; i < WAVE_BAND_COUNT; i++) {
            float bandOffset = i * spacing;
            float bandProgress = currentProgress - (bandOffset / (Math.max(width, height) + travelPadding));
            if (bandProgress < 0f) {
                continue;
            }
            float alphaScale = 1f - (i / (float) WAVE_BAND_COUNT);
            int alpha = (int) (BASE_ALPHA * alphaScale);
            paint.setColor((alpha << 24) | BASE_COLOR);
            float phase = currentProgress * (float) (Math.PI * 2f) + (i * 0.6f);
            if (activeDirection == Direction.LEFT || activeDirection == Direction.RIGHT) {
                float startX = activeDirection == Direction.RIGHT ? -travelPadding : width + travelPadding;
                float endX = activeDirection == Direction.RIGHT ? width + travelPadding : -travelPadding;
                float waveX = lerp(startX, endX, bandProgress);
                drawVerticalWave(canvas, waveX, height, amplitude, wavelength, phase);
            } else {
                float startY = activeDirection == Direction.DOWN ? -travelPadding : height + travelPadding;
                float endY = activeDirection == Direction.DOWN ? height + travelPadding : -travelPadding;
                float waveY = lerp(startY, endY, bandProgress);
                drawHorizontalWave(canvas, waveY, width, amplitude, wavelength, phase);
            }
        }
    }

    private void drawVerticalWave(Canvas canvas, float baseX, float height, float amplitude, float wavelength, float phase) {
        Path path = new Path();
        float step = dpToPx(12f);
        float startY = -dpToPx(30f);
        float endY = height + dpToPx(30f);
        for (float y = startY; y <= endY; y += step) {
            float offset = (float) Math.sin((y / wavelength) * Math.PI * 2f + phase) * amplitude;
            float x = baseX + offset;
            if (y == startY) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paint);
    }

    private void drawHorizontalWave(Canvas canvas, float baseY, float width, float amplitude, float wavelength, float phase) {
        Path path = new Path();
        float step = dpToPx(12f);
        float startX = -dpToPx(30f);
        float endX = width + dpToPx(30f);
        for (float x = startX; x <= endX; x += step) {
            float offset = (float) Math.sin((x / wavelength) * Math.PI * 2f + phase) * amplitude;
            float y = baseY + offset;
            if (x == startX) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, paint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private boolean isRunning() {
        return animator != null && animator.isRunning();
    }
}
