package com.daille.zonadepescajava_app.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daille.zonadepescajava_app.R;

public final class CardFullscreenDialog {
    private CardFullscreenDialog() {
    }

    public static void show(Context context, Bitmap image) {
        if (image == null) {
            return;
        }
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fullscreen_card);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView imageView = dialog.findViewById(R.id.fullscreenImage);
        imageView.setImageBitmap(image);
        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
