package com.daille.zonadepescajava_app.ui;

import android.content.Context;

import androidx.annotation.StringRes;

import com.daille.zonadepescajava_app.model.GameTextProvider;

public class AndroidGameTextProvider implements GameTextProvider {
    private final Context context;

    public AndroidGameTextProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getString(@StringRes int resId, Object... formatArgs) {
        if (resId == 0) {
            return "";
        }
        if (formatArgs == null || formatArgs.length == 0) {
            return context.getString(resId);
        }
        return context.getString(resId, formatArgs);
    }
}
