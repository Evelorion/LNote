package com.example.noteapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * 笔记数据访问对象
 */
@Dao
public interface NoteDao {
    
    @Insert
    long insert(NoteEntity note);
    
    @Update
    void update(NoteEntity note);
    
    @Delete
    void delete(NoteEntity note);
    
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    LiveData<List<NoteEntity>> getAllNotes();
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<NoteEntity> getNoteById(int noteId);
    
    @Query("DELETE FROM notes")
    void deleteAllNotes();
    
    /**
     * 同步获取所有笔记（供备份使用）
     */
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    List<NoteEntity> getAllNotesSync();
    
    /**
     * 按分类查询笔记
     */
    @Query("SELECT * FROM notes WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<NoteEntity>> getNotesByCategory(String category);
    
    /**
     * 搜索笔记（按标题和内容）
     */
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    LiveData<List<NoteEntity>> searchNotes(String keyword);
}
