package com.example.noteapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.noteapp.adapter.NoteAdapter;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntity;
import com.example.noteapp.utils.HapticFeedbackUtil;
import com.example.noteapp.utils.AppInitializer;
import com.example.noteapp.utils.AnimationManager;
import com.example.noteapp.utils.SearchManager;
import com.example.noteapp.utils.ShareUtil;
import com.example.noteapp.utils.ThemeModeManager;
import com.example.noteapp.utils.TimeCapsuleManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 主活动 - 笔记列表 (深色模式现代版)
 * 已彻底修复符号解析错误、变量警告及 ID 冲突问题
 */
public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private LinearLayout emptyView;
    private EditText searchEditText;
    private NoteDatabase database;
    private List<NoteEntity> allNotes = new ArrayList<>();
    private ActivityResultLauncher<Intent> speechLauncher;
    private String activeCategory = null;
    
    // 分类 Chip 引用
    private TextView cAll, cFavorites, cJournal, cTodo;
    // 底部导航图标和文字
    private ImageView iNotes, iTags, iProfile;
    private TextView lNotes, lTags, lProfile;
    private LinearLayout nNotes, nTags, nProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeModeManager.applySavedMode(this);
        ThemeModeManager.applyTheme(this);
        
        initLaunchers();
        setContentView(R.layout.activity_main);
        
        AppInitializer.getInstance(this).initialize();
        
        initViews();
        initDatabase();
        setupRecyclerView();
        loadNotes();
        setupSearch();
        handleIncomingSearchIntent(getIntent());
        playMainIntroAnimations();
    }

    private void initLaunchers() {
        speechLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty() && searchEditText != null) {
                        searchEditText.setText(matches.get(0));
                        filterNotes(matches.get(0));
                    }
                }
            }
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingSearchIntent(intent);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_notes);
        emptyView = findViewById(R.id.tv_empty);
        searchEditText = findViewById(R.id.et_search);
        
        ImageButton fab = findViewById(R.id.fab_add_note);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                HapticFeedbackUtil.tap(v);
                startActivity(new Intent(this, EditNoteActivity.class));
            });
        }

        // 绑定底部导航
        nNotes = findViewById(R.id.nav_notes);
        nTags = findViewById(R.id.nav_tags);
        nProfile = findViewById(R.id.nav_profile);
        iNotes = findViewById(R.id.icon_notes);
        iTags = findViewById(R.id.icon_tags);
        iProfile = findViewById(R.id.icon_profile);
        lNotes = findViewById(R.id.label_notes);
        lTags = findViewById(R.id.label_tags);
        lProfile = findViewById(R.id.label_profile);

        // 绑定分类 Chip
        cAll = findViewById(R.id.chip_all);
        cFavorites = findViewById(R.id.chip_favorites);
        cJournal = findViewById(R.id.chip_journal);
        cTodo = findViewById(R.id.chip_todo);

        View voiceBtn = findViewById(R.id.btn_voice_search);
        if (voiceBtn != null) voiceBtn.setOnClickListener(v -> startVoiceRecognition());

        if (cAll != null) cAll.setOnClickListener(v -> selectCategory(null, cAll));
        if (cFavorites != null) cFavorites.setOnClickListener(v -> selectCategory("Favorites", cFavorites));
        if (cJournal != null) cJournal.setOnClickListener(v -> selectCategory("Journal", cJournal));
        if (cTodo != null) cTodo.setOnClickListener(v -> selectCategory("To-Do", cTodo));

        if (nNotes != null) {
            nNotes.setOnClickListener(v -> {
                updateBottomNavSelection(0);
                selectCategory(null, cAll);
            });
        }
        if (nTags != null) {
            nTags.setOnClickListener(v -> {
                updateBottomNavSelection(1);
                if (searchEditText != null) {
                    searchEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
        if (nProfile != null) {
            nProfile.setOnClickListener(v -> {
                updateBottomNavSelection(2);
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }

        applyThemeColors();
        updateBottomNavSelection(0);
    }

    private void applyThemeColors() {
        TypedArray ta = obtainStyledAttributes(new int[]{
                R.attr.themeBgSearch, R.attr.themeNavBg, R.attr.themeSeparator,
                R.attr.themeTextPrimary, R.attr.themeTextTertiary
        });
        int searchBg = ta.getColor(0, 0xFF1C2128);
        int navBg = ta.getColor(1, 0xFF161B22);
        int separator = ta.getColor(2, 0xFF30363D);
        ta.recycle();

        // Search capsule background
        View searchCapsule = findViewById(R.id.search_capsule);
        if (searchCapsule != null) {
            GradientDrawable searchDrawable = new GradientDrawable();
            searchDrawable.setColor(searchBg);
            searchDrawable.setCornerRadius(dpToPx(24));
            searchDrawable.setStroke(dpToPx(1), separator);
            searchCapsule.setBackground(searchDrawable);
        }

        // Bottom nav background
        View bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            GradientDrawable navDrawable = new GradientDrawable();
            navDrawable.setColor(navBg);
            navDrawable.setCornerRadius(dpToPx(30));
            navDrawable.setStroke(dpToPx(1), separator);
            bottomNav.setBackground(navDrawable);
        }

        // Apply theme to category chips on initial load
        applyChipTheme();
    }

    private void applyChipTheme() {
        TypedArray ta = obtainStyledAttributes(new int[]{R.attr.themeBgSearch, R.attr.themeSeparator, R.attr.themeTextSecondary});
        int chipUnselectedBg = ta.getColor(0, 0xFF1C2128);
        int chipBorder = ta.getColor(1, 0xFF30363D);
        int textSecondary = ta.getColor(2, 0xFFB0B0B0);
        ta.recycle();

        TextView[] chips = {cAll, cFavorites, cJournal, cTodo};
        for (TextView chip : chips) {
            if (chip != null) {
                GradientDrawable unselectedBg = new GradientDrawable();
                unselectedBg.setColor(chipUnselectedBg);
                unselectedBg.setCornerRadius(dpToPx(20));
                unselectedBg.setStroke(dpToPx(1), chipBorder);
                chip.setBackground(unselectedBg);
                chip.setTextColor(textSecondary);
            }
        }

        // Default "All" selected
        if (cAll != null) {
            cAll.setBackgroundResource(R.drawable.category_chip_selected_bg);
            cAll.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出搜索内容");
        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "语音搜索不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectCategory(String category, @NonNull TextView chipView) {
        HapticFeedbackUtil.tap(chipView);
        activeCategory = category;
        
        TypedArray ta = obtainStyledAttributes(new int[]{R.attr.themeBgSearch, R.attr.themeSeparator, R.attr.themeTextSecondary});
        int chipUnselectedBg = ta.getColor(0, 0xFF1C2128);
        int chipBorder = ta.getColor(1, 0xFF30363D);
        int textSecondary = ta.getColor(2, 0xFFB0B0B0);
        ta.recycle();

        TextView[] chips = {cAll, cFavorites, cJournal, cTodo};
        for (TextView chip : chips) {
            if (chip != null) {
                GradientDrawable unselectedBg = new GradientDrawable();
                unselectedBg.setColor(chipUnselectedBg);
                unselectedBg.setCornerRadius(dpToPx(20));
                unselectedBg.setStroke(dpToPx(1), chipBorder);
                chip.setBackground(unselectedBg);
                chip.setTextColor(textSecondary);
            }
        }
        
        chipView.setBackgroundResource(R.drawable.category_chip_selected_bg);
        chipView.setTextColor(ContextCompat.getColor(this, R.color.white));
        
        if (searchEditText != null) filterNotes(searchEditText.getText().toString());
    }

    private void updateBottomNavSelection(int index) {
        int activeColor = ContextCompat.getColor(this, R.color.accent_blue);
        TypedArray ta = obtainStyledAttributes(new int[]{R.attr.themeTextTertiary});
        int inactiveColor = ta.getColor(0, ContextCompat.getColor(this, R.color.ios_text_tertiary));
        ta.recycle();

        if (iNotes != null) iNotes.setColorFilter(index == 0 ? activeColor : inactiveColor);
        if (lNotes != null) lNotes.setTextColor(index == 0 ? activeColor : inactiveColor);
        if (iTags != null) iTags.setColorFilter(index == 1 ? activeColor : inactiveColor);
        if (lTags != null) lTags.setTextColor(index == 1 ? activeColor : inactiveColor);
        if (iProfile != null) iProfile.setColorFilter(index == 2 ? activeColor : inactiveColor);
        if (lProfile != null) lProfile.setTextColor(index == 2 ? activeColor : inactiveColor);
        
        View target = index == 0 ? nNotes : (index == 1 ? nTags : nProfile);
        if (target != null) AnimationManager.createBounceScaleAnimation(target, 200);
    }

    private void playMainIntroAnimations() {
        View capsule = findViewById(R.id.search_capsule);
        if (capsule != null) AnimationManager.createSpringInAnimation(capsule, 0);
        View fab = findViewById(R.id.fab_add_note);
        if (fab != null) AnimationManager.createPopInAnimation(fab, 400);
        View nav = findViewById(R.id.bottom_nav);
        if (nav != null) AnimationManager.createSpringInAnimation(nav, 600);
    }

    private void initDatabase() {
        database = NoteDatabase.getInstance(this);
        new SearchManager(this); 
        TimeCapsuleManager.getInstance(this).startBackgroundChecking();
    }
    
    private void setupRecyclerView() {
        adapter = new NoteAdapter(this);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);
        }
    }
    
    private void loadNotes() {
        if (database != null) {
            database.noteDao().getAllNotes().observe(this, notes -> {
                allNotes = notes != null ? notes : new ArrayList<>();
                if (searchEditText != null) filterNotes(searchEditText.getText().toString());
            });
        }
    }

    private void setupSearch() {
        if (searchEditText == null) return;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    
    private void filterNotes(String query) {
        List<NoteEntity> filtered = new ArrayList<>();
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        for (NoteEntity note : allNotes) {
            String title = note.getTitle() != null ? note.getTitle().toLowerCase(Locale.ROOT) : "";
            String content = note.getContent() != null ? note.getContent().toLowerCase(Locale.ROOT) : "";
            boolean matchesSearch = q.isEmpty() || title.contains(q) || content.contains(q);
            boolean matchesCat = activeCategory == null || activeCategory.equals(note.getCategory());
            if (matchesSearch && matchesCat) filtered.add(note);
        }
        updateUI(filtered);
    }

    private void updateUI(List<NoteEntity> notes) {
        if (notes == null || notes.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        } else {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setNotes(notes);
            }
        }
    }

    private void handleIncomingSearchIntent(Intent intent) {
        if (intent != null && intent.hasExtra("search_query") && searchEditText != null) {
            String q = intent.getStringExtra("search_query");
            searchEditText.setText(q);
            filterNotes(q);
        }
    }

    @Override public void onNoteClick(NoteEntity note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("note", note);
        startActivity(intent);
    }

    @Override public void onNoteDelete(NoteEntity note) {
        HapticFeedbackUtil.warning();
        String title = note.getTitle() != null && !note.getTitle().isEmpty() ? note.getTitle() : "笔记";
        new android.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("删除笔记")
                .setMessage("确定要永久删除「" + title + "」吗？\n\n此操作不可撤销。")
                .setPositiveButton("删除", (d, w) -> {
                    HapticFeedbackUtil.success();
                    new Thread(() -> database.noteDao().delete(note)).start();
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null).show();
    }

    @Override public void onNoteShare(NoteEntity note) { ShareUtil.shareNoteAsText(this, note); }
    @Override public void onNoteShareImages(NoteEntity note) { ShareUtil.shareNoteWithImages(this, note); }
    @Override public void onNoteCopy(NoteEntity note) { 
        ShareUtil.copyToClipboard(this, note); 
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
}
