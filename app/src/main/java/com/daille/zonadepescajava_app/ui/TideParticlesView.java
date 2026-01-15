package com.daille.zonadepescajava_app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TideParticlesView extends View {
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final int BASE_COLOR = 0xB3E5FC;
    private static final float MAX_DT = 0.05f;

    private static class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float size;
        float age;
        float life;
        float driftPhase;
        float driftAmplitude;
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Direction> pendingDirections = new ArrayList<>();

    private ValueAnimator animator;
    private Direction activeDirection;
    private long lastFrameTime;
    private float emissionRemainder;

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
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BASE_COLOR);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void playSequence(List<Direction> directions) {
        if (directions == null || directions.isEmpty()) {
            return;
        }
        pendingDirections.addAll(directions);
        if (!isRunning()) {
            startNextAnimation();
        }
    }

    private void startNextAnimation() {
        if (pendingDirections.isEmpty()) {
            return;
        }
        activeDirection = pendingDirections.remove(0);
        startAnimation();
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        particles.clear();
        emissionRemainder = 0f;
        lastFrameTime = SystemClock.uptimeMillis();

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900L);
        animator.setInterpolator(new DecelerateInterpolator(1.2f));
        animator.addUpdateListener(animation -> {
            long now = SystemClock.uptimeMillis();
            float dt = Math.min(MAX_DT, (now - lastFrameTime) / 1000f);
            lastFrameTime = now;
            float progress = (float) animation.getAnimatedFraction();
            emitParticles(dt, progress);
            updateParticles(dt);
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                particles.clear();
                invalidate();
                startNextAnimation();
            }
        });
        animator.start();
    }

    private void emitParticles(float dt, float progress) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        float emissionRate = lerp(260f, 140f, progress);
        float toEmit = emissionRate * dt + emissionRemainder;
        int count = (int) Math.floor(toEmit);
        emissionRemainder = toEmit - count;

        float pad = dpToPx(12f);
        for (int i = 0; i < count; i++) {
            Particle particle = new Particle();
            float speed = dpToPx(140f + random.nextFloat() * 90f);
            float spread = dpToPx(26f);
            float amplitude = dpToPx(6f + random.nextFloat() * 8f);
            particle.size = dpToPx(2.8f + random.nextFloat() * 3.5f);
            particle.life = 0.6f + random.nextFloat() * 0.4f;
            particle.age = random.nextFloat() * 0.1f;
            particle.driftPhase = (float) (random.nextFloat() * Math.PI * 2f);
            particle.driftAmplitude = amplitude;

            switch (activeDirection) {
                case RIGHT:
                    particle.x = -pad;
                    particle.y = random.nextFloat() * getHeight();
                    particle.vx = speed;
                    particle.vy = (random.nextFloat() - 0.5f) * spread;
                    break;
                case LEFT:
                    particle.x = getWidth() + pad;
                    particle.y = random.nextFloat() * getHeight();
                    particle.vx = -speed;
                    particle.vy = (random.nextFloat() - 0.5f) * spread;
                    break;
                case DOWN:
                    particle.x = random.nextFloat() * getWidth();
                    particle.y = -pad;
                    particle.vx = (random.nextFloat() - 0.5f) * spread;
                    particle.vy = speed;
                    break;
                case UP:
                default:
                    particle.x = random.nextFloat() * getWidth();
                    particle.y = getHeight() + pad;
                    particle.vx = (random.nextFloat() - 0.5f) * spread;
                    particle.vy = -speed;
                    break;
            }
            particles.add(particle);
        }
    }

    private void updateParticles(float dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.age += dt;
            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;
            if (particle.age > particle.life) {
                particles.remove(i);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) {
            return;
        }
        for (Particle particle : particles) {
            float alpha = 1f - (particle.age / particle.life);
            int alphaInt = (int) (alpha * 180f);
            int color = (alphaInt << 24) | BASE_COLOR;
            paint.setColor(color);
            float driftOffset = (float) Math.sin(particle.age * 8f + particle.driftPhase) * particle.driftAmplitude;
            float drawX = particle.x;
            float drawY = particle.y;
            if (activeDirection == Direction.LEFT || activeDirection == Direction.RIGHT) {
                drawY += driftOffset;
            } else {
                drawX += driftOffset;
            }
            canvas.drawCircle(drawX, drawY, particle.size, paint);
        }
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
