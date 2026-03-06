package com.example.noteapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

/**
 * 图片拾取工具类 - 支持相机拍照和相册选择
 */
public class ImagePickerUtil {
    
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private OnImagePickedListener listener;
    
    public interface OnImagePickedListener {
        void onImagePicked(String imagePath);
        void onImagePickedFromCamera(File file);
        void onImagePickedFromGallery(Uri uri);
        void onError(String error);
    }
    
    /**
     * 初始化图片选择器（在Activity中调用）
     */
    public ImagePickerUtil(AppCompatActivity activity, OnImagePickedListener listener) {
        this.listener = listener;
        
        // 相机启动器
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && listener != null) {
                        listener.onImagePickedFromCamera(ImageUtil.FILE_BEING_CAPTURED);
                    } else if (listener != null) {
                        listener.onError("相机拍照失败");
                    }
                }
        );
        
        // 相册启动器
        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && listener != null) {
                        String imagePath = getImagePathFromUri(activity, uri);
                        if (imagePath != null) {
                            listener.onImagePicked(imagePath);
                            listener.onImagePickedFromGallery(uri);
                        } else {
                            listener.onError("无法获取图片路径");
                        }
                    }
                }
        );
    }
    
    /**
     * 打开相机拍照
     */
    public void openCamera(androidx.appcompat.app.AppCompatActivity activity) {
        try {
            File photoFile = ImageUtil.createImageFile(activity);
            // 保存文件引用，供回调使用
            ImageUtil.FILE_BEING_CAPTURED = photoFile;
            
            Uri photoUri = (Uri) ImageUtil.getImageUri(activity, photoFile);
            cameraLauncher.launch(photoUri);
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("无法打开相机: " + e.getMessage());
            }
        }
    }
    
    /**
     * 打开相册选择
     */
    public void openGallery() {
        galleryLauncher.launch("image/*");
    }
    
    /**
     * 从URI获取图片路径
     */
    @SuppressWarnings("deprecation")
    private String getImagePathFromUri(Activity activity, Uri uri) {
        try {
            // 如果是文件URI
            if ("file".equals(uri.getScheme())) {
                return uri.getPath();
            }
            
            // 如果是content URI
            if ("content".equals(uri.getScheme())) {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
                
                if (cursor != null) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(index);
                    cursor.close();
                    
                    // 复制到应用目录
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), uri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                    }
                    return ImageUtil.saveBitmapToFile(activity, bitmap).getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 从Intent获取图片（用于onActivityResult方式）
     */
    @SuppressWarnings("deprecation")
    public static String getImageFromIntent(Intent data, Activity activity) {
        try {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), uri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                activity.getContentResolver(), uri);
                    }
                    return ImageUtil.saveBitmapToFile(activity, bitmap).getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取相机拍摄的照片
     */
    @SuppressWarnings("deprecation")
    public static String getCameraPhoto(Intent data, Activity activity) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bitmap = data.getParcelableExtra("data", Bitmap.class);
            } else {
                bitmap = (Bitmap) data.getParcelableExtra("data");
            }
            if (bitmap != null) {
                return ImageUtil.saveBitmapToFile(activity, bitmap).getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
