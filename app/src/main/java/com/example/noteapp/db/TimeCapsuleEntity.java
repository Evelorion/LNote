package com.example.noteapp.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * 时间胶囊实体类
 * 允许用户创建定时发送的笔记
 */
@Entity(
    tableName = "time_capsules",
    indices = {@Index(value = {"noteId"})},
    foreignKeys = @ForeignKey(
        entity = NoteEntity.class,
        parentColumns = "id",
        childColumns = "noteId",
        onDelete = ForeignKey.CASCADE
    )
)
public class TimeCapsuleEntity implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int noteId;                 // 关联的笔记ID
    private String title;               // 胶囊标题
    private String content;             // 胶囊内容
    private long createdTime;           // 创建时间
    private long scheduledTime;         // 计划开启时间
    private long openedTime;            // 实际打开时间（0表示未打开）
    private int status;                 // 0=等待中, 1=已打开, 2=已删除
    private String tags;                // 标签
    private boolean hasNotification;    // 打开时是否显示通知
    private int reminderMinutesBefore;  // 提前多少分钟提醒（0表示不提醒）
    
    public TimeCapsuleEntity() {
    }
    
    @Ignore
    public TimeCapsuleEntity(String title, String content, long scheduledTime) {
        this.title = title;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.createdTime = System.currentTimeMillis();
        this.openedTime = 0;
        this.status = 0; // 等待中
        this.tags = "";
        this.hasNotification = true;
        this.reminderMinutesBefore = 0;
        this.noteId = 0; // 可选关联
    }
    
    // Getters 和 Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getNoteId() {
        return noteId;
    }
    
    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }
    
    public String getTitle() {
        return title == null ? "" : title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content == null ? "" : content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
    
    public long getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public long getOpenedTime() {
        return openedTime;
    }
    
    public void setOpenedTime(long openedTime) {
        this.openedTime = openedTime;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getTags() {
        return tags == null ? "" : tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public boolean isHasNotification() {
        return hasNotification;
    }
    
    public void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
    }
    
    public int getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }
    
    public void setReminderMinutesBefore(int reminderMinutesBefore) {
        this.reminderMinutesBefore = reminderMinutesBefore;
    }
    
    /**
     * 检查胶囊是否应该打开
     */
    public boolean shouldOpen() {
        return status == 0 && System.currentTimeMillis() >= scheduledTime;
    }
    
    /**
     * 标记为已打开
     */
    public void markAsOpened() {
        this.status = 1;
        this.openedTime = System.currentTimeMillis();
    }
    
    /**
     * 获取距离开启的剩余时间（毫秒）
     * 如果已打开或已过期，返回0或负数
     */
    public long getTimeRemaining() {
        return scheduledTime - System.currentTimeMillis();
    }
    
    /**
     * 获取目标开启日期的格式化字符串
     */
    public String getFormattedScheduledTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(scheduledTime));
    }
    
    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag == null || tag.isEmpty()) return;
        String current = getTags();
        if (current.isEmpty()) {
            this.tags = tag;
        } else if (!current.contains(tag)) {
            this.tags = current + "," + tag;
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
}
