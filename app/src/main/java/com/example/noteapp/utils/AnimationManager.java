package com.example.noteapp.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

/**
 * 动画管理器 - 集成Lottie动画库和原生Android动画
 * 提供丰富的动画效果用于增强UI体验
 */
public class AnimationManager {
    
    private static final long DEFAULT_DURATION = 300;
    
    /**
     * 播放加载动画
     */
    public static void playLoadingAnimation(LottieAnimationView animationView) {
        if (animationView != null) {
            animationView.playAnimation();
        }
    }
    
    /**
     * 停止动画
     */
    public static void stopAnimation(LottieAnimationView animationView) {
        if (animationView != null) {
            animationView.pauseAnimation();
        }
    }
    
    /**
     * 设置动画速度
     */
    public static void setAnimationSpeed(LottieAnimationView animationView, float speed) {
        if (animationView != null) {
            animationView.setSpeed(speed);
        }
    }
    
    /**
     * 播放一次性动画
     */
    public static void playOnceAnimation(LottieAnimationView animationView) {
        if (animationView != null) {
            animationView.setRepeatCount(0);
            animationView.playAnimation();
        }
    }
    
    /**
     * 循环播放动画
     */
    public static void playLoopAnimation(LottieAnimationView animationView) {
        if (animationView != null) {
            animationView.setRepeatCount(LottieDrawable.INFINITE);
            animationView.playAnimation();
        }
    }
    
    /**
     * 动画监听器接口
     */
    public static abstract class LottieAnimationViewListener implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) { onAnimationStart(); }
        @Override public void onAnimationEnd(Animator animation) { onAnimationEnd(); }
        @Override public void onAnimationCancel(Animator animation) { onAnimationCancel(); }
        @Override public void onAnimationRepeat(Animator animation) { onAnimationRepeat(); }
        
        public abstract void onAnimationStart();
        public abstract void onAnimationEnd();
        public abstract void onAnimationCancel();
        public abstract void onAnimationRepeat();
    }
    
    /**
     * 抖动动画 - 用于错误提示
     */
    public static void createShakeAnimation(View view) {
        if (view == null) return;
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    /**
     * 弹性进入动画
     */
    public static void createSpringInAnimation(View view, long delay) {
        if (view == null) return;
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(500)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();
    }

    /**
     * 悬浮浮动动画 - 循环
     */
    public static void createFloatingAnimation(View view, float range, long duration) {
        if (view == null) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -range, range);
        animator.setDuration(duration);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * 背景颜色过渡动画
     */
    public static void createColorTransition(View view, int fromColor, int toColor, long duration) {
        if (view == null) return;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(animator -> view.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
    
    /**
     * 创建详细的缩放动画
     */
    public static void createScaleAnimation(View view, float scale, long duration) {
        if (view == null) return;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, scale, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, scale, 1.0f);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new FastOutSlowInInterpolator());
        animatorSet.start();
    }
    
    /**
     * 创建回弹缩放动画
     */
    public static void createBounceScaleAnimation(View view, long duration) {
        if (view == null) return;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.85f, 1.08f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.85f, 1.08f, 1.0f);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new LinearOutSlowInInterpolator());
        animatorSet.start();
    }
    
    /**
     * 创建弹出动画
     */
    public static void createPopInAnimation(View view, long duration) {
        if (view == null) return;
        view.setScaleX(0);
        view.setScaleY(0);
        view.setAlpha(0);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1.08f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1.08f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new OvershootInterpolator());
        animatorSet.start();
    }
    
    /**
     * 创建脉冲动画
     */
    public static void createPulseAnimation(View view, long duration) {
        if (view == null) return;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.12f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.12f, 1.0f);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    /**
     * 创建闪烁动画
     */
    public static void createFlashAnimation(View view, int times, long duration) {
        if (view == null) return;
        ObjectAnimator flash = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.3f, 1.0f);
        flash.setDuration(duration / times);
        flash.setRepeatCount(times - 1);
        flash.start();
    }
}
