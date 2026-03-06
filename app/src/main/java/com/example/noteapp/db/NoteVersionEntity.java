package com.example.noteapp.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * 笔记版本历史实体类
 */
@Entity(
    tableName = "note_versions",
    indices = {@Index(value = {"noteId"})},
    foreignKeys = @ForeignKey(
        entity = NoteEntity.class,
        parentColumns = "id",
        childColumns = "noteId",
        onDelete = ForeignKey.CASCADE
    )
)
public class NoteVersionEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int noteId;
    private String title;
    private String content;
    private long timestamp;

    public NoteVersionEntity(int noteId, String title, String content) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
