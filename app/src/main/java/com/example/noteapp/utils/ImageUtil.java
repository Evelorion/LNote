package com.example.noteapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片处理工具类
 * 处理图片的保存、压缩和加载
 */
public class ImageUtil {
    
    private static final String IMAGE_DIR = "NoteApp-Images";
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    private static final int COMPRESS_QUALITY = 85;
    
    // 用于相机拍照时存储临时文件
    public static File FILE_BEING_CAPTURED = null;
    
    /**
     * 获取图片目录
     */
    public static File getImageDirectory(Context context) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    /**
     * 创建新图片文件
     */
    public static File createImageFile(Context context) throws Exception {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(new Date());
        File storageDir = getImageDirectory(context);
        return new File(storageDir, "IMG_" + timestamp + ".jpg");
    }
    
    /**
     * 获取图片的File Provider URI
     */
    public static Object getImageUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );
    }
    
    /**
     * 保存Bitmap到文件
     */
    public static File saveBitmapToFile(Context context, Bitmap bitmap) throws Exception {
        File file = createImageFile(context);
        
        // 压缩位图
        Bitmap compressed = compressBitmap(bitmap);
        
        FileOutputStream fos = new FileOutputStream(file);
        compressed.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
        fos.close();
        
        if (compressed != bitmap) {
            compressed.recycle();
        }
        
        return file;
    }
    
    /**
     * 压缩Bitmap
     */
    public static Bitmap compressBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() <= MAX_IMAGE_WIDTH && bitmap.getHeight() <= MAX_IMAGE_HEIGHT) {
            return bitmap;
        }
        
        float ratio = Math.min(
                (float) MAX_IMAGE_WIDTH / bitmap.getWidth(),
                (float) MAX_IMAGE_HEIGHT / bitmap.getHeight()
        );
        
        int newWidth = (int) (bitmap.getWidth() * ratio);
        int newHeight = (int) (bitmap.getHeight() * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * 加载Bitmap（带压缩）
     */
    public static Bitmap loadBitmap(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        
        int scale = 1;
        while (options.outWidth / scale > MAX_IMAGE_WIDTH || options.outHeight / scale > MAX_IMAGE_HEIGHT) {
            scale *= 2;
        }
        
        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        
        return BitmapFactory.decodeFile(imagePath, options);
    }
    
    /**
     * 删除图片文件
     */
    public static boolean deleteImage(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * 获取图片文件相对应用的相对路径
     */
    public static String getImageRelativePath(Context context, File file) {
        File imageDir = getImageDirectory(context);
        if (file.getAbsolutePath().startsWith(imageDir.getAbsolutePath())) {
            return file.getName();
        }
        return null;
    }
    
    /**
     * 从相对路径获取完整路径的图片文件
     */
    public static File getImageFileFromRelativePath(Context context, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        File file = new File(getImageDirectory(context), relativePath);
        return file.exists() ? file : null;
    }
}
