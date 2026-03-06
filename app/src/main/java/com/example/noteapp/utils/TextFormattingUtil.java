package com.example.noteapp.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.StrikethroughSpan;
import android.graphics.Typeface;
import android.widget.EditText;

/**
 * 文本格式化工具类
 * 支持加粗、斜体、下划线、删除线等格式
 */
public class TextFormattingUtil {
    
    /**
     * 应用加粗格式
     */
    public static void makeBold(EditText editText) {
        applyStyle(editText, new StyleSpan(Typeface.BOLD));
    }
    
    /**
     * 应用斜体格式
     */
    public static void makeItalic(EditText editText) {
        applyStyle(editText, new StyleSpan(Typeface.ITALIC));
    }
    
    /**
     * 应用加粗斜体格式
     */
    public static void makeBoldItalic(EditText editText) {
        applyStyle(editText, new StyleSpan(Typeface.BOLD_ITALIC));
    }
    
    /**
     * 应用下划线格式
     */
    public static void makeUnderline(EditText editText) {
        applyStyle(editText, new UnderlineSpan());
    }
    
    /**
     * 应用删除线格式
     */
    public static void makeStrikethrough(EditText editText) {
        applyStyle(editText, new StrikethroughSpan());
    }
    
    /**
     * 应用格式到选中文本
     */
    private static void applyStyle(EditText editText, Object span) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        if (start < 0 || start == end) {
            return;  // 没有选中文本
        }
        
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        
        SpannableString spannableString = new SpannableString(editText.getText());
        spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        editText.setText(spannableString);
        editText.setSelection(start, end);  // 恢复选择
    }
    
    /**
     * 清除选中文本的格式
     */
    public static void clearFormatting(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        if (start < 0 || start == end) {
            return;
        }
        
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        
        SpannableString spannableString = new SpannableString(editText.getText());
        
        // 移除所有样式
        Object[] spans = spannableString.getSpans(start, end, Object.class);
        for (Object span : spans) {
            if (span instanceof StyleSpan || span instanceof UnderlineSpan || 
                span instanceof StrikethroughSpan) {
                spannableString.removeSpan(span);
            }
        }
        
        editText.setText(spannableString);
        editText.setSelection(start, end);
    }
    
    /**
     * 增加文本大小标记（用于标题）
     */
    public static void makeHeading1(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        if (start < 0) {
            start = editText.length();
        }
        
        editText.setSelection(0, 0);
        editText.getText().insert(start, "# ");
    }
    
    public static void makeHeading2(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        if (start < 0) {
            start = editText.length();
        }
        
        editText.setSelection(0, 0);
        editText.getText().insert(start, "## ");
    }
    
    public static void makeHeading3(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        
        if (start < 0) {
            start = editText.length();
        }
        
        editText.setSelection(0, 0);
        editText.getText().insert(start, "### ");
    }
}
