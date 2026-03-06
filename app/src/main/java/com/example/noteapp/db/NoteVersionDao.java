package com.example.noteapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface NoteVersionDao {
    @Insert
    long insert(NoteVersionEntity version);

    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY timestamp DESC LIMIT 3")
    List<NoteVersionEntity> getVersionsForNote(int noteId);

    @Query("DELETE FROM note_versions WHERE noteId = :noteId AND id NOT IN (SELECT id FROM note_versions WHERE noteId = :noteId ORDER BY timestamp DESC LIMIT 3)")
    void trimVersions(int noteId);

    @Query("SELECT * FROM note_versions ORDER BY timestamp DESC")
    List<NoteVersionEntity> getAllSync();
}
