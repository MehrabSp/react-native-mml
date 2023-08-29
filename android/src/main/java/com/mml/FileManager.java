package com.mml;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.mml.GetMusicFilesModule.quality;

public class FileManager {

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void FileSaver(
            File file,
            Bitmap bmp
    ) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
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