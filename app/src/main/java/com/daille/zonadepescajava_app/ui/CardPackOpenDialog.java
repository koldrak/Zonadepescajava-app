package com.daille.zonadepescajava_app.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;

import java.util.ArrayList;
import java.util.List;

public final class CardPackOpenDialog {
    private CardPackOpenDialog() {
    }

    public static void show(Context context, Bitmap packImage, List<Card> cards, CardImageResolver resolver) {
        if (context == null || cards == null || cards.isEmpty() || resolver == null) {
            return;
        }
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_card_pack);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView packImageView = dialog.findViewById(R.id.cardPackImage);
        Bitmap packBitmap = packImage != null ? packImage : resolver.getCardBack();
        packImageView.setImageBitmap(packBitmap);

        FrameLayout cardsContainer = dialog.findViewById(R.id.cardPackCards);
        List<ImageView> cardViews = new ArrayList<>();

        int cardWidth = dpToPx(context, 180);
        int cardHeight = dpToPx(context, 260);
        int offsetX = dpToPx(context, 120);
        int startY = dpToPx(context, 200);
        int endY = dpToPx(context, -180);

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Bitmap cardBitmap = resolver.getImageFor(card, true);
            Card cardFinal = card;
            Bitmap cardBitmapFinal = cardBitmap;
            ImageView cardView = new ImageView(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardWidth, cardHeight);
            params.gravity = android.view.Gravity.CENTER;
            cardView.setLayoutParams(params);
            cardView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cardView.setImageBitmap(cardBitmap != null ? cardBitmap : resolver.getCardBack());
            cardView.setAlpha(0f);
            cardView.setTranslationY(startY);
            cardView.setTranslationX((i - 1) * offsetX);
            cardView.setScaleX(0.85f);
            cardView.setScaleY(0.85f);
            cardsContainer.addView(cardView);
            cardViews.add(cardView);

            cardView.setOnClickListener(v -> {
                v.setEnabled(false);
                String overlay = context.getString(R.string.card_pack_reward_detail, cardFinal.getName());
                Bitmap detailBitmap = cardBitmapFinal != null ? cardBitmapFinal : resolver.getCardBack();
                CardFullscreenDialog.show(context, detailBitmap, overlay, () -> {
                    cardsContainer.removeView(cardView);
                    if (cardsContainer.getChildCount() == 0) {
                        dialog.dismiss();
                    }
                });
            });
        }

        dialog.setOnShowListener(dlg -> playOpeningAnimation(packImageView, cardViews, startY, endY));
        dialog.setOnDismissListener((DialogInterface dlg) -> {
            for (ImageView cardView : cardViews) {
                cardView.animate().cancel();
            }
            packImageView.animate().cancel();
        });
        dialog.show();
    }

    private static void playOpeningAnimation(ImageView packImageView, List<ImageView> cardViews, int startY, int endY) {
        if (packImageView == null || cardViews == null) {
            return;
        }
        packImageView.setPivotY(0f);
        ObjectAnimator packScale = ObjectAnimator.ofFloat(packImageView, View.SCALE_Y, 1f, 0.9f);
        packScale.setDuration(350);

        List<Animator> animations = new ArrayList<>();
        animations.add(packScale);
        int delayBase = 200;
        for (int i = 0; i < cardViews.size(); i++) {
            ImageView cardView = cardViews.get(i);
            cardView.setTranslationY(startY);
            ObjectAnimator translate = ObjectAnimator.ofFloat(cardView, View.TRANSLATION_Y, startY, endY);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, View.ALPHA, 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, View.SCALE_X, 0.85f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, View.SCALE_Y, 0.85f, 1f);
            AnimatorSet cardSet = new AnimatorSet();
            cardSet.playTogether(translate, alpha, scaleX, scaleY);
            cardSet.setDuration(500);
            cardSet.setStartDelay(delayBase + (i * 120L));
            animations.add(cardSet);
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animations);
        set.start();
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }
}
