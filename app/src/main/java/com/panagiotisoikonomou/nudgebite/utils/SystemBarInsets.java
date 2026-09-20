package com.panagiotisoikonomou.nudgebite.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Pads an edge-to-edge screen so its content stays clear of the status and navigation bars. */
public final class SystemBarInsets {

    private SystemBarInsets() {
    }

    public static void padForSystemBars(View root) {
        padFor(root, WindowInsetsCompat.Type.systemBars());
    }

    /** Pads for the keyboard too: edge-to-edge stops the window resizing for the IME. */
    public static void padForSystemBarsAndKeyboard(View root) {
        padFor(root, WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
    }

    private static void padFor(View root, int insetTypes) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(insetTypes);
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }
}
