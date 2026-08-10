package com.daille.zonadepescajava_app.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import com.daille.zonadepescajava_app.R;
import com.daille.zonadepescajava_app.model.Card;
import com.daille.zonadepescajava_app.model.CardType;
import com.daille.zonadepescajava_app.model.Condition;
import com.daille.zonadepescajava_app.model.ConditionInfo;
import com.daille.zonadepescajava_app.model.GameTextProvider;
import com.daille.zonadepescajava_app.model.GameTextProvider;

public final class CardFullscreenDialog {
    public interface SellAction {
        int onSell();
    }

    private CardFullscreenDialog() {
    }

    public static void show(Context context, Bitmap image) {
        show(context, image, null, null, null, null, null, null);
    }

    public static void show(Context context, Bitmap image, String overlayText, Runnable onDismiss) {
        show(context, image, null, overlayText, onDismiss, null, null, null);
    }

    public static void show(Context context, Bitmap image, Card card) {
        show(context, image, card, null, null, null, null, null);
    }

    public static void showWithSellOption(Context context, Bitmap image, Card card, String overlayText,
                                          int ownedCopies, int sellPrice, SellAction sellAction,
                                          Runnable onDismiss) {
        show(context, image, card, overlayText, onDismiss, ownedCopies, sellPrice, sellAction);
    }

    public static void show(Context context, Bitmap image, Card card, String overlayText, Runnable onDismiss) {
        show(context, image, card, overlayText, onDismiss, null, null, null);
    }

    private static void show(Context context, Bitmap image, Card card, String overlayText, Runnable onDismiss,
                             Integer ownedCopies, Integer sellPrice, SellAction sellAction) {
        if (image == null) {
            return;
        }
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fullscreen_card);

        ImageView imageView = dialog.findViewById(R.id.fullscreenImage);
        if (imageView == null) {
            return;
        }
        imageView.setImageBitmap(image);
        imageView.setOnClickListener(v -> dialog.dismiss());

        TextView overlay = dialog.findViewById(R.id.fullscreenOverlay);
        if (overlay != null) {
            overlay.setOnClickListener(v -> dialog.dismiss());
            if (overlayText == null || overlayText.isEmpty()) {
                overlay.setVisibility(android.view.View.GONE);
            } else {
                overlay.setText(overlayText);
                overlay.setVisibility(android.view.View.VISIBLE);
            }
        }

        TextView topInfo = dialog.findViewById(R.id.fullscreenTopInfo);
        TextView bottomInfo = dialog.findViewById(R.id.fullscreenBottomInfo);
        TextView copiesInfo = dialog.findViewById(R.id.fullscreenCopies);
        MaterialButton sellButton = dialog.findViewById(R.id.fullscreenSellButton);
        if (card == null) {
            if (topInfo != null) {
                topInfo.setVisibility(android.view.View.GONE);
            }
            if (bottomInfo != null) {
                bottomInfo.setVisibility(android.view.View.GONE);
            }
            if (copiesInfo != null) {
                copiesInfo.setVisibility(android.view.View.GONE);
            }
            if (sellButton != null) {
                sellButton.setVisibility(android.view.View.GONE);
            }
        } else {
            GameTextProvider textProvider = new AndroidGameTextProvider(context);
            if (topInfo != null) {
                String conditionText = formatCondition(context, card.getCondition());
                SpannableStringBuilder topText = new SpannableStringBuilder();
                appendLabeledText(topText, context.getString(R.string.card_detail_name_label), card.getName(textProvider));
                topText.append("\n");
                appendLabeledText(topText, context.getString(R.string.card_detail_condition_label), conditionText);
                topText.append("\n");
                appendLabeledText(topText, context.getString(R.string.card_detail_points_label),
                        String.valueOf(card.getPoints()));
                topInfo.setText(topText);
                topInfo.setVisibility(android.view.View.VISIBLE);
                topInfo.setOnClickListener(v -> dialog.dismiss());
            }
            if (bottomInfo != null) {
                String abilityText = buildAbilityText(context, card);
                String typeText = formatType(context, card.getType());
                SpannableStringBuilder bottomText = new SpannableStringBuilder();
                appendLabeledText(bottomText, context.getString(R.string.card_detail_ability_label), abilityText);
                bottomText.append("\n");
                appendLabeledText(bottomText, context.getString(R.string.card_detail_type_label), typeText);
                bottomInfo.setText(bottomText);
                bottomInfo.setVisibility(android.view.View.VISIBLE);
                bottomInfo.setOnClickListener(v -> dialog.dismiss());
            }
        }

        if (copiesInfo != null) {
            if (ownedCopies != null) {
                copiesInfo.setText(context.getString(R.string.card_detail_copies_format, ownedCopies));
                copiesInfo.setVisibility(android.view.View.VISIBLE);
                copiesInfo.setOnClickListener(v -> dialog.dismiss());
            } else {
                copiesInfo.setVisibility(android.view.View.GONE);
            }
        }

        if (sellButton != null) {
            if (sellAction != null && ownedCopies != null && sellPrice != null) {
                sellButton.setText(context.getString(R.string.card_detail_sell_button_format, sellPrice));
                sellButton.setVisibility(android.view.View.VISIBLE);
                sellButton.setEnabled(ownedCopies > 0);
                sellButton.setAlpha(ownedCopies > 0 ? 1f : 0.6f);
                sellButton.setOnClickListener(v -> {
                    int updatedCount = sellAction.onSell();
                    if (updatedCount >= 0) {
                        if (copiesInfo != null) {
                            copiesInfo.setText(context.getString(
                                    R.string.card_detail_copies_format, updatedCount));
                        }
                        boolean canSell = updatedCount > 0;
                        sellButton.setEnabled(canSell);
                        sellButton.setAlpha(canSell ? 1f : 0.6f);
                    }
                });
            } else {
                sellButton.setVisibility(android.view.View.GONE);
            }
        }

        dialog.setOnDismissListener((DialogInterface dlg) -> {
            if (onDismiss != null) {
                onDismiss.run();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private static String buildAbilityText(Context context, Card card) {
        if (card == null) {
            return context.getString(R.string.card_detail_ability_default);
        }
        GameTextProvider provider = (resId, formatArgs) -> context.getString(resId, formatArgs);
        String detail = card.getOnCatch(provider);
        if (detail == null || detail.isEmpty()) {
            detail = card.getBonus(provider);
        }
        if (detail == null || detail.isEmpty()) {
            detail = card.getOnFail(provider);
        }
        return detail == null || detail.isEmpty()
                ? context.getString(R.string.card_detail_ability_default)
                : detail;
    }

    private static String formatType(Context context, CardType type) {
        if (type == null) {
            return context.getString(R.string.card_detail_type_unknown);
        }
        switch (type) {
            case CRUSTACEO:
                return context.getString(R.string.card_type_orange);
            case PEZ:
                return context.getString(R.string.card_type_celeste);
            case PEZ_GRANDE:
                return context.getString(R.string.card_type_green);
            case OBJETO:
                return context.getString(R.string.card_type_black);
            default:
                return context.getString(R.string.card_detail_type_unknown);
        }
    }

    private static String formatCondition(Context context, Condition condition) {
        if (condition instanceof ConditionInfo) {
            ConditionInfo info = (ConditionInfo) condition;
            if (info.getDescriptionResId() != null) {
                Object[] args = info.getDescriptionArgs();
                return args == null || args.length == 0
                        ? context.getString(info.getDescriptionResId())
                        : context.getString(info.getDescriptionResId(), args);
            }
            switch (info.getType()) {
                case SUM_RANGE:
                    return context.getString(R.string.card_condition_sum_range_format, info.getFirst(), info.getSecond());
                case SUM_EXACT:
                    return context.getString(R.string.card_condition_sum_exact_format, info.getFirst());
                case SUM_AT_LEAST:
                    return context.getString(R.string.card_condition_sum_at_least_format, info.getFirst());
                case SUM_GREATER_THAN:
                    return context.getString(R.string.card_condition_sum_greater_than_format, info.getFirst());
                case SUM_LESS_OR_EQUAL:
                    return context.getString(R.string.card_condition_sum_less_or_equal_format, info.getFirst());
                case SUM_LESS_THAN:
                    return context.getString(R.string.card_condition_sum_less_than_format, info.getFirst());
                case DIFFERENCE_AT_LEAST:
                    return context.getString(R.string.card_condition_difference_at_least_format, info.getFirst());
                case CUSTOM:
                    break;
                default:
                    break;
            }
        }
        return context.getString(R.string.card_condition_special);
    }

    private static void appendLabeledText(SpannableStringBuilder builder, String label, String value) {
        int start = builder.length();
        builder.append(label);
        int end = builder.length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ").append(value);
    }
}
