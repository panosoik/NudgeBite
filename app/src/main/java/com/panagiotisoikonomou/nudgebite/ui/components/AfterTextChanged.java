package com.panagiotisoikonomou.nudgebite.ui.components;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Consumer;

/** A TextWatcher for the usual case of only wanting the new text. */
public final class AfterTextChanged implements TextWatcher {

    private final Consumer<String> onTextChanged;

    public AfterTextChanged(Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable text) {
        onTextChanged.accept(text.toString());
    }
}
