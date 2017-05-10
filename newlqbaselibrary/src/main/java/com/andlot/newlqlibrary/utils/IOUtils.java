package com.andlot.newlqlibrary.utils;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class IOUtils {

    public static final String BAK = ".bak";
    public static final String TEM = ".tem";

    public static final String JPG = ".jpg";
    public static final String PNG = ".png";
    public static final String WEBP = ".webp";

    public static final String APK = ".apk";

    public static final String SDCardPath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

    public static final String PROJECT_DIR =
            SDCardPath + UIUtils.getContext().getPackageName() + File.separator;

    private IOUtils() {}

    public static void close(Closeable... stream) {
        try {
            if (stream != null){
                for (Closeable colsed : stream){
                    if (colsed != null) colsed.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface FileType {
        String FILE_TYPE_IMAGE = "image/*";
    }

}
