package com.example.noteapp.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * 时间胶囊数据访问对象
 */
@Dao
public interface TimeCapsuleDao {
    
    /**
     * 插入时间胶囊
     */
    @Insert
    long insert(TimeCapsuleEntity timeCapsule);
    
    /**
     * 更新时间胶囊
     */
    @Update
    int update(TimeCapsuleEntity timeCapsule);
    
    /**
     * 删除时间胶囊
     */
    @Delete
    int delete(TimeCapsuleEntity timeCapsule);
    
    /**
     * 根据ID获取时间胶囊
     */
    @Query("SELECT * FROM time_capsules WHERE id = :id")
    TimeCapsuleEntity getById(int id);
    
    /**
     * 获取所有等待中的时间胶囊（按计划时间排序）
     */
    @Query("SELECT * FROM time_capsules WHERE status = 0 ORDER BY scheduledTime ASC")
    LiveData<List<TimeCapsuleEntity>> getPendingCapsules();
    
    /**
     * 获取所有已打开的时间胶囊
     */
    @Query("SELECT * FROM time_capsules WHERE status = 1 ORDER BY openedTime DESC")
    LiveData<List<TimeCapsuleEntity>> getOpenedCapsules();
    
    /**
     * 获取所有时间胶囊
     */
    @Query("SELECT * FROM time_capsules WHERE status != 2 ORDER BY scheduledTime DESC")
    LiveData<List<TimeCapsuleEntity>> getAllCapsules();
    
    /**
     * 根据笔记ID获取关联的时间胶囊
     */
    @Query("SELECT * FROM time_capsules WHERE noteId = :noteId AND status != 2")
    LiveData<List<TimeCapsuleEntity>> getCapsulesByNoteId(int noteId);
    
    /**
     * 获取即将打开的时间胶囊（在指定时间范围内）
     */
    @Query("SELECT * FROM time_capsules WHERE status = 0 AND scheduledTime BETWEEN :startTime AND :endTime ORDER BY scheduledTime ASC")
    List<TimeCapsuleEntity> getCapsulesByTimeRange(long startTime, long endTime);
    
    /**
     * 获取应该打开的时间胶囊（过期但未打开）
     */
    @Query("SELECT * FROM time_capsules WHERE status = 0 AND scheduledTime <= :currentTime ORDER BY scheduledTime ASC")
    List<TimeCapsuleEntity> getCapsulesReadyToOpen(long currentTime);
    
    /**
     * 搜索时间胶囊
     */
    @Query("SELECT * FROM time_capsules WHERE status != 2 AND (title LIKE :query OR content LIKE :query OR tags LIKE :query) ORDER BY scheduledTime DESC")
    LiveData<List<TimeCapsuleEntity>> searchCapsules(String query);
    
    /**
     * 获取去年创建的时间胶囊（时间胶囊特色）
     */
    @Query("SELECT * FROM time_capsules WHERE status = 0 AND createdTime BETWEEN :oneYearAgo AND :currentTime")
    LiveData<List<TimeCapsuleEntity>> getCapsulesCreatedOneYearAgo(long oneYearAgo, long currentTime);
    
    /**
     * 计数待打开的时间胶囊
     */
    @Query("SELECT COUNT(*) FROM time_capsules WHERE status = 0")
    LiveData<Integer> countPendingCapsules();
    
    /**
     * 计数已打开的时间胶囊
     */
    @Query("SELECT COUNT(*) FROM time_capsules WHERE status = 1")
    LiveData<Integer> countOpenedCapsules();
    
    /**
     * 删除老旧的已打开胶囊（可选的清理操作）
     */
    @Query("DELETE FROM time_capsules WHERE status = 1 AND openedTime < :cutoffTime")
    int deleteOldOpenedCapsules(long cutoffTime);

    @Query("SELECT * FROM time_capsules ORDER BY createdTime DESC")
    List<TimeCapsuleEntity> getAllSync();
}
