package com.daille.zonadepescajava_app.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class PerspectiveBoardLayoutManager extends RecyclerView.LayoutManager {
    private static final int SPAN_COUNT = 3;
    private final float[] rowScale;

    public PerspectiveBoardLayoutManager(float[] rowScale) {
        this.rowScale = rowScale != null ? rowScale.clone() : new float[] {1f, 1f, 1f};
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (getItemCount() == 0) {
            return;
        }

        int parentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int top = getPaddingTop();

        int rows = (int) Math.ceil(getItemCount() / (float) SPAN_COUNT);
        for (int row = 0; row < rows; row++) {
            float scale = rowScale[Math.min(row, rowScale.length - 1)];
            int rowWidth = Math.round(parentWidth * scale);
            int colWidth = rowWidth / SPAN_COUNT;
            int rowHeight = colWidth;
            int left = getPaddingLeft() + (parentWidth - rowWidth) / 2;

            for (int col = 0; col < SPAN_COUNT; col++) {
                int position = row * SPAN_COUNT + col;
                if (position >= getItemCount()) {
                    break;
                }

                View view = recycler.getViewForPosition(position);
                addView(view);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                params.width = colWidth;
                params.height = rowHeight;
                view.setLayoutParams(params);
                measureChildWithMargins(view, 0, 0);

                int childLeft = left + (col * colWidth);
                int childTop = top;
                layoutDecoratedWithMargins(
                        view,
                        childLeft,
                        childTop,
                        childLeft + colWidth,
                        childTop + rowHeight
                );
            }
            top += rowHeight;
        }
    }
}
