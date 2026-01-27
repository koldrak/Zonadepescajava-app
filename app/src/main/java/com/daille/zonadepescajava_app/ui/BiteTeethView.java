package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class BiteTeethView extends View {

    public enum Direction { DOWN, UP }

    private static final int DEFAULT_COLOR = 0xE6FFFFFF;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private Direction direction = Direction.DOWN;
    private int toothColor = DEFAULT_COLOR;

    public BiteTeethView(Context context) {
        super(context);
        init();
    }

    public BiteTeethView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BiteTeethView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(toothColor);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void setDirection(Direction direction) {
        this.direction = direction == null ? Direction.DOWN : direction;
        invalidate();
    }

    public void setToothColor(int color) {
        toothColor = color;
        paint.setColor(toothColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }

        float toothWidth = dpToPx(18f);
        float toothHeight = Math.min(dpToPx(16f), height);
        int count = Math.max(1, (int) Math.ceil(width / toothWidth));
        float actualWidth = width / count;

        path.reset();
        if (direction == Direction.DOWN) {
            for (int i = 0; i < count; i++) {
                float left = i * actualWidth;
                float right = left + actualWidth;
                float center = (left + right) / 2f;
                path.moveTo(left, 0f);
                path.lineTo(center, toothHeight);
                path.lineTo(right, 0f);
                path.close();
            }
        } else {
            for (int i = 0; i < count; i++) {
                float left = i * actualWidth;
                float right = left + actualWidth;
                float center = (left + right) / 2f;
                path.moveTo(left, height);
                path.lineTo(center, height - toothHeight);
                path.lineTo(right, height);
                path.close();
            }
        }
        canvas.drawPath(path, paint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
