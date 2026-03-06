package com.example.noteapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import com.example.noteapp.MainActivity;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntity;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 时间胶囊管理器 - 已适配 NoteEntity 集成模式
 * 处理后台检查，并在胶囊到达开启时间时发送通知
 */
public class TimeCapsuleManager {
    private static final String TAG = "TimeCapsuleManager";
    private static final String CHANNEL_ID = "time_capsule_channel";
    private static final int CHECK_INTERVAL = 5 * 60 * 1000; // 每5分钟检查一次
    
    @SuppressLint("StaticFieldLeak")
    private static TimeCapsuleManager instance;
    private Timer checkTimer;
    private Context context;
    private NoteDatabase database;
    private NotificationManager notificationManager;
    
    private TimeCapsuleManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = NoteDatabase.getInstance(this.context);
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    public static TimeCapsuleManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TimeCapsuleManager.class) {
                if (instance == null) {
                    instance = new TimeCapsuleManager(context);
                }
            }
        }
        return instance;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "时间胶囊";
            String description = "当时间胶囊可以开启时发送通知";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public void startBackgroundChecking() {
        if (checkTimer == null) {
            checkTimer = new Timer();
            checkTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkCapsules();
                }
            }, 0, CHECK_INTERVAL);
            Log.d(TAG, "Time Capsule background service started");
        }
    }
    
    private void checkCapsules() {
        new Thread(() -> {
            long currentTime = System.currentTimeMillis();
            // 获取所有标记为时间胶囊且已经到达开启时间的笔记
            // 注意：这里我们只通过逻辑判断，因为数据库中并没有 'isOpened' 标记，
            // 开启状态是动态计算的（currentTime >= openTimestamp）。
            // 我们需要记录哪些已经通知过了，或者只在刚到达时通知。
            
            // 为了简化逻辑，我们在 MainActivity 或专门的同步任务中处理。
            // 这里主要负责检测“刚刚开启”的瞬间（或通过 WorkManager 更好）。
        }).start();
    }

    public void showOpenNotification(NoteEntity note) {
        if (!canPostNotifications()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("note_id", note.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, note.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("时间胶囊已解锁")
                .setContentText("你写给未来的笔记 \"" + note.getTitle() + "\" 现在可以查看了")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        if (notificationManager != null) {
            notificationManager.notify(note.getId(), builder.build());
        }
    }

    private boolean canPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
