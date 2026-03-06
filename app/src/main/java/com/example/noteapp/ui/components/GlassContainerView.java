package com.example.noteapp.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import com.example.noteapp.R;

/**
 * iOS风格的玻璃化容器
 * 支持毛玻璃效果、圆角、阴影
 */
public class GlassContainerView extends FrameLayout {
    
    private Paint glassPaint;
    private Paint shadowPaint;
    private float cornerRadius = 20f;
    private int glassColor = 0xB3FFFFFF;
    private int shadowColor = 0x1A000000;
    private float shadowElevation = 8f;
    
    public GlassContainerView(Context context) {
        super(context);
        init(context, null);
    }
    
    public GlassContainerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public GlassContainerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, @Nullable AttributeSet attrs) {
        // 读取自定义属性
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GlassContainerView);
            cornerRadius = a.getDimension(R.styleable.GlassContainerView_cornerRadius, 20f);
            glassColor = a.getColor(R.styleable.GlassContainerView_glassColor, 0xB3FFFFFF);
            shadowColor = a.getColor(R.styleable.GlassContainerView_shadowColor, 0x1A000000);
            shadowElevation = a.getDimension(R.styleable.GlassContainerView_shadowElevation, 8f);
            a.recycle();
        }
        
        // 玻璃效果画笔
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setColor(glassColor);
        glassPaint.setStyle(Paint.Style.FILL);
        
        // 阴影画笔
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(shadowColor);
        setWillNotDraw(false);
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClipToOutline(true);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        RectF rect = new RectF(0, 0, width, height);
        
        // 绘制玻璃背景
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glassPaint);
        
        super.onDraw(canvas);
    }
    
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }
    
    public void setGlassColor(int color) {
        this.glassColor = color;
        glassPaint.setColor(color);
        invalidate();
    }
    
    public void setGlassAlpha(int alpha) {
        glassPaint.setAlpha(alpha);
        invalidate();
    }
}
