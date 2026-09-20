package com.panagiotisoikonomou.nudgebite.logging;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Only files directly inside our own meals directory are ours to delete; anything else is left alone. */
public final class MealPhotoFile {

    private MealPhotoFile() {
    }

    /** Returns the saved file's path, or null when it could not be written. */
    @Nullable
    public static String save(File filesDir, byte[] jpegBytes) {
        File mealsDir = mealsDirIn(filesDir);
        if (!mealsDir.exists() && !mealsDir.mkdirs()) {
            return null;
        }
        File photoFile = new File(mealsDir, "meal_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(photoFile)) {
            out.write(jpegBytes);
            return photoFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    public static void deleteIfSavedByApp(File filesDir, @Nullable String photoPath) {
        if (isDeletable(photoPath, mealsDirIn(filesDir).getAbsolutePath()) && !new File(photoPath).delete()) {
            Log.w("NudgeBiteMealEdit", "Meal photo could not be deleted: " + photoPath);
        }
    }

    private static File mealsDirIn(File filesDir) {
        return new File(filesDir, "meals");
    }

    public static boolean isDeletable(String photoUri, String mealsDirPath) {
        if (photoUri == null || photoUri.isEmpty() || mealsDirPath == null || mealsDirPath.isEmpty()) {
            return false;
        }
        if (photoUri.contains("://")) {
            return false;
        }
        if (photoUri.contains("..")) {
            return false;
        }
        String prefix = mealsDirPath.endsWith(File.separator) ? mealsDirPath : mealsDirPath + File.separator;
        if (!photoUri.startsWith(prefix)) {
            return false;
        }
        return photoUri.indexOf(File.separatorChar, prefix.length()) < 0;
    }
}
