package com.example.noteapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 应用初始化工具类 - 初始化所有工具和库
 */
public class AppInitializer {
    
    private static volatile AppInitializer instance;
    private Context context;
    
    private AppInitializer(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * 单例模式获取实例
     */
    public static AppInitializer getInstance(Context context) {
        if (instance == null) {
            synchronized (AppInitializer.class) {
                if (instance == null) {
                    instance = new AppInitializer(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化应用所有工具
     */
    public void initialize() {
        // 初始化Haptic反馈
        HapticFeedbackUtil.init(context);
        
        // 其他初始化...
    }
    
    /**
     * 在后台线程中初始化（可选）
     */
    public void initializeAsync() {
        new Thread(() -> {
            initialize();
        }).start();
    }
}
