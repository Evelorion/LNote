package com.example.noteapp.ui.components;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import com.example.noteapp.R;

public class MediaStickerView extends FrameLayout {

    public enum Type { IMAGE, VIDEO, AUDIO }

    private Type type;
    private String mediaPath;
    private Bitmap thumbnail;
    private View contentView;
    private View deleteButton;
    private View resizeHandle;
    private OnLayoutChangeListener layoutChangeListener;

    private float lastX, lastY;
    private int baseWidth, baseHeight;
    private float lastRawX, lastYRaw;
    private float touchDownX, touchDownY;
    private long touchDownTime;
    private static final int TAP_THRESHOLD = 15;
    private static final long TAP_TIME_THRESHOLD = 300;

    public interface OnLayoutChangeListener {
        void onLayoutChanged();
    }

    public MediaStickerView(@NonNull Context context, Type type, String path, Bitmap thumbnail) {
        super(context);
        this.type = type;
        this.mediaPath = path;
        this.thumbnail = thumbnail;
        init(context, thumbnail);
    }

    private void init(Context context, Bitmap thumbnail) {
        if (type == Type.VIDEO) {
            // 视频缩略图预览（不自动播放）
            ImageView videoThumb = new ImageView(context);
            videoThumb.setScaleType(ImageView.ScaleType.FIT_XY);
            videoThumb.setBackgroundColor(0xFF222222);
            // 尝试获取视频缩略图
            try {
                android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
                if (mediaPath.startsWith("content://")) {
                    retriever.setDataSource(context, Uri.parse(mediaPath));
                } else {
                    retriever.setDataSource(mediaPath);
                }
                Bitmap frame = retriever.getFrameAtTime();
                if (frame != null) videoThumb.setImageBitmap(frame);
                retriever.release();
            } catch (Exception ignored) {}

            // 播放图标叠加
            ImageView playIcon = new ImageView(context);
            playIcon.setImageResource(android.R.drawable.ic_media_play);
            playIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            playIcon.setAlpha(0.8f);

            FrameLayout videoContainer = new FrameLayout(context);
            videoContainer.addView(videoThumb, new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            LayoutParams playParams = new LayoutParams(80, 80);
            playParams.gravity = Gravity.CENTER;
            videoContainer.addView(playIcon, playParams);
            contentView = videoContainer;
        } else if (type == Type.AUDIO) {
            ImageView audioIcon = new ImageView(context);
            audioIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            audioIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            audioIcon.setBackgroundColor(0xFF333333);
            contentView = audioIcon;

            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mediaPath);
                mediaPlayer.prepare();
            } catch (Exception ignored) {}

            audioIcon.setOnClickListener(v -> {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                else mediaPlayer.start();
            });
        } else {
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(thumbnail);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            contentView = imageView;
        }

        LayoutParams contentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentParams.setMargins(20, 20, 20, 20);
        addView(contentView, contentParams);

        // Delete Button
        deleteButton = new View(context);
        deleteButton.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
        LayoutParams delParams = new LayoutParams(60, 60);
        delParams.gravity = Gravity.TOP | Gravity.START;
        addView(deleteButton, delParams);
        deleteButton.setOnClickListener(v -> {
            if (getParent() != null) {
                ((ViewGroup) getParent()).removeView(this);
                if (layoutChangeListener != null) layoutChangeListener.onLayoutChanged();
            }
        });

        // Resize Handle
        resizeHandle = new View(context);
        resizeHandle.setBackgroundResource(android.R.drawable.ic_menu_crop);
        LayoutParams resParams = new LayoutParams(60, 60);
        resParams.gravity = Gravity.BOTTOM | Gravity.END;
        addView(resizeHandle, resParams);

        setupTouchListeners();
        setLayoutParams(new ViewGroup.LayoutParams(400, 400));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListeners() {
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
                    setX(getX() + (event.getRawX() - lastX));
                    setY(getY() + (event.getRawY() - lastY));
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float totalDx = Math.abs(event.getRawX() - touchDownX);
                    float totalDy = Math.abs(event.getRawY() - touchDownY);
                    long duration = System.currentTimeMillis() - touchDownTime;
                    if (totalDx < TAP_THRESHOLD && totalDy < TAP_THRESHOLD && duration < TAP_TIME_THRESHOLD) {
                        onTap();
                    }
                    if (layoutChangeListener != null) layoutChangeListener.onLayoutChanged();
                    performClick();
                    break;
            }
            return true;
        });

        resizeHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastRawX = event.getRawX();
                    lastYRaw = event.getRawY();
                    baseWidth = getWidth();
                    baseHeight = getHeight();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.width = (int) Math.max(150, baseWidth + (event.getRawX() - lastRawX));
                    params.height = (int) Math.max(150, baseHeight + (event.getRawY() - lastYRaw));
                    setLayoutParams(params);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (layoutChangeListener != null) layoutChangeListener.onLayoutChanged();
                    break;
            }
            return false;
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void onTap() {
        if (type == Type.IMAGE) {
            showFullscreenImage();
        } else if (type == Type.VIDEO) {
            showFullscreenVideo();
        }
    }

    /**
     * 点击图片全屏放大查看
     */
    private void showFullscreenImage() {
        if (thumbnail == null) return;
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
        fullImage.setImageBitmap(thumbnail);
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

        // 点击关闭（带退出动画）
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

    /**
     * 点击视频全屏播放，支持暂停/快进/进度条
     */
    private void showFullscreenVideo() {
        Context ctx = getContext();

        Dialog dialog = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        FrameLayout container = new FrameLayout(ctx);
        container.setBackgroundColor(Color.BLACK);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 全屏VideoView
        VideoView videoView = new VideoView(ctx);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoParams.gravity = Gravity.CENTER;
        videoView.setLayoutParams(videoParams);

        // MediaController 提供播放/暂停/快进/进度条
        MediaController mediaController = new MediaController(ctx);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if (mediaPath.startsWith("content://")) {
            videoView.setVideoURI(Uri.parse(mediaPath));
        } else {
            videoView.setVideoPath(mediaPath);
        }

        videoView.setOnPreparedListener(mp -> {
            videoView.start();
            // 显示控制栏
            mediaController.show(3000);
        });

        videoView.setOnCompletionListener(mp -> {
            // 播放结束后可重播
            videoView.seekTo(0);
            mediaController.show(0);
        });

        container.addView(videoView);

        // 关闭按钮（右上角）
        ImageView closeBtn = new ImageView(ctx);
        closeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeBtn.setPadding(24, 24, 24, 24);
        closeBtn.setBackgroundColor(0x66000000);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(120, 120);
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.setMargins(0, 48, 24, 0);
        closeBtn.setLayoutParams(closeParams);
        closeBtn.setOnClickListener(v -> {
            videoView.stopPlayback();
            dialog.dismiss();
        });
        container.addView(closeBtn);

        dialog.setOnDismissListener(d -> {
            try { videoView.stopPlayback(); } catch (Exception ignored) {}
        });

        dialog.setContentView(container);
        dialog.show();
    }

    public void setLayoutChangeListener(OnLayoutChangeListener listener) {
        this.layoutChangeListener = listener;
    }

    public Type getType() { return type; }
    public String getMediaPath() { return mediaPath; }
    public void setPosition(float x, float y) { setX(x); setY(y); }
    public void setSize(int w, int h) {
        ViewGroup.LayoutParams p = getLayoutParams();
        p.width = w; p.height = h; setLayoutParams(p);
    }
}
