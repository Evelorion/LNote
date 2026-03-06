package com.example.noteapp.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * 笔记实体类 - 扩展支持时间胶囊功能
 */
@Entity(tableName = "notes")
public class NoteEntity implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String content;
    private long timestamp;
    private String tags;           // 标签，多个标签用","分隔
    private String category;       // 分类
    private String color;          // 笔记颜色（iOS风格颜色）
    private String imagePaths;     // 图片路径，多个路径用","分隔
    private String imageLayoutData; // 图片布局数据 (JSON格式：位置、缩放等)
    private boolean isEncrypted;   // 是否加密
    private String encryptedContent; // 加密失败内容（当isEncrypted==true时使用）
    
    // 时间胶囊相关字段
    private boolean isTimeCapsule;  // 是否为时间胶囊
    private long openTimestamp;     // 开启时间戳
    
    public NoteEntity() {
    }
    
    @Ignore
    public NoteEntity(String title, String content) {
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.tags = "";
        this.category = "默认";
        this.color = "#FFFFFF";
        this.imagePaths = "";
        this.imageLayoutData = "";
        this.isEncrypted = false;
        this.encryptedContent = "";
        this.isTimeCapsule = false;
        this.openTimestamp = 0;
    }
    
    // Getters 和 Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTags() {
        return tags == null ? "" : tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getCategory() {
        return category == null ? "默认" : category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getColor() {
        return color == null ? "#FFFFFF" : color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getImagePaths() {
        return imagePaths == null ? "" : imagePaths;
    }
    
    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getImageLayoutData() {
        return imageLayoutData == null ? "" : imageLayoutData;
    }

    public void setImageLayoutData(String imageLayoutData) {
        this.imageLayoutData = imageLayoutData;
    }
    
    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getEncryptedContent() {
        return encryptedContent == null ? "" : encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public boolean isTimeCapsule() {
        return isTimeCapsule;
    }

    public void setTimeCapsule(boolean timeCapsule) {
        isTimeCapsule = timeCapsule;
    }

    public long getOpenTimestamp() {
        return openTimestamp;
    }

    public void setOpenTimestamp(long openTimestamp) {
        this.openTimestamp = openTimestamp;
    }

    /**
     * 判断时间胶囊是否已封存（即当前时间早于开启时间）
     */
    public boolean isSealed() {
        return isTimeCapsule && System.currentTimeMillis() < openTimestamp;
    }

    /**
     * 获取剩余天数
     */
    public long getRemainingDays() {
        if (!isSealed()) return 0;
        long diff = openTimestamp - System.currentTimeMillis();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return;
        String t = tag.trim();
        String current = getTags();
        if (current.isEmpty()) {
            this.tags = t;
        } else {
            String[] existing = current.split(",");
            for (String s : existing) {
                if (s.equals(t)) return;
            }
            this.tags = current + "," + t;
        }
    }
    
    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        if (tag == null || tag.isEmpty()) return;
        String current = getTags();
        if (current.contains("," + tag)) {
            this.tags = current.replace("," + tag, "");
        } else if (current.startsWith(tag + ",")) {
            this.tags = current.replace(tag + ",", "");
        } else if (current.equals(tag)) {
            this.tags = "";
        }
    }
    
    /**
     * 获取标签列表
     */
    public String[] getTagsList() {
        String tags = getTags();
        if (tags.isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
    
    public void addImagePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return;
        String current = getImagePaths();
        if (current.isEmpty()) {
            this.imagePaths = imagePath;
        } else if (!current.contains(imagePath)) {
            this.imagePaths = current + "," + imagePath;
        }
    }
    
    public String[] getImagePathsList() {
        String paths = getImagePaths();
        if (paths.isEmpty()) {
            return new String[0];
        }
        return paths.split(",");
    }
}
