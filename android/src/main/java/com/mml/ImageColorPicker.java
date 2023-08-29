package com.max;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.palette.graphics.Palette; //getColor

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import static com.max.GetMusicFilesModule.fallback;

public class ImageColorPicker extends Application {

    /**
     * pixelSpacing tells how many pixels to skip each pixel.
     * If pixelSpacing > 1: the average color is an estimate, but higher values mean better performance.
     * If pixelSpacing == 1: the average color will be the real average.
     * If pixelSpacing < 1: the method will most likely crash (don't use values below 1).
     * MRB.
     */
    private static int calculateAverageColor(Bitmap bitmap, int pixelSpacing) {
        int segmentWidth = 500;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int numSegments = (int) Math.ceil((double) width / segmentWidth);
        int[] segmentPixels = new int[segmentWidth * height];

        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int pixelCount = 0;

        for (int i = 0; i < numSegments; i++) {
            int xStart = i * segmentWidth;
            int xEnd = Math.min(width, (i + 1) * segmentWidth);

            bitmap.getPixels(segmentPixels, 0, segmentWidth, xStart, 0, xEnd - xStart, height);

            for (int index = 0; index < segmentPixels.length; index += pixelSpacing) {
                redSum += Color.red(segmentPixels[index]);
                greenSum += Color.green(segmentPixels[index]);
                blueSum += Color.blue(segmentPixels[index]);
                pixelCount++;
            }
        }
        if (pixelCount == 0) {
            return Color.BLACK;
        } else {
            int red = redSum / pixelCount;
            int green = greenSum / pixelCount;
            int blue = blueSum / pixelCount;
            return Color.rgb(red, green, blue);
        }
    }

    public static String parseFallbackColor(String hex) throws Exception {
        if (!hex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))
            throw new Exception("Invalid fallback hex color. Must be in the format #ffffff or #fff");
        if (hex.length() == 7) return hex;
        return "#" + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2) + hex.charAt(3) + hex.charAt(3);
    }

    public static String getHex(int rgb) {
        return String.format("#%06X", 0xFFFFFF & rgb);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static WritableMap getColor(
            Number pixelSpacing,
            Bitmap songImage
    ) throws Exception {
        WritableMap result = new WritableNativeMap();
        try {
            String fallbackColor = parseFallbackColor(fallback);
            int fallbackColorInt = Color.parseColor(fallbackColor);
            if (songImage == null) throw new Exception("Failed to get image");
            Palette.Builder paletteBuilder = new Palette.Builder(songImage);
            result.putString("average", getHex(calculateAverageColor(songImage, (Integer) pixelSpacing)));
            try {
                Palette palette = paletteBuilder.generate();
                result.putString("dominant", getHex(palette.getDominantColor(fallbackColorInt)));
                result.putString("vibrant", getHex(palette.getVibrantColor(fallbackColorInt)));
                result.putString("darkVibrant", getHex(palette.getDarkVibrantColor(fallbackColorInt)));
                result.putString("lightVibrant", getHex(palette.getLightVibrantColor(fallbackColorInt)));
                result.putString("muted", getHex(palette.getMutedColor(fallbackColorInt)));
                result.putString("darkMuted", getHex(palette.getDarkMutedColor(fallbackColorInt)));
                result.putString("lightMuted", getHex(palette.getLightMutedColor(fallbackColorInt)));
            } catch (Exception e) {
                result.putString("dominant", fallbackColor);
                result.putString("vibrant", fallbackColor);
                result.putString("darkVibrant", fallbackColor);
                result.putString("lightVibrant", fallbackColor);
                result.putString("muted", fallbackColor);
                result.putString("darkMuted", fallbackColor);
                result.putString("lightMuted", fallbackColor);
            }
        } catch (Exception e) {
            throw new Exception("Failed to get image");
        }
        return result;
    }
}