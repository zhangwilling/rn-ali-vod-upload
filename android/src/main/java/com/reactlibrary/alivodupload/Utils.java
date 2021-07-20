package com.reactlibrary.alivodupload;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class Utils {
    final static String DIR_NAME = "vodUploadTemp";
    public static String getFirstFramePath(String videoPath, Context context) {
        final String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + DIR_NAME + "/";
        String fileName = UUID.randomUUID().toString() + ".JPEG";
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(videoPath);
        Bitmap firstFrame = mmr.getFrameAtTime();
        mmr.release();
        saveBitmap2file(context, firstFrame, fileName);
        return SD_PATH + fileName;
    }

    private static void saveBitmap2file(Context context, Bitmap bmp, String fileName) {

        String savePath;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory().getPath() + "/" + DIR_NAME + "/";
        } else {
            Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        File filePic = new File(savePath + fileName);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}