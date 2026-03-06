package com.example.noteapp.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 笔记数据库（包含笔记、时间胶囊和版本历史）
 */
@Database(entities = {NoteEntity.class, TimeCapsuleEntity.class, NoteVersionEntity.class}, version = 5, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    
    public static final String DATABASE_NAME = "note_database";
    private static volatile NoteDatabase INSTANCE;
    
    public abstract NoteDao noteDao();
    public abstract TimeCapsuleDao timeCapsuleDao();
    public abstract NoteVersionDao noteVersionDao();
    
    public static NoteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            DATABASE_NAME
                    ).fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
