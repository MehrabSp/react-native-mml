package com.mml;

import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * MRB
 * 8/16/23
 * Nesfe Shab. Yadegara
 * Add turbo module on 8/24/23
 */

public class FileManager {
    public static void FileSaver(
            File file,
            Bitmap bmp
    ) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        } catch (IOException e) {
            throw new Exception("Failed to save image!");
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
    }
}