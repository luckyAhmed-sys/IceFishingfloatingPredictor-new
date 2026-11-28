package com.icefishing.autodetection;

import android.media.Image;

import java.nio.ByteBuffer;

public class FrameProcessor {

    public static final int GRID_WIDTH = 32;
    public static final int GRID_HEIGHT = 32;

    public byte[] extractRgbGrid(Image image) {

        if (image == null) {
            return null;
        }

        Image.Plane[] planes = image.getPlanes();

        if (planes == null || planes.length == 0) {
            return null;
        }

        Image.Plane plane = planes[0];

        ByteBuffer buffer = plane.getBuffer().duplicate();

        int pixelStride = plane.getPixelStride();
        int rowStride = plane.getRowStride();

        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= 0 || height <= 0 ||
            pixelStride < 3 || rowStride <= 0) {
            return null;
        }

        int basePosition = buffer.position();

        byte[] rgb = new byte[
            GRID_WIDTH * GRID_HEIGHT * 3
        ];

        for (int gy = 0; gy < GRID_HEIGHT; gy++) {

            int y = Math.min(
                height - 1,
                gy * height / GRID_HEIGHT
            );

            for (int gx = 0; gx < GRID_WIDTH; gx++) {

                int x = Math.min(
                    width - 1,
                    gx * width / GRID_WIDTH
                );

                int sourceIndex =
                    basePosition + y * rowStride + x * pixelStride;

                if (sourceIndex < 0 ||
                    sourceIndex + 2 >= buffer.limit()) {
                    return null;
                }

                int targetIndex =
                    (gy * GRID_WIDTH + gx) * 3;

                rgb[targetIndex] =
                    buffer.get(sourceIndex);

                rgb[targetIndex + 1] =
                    buffer.get(sourceIndex + 1);

                rgb[targetIndex + 2] =
                    buffer.get(sourceIndex + 2);
            }
        }

        return rgb;
    }

    public float calculateFrameDifference(byte[] previous, byte[] current) {
        if (previous == null || current == null ||
            previous.length != current.length || current.length == 0) {
            return 100.0f;
        }

        long totalDifference = 0;

        for (int i = 0; i < current.length; i++) {
            totalDifference += Math.abs(
                (previous[i] & 0xFF) - (current[i] & 0xFF)
            );
        }

        return (float) totalDifference / current.length;
    }

    public boolean isFrameStable(byte[] previous, byte[] current) {
        return calculateFrameDifference(previous, current) <= 8.0f;
    }


    public boolean hasSignificantChange(byte[] previous, byte[] current) {
        if (previous == null || current == null ||
            previous.length != current.length || current.length == 0) {
            return false;
        }

        return calculateFrameDifference(previous, current) >= 12.0f;
    }
}
