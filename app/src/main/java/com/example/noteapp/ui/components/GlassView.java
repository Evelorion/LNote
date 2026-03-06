package com.example.noteapp.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 玻璃化效果背景视图 - iOS风格毛玻璃
 */
public class GlassView extends View {
    
    private Paint glassPaint;
    private Paint borderPaint;
    private float cornerRadius = 20f;
    
    public GlassView(Context context) {
        super(context);
        init();
    }
    
    public GlassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GlassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // 玻璃效果画笔 - 半透明白色
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setColor(0xB3FFFFFF);  // 70%透明度白色
        glassPaint.setStyle(Paint.Style.FILL);
        
        // 边框画笔 - 更亮的白色边框
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0x66FFFFFF);  // 40%透明度白色
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        
        // 绘制圆角玻璃背景
        canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, glassPaint);
        
        // 绘制边框
        canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, borderPaint);
    }
    
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }
    
    public void setGlassAlpha(int alpha) {
        glassPaint.setAlpha(alpha);
        invalidate();
    }
}
