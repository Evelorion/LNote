package com.example.noteapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.noteapp.db.NoteEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 搜索管理工具类
 * 支持高效的笔记搜索、搜索历史和搜索优化
 */
public class SearchManager {
    
    private static final String PREFS_NAME = "SearchPrefs";
    private static final String SEARCH_HISTORY_KEY = "search_history";
    private static final int MAX_HISTORY = 10;
    
    private SharedPreferences prefs;
    
    public SearchManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 高级搜索 - 支持多个关键词、分类、标签过滤
     */
    public List<NoteEntity> advancedSearch(List<NoteEntity> allNotes, SearchQuery query) {
        List<NoteEntity> results = new ArrayList<>();
        
        for (NoteEntity note : allNotes) {
            if (matchesQuery(note, query)) {
                results.add(note);
            }
        }
        
        // 按相关度排序
        Collections.sort(results, (n1, n2) -> {
            int score1 = calculateRelevance(n1, query);
            int score2 = calculateRelevance(n2, query);
            return Integer.compare(score2, score1);  // 倒序
        });
        
        return results;
    }
    
    /**
     * 检查笔记是否匹配搜索条件
     */
    private boolean matchesQuery(NoteEntity note, SearchQuery query) {
        // 关键词匹配（标题优先，然后内容）
        if (query.hasKeywords()) {
            boolean keywordMatch = false;
            for (String keyword : query.keywords) {
                String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
                String title = note.getTitle() != null ? note.getTitle().toLowerCase(Locale.ROOT) : "";
                String content = note.getContent() != null ? note.getContent().toLowerCase(Locale.ROOT) : "";
                
                if (title.contains(lowerKeyword) || content.contains(lowerKeyword)) {
                    keywordMatch = true;
                    break;
                }
            }
            if (!keywordMatch) return false;
        }
        
        // 分类过滤
        if (query.hasCategories()) {
            String noteCategory = note.getCategory() != null ? note.getCategory() : "";
            if (!query.categories.contains(noteCategory)) {
                return false;
            }
        }
        
        // 标签过滤
        if (query.hasTags()) {
            String[] noteTags = note.getTagsList();
            boolean hasTag = false;
            for (String tag : noteTags) {
                if (query.tags.contains(tag)) {
                    hasTag = true;
                    break;
                }
            }
            if (!hasTag) return false;
        }
        
        // 日期范围过滤
        if (query.hasDateRange()) {
            long noteTime = note.getTimestamp();
            if (noteTime < query.startDate || noteTime > query.endDate) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算笔记与搜索条件的相关度分数
     */
    private int calculateRelevance(NoteEntity note, SearchQuery query) {
        int score = 0;
        
        if (!query.hasKeywords()) {
            return 0;
        }
        
        String title = note.getTitle() != null ? note.getTitle().toLowerCase(Locale.ROOT) : "";
        String content = note.getContent() != null ? note.getContent().toLowerCase(Locale.ROOT) : "";
        
        for (String keyword : query.keywords) {
            String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
            
            // 标题匹配加分更多
            if (title.equals(lowerKeyword)) {
                score += 100;
            } else if (title.startsWith(lowerKeyword)) {
                score += 50;
            } else if (title.contains(lowerKeyword)) {
                score += 30;
            }
            
            // 内容匹配加分
            if (content.contains(lowerKeyword)) {
                int count = countOccurrences(content, lowerKeyword);
                score += Math.min(count * 10, 20);
            }
        }
        
        return score;
    }
    
    /**
     * 计算子串在字符串中出现的次数
     */
    private int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
    
    /**
     * 添加搜索历史
     */
    public void addSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        List<String> historyList = getSearchHistory();
        Set<String> history = new HashSet<>(historyList);
        history.remove(query);  // 移除已有的，确保最新的在前
        history.add(query);
        
        // 保持最多10条历史
        if (history.size() > MAX_HISTORY) {
            Set<String> newHistory = new HashSet<>();
            int count = 0;
            for (String h : history) {
                if (count < MAX_HISTORY) {
                    newHistory.add(h);
                    count++;
                } else {
                    break;
                }
            }
            history = newHistory;
        }
        
        prefs.edit().putStringSet(SEARCH_HISTORY_KEY, history).apply();
    }
    
    /**
     * 获取搜索历史
     */
    public List<String> getSearchHistory() {
        Set<String> history = prefs.getStringSet(SEARCH_HISTORY_KEY, new HashSet<>());
        List<String> list = new ArrayList<>(history);
        // 反序显示最新的在前
        Collections.sort(list, (a, b) -> b.compareTo(a));
        return list;
    }
    
    /**
     * 清除搜索历史
     */
    public void clearSearchHistory() {
        prefs.edit().remove(SEARCH_HISTORY_KEY).apply();
    }
    
    /**
     * 删除特定的搜索历史
     */
    public void removeFromHistory(String query) {
        Set<String> history = new HashSet<>(getSearchHistory());
        history.remove(query);
        prefs.edit().putStringSet(SEARCH_HISTORY_KEY, history).apply();
    }
    
    /**
     * 搜索查询对象
     */
    public static class SearchQuery {
        public List<String> keywords = new ArrayList<>();
        public List<String> categories = new ArrayList<>();
        public List<String> tags = new ArrayList<>();
        public long startDate = 0;
        public long endDate = Long.MAX_VALUE;
        
        public SearchQuery addKeyword(String keyword) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                keywords.add(keyword.trim());
            }
            return this;
        }
        
        public SearchQuery addCategory(String category) {
            if (category != null && !category.isEmpty()) {
                categories.add(category);
            }
            return this;
        }
        
        public SearchQuery addTag(String tag) {
            if (tag != null && !tag.isEmpty()) {
                tags.add(tag);
            }
            return this;
        }
        
        public SearchQuery setDateRange(long start, long end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }
        
        public boolean hasKeywords() {
            return !keywords.isEmpty();
        }
        
        public boolean hasCategories() {
            return !categories.isEmpty();
        }
        
        public boolean hasTags() {
            return !tags.isEmpty();
        }
        
        public boolean hasDateRange() {
            return startDate > 0;
        }
    }
}
