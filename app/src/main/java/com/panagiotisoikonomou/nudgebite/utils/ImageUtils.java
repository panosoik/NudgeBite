package com.panagiotisoikonomou.nudgebite.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class ImageUtils {

    private ImageUtils() {
    }

    public static byte[] downscaleToJpeg(
            Context context,
            Uri imageUri,
            int maxDimension,
            int jpegQuality
    ) throws IOException {

        ImageDecoder.Source source = ImageDecoder.createSource(
                context.getContentResolver(),
                imageUri
        );

        Bitmap bitmap = ImageDecoder.decodeBitmap(
                source,
                (decoder, imageInfo, source1) -> {
                    Size originalSize = imageInfo.getSize();

                    int originalWidth = originalSize.getWidth();
                    int originalHeight = originalSize.getHeight();
                    int longestSide = Math.max(originalWidth, originalHeight);

                    if (longestSide > maxDimension) {
                        float scale = maxDimension / (float) longestSide;

                        int targetWidth = Math.round(originalWidth * scale);
                        int targetHeight = Math.round(originalHeight * scale);

                        decoder.setTargetSize(targetWidth, targetHeight);
                    }

                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                }
        );

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            boolean compressed = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    jpegQuality,
                    output
            );

            if (!compressed) {
                throw new IOException("Failed to compress image");
            }

            return output.toByteArray();
        } finally {
            bitmap.recycle();
        }
    }
}
