package com.example.noteapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.noteapp.db.NoteEntity;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 笔记分享工具类
 * 支持通过各种方式分享笔记内容
 */
public class ShareUtil {
    
    /**
     * 分享笔记为纯文本
     */
    public static void shareNoteAsText(Activity activity, NoteEntity note) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        
        String shareContent = buildShareContent(note);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        
        Activity activityToUse = activity;
        activityToUse.startActivity(Intent.createChooser(shareIntent, "分享笔记"));
    }
    
    /**
     * 分享笔记为文件（附带图片）
     */
    public static void shareNoteAsFile(Activity activity, NoteEntity note) {
        try {
            // 创建临时文件
            File shareFile = createShareFile(activity, note);
            if (shareFile == null) {
                return;
            }
            
            // 获取FileProvider URI
            Uri fileUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    shareFile
            );
            
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            activity.startActivity(Intent.createChooser(shareIntent, "分享笔记"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 分享多张图片
     */
    public static void shareNoteWithImages(Activity activity, NoteEntity note) {
        try {
            String[] imagePaths = note.getImagePathsList();
            if (imagePaths.length == 0) {
                shareNoteAsText(activity, note);
                return;
            }
            
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/*");
            
            // 添加图片URIs
            java.util.ArrayList<Uri> imageUris = new java.util.ArrayList<>();
            for (String imagePath : imagePaths) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Uri imageUri = FileProvider.getUriForFile(
                            activity,
                            activity.getPackageName() + ".fileprovider",
                            imageFile
                    );
                    imageUris.add(imageUri);
                }
            }
            
            if (!imageUris.isEmpty()) {
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, note.getContent());
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                activity.startActivity(Intent.createChooser(shareIntent, "分享笔记"));
            } else {
                shareNoteAsText(activity, note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 通过邮件分享笔记
     */
    public static void shareNoteViaEmail(Activity activity, NoteEntity note) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "笔记：" + note.getTitle());
        emailIntent.putExtra(Intent.EXTRA_TEXT, buildShareContent(note));
        
        activity.startActivity(Intent.createChooser(emailIntent, "通过邮件分享"));
    }
    
    /**
     * 构建分享内容
     */
    private static String buildShareContent(NoteEntity note) {
        StringBuilder content = new StringBuilder();
        
        // 标题
        if (note.getTitle() != null && !note.getTitle().isEmpty()) {
            content.append("【").append(note.getTitle()).append("】\n\n");
        }
        
        // 分类和标签
        if (note.getCategory() != null && !note.getCategory().isEmpty()) {
            content.append("分类：").append(note.getCategory()).append("\n");
        }
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            content.append("标签：").append(note.getTags()).append("\n");
        }
        if (note.getCategory() != null || (note.getTags() != null && !note.getTags().isEmpty())) {
            content.append("\n");
        }
        
        // 内容
        if (note.getContent() != null && !note.getContent().isEmpty()) {
            content.append(note.getContent()).append("\n");
        }
        
        // 时间戳
        content.append("\n---\n");
        content.append("来自笔记本应用\n");
        content.append("时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(note.getTimestamp())));
        
        return content.toString();
    }
    
    /**
     * 创建分享用的文件
     */
    private static File createShareFile(Activity activity, NoteEntity note) {
        try {
            File shareDir = new File(activity.getCacheDir(), "share");
            if (!shareDir.exists()) {
                shareDir.mkdirs();
            }
            
            String filename = "note_" + System.currentTimeMillis() + ".txt";
            File shareFile = new File(shareDir, filename);
            
            FileWriter writer = new FileWriter(shareFile);
            writer.write(buildShareContent(note));
            writer.close();
            
            return shareFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 复制到剪贴板
     */
    public static void copyToClipboard(Activity activity, NoteEntity note) {
        try {
            android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            
            String content = buildShareContent(note);
            android.content.ClipData clip = android.content.ClipData.newPlainText("note", content);
            clipboard.setPrimaryClip(clip);
            
            android.widget.Toast.makeText(activity, "已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
