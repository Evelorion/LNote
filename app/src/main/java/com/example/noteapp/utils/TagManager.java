package com.example.noteapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标签管理工具类
 * 管理所有笔记标签和分类
 */
public class TagManager {
    private static final String PREFS_NAME = "TagPrefs";
    private static final String TAGS_KEY = "all_tags";
    private static final String CATEGORIES_KEY = "all_categories";
    
    private static TagManager instance;
    private SharedPreferences prefs;
    
    private TagManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized TagManager getInstance(Context context) {
        if (instance == null) {
            instance = new TagManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag == null || tag.isEmpty()) return;
        
        Set<String> tags = getAllTags();
        tags.add(tag);
        saveTags(tags);
    }
    
    /**
     * 删除标签
     */
    public void removeTag(String tag) {
        if (tag == null || tag.isEmpty()) return;
        
        Set<String> tags = getAllTags();
        tags.remove(tag);
        saveTags(tags);
    }
    
    /**
     * 获取所有标签
     */
    public Set<String> getAllTags() {
        return new HashSet<>(prefs.getStringSet(TAGS_KEY, new HashSet<>()));
    }
    
    /**
     * 获取标签列表
     */
    public List<String> getTagsList() {
        return new ArrayList<>(getAllTags());
    }
    
    /**
     * 保存标签列表
     */
    private void saveTags(Set<String> tags) {
        prefs.edit().putStringSet(TAGS_KEY, tags).apply();
    }
    
    /**
     * 添加分类
     */
    public void addCategory(String category) {
        if (category == null || category.isEmpty()) return;
        
        Set<String> categories = getAllCategories();
        categories.add(category);
        saveCategories(categories);
    }
    
    /**
     * 删除分类
     */
    public void removeCategory(String category) {
        if (category == null || category.isEmpty()) return;
        
        Set<String> categories = getAllCategories();
        categories.remove(category);
        saveCategories(categories);
    }
    
    /**
     * 获取所有分类
     */
    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>(prefs.getStringSet(CATEGORIES_KEY, new HashSet<>()));
        categories.add("默认");  // 始终包含默认分类
        return categories;
    }
    
    /**
     * 获取分类列表
     */
    public List<String> getCategoriesList() {
        return new ArrayList<>(getAllCategories());
    }
    
    /**
     * 保存分类列表
     */
    private void saveCategories(Set<String> categories) {
        prefs.edit().putStringSet(CATEGORIES_KEY, categories).apply();
    }
    
    /**
     * 清除所有标签和分类
     */
    public void clearAll() {
        prefs.edit()
                .remove(TAGS_KEY)
                .remove(CATEGORIES_KEY)
                .apply();
    }
}
