package com.example.noteapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.annotation.RequiresApi;

/**
 * Haptic 反馈工具类 - iOS风格振动反馈
 * 用于提供触觉反馈，增强用户交互体验
 */
@SuppressWarnings("deprecation")
public class HapticFeedbackUtil {
    
    private static Vibrator vibrator;
    
    /**
     * 初始化 Vibrator
     */
    public static void init(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    /**
     * 轻击反馈 - iOS风格（非常短的振动）
     * 用于：按钮点击、轻触操作
     */
    public static void tap() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
        } else {
            vibrator.vibrate(30);
        }
    }
    
    /**
     * 轻击反馈（通过View）
     */
    public static void tap(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        tap();
    }
    
    /**
     * 成功反馈 - iOS风格成功反馈
     * 用于：操作成功（保存、删除成功）
     */
    public static void success() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK));
        } else {
            vibrator.vibrate(new long[]{0, 50, 30, 50}, -1);
        }
    }
    
    /**
     * 成功反馈（通过View）
     */
    public static void success(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        success();
    }
    
    /**
     * 警告反馈 - iOS风格警告反馈
     * 用于：确认删除、警告操作
     */
    public static void warning() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK));
        } else {
            vibrator.vibrate(new long[]{0, 80, 50, 80}, -1);
        }
    }
    
    /**
     * 警告反馈（通过View）
     */
    public static void warning(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        warning();
    }
    
    /**
     * 错误反馈 - iOS风格错误反馈
     * 用于：错误提示、操作失败
     */
    public static void error() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK));
        } else {
            vibrator.vibrate(new long[]{0, 100, 50, 100, 50, 100}, -1);
        }
    }
    
    /**
     * 错误反馈（通过View）
     */
    public static void error(View view) {
        error();
    }
    
    /**
     * 自定义振动 - 用于特定场景
     * @param pattern 振动模式 [delay, vibrate, sleep, vibrate, ...]
     * @param repeat 是否循环（-1表示不循环）
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ObsoleteSdkInt")
    public static void custom(long[] pattern, int repeat) {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
        } else {
            @SuppressWarnings("deprecation")
            Vibrator oldVibrator = vibrator;
            oldVibrator.vibrate(pattern, repeat);
        }
    }
    
    /**
     * 停止所有振动
     */
    public static void cancel() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
