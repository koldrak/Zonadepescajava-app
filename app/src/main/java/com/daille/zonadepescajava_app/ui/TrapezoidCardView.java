package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import com.google.android.material.card.MaterialCardView;

public class TrapezoidCardView extends MaterialCardView {
    private final Path clipPath = new Path();
    private float topInsetFraction = 0f;

    public TrapezoidCardView(Context context) {
        super(context);
    }

    public TrapezoidCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrapezoidCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTopInsetFraction(float fraction) {
        float clamped = Math.max(0f, Math.min(fraction, 0.45f));
        if (Math.abs(clamped - topInsetFraction) < 0.001f) {
            return;
        }
        topInsetFraction = clamped;
        updateClipPath(getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateClipPath(w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    private void updateClipPath(int width, int height) {
        clipPath.reset();
        if (width <= 0 || height <= 0) {
            return;
        }
        float inset = width * topInsetFraction;
        clipPath.moveTo(inset, 0);
        clipPath.lineTo(width - inset, 0);
        clipPath.lineTo(width, height);
        clipPath.lineTo(0, height);
        clipPath.close();
    }
}
