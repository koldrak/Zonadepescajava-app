package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TutorialScrimView extends View {
    private static final int SCRIM_COLOR = 0xB3000000;
    private final Paint scrimPaint = new Paint();
    private final Paint clearPaint = new Paint();
    private final List<RectF> highlightRects = new ArrayList<>();

    public TutorialScrimView(Context context) {
        super(context);
        init();
    }

    public TutorialScrimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TutorialScrimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scrimPaint.setColor(SCRIM_COLOR);
        scrimPaint.setStyle(Paint.Style.FILL);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setHighlightRects(List<RectF> rects) {
        highlightRects.clear();
        if (rects != null) {
            highlightRects.addAll(rects);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), scrimPaint);
        for (RectF rect : highlightRects) {
            if (rect != null) {
                canvas.drawRect(rect, clearPaint);
            }
        }
    }
}
