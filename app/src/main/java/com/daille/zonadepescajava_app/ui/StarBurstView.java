package com.daille.zonadepescajava_app.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class StarBurstView extends View {

    private static final int COLOR_STAR = 0xFFFFF2A6;

    // “Ticker” (solo para invalidar a ~60fps)
    private static final long TICK_MS = 1000L; // el valor real no importa, repetimos infinito

    private final List<StarParticle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final Path starPath = new Path();

    private final float baseRadius;
    private final float coreRadius;

    @Nullable private ValueAnimator ticker;

    public StarBurstView(Context context) { this(context, null); }
    public StarBurstView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public StarBurstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setStyle(Paint.Style.FILL);
        baseRadius = dpToPx(5f);
        coreRadius = dpToPx(2.2f);
    }

    public void burst(float centerX, float centerY, int count) {
        burst(centerX, centerY, count, COLOR_STAR);
    }

    public void burst(float centerX, float centerY, int count, int color) {
        if (count <= 0) return;

        long now = SystemClock.uptimeMillis();
        for (int i = 0; i < count; i++) {
            particles.add(createParticle(centerX, centerY, color, now));
        }
        ensureTickerRunning();
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (ticker != null) ticker.cancel();
        ticker = null;
        particles.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) return;

        long now = SystemClock.uptimeMillis();

        Iterator<StarParticle> it = particles.iterator();
        while (it.hasNext()) {
            StarParticle p = it.next();

            float t = (now - p.bornTimeMs) / (float) p.lifeMs;
            if (t >= 1f) {
                it.remove();
                continue;
            }

            // easing hacia afuera (sale rápido y desacelera un poco)
            float eased = 1f - (1f - t) * (1f - t);

            float alpha = (1f - t);
            paint.setColor(p.color);
            paint.setAlpha((int) (alpha * 255));

            float x = p.startX + p.velocityX * eased;
            float y = p.startY + p.velocityY * eased;

            float radius = p.radius * (1f - t * 0.35f);
            float rotation = p.rotation + p.spin * t;

            drawStar(canvas, x, y, radius, rotation);
        }

        // Si no queda nada, paramos ticker para ahorrar
        if (particles.isEmpty() && ticker != null) {
            ticker.cancel();
        }
    }

    private void ensureTickerRunning() {
        if (ticker == null) {
            ticker = ValueAnimator.ofFloat(0f, 1f);
            ticker.setDuration(TICK_MS);
            ticker.setRepeatCount(ValueAnimator.INFINITE);
            ticker.setRepeatMode(ValueAnimator.RESTART);
            ticker.setInterpolator(new LinearInterpolator());
            ticker.addUpdateListener(a -> {
                // Solo “redibuja”; la física real depende del tiempo (bornTime)
                if (!particles.isEmpty()) invalidate();
            });
        }
        if (!ticker.isRunning()) ticker.start();
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, float rotation) {
        starPath.reset();
        float angleStep = (float) (Math.PI / 5.0);
        float angle = (float) Math.toRadians(rotation - 90f);

        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? radius : coreRadius;
            float x = (float) (cx + Math.cos(angle) * r);
            float y = (float) (cy + Math.sin(angle) * r);
            if (i == 0) starPath.moveTo(x, y);
            else starPath.lineTo(x, y);
            angle += angleStep;
        }
        starPath.close();
        canvas.drawPath(starPath, paint);
    }

    private StarParticle createParticle(float centerX, float centerY, int color, long nowMs) {
        float angle = (float) (random.nextFloat() * Math.PI * 2f);

        // RADIO: si quieres más “fuego artificial”, sube estos valores
        float distance = dpToPx(110f) + random.nextFloat() * dpToPx(90f);

        float vx = (float) Math.cos(angle) * distance;
        float vy = (float) Math.sin(angle) * distance;

        float radius = baseRadius + random.nextFloat() * dpToPx(2.5f);
        float rotation = random.nextFloat() * 360f;

        // Vida por partícula (clave para que NO se vea punto)
        long life = 650L + random.nextInt(450); // 650–1100ms

        float spin = 220f + random.nextFloat() * 380f;

        return new StarParticle(centerX, centerY, vx, vy, radius, rotation, spin, color, nowMs, life);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private static final class StarParticle {
        final float startX, startY;
        final float velocityX, velocityY;
        final float radius;
        final float rotation;
        final float spin;
        final int color;
        final long bornTimeMs;
        final long lifeMs;

        StarParticle(float sx, float sy, float vx, float vy,
                     float r, float rot, float spin,
                     int color, long born, long life) {
            this.startX = sx;
            this.startY = sy;
            this.velocityX = vx;
            this.velocityY = vy;
            this.radius = r;
            this.rotation = rot;
            this.spin = spin;
            this.color = color;
            this.bornTimeMs = born;
            this.lifeMs = life;
        }
    }
}
