package com.daille.zonadepescajava_app.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class StarBurstView extends View {
    private static final int COLOR_STAR = 0xFFFFF2A6;
    private static final long BURST_DURATION_MS = 700L;

    private final List<StarParticle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final Path starPath = new Path();
    private final float baseRadius;
    private final float coreRadius;

    @Nullable
    private ValueAnimator animator;

    public StarBurstView(Context context) {
        this(context, null);
    }

    public StarBurstView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarBurstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(COLOR_STAR);
        paint.setStyle(Paint.Style.FILL);
        baseRadius = dpToPx(5f);
        coreRadius = dpToPx(2.2f);
    }

    public void burst(float centerX, float centerY, int count) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            particles.add(createParticle(centerX, centerY));
        }
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) return;

        Iterator<StarParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            StarParticle particle = iterator.next();
            if (particle.progress >= 1f) {
                iterator.remove();
                continue;
            }
            float alpha = (1f - particle.progress);
            paint.setAlpha((int) (alpha * 255));

            float x = particle.startX + particle.velocityX * particle.progress;
            float y = particle.startY + particle.velocityY * particle.progress;
            float radius = particle.radius * (1f - particle.progress * 0.35f);
            float rotation = particle.rotation + 240f * particle.progress;
            drawStar(canvas, x, y, radius, rotation);
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, float rotation) {
        starPath.reset();
        float angleStep = (float) (Math.PI / 5.0);
        float angle = (float) Math.toRadians(rotation - 90f);
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? radius : coreRadius;
            float x = (float) (cx + Math.cos(angle) * r);
            float y = (float) (cy + Math.sin(angle) * r);
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
            angle += angleStep;
        }
        starPath.close();
        canvas.drawPath(starPath, paint);
    }

    private void startAnimator() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(BURST_DURATION_MS);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                for (StarParticle particle : particles) {
                    particle.progress = value;
                }
                invalidate();
            });
        }
        if (animator.isRunning()) {
            animator.cancel();
        }
        animator.start();
    }

    private StarParticle createParticle(float centerX, float centerY) {
        float angle = (float) (random.nextFloat() * Math.PI * 2f);
        float distance = dpToPx(80f) + random.nextFloat() * dpToPx(40f);
        float velocityX = (float) Math.cos(angle) * distance;
        float velocityY = (float) Math.sin(angle) * distance;
        float radius = baseRadius + random.nextFloat() * dpToPx(2.5f);
        float rotation = random.nextFloat() * 360f;
        return new StarParticle(centerX, centerY, velocityX, velocityY, radius, rotation);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private static final class StarParticle {
        final float startX;
        final float startY;
        final float velocityX;
        final float velocityY;
        final float radius;
        final float rotation;
        float progress;

        private StarParticle(float startX, float startY, float velocityX, float velocityY, float radius, float rotation) {
            this.startX = startX;
            this.startY = startY;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.radius = radius;
            this.rotation = rotation;
        }
    }
}
