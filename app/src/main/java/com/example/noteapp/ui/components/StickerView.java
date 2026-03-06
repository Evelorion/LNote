package com.example.noteapp.ui.components;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 可移动、可缩放的贴纸图片视图
 */
public class StickerView extends FrameLayout {

    private ImageView imageView;
    private View deleteButton;
    private View resizeHandle;
    private String imagePath;
    private Bitmap originalBitmap;

    private float lastX, lastY;
    private int baseWidth, baseHeight;
    private float lastRawX, lastYRaw;
    private long touchDownTime;
    private float touchDownX, touchDownY;
    private static final int TAP_THRESHOLD = 15;
    private static final long TAP_TIME_THRESHOLD = 300;

    public StickerView(@NonNull Context context) {
        super(context);
    }

    public StickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerView(@NonNull Context context, String path, Bitmap bitmap) {
        super(context);
        this.imagePath = path;
        this.originalBitmap = bitmap;
        init(context, bitmap);
    }

    private void init(Context context, Bitmap bitmap) {
        // 主图片
        imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams imgParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgParams.setMargins(20, 20, 20, 20);
        addView(imageView, imgParams);

        // 删除按钮
        deleteButton = new View(context);
        deleteButton.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
        LayoutParams delParams = new LayoutParams(60, 60);
        delParams.gravity = Gravity.TOP | Gravity.START;
        addView(deleteButton, delParams);
        deleteButton.setOnClickListener(v -> {
            if (getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
            }
        });

        // 缩放手柄
        resizeHandle = new View(context);
        resizeHandle.setBackgroundResource(android.R.drawable.ic_menu_crop); 
        LayoutParams resParams = new LayoutParams(60, 60);
        resParams.gravity = Gravity.BOTTOM | Gravity.END;
        addView(resizeHandle, resParams);

        setupTouchListeners();
        
        // 设置初始大小
        setLayoutParams(new ViewGroup.LayoutParams(400, 400));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListeners() {
        // 拖动逻辑 + 点击放大
        this.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    touchDownX = event.getRawX();
                    touchDownY = event.getRawY();
                    touchDownTime = System.currentTimeMillis();
                    bringToFront();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;
                    setX(getX() + dx);
                    setY(getY() + dy);
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float totalDx = Math.abs(event.getRawX() - touchDownX);
                    float totalDy = Math.abs(event.getRawY() - touchDownY);
                    long duration = System.currentTimeMillis() - touchDownTime;
                    if (totalDx < TAP_THRESHOLD && totalDy < TAP_THRESHOLD && duration < TAP_TIME_THRESHOLD) {
                        showFullscreenImage();
                    }
                    performClick();
                    break;
            }
            return true;
        });

        // 缩放逻辑
        resizeHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastRawX = event.getRawX();
                    lastYRaw = event.getRawY();
                    baseWidth = getWidth();
                    baseHeight = getHeight();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float diffX = event.getRawX() - lastRawX;
                    float diffY = event.getRawY() - lastYRaw;
                    
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.width = (int) Math.max(150, baseWidth + diffX);
                    params.height = (int) Math.max(150, baseHeight + diffY);
                    setLayoutParams(params);
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * 点击图片后全屏放大显示
     */
    private void showFullscreenImage() {
        if (originalBitmap == null) return;
        Context ctx = getContext();

        Dialog dialog = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#E6000000")));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        FrameLayout container = new FrameLayout(ctx);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView fullImage = new ImageView(ctx);
        fullImage.setImageBitmap(originalBitmap);
        fullImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgParams.gravity = Gravity.CENTER;
        fullImage.setLayoutParams(imgParams);

        // 入场动画
        fullImage.setScaleX(0.5f);
        fullImage.setScaleY(0.5f);
        fullImage.setAlpha(0f);
        fullImage.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        container.addView(fullImage);

        // 点击关闭 (带退出动画)
        container.setOnClickListener(v -> {
            fullImage.animate()
                    .scaleX(0.5f).scaleY(0.5f).alpha(0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(dialog::dismiss)
                    .start();
        });

        dialog.setContentView(container);
        dialog.show();
    }

    public String getImagePath() {
        return imagePath;
    }
    
    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }
    
    public void setSize(int width, int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(width, height);
        } else {
            params.width = width;
            params.height = height;
        }
        setLayoutParams(params);
    }
}
