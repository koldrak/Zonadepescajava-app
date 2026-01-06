package com.daille.zonadepescajava_app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Link;
import com.daille.zonadepescajava_app.model.LinkType;

import java.util.ArrayList;
import java.util.List;

public class BoardLinksDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Context context;
    private final List<Link> links = new ArrayList<>();

    public BoardLinksDecoration(Context context) {
        this.context = context.getApplicationContext();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setLinks(List<Link> newLinks) {
        links.clear();
        if (newLinks != null) links.addAll(newLinks);
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (links.isEmpty()) return;

        for (Link link : links) {
            int from = link.from;
            int to = link.to;

            View fromView = parent.getLayoutManager() != null ? parent.getLayoutManager().findViewByPosition(from) : null;
            View toView = parent.getLayoutManager() != null ? parent.getLayoutManager().findViewByPosition(to) : null;

            // Si no están visibles (en un board 3x3 deberían estarlo), se omite.
            if (fromView == null || toView == null) continue;

            PointF a = centerOf(fromView);
            PointF b = centerOf(toView);

            applyStyleFor(link.type);
            canvas.drawLine(a.x, a.y, b.x, b.y, paint);
        }
    }

    private PointF centerOf(View v) {
        float x = v.getLeft() + v.getWidth() / 2f;
        float y = v.getTop() + v.getHeight() / 2f;
        return new PointF(x, y);
    }

    private void applyStyleFor(LinkType type) {
        // Grosor base (dp)
        float density = context.getResources().getDisplayMetrics().density;
        float widthDp;

        int colorRes;
        switch (type) {
            case PAYASO_PROTEGE:
                colorRes = R.color.selection_highlight; // reutiliza tu highlight (o crea uno nuevo)
                widthDp = 5f;
                break;

            case ALMEJAS_REACCION:
                colorRes = android.R.color.darker_gray;
                widthDp = 4f;
                break;

            case BOTA_VIEJA_PENALIZA:
                colorRes = android.R.color.black;
                widthDp = 4f;
                break;

            case BOTELLA_PLASTICO_AJUSTA:
                colorRes = android.R.color.holo_blue_dark;
                widthDp = 4f;
                break;

            default:
                colorRes = android.R.color.black;
                widthDp = 3f;
                break;
        }

        paint.setColor(ContextCompat.getColor(context, colorRes));
        paint.setStrokeWidth(widthDp * density);
    }
}
