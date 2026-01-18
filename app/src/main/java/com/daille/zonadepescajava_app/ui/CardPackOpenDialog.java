package com.daille.zonadepescajava_app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CardPackOpenDialog {
    private CardPackOpenDialog() {
    }

    public static void show(
            Context context,
            Bitmap packImage,
            List<Card> cards,
            CardImageResolver resolver,
            Runnable onAllCardsViewed
    ) {
        if (context == null || cards == null || cards.isEmpty() || resolver == null) return;

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_card_pack);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView packImageView = dialog.findViewById(R.id.cardPackImage);
        FrameLayout cardsContainer = dialog.findViewById(R.id.cardPackCards);

        StarBurstView starBurstView = new StarBurstView(context);
        cardsContainer.addView(starBurstView, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        Bitmap packBitmap = packImage != null ? packImage : resolver.getCardBack();
        packImageView.setImageBitmap(packBitmap);

        List<ImageView> cardViews = new ArrayList<>();

        int cardWidth = dpToPx(context, 180);
        int cardHeight = dpToPx(context, 260);

        // Ajustes de salida
        int startY = 0; // centro exacto (quedan debajo del sobre al inicio)
        int endY = dpToPx(context, -190);      // arriba de la pantalla

        final boolean[] completedAll = {false};
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Bitmap cardBitmap = resolver.getImageFor(card, true);
            Card cardFinal = card;
            Bitmap cardBitmapFinal = cardBitmap;

            ImageView cardView = new ImageView(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardWidth, cardHeight);
            params.gravity = Gravity.CENTER;
            cardView.setLayoutParams(params);
            cardView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cardView.setImageBitmap(cardBitmap != null ? cardBitmap : resolver.getCardBack());

            // Estado inicial (bloqueadas, dentro, con tilt)
            cardView.setAlpha(0f);
            cardView.setTranslationY(startY);
            cardView.setTranslationX(0f);
            cardView.setScaleX(0.78f);
            cardView.setScaleY(0.78f);
            cardView.setEnabled(false);
            cardView.setClickable(false);
            cardView.setCameraDistance(8000f * context.getResources().getDisplayMetrics().density);
            cardView.setRotationX(25f);
            cardView.setRotation(0f);
            cardView.setElevation(dpToPx(context, 5)); // puede quedar así, pero pack queda mucho más arriba por 30+

            cardsContainer.addView(cardView);
            cardViews.add(cardView);

            cardView.setOnClickListener(v -> {
                v.setEnabled(false);
                String overlay = context.getString(R.string.card_pack_reward_detail, cardFinal.getName());
                Bitmap detailBitmap = cardBitmapFinal != null ? cardBitmapFinal : resolver.getCardBack();
                CardFullscreenDialog.show(context, detailBitmap, overlay, () -> {
                    cardsContainer.removeView(cardView);
                    if (cardsContainer.getChildCount() == 0) {
                        completedAll[0] = true;
                        dialog.dismiss();
                    }
                });
            });
        }

        final List<Animator> running = new ArrayList<>();
        final PackTearViews[] tornHolder = new PackTearViews[1];

        dialog.setOnShowListener(dlg -> {
            // Reemplaza el pack por dos mitades + borde rasgado, y lanza animación completa
            packImageView.post(() -> {
                PackTearViews torn = replacePackWithTornViews(packImageView, packBitmap, context);
                tornHolder[0] = torn;

                // Si por alguna razón no se pudo rasgar, cae al fallback (solo cartas)
                if (torn == null) {
                    AnimatorSet fallback = playCardsOnly(cards, cardViews, cardsContainer, starBurstView, startY, endY);
                    running.add(fallback);
                    fallback.start();
                    return;
                }

                AnimatorSet full = playTearOpenAndReveal(
                        torn,
                        cards,
                        cardViews,
                        cardsContainer,
                        starBurstView,
                        startY,
                        endY
                );

                running.add(full);
                full.start();
            });
        });

        dialog.setOnDismissListener((DialogInterface dlg) -> {
            for (Animator a : running) {
                if (a != null) a.cancel();
            }
            running.clear();

            // Cancela animaciones por ViewPropertyAnimator, por si existieran
            for (ImageView cv : cardViews) {
                if (cv != null) cv.animate().cancel();
            }
            PackTearViews t = tornHolder[0];
            if (t != null) {
                if (t.top != null) t.top.animate().cancel();
                if (t.bottom != null) t.bottom.animate().cancel();
                if (t.edge != null) t.edge.animate().cancel();
            }

            if (completedAll[0] && onAllCardsViewed != null) {
                onAllCardsViewed.run();
            }
        });

        dialog.show();
    }

    // =========================================================
    // ANIMACION COMPLETA: rasgar -> cartas desde interior -> pack cae abajo
    // =========================================================

    private static AnimatorSet playTearOpenAndReveal(
            PackTearViews torn,
            List<Card> cards,
            List<ImageView> cardViews,
            FrameLayout cardsContainer,
            StarBurstView starBurstView,
            int startY,
            int endY
    ) {
        final float density = cardsContainer.getResources().getDisplayMetrics().density;

        // --- PACK: pivots para movimiento "rasgado"
        torn.top.setPivotX(torn.top.getWidth() * 0.5f);
        torn.top.setPivotY(torn.top.getHeight() * 0.20f);

        torn.bottom.setPivotX(torn.bottom.getWidth() * 0.5f);
        torn.bottom.setPivotY(torn.bottom.getHeight() * 0.80f);

        // --- Stage 1: tensión (apretar + shake)
        ObjectAnimator preSquashYTop = ObjectAnimator.ofFloat(torn.top, View.SCALE_Y, 1f, 0.96f, 0.99f);
        preSquashYTop.setDuration(170);
        preSquashYTop.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator preSquashYBottom = ObjectAnimator.ofFloat(torn.bottom, View.SCALE_Y, 1f, 0.96f, 0.99f);
        preSquashYBottom.setDuration(170);
        preSquashYBottom.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator shakeTop = ObjectAnimator.ofFloat(torn.top, View.ROTATION, 0f, -2.2f, 2.2f, -1.6f, 1.6f, -0.8f, 0.8f, 0f);
        shakeTop.setDuration(240);
        shakeTop.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator shakeBottom = ObjectAnimator.ofFloat(torn.bottom, View.ROTATION, 0f, 1.4f, -1.4f, 1.0f, -1.0f, 0.6f, -0.6f, 0f);
        shakeBottom.setDuration(240);
        shakeBottom.setInterpolator(new DecelerateInterpolator());

        AnimatorSet tension = new AnimatorSet();
        tension.playTogether(preSquashYTop, preSquashYBottom, shakeTop, shakeBottom);

        // --- Stage 2: rasgado (se separan mitades + aparece borde)
        float tearGap = 36f * density;

        ObjectAnimator edgeIn = ObjectAnimator.ofFloat(torn.edge, View.ALPHA, 0f, 1f);
        edgeIn.setDuration(120);
        edgeIn.setInterpolator(new DecelerateInterpolator());

        float slideRight = cardsContainer.getWidth() * 0.90f; // cuánto se va a la derecha

        ObjectAnimator topUp = ObjectAnimator.ofFloat(
                torn.top,
                View.TRANSLATION_Y,
                torn.top.getTranslationY(),
                torn.top.getTranslationY() - (tearGap * 0.55f)
        );
        topUp.setDuration(260);
        topUp.setInterpolator(new OvershootInterpolator(1.05f));

        ObjectAnimator topRight = ObjectAnimator.ofFloat(
                torn.top,
                View.TRANSLATION_X,
                torn.top.getTranslationX(),
                torn.top.getTranslationX() + slideRight
        );
        topRight.setDuration(280);
        topRight.setInterpolator(new OvershootInterpolator(1.05f));


        ObjectAnimator bottomDown = ObjectAnimator.ofFloat(torn.bottom, View.TRANSLATION_Y, torn.bottom.getTranslationY(), torn.bottom.getTranslationY() + tearGap);
        bottomDown.setDuration(260);
        bottomDown.setInterpolator(new OvershootInterpolator(1.05f));

        ObjectAnimator topRot = ObjectAnimator.ofFloat(torn.top, View.ROTATION, 0f, -10f, 3f, 0f);
        topRot.setDuration(320);
        topRot.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator bottomRot = ObjectAnimator.ofFloat(torn.bottom, View.ROTATION, 0f, 3.5f, -1.2f, 0f);
        bottomRot.setDuration(320);
        bottomRot.setInterpolator(new DecelerateInterpolator());

        AnimatorSet tear = new AnimatorSet();
        tear.playTogether(edgeIn, topUp, topRight, bottomDown, topRot, bottomRot);

        // --- Stage 3: cartas "aparecen desde el interior" (peek -> pull out)
        AnimatorSet cardsReveal = buildCardsReveal(cards, cardViews, cardsContainer, starBurstView, startY, endY);
        cardsReveal.setStartDelay(140); // deja que se note el rasgado antes de que asomen

        // --- Stage 4: pack desaparece hacia la zona inferior
        // El pack cae DESPUÉS de que las cartas ya estén visibles.
        long packDropDelay = 250 + (cardViews.size() - 1) * 120L + 640L; // sincroniza con la última carta

        float dropDistance = cardsContainer.getHeight() * 0.65f + 220f * density;

        ObjectAnimator topDropY = ObjectAnimator.ofFloat(torn.top, View.TRANSLATION_Y, torn.top.getTranslationY() - tearGap, (torn.top.getTranslationY() - tearGap) + dropDistance);
        ObjectAnimator bottomDropY = ObjectAnimator.ofFloat(torn.bottom, View.TRANSLATION_Y, torn.bottom.getTranslationY() + tearGap, (torn.bottom.getTranslationY() + tearGap) + dropDistance);
        ObjectAnimator edgeDropY = ObjectAnimator.ofFloat(torn.edge, View.TRANSLATION_Y, torn.edge.getTranslationY(), torn.edge.getTranslationY() + dropDistance);

        ObjectAnimator topFade = ObjectAnimator.ofFloat(torn.top, View.ALPHA, 1f, 0f);
        ObjectAnimator bottomFade = ObjectAnimator.ofFloat(torn.bottom, View.ALPHA, 1f, 0f);
        ObjectAnimator edgeFade = ObjectAnimator.ofFloat(torn.edge, View.ALPHA, 1f, 0f);

        ObjectAnimator topScale = ObjectAnimator.ofFloat(torn.top, View.SCALE_X, 1f, 0.96f);
        ObjectAnimator bottomScale = ObjectAnimator.ofFloat(torn.bottom, View.SCALE_X, 1f, 0.96f);

        AnimatorSet packDrop = new AnimatorSet();
        packDrop.playTogether(topDropY, bottomDropY, edgeDropY, topFade, bottomFade, edgeFade, topScale, bottomScale);
        packDrop.setDuration(420);
        packDrop.setInterpolator(new AccelerateDecelerateInterpolator());
        packDrop.setStartDelay(packDropDelay);

        // --- Orquestación completa
        AnimatorSet full = new AnimatorSet();
        full.playSequentially(tension, tear);

        // cards + packDrop van en paralelo tras el rasgado
        AnimatorSet afterTear = new AnimatorSet();
        afterTear.playTogether(cardsReveal, packDrop);

        AnimatorSet wrapper = new AnimatorSet();
        wrapper.playSequentially(full, afterTear);

        // Al final habilita clicks de cartas y deja el pack fuera
        wrapper.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for (int i = 0; i < cardViews.size(); i++) {
                    ImageView v = cardViews.get(i);
                    v.setEnabled(true);
                    v.setClickable(true);
                    v.setRotationX(0f);
                    v.setAlpha(1f);
                }
                // Opcional: remover views del pack para liberar
                ViewGroup parent = (ViewGroup) torn.top.getParent();
                if (parent != null) {
                    parent.removeView(torn.top);
                    parent.removeView(torn.bottom);
                    parent.removeView(torn.edge);
                }
            }
        });

        return wrapper;
    }

    private static AnimatorSet buildCardsReveal(
            List<Card> cards,
            List<ImageView> cardViews,
            FrameLayout container,
            StarBurstView starBurstView,
            int startY,
            int endY
    ) {
        final float density = container.getResources().getDisplayMetrics().density;

        int n = cardViews.size();
        int mid = (n - 1) / 2;

        float baseOffsetX = 120f * density;
        float peekDeltaY = 48f * density;
        float pullOvershootY = 22f * density;
        float pullOvershootX = 14f * density;

        TimeInterpolator peekInterp = new DecelerateInterpolator();
        TimeInterpolator pullInterp = new AnticipateOvershootInterpolator(0.9f);
        TimeInterpolator settleInterp = new OvershootInterpolator(0.9f);

        List<Animator> all = new ArrayList<>();

        // Stage A: peek (asoman desde adentro)
        for (int i = 0; i < n; i++) {
            ImageView v = cardViews.get(i);

            v.setAlpha(0f);
            v.setScaleX(0.78f);
            v.setScaleY(0.78f);
            v.setTranslationX(0f);
            v.setTranslationY(startY);
            v.setRotation(0f);
            v.setRotationX(25f);

            float r = (i - mid) * 3.2f;
            float jitter = ((i % 2 == 0) ? 1f : -1f) * (0.8f + 0.2f * i);

            ObjectAnimator alphaIn = ObjectAnimator.ofFloat(v, View.ALPHA, 0f, 1f);
            alphaIn.setDuration(160);

            ObjectAnimator peekY = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, startY, startY - peekDeltaY);
            peekY.setDuration(220);
            peekY.setInterpolator(peekInterp);

            ObjectAnimator peekRot = ObjectAnimator.ofFloat(v, View.ROTATION, 0f, r * 0.35f + jitter);
            peekRot.setDuration(220);
            peekRot.setInterpolator(peekInterp);

            ObjectAnimator peekTilt = ObjectAnimator.ofFloat(v, View.ROTATION_X, 25f, 14f);
            peekTilt.setDuration(220);
            peekTilt.setInterpolator(peekInterp);

            AnimatorSet peekSet = new AnimatorSet();
            peekSet.playTogether(alphaIn, peekY, peekRot, peekTilt);
            peekSet.setStartDelay(80L + i * 70L);

            all.add(peekSet);
        }

        // Stage B: pull out (sale con arco/overshoot + abanico)
        for (int i = 0; i < n; i++) {
            ImageView v = cardViews.get(i);
            Card card = cards.get(i);
            int starColor = getStarBurstColor(card);

            float targetX = (i - mid) * baseOffsetX;
            float targetY = endY;
            float rotTarget = (i - mid) * 6.5f;

            PropertyValuesHolder pvhY = PropertyValuesHolder.ofKeyframe(
                    View.TRANSLATION_Y,
                    android.animation.Keyframe.ofFloat(0f, startY - peekDeltaY),
                    android.animation.Keyframe.ofFloat(0.72f, targetY + pullOvershootY),
                    android.animation.Keyframe.ofFloat(1f, targetY)
            );

            float sign = (targetX == 0f) ? 1f : Math.signum(targetX);
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofKeyframe(
                    View.TRANSLATION_X,
                    android.animation.Keyframe.ofFloat(0f, 0f),
                    android.animation.Keyframe.ofFloat(0.72f, targetX + sign * pullOvershootX),
                    android.animation.Keyframe.ofFloat(1f, targetX)
            );

            PropertyValuesHolder pvhSX = PropertyValuesHolder.ofKeyframe(
                    View.SCALE_X,
                    android.animation.Keyframe.ofFloat(0f, 0.78f),
                    android.animation.Keyframe.ofFloat(0.72f, 1.06f),
                    android.animation.Keyframe.ofFloat(1f, 1f)
            );
            PropertyValuesHolder pvhSY = PropertyValuesHolder.ofKeyframe(
                    View.SCALE_Y,
                    android.animation.Keyframe.ofFloat(0f, 0.78f),
                    android.animation.Keyframe.ofFloat(0.72f, 1.06f),
                    android.animation.Keyframe.ofFloat(1f, 1f)
            );

            PropertyValuesHolder pvhR = PropertyValuesHolder.ofKeyframe(
                    View.ROTATION,
                    android.animation.Keyframe.ofFloat(0f, v.getRotation()),
                    android.animation.Keyframe.ofFloat(1f, rotTarget)
            );

            PropertyValuesHolder pvhTilt = PropertyValuesHolder.ofKeyframe(
                    View.ROTATION_X,
                    android.animation.Keyframe.ofFloat(0f, 14f),
                    android.animation.Keyframe.ofFloat(1f, 0f)
            );

            ObjectAnimator pull = ObjectAnimator.ofPropertyValuesHolder(v, pvhX, pvhY, pvhSX, pvhSY, pvhR, pvhTilt);
            pull.setDuration(640);
            pull.setInterpolator(pullInterp);
            pull.setStartDelay(320L + i * 120L);

            if (starColor != 0 && starBurstView != null) {
                final boolean[] spawned = {false};
                pull.addUpdateListener(anim -> {
                    if (!spawned[0] && anim.getAnimatedFraction() >= 0.35f) {
                        float centerX = v.getX() + v.getWidth() * 0.5f;
                        float centerY = v.getY() + v.getHeight() * 0.5f;
                        starBurstView.burst(centerX, centerY, 18, starColor);
                        spawned[0] = true;
                    }
                });
            }

            int elev = dpToPx(container.getContext(), 3 + i * 2);
            pull.addUpdateListener(anim -> v.setElevation(elev));

            all.add(pull);

            // settle suave
            ObjectAnimator settleScale = ObjectAnimator.ofFloat(v, View.SCALE_X, 1f, 1.02f, 1f);
            settleScale.setDuration(220);
            settleScale.setInterpolator(settleInterp);
            settleScale.setStartDelay(320L + i * 120L + 640L - 120L);

            ObjectAnimator settleY = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, targetY, targetY - 6f * density, targetY);
            settleY.setDuration(220);
            settleY.setInterpolator(settleInterp);
            settleY.setStartDelay(320L + i * 120L + 640L - 120L);

            AnimatorSet settle = new AnimatorSet();
            settle.playTogether(settleScale, settleY);
            all.add(settle);
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(all);
        return set;
    }

    // Fallback simple si no se pudo crear el rasgado (no debería ocurrir)
    private static AnimatorSet playCardsOnly(
            List<Card> cards,
            List<ImageView> cardViews,
            FrameLayout container,
            StarBurstView starBurstView,
            int startY,
            int endY
    ) {
        AnimatorSet cardsReveal = buildCardsReveal(cards, cardViews, container, starBurstView, startY, endY);
        cardsReveal.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                for (ImageView v : cardViews) {
                    v.setEnabled(true);
                    v.setClickable(true);
                }
            }
        });
        return cardsReveal;
    }

    private static int getStarBurstColor(Card card) {
        if (card == null) return 0;
        int points = card.getPoints();
        if (points == 7) {
            return 0xFFFF9A4D;
        }
        if (points == 8 || points == 9) {
            return 0xFFFFFFFF;
        }
        if (points == 10) {
            return 0xFFFFF07A;
        }
        return 0;
    }

    // =========================================================
    // RASGADO REAL: se reemplaza el pack por top/bottom/edge (generados desde Bitmap)
    // =========================================================

    private static final class PackTearViews {
        ImageView top;
        ImageView bottom;
        ImageView edge;
        float tearY;
    }

    private static final class TearBitmaps {
        Bitmap top;
        Bitmap bottom;
        Bitmap edge;
        float tearY;
    }

    private static PackTearViews replacePackWithTornViews(ImageView originalPack, Bitmap packBitmap, Context context) {
        if (originalPack == null || packBitmap == null || context == null) return null;

        ViewGroup parent = (ViewGroup) originalPack.getParent();
        if (parent == null) return null;

        float tearFrac = 0.22f;                 // donde se rasga (relativo)
        int edgeThickness = dpToPx(context, 10); // grosor del borde rasgado

        TearBitmaps torn = createTornBitmaps(packBitmap, tearFrac, edgeThickness);
        if (torn == null || torn.top == null || torn.bottom == null || torn.edge == null) return null;

        int index = parent.indexOfChild(originalPack);

        ImageView bottom = new ImageView(context);
        bottom.setImageBitmap(torn.bottom);
        bottom.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bottom.setLayoutParams(originalPack.getLayoutParams());
        bottom.setTranslationX(originalPack.getTranslationX());
        bottom.setTranslationY(originalPack.getTranslationY());
        bottom.setScaleX(originalPack.getScaleX());
        bottom.setScaleY(originalPack.getScaleY());
        bottom.setAlpha(1f);

        ImageView edge = new ImageView(context);
        edge.setImageBitmap(torn.edge);
        edge.setScaleType(ImageView.ScaleType.FIT_CENTER);
        edge.setLayoutParams(originalPack.getLayoutParams());
        edge.setTranslationX(originalPack.getTranslationX());
        edge.setTranslationY(originalPack.getTranslationY());
        edge.setScaleX(originalPack.getScaleX());
        edge.setScaleY(originalPack.getScaleY());
        edge.setAlpha(0f); // aparece al rasgar

        ImageView top = new ImageView(context);
        top.setImageBitmap(torn.top);
        top.setScaleType(ImageView.ScaleType.FIT_CENTER);
        top.setLayoutParams(originalPack.getLayoutParams());
        top.setTranslationX(originalPack.getTranslationX());
        top.setTranslationY(originalPack.getTranslationY());
        top.setScaleX(originalPack.getScaleX());
        top.setScaleY(originalPack.getScaleY());
        top.setAlpha(1f);

        parent.removeView(originalPack);
        parent.addView(bottom, index);
        parent.addView(edge, index + 1);
        parent.addView(top, index + 2);

        originalPack.setImageDrawable(null);

        PackTearViews out = new PackTearViews();
        out.top = top;
        out.bottom = bottom;
        out.edge = edge;
        out.tearY = torn.tearY;
        return out;
    }

    private static TearBitmaps createTornBitmaps(Bitmap src, float tearFrac, int edgeThicknessPx) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();
        float tearY = Math.max(2f, Math.min(h - 3f, h * tearFrac));

        // Línea irregular
        int points = 22;
        float[] xs = new float[points];
        float[] ys = new float[points];

        Random rng = new Random(1337);
        for (int i = 0; i < points; i++) {
            float t = (float) i / (points - 1);
            xs[i] = t * w;
            float jitter = (rng.nextFloat() - 0.5f) * (edgeThicknessPx * 1.6f);
            ys[i] = tearY + jitter;
        }
// Gap para que TOP y BOTTOM no compartan los mismos pixels en el borde (evita la línea)
        final float cut = Math.max(1f, edgeThicknessPx * 0.45f);

        Path tearLine = new Path();
        tearLine.moveTo(xs[0], ys[0]);
        for (int i = 1; i < points; i++) {
            float midX = (xs[i - 1] + xs[i]) * 0.5f;
            float midY = (ys[i - 1] + ys[i]) * 0.5f;
            tearLine.quadTo(xs[i - 1], ys[i - 1], midX, midY);
        }

        // Masks
        Bitmap maskTop = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Bitmap maskBottom = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas cTop = new Canvas(maskTop);
        Canvas cBottom = new Canvas(maskBottom);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(0xFFFFFFFF);

        // Top area: from top to tear line
        Path topArea = new Path();
        topArea.moveTo(0, 0);
        topArea.lineTo(w, 0);
        topArea.lineTo(w, 0);
        topArea.lineTo(w, 0);

        // Recorre puntos en reversa para cerrar
        topArea.lineTo(xs[points - 1], ys[points - 1] - cut);
        for (int i = points - 2; i >= 0; i--) {
            topArea.lineTo(xs[i], ys[i] - cut);
        }

        topArea.close();
        cTop.drawPath(topArea, fill);

        // Bottom area: from tear line to bottom
        Path bottomArea = new Path();
        bottomArea.moveTo(0, h);
        bottomArea.lineTo(w, h);
        bottomArea.lineTo(xs[points - 1], ys[points - 1] + cut);
        for (int i = points - 2; i >= 0; i--) {
            bottomArea.lineTo(xs[i], ys[i] + cut);
        }

        bottomArea.close();
        cBottom.drawPath(bottomArea, fill);

        Bitmap top = applyMask(src, maskTop);
        Bitmap bottom = applyMask(src, maskBottom);

        // Edge bitmap: dibuja el borde rasgado (2 strokes para dar profundidad)
        Bitmap edge = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas ce = new Canvas(edge);

        Paint strokeDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokeDark.setStyle(Paint.Style.STROKE);
        strokeDark.setStrokeCap(Paint.Cap.ROUND);
        strokeDark.setStrokeJoin(Paint.Join.ROUND);
        strokeDark.setStrokeWidth(edgeThicknessPx * 1.15f);
        strokeDark.setColor(0x66000000);

        Paint strokeLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokeLight.setStyle(Paint.Style.STROKE);
        strokeLight.setStrokeCap(Paint.Cap.ROUND);
        strokeLight.setStrokeJoin(Paint.Join.ROUND);
        strokeLight.setStrokeWidth(edgeThicknessPx);
        strokeLight.setColor(0xCCFFFFFF);


        TearBitmaps out = new TearBitmaps();
        out.top = top;
        out.bottom = bottom;
        out.edge = edge;
        out.tearY = tearY;
        return out;
    }

    private static Bitmap applyMask(Bitmap src, Bitmap mask) {
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        c.drawBitmap(src, 0f, 0f, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        c.drawBitmap(mask, 0f, 0f, p);
        p.setXfermode(null);

        return out;
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }
}
