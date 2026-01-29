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
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.SystemClock;

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
        View tearHintLine = dialog.findViewById(R.id.cardPackTearHintLine);
        View tearHintSwipe = dialog.findViewById(R.id.cardPackSwipeHint);

        View innerGlow = new View(context);
        innerGlow.setBackgroundResource(R.drawable.pack_inner_glow);
        innerGlow.setAlpha(0f);
        cardsContainer.addView(innerGlow, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        StarBurstView starBurstView = new StarBurstView(context);
        cardsContainer.addView(starBurstView, 1, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        Bitmap packBitmap = packImage != null ? packImage : resolver.getCardBack();
        packImageView.setImageBitmap(packBitmap);
        packImageView.setElevation(dpToPx(context, 18));

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
                stopTrailEmitter(cardView); // corta el emisor cuando entra al detalle
                String overlay = context.getString(R.string.card_pack_reward_detail, cardFinal.getName());
                Bitmap detailBitmap = cardBitmapFinal != null ? cardBitmapFinal : resolver.getCardBack();
                CardFullscreenDialog.show(context, detailBitmap, overlay, () -> {
                    cardsContainer.removeView(cardView);
                    if (cardsContainer.getChildCount() <= 2) { // 0 = inner glow, 1 = StarBurstView

                        completedAll[0] = true;
                        dialog.dismiss();
                    }
                });
            });
        }

        final List<Animator> running = new ArrayList<>();
        final PackTearViews[] tornHolder = new PackTearViews[1];

        final AnimatorSet[] hintAnimation = new AnimatorSet[1];

        dialog.setOnShowListener(dlg -> {
            // Reemplaza el pack por dos mitades + borde rasgado, y lanza animación completa
            packImageView.post(() -> {
                positionTearHints(packImageView, tearHintLine, tearHintSwipe);
                hintAnimation[0] = playTearHint(tearHintLine, tearHintSwipe);
                if (hintAnimation[0] != null) {
                    hintAnimation[0].start();
                }

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
                        endY,
                        tearHintLine,
                        tearHintSwipe,
                        hintAnimation[0],
                        innerGlow
                );

                running.add(full);
                full.setStartDelay(520);
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
                if (cv != null) {
                    cv.animate().cancel();
                    stopTrailEmitter(cv);
                }
            }

            PackTearViews t = tornHolder[0];
            if (t != null) {
                if (t.top != null) t.top.animate().cancel();
                if (t.bottom != null) t.bottom.animate().cancel();
                if (t.edge != null) t.edge.animate().cancel();
            }

            if (hintAnimation[0] != null) {
                hintAnimation[0].cancel();
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
    private static void startTrailEmitter(ImageView cardView, StarBurstView starBurstView, int color, float density) {
        if (cardView == null || starBurstView == null || color == 0) return;

        // Evita duplicados
        Object prev = cardView.getTag();
        if (prev instanceof ValueAnimator) {
            ((ValueAnimator) prev).cancel();
        }


        final long[] lastEmit = {0L};

        ValueAnimator emitter = ValueAnimator.ofFloat(0f, 1f);
        emitter.setDuration(1000L);
        emitter.setRepeatCount(ValueAnimator.INFINITE);
        emitter.setRepeatMode(ValueAnimator.RESTART);
        emitter.setInterpolator(new DecelerateInterpolator());

        emitter.addUpdateListener(a -> {
            // Solo si la carta sigue en pantalla y visible
            if (cardView.getParent() == null || cardView.getAlpha() <= 0.01f) return;

            long now = SystemClock.uptimeMillis();
            if (now - lastEmit[0] < 45L) return; // densidad (baja a 30ms = más)
            lastEmit[0] = now;

            float cx = cardView.getX() + cardView.getWidth() * 0.5f;
            float cy = cardView.getY() + cardView.getHeight() * 0.55f; // punto “detrás” de la carta

            starBurstView.burst(cx, cy, 8, color); // sube count si quieres más densidad

        });

        cardView.setTag(emitter);
        emitter.start();
    }

    private static void stopTrailEmitter(ImageView cardView) {
        if (cardView == null) return;
        Object tag = cardView.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
        // Limpia el tag por si lo reutilizas
        cardView.setTag(null);
    }

    private static void positionTearHints(ImageView packImageView, View line, View swipe) {
        if (packImageView == null || line == null || swipe == null) return;

        float packX = packImageView.getX();
        float packY = packImageView.getY();
        float packW = packImageView.getWidth();
        float packH = packImageView.getHeight();

        float lineY = packY + packH * 0.22f;
        float lineX = packX + packW * 0.12f;

        line.setX(lineX);
        line.setY(lineY);

        swipe.setX(lineX);
        swipe.setY(lineY - swipe.getHeight() * 0.5f);
    }

    private static AnimatorSet playTearHint(View line, View swipe) {
        if (line == null || swipe == null) return null;

        line.setAlpha(0f);
        swipe.setAlpha(0f);

        ObjectAnimator lineFadeIn = ObjectAnimator.ofFloat(line, View.ALPHA, 0f, 0.85f);
        lineFadeIn.setDuration(220);
        lineFadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator lineGlow = ObjectAnimator.ofFloat(line, View.ALPHA, 0.85f, 0.55f, 0.9f, 0.4f);
        lineGlow.setDuration(900);
        lineGlow.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator swipeIn = ObjectAnimator.ofFloat(swipe, View.ALPHA, 0f, 0.85f);
        swipeIn.setDuration(180);
        swipeIn.setStartDelay(180);

        ObjectAnimator swipeMove = ObjectAnimator.ofFloat(swipe, View.TRANSLATION_X, 0f, dpToPx(swipe.getContext(), 160));
        swipeMove.setDuration(900);
        swipeMove.setInterpolator(new DecelerateInterpolator());
        swipeMove.setStartDelay(220);

        ObjectAnimator swipeFadeOut = ObjectAnimator.ofFloat(swipe, View.ALPHA, 0.85f, 0f);
        swipeFadeOut.setDuration(260);
        swipeFadeOut.setStartDelay(980);

        AnimatorSet hint = new AnimatorSet();
        hint.playTogether(lineFadeIn, lineGlow, swipeIn, swipeMove, swipeFadeOut);
        hint.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                line.animate().alpha(0f).setDuration(220).start();
            }
        });
        return hint;
    }

    private static AnimatorSet playTearOpenAndReveal(
            PackTearViews torn,
            List<Card> cards,
            List<ImageView> cardViews,
            FrameLayout cardsContainer,
            StarBurstView starBurstView,
            int startY,
            int endY,
            View tearHintLine,
            View tearHintSwipe,
            AnimatorSet hintAnimation,
            View innerGlow
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

        // --- Stage 2: rasgado progresivo (onda de bisagra local)
        float tearGap = 36f * density;
        float slideRight = cardsContainer.getWidth() * 0.90f;

        float topBaseX = torn.top.getTranslationX();
        float topBaseY = torn.top.getTranslationY();
        float bottomBaseY = torn.bottom.getTranslationY();

        torn.edge.setPivotX(0f);
        torn.edge.setPivotY(torn.edge.getHeight() * 0.5f);

        ValueAnimator tearProgress = ValueAnimator.ofFloat(0f, 1f);
        tearProgress.setDuration(620);
        tearProgress.setInterpolator(new PathInterpolator(0.22f, 0.9f, 0.2f, 1f));
        tearProgress.addUpdateListener(anim -> {
            float p = (float) anim.getAnimatedValue();
            float eased = 1f - (1f - p) * (1f - p);
            float wobble = (float) Math.sin(p * Math.PI) * 0.6f;

            torn.edge.setAlpha(Math.min(1f, p * 1.2f));
            torn.edge.setScaleX(Math.max(0.02f, p));

            torn.top.setTranslationX(topBaseX + slideRight * eased);
            torn.top.setTranslationY(topBaseY - (tearGap * 0.55f * eased));
            torn.top.setRotation(-12f * eased + wobble);
            torn.top.setScaleY(1f - 0.01f * (1f - p));

            torn.bottom.setTranslationY(bottomBaseY + tearGap * eased);
            torn.bottom.setRotation(3.2f * eased - wobble * 0.35f);

            if (innerGlow != null) {
                innerGlow.setAlpha(0.28f * eased);
            }
        });

        ObjectAnimator snap = ObjectAnimator.ofFloat(torn.top, View.ROTATION, -12f, -9f, -10.5f);
        snap.setDuration(140);
        snap.setInterpolator(new OvershootInterpolator(1.1f));

        AnimatorSet tear = new AnimatorSet();
        tear.playTogether(tearProgress);
        tear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (tearHintLine != null) tearHintLine.animate().alpha(0f).setDuration(160).start();
                if (tearHintSwipe != null) tearHintSwipe.animate().alpha(0f).setDuration(160).start();
                if (hintAnimation != null) hintAnimation.cancel();
            }
        });

        AnimatorSet tearWithSnap = new AnimatorSet();
        tearWithSnap.playSequentially(tear, snap);

        // --- Stage 3: cartas "aparecen desde el interior" (peek -> pull out)
        AnimatorSet cardsReveal = buildCardsReveal(cards, cardViews, cardsContainer, starBurstView, startY, endY);
        cardsReveal.setStartDelay(160); // deja que se note el rasgado antes de que asomen

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

        ObjectAnimator glowFade = null;
        if (innerGlow != null) {
            glowFade = ObjectAnimator.ofFloat(innerGlow, View.ALPHA, innerGlow.getAlpha(), 0f);
        }

        AnimatorSet packDrop = new AnimatorSet();
        if (glowFade != null) {
            packDrop.playTogether(topDropY, bottomDropY, edgeDropY, topFade, bottomFade, edgeFade, topScale, bottomScale, glowFade);
        } else {
            packDrop.playTogether(topDropY, bottomDropY, edgeDropY, topFade, bottomFade, edgeFade, topScale, bottomScale);
        }
        packDrop.setDuration(420);
        packDrop.setInterpolator(new AccelerateDecelerateInterpolator());
        packDrop.setStartDelay(packDropDelay);

        // --- Orquestación completa
        AnimatorSet full = new AnimatorSet();
        full.playSequentially(tension, tearWithSnap);

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

                for (int i = 0; i < cardViews.size(); i++) {
                    Card c = cards.get(i);
                    int color = getStarBurstColor(c);
                    startTrailEmitter(cardViews.get(i), starBurstView, color, density);
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
        bottom.setElevation(dpToPx(context, 10));

        ImageView edge = new ImageView(context);
        edge.setImageBitmap(torn.edge);
        edge.setScaleType(ImageView.ScaleType.FIT_CENTER);
        edge.setLayoutParams(originalPack.getLayoutParams());
        edge.setTranslationX(originalPack.getTranslationX());
        edge.setTranslationY(originalPack.getTranslationY());
        edge.setScaleX(originalPack.getScaleX());
        edge.setScaleY(originalPack.getScaleY());
        edge.setAlpha(0f); // aparece al rasgar
        edge.setElevation(dpToPx(context, 12));

        ImageView top = new ImageView(context);
        top.setImageBitmap(torn.top);
        top.setScaleType(ImageView.ScaleType.FIT_CENTER);
        top.setLayoutParams(originalPack.getLayoutParams());
        top.setTranslationX(originalPack.getTranslationX());
        top.setTranslationY(originalPack.getTranslationY());
        top.setScaleX(originalPack.getScaleX());
        top.setScaleY(originalPack.getScaleY());
        top.setAlpha(1f);
        top.setElevation(dpToPx(context, 18));

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

        ce.drawPath(tearLine, strokeDark);
        ce.drawPath(tearLine, strokeLight);

        Paint fibers = new Paint(Paint.ANTI_ALIAS_FLAG);
        fibers.setStyle(Paint.Style.FILL);
        fibers.setColor(0x88FFFFFF);
        float fiberRadius = edgeThicknessPx * 0.18f;
        for (int i = 2; i < points - 2; i++) {
            if (rng.nextFloat() > 0.35f) continue;
            float fx = xs[i] + (rng.nextFloat() - 0.5f) * edgeThicknessPx;
            float fy = ys[i] + (rng.nextFloat() - 0.5f) * edgeThicknessPx;
            ce.drawCircle(fx, fy, fiberRadius, fibers);
        }

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
