package com.example.noteapp;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntity;
import com.example.noteapp.db.NoteVersionEntity;
import com.example.noteapp.ui.components.MediaStickerView;
import com.example.noteapp.utils.AnimationManager;
import com.example.noteapp.utils.EncryptionUtil;
import com.example.noteapp.utils.HapticFeedbackUtil;
import com.example.noteapp.utils.ImagePickerUtil;
import com.example.noteapp.utils.ImageUtil;
import com.example.noteapp.utils.TagManager;
import com.example.noteapp.utils.TextFormattingUtil;
import com.example.noteapp.utils.ThemeModeManager;
import com.example.noteapp.utils.TimeCapsuleManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 编辑笔记活动 - 支持实时保存和多媒体贴纸（图片、视频、录音）
 */
public class EditNoteActivity extends AppCompatActivity {
    
    private EditText titleEditText;
    private EditText contentEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private ImageButton backButton, moreButton, timeCapsuleButton, imageButton;
    private TextView dateTextView, savedBadge;
    private FrameLayout stickerContainer;
    
    private NoteDatabase database;
    private NoteEntity currentNote;
    private TagManager tagManager;
    private EncryptionUtil encryptionUtil;
    private ImagePickerUtil imagePickerUtil;
    
    private boolean isMarkedAsCapsule = false;
    private long capsuleOpenTime = 0;
    private boolean isAutoSaving = false;

    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private ActivityResultLauncher<Intent> audioPickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeModeManager.applySavedMode(this);
        ThemeModeManager.applyTheme(this);
        setContentView(R.layout.activity_edit_note);
        
        initDatabase();
        initViews();
        initPickers();
        loadNote();
        setupAutoSave();
    }
    
    private void initViews() {
        titleEditText = findViewById(R.id.et_title);
        contentEditText = findViewById(R.id.et_content);
        saveButton = findViewById(R.id.btn_save);
        backButton = findViewById(R.id.btn_back);
        moreButton = findViewById(R.id.btn_more);
        timeCapsuleButton = findViewById(R.id.btn_time_capsule);
        imageButton = findViewById(R.id.btn_image);
        dateTextView = findViewById(R.id.tv_edit_date);
        savedBadge = findViewById(R.id.tv_saved_badge);
        stickerContainer = findViewById(R.id.sticker_container);
        
        updateDateDisplay(new Date().getTime());
        
        categorySpinner = findViewById(R.id.spinner_category);
        List<String> categories = tagManager.getCategoriesList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categorySpinner.setAdapter(adapter);
        
        setupListeners();
        applyThemeToToolbar();
        playEditorIntroAnimations();
    }

    private void applyThemeToToolbar() {
        int currentTheme = ThemeModeManager.getCustomTheme(this);
        boolean isDark = (currentTheme == ThemeModeManager.THEME_DARK) ||
                (currentTheme == ThemeModeManager.THEME_SYSTEM && (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        View toolbar = findViewById(R.id.bottom_toolbar_container);
        if (toolbar == null) return;
        View innerToolbar = ((android.view.ViewGroup) toolbar).getChildAt(0);
        if (innerToolbar == null) return;

        GradientDrawable bg = new GradientDrawable();
        if (isDark) {
            // 深色模式：深色玻璃质感
            bg.setColor(0xCC1C2128);
            bg.setStroke(dpToPx(1), 0x40FFFFFF);
        } else if (currentTheme == ThemeModeManager.THEME_EYE_CARE) {
            // 护眼模式：暖色磨砂玻璃
            bg.setColor(0xE6D4C4A0);
            bg.setStroke(dpToPx(1), 0x30C8B898);
        } else {
            // 白天模式：光洁磨砂玻璃
            bg.setColor(0xE6E8E4E0);
            bg.setStroke(dpToPx(1), 0x30B0A8A0);
        }
        bg.setCornerRadius(dpToPx(28));
        innerToolbar.setBackground(bg);
        innerToolbar.setElevation(isDark ? dpToPx(12) : dpToPx(4));

        // 设置图标颜色
        int iconColor = isDark ? 0xFFFFFFFF : 0xFF4A4035;
        int iconSecondary = isDark ? 0xB0FFFFFF : 0x994A4035;
        int accentBlue = ContextCompat.getColor(this, R.color.accent_blue);

        ImageButton btnBold = findViewById(R.id.btn_bold);
        ImageButton btnItalic = findViewById(R.id.btn_italic_text);
        ImageButton btnImage = findViewById(R.id.btn_image);
        ImageButton btnList = findViewById(R.id.btn_list);
        ImageButton btnCapsule = findViewById(R.id.btn_time_capsule);

        if (btnBold != null) btnBold.setColorFilter(iconColor);
        if (btnItalic != null) btnItalic.setColorFilter(iconColor);
        if (btnImage != null) btnImage.setColorFilter(iconColor);
        if (btnList != null) btnList.setColorFilter(iconSecondary);
        if (btnCapsule != null) btnCapsule.setColorFilter(accentBlue);

        // 按钮背景
        int btnBgColor = isDark ? 0x33FFFFFF : 0x20000000;
        int[] btnIds = {R.id.btn_bold, R.id.btn_italic_text, R.id.btn_image, R.id.btn_time_capsule, R.id.btn_list};
        for (int id : btnIds) {
            View btn = findViewById(id);
            if (btn != null) {
                GradientDrawable btnBg = new GradientDrawable();
                btnBg.setColor(btnBgColor);
                btnBg.setCornerRadius(dpToPx(22));
                btn.setBackground(btnBg);
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void initPickers() {
        imagePickerUtil = new ImagePickerUtil(this, new ImagePickerUtil.OnImagePickedListener() {
            @Override
            public void onImagePicked(String imagePath) {
                addMediaSticker(MediaStickerView.Type.IMAGE, imagePath, 100, 100, 500, 500);
            }
            @Override
            public void onImagePickedFromCamera(File file) {
                if (file != null) addMediaSticker(MediaStickerView.Type.IMAGE, file.getAbsolutePath(), 100, 100, 500, 500);
            }
            @Override public void onImagePickedFromGallery(Uri uri) {}
            @Override public void onError(String error) { Toast.makeText(EditNoteActivity.this, error, Toast.LENGTH_SHORT).show(); }
        });

        videoPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri videoUri = result.getData().getData();
                String localPath = copyMediaToLocal(videoUri, "video");
                if (localPath != null) {
                    addMediaSticker(MediaStickerView.Type.VIDEO, localPath, 100, 100, 500, 500);
                } else {
                    Toast.makeText(this, "视频导入失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        audioPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri audioUri = result.getData().getData();
                String localPath = copyMediaToLocal(audioUri, "audio");
                if (localPath != null) {
                    addMediaSticker(MediaStickerView.Type.AUDIO, localPath, 100, 100, 400, 200);
                } else {
                    Toast.makeText(this, "音频导入失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            HapticFeedbackUtil.success(v);
            saveNote();
            finish();
        });
        
        backButton.setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            finish();
        });

        moreButton.setOnClickListener(this::showMoreMenu);

        findViewById(R.id.btn_bold).setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            TextFormattingUtil.makeBold(contentEditText);
        });

        findViewById(R.id.btn_italic_text).setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            TextFormattingUtil.makeItalic(contentEditText);
        });

        imageButton.setOnClickListener(v -> showMediaPickerMenu(v));

        findViewById(R.id.btn_list).setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            int start = contentEditText.getSelectionStart();
            contentEditText.getText().insert(start, "\n• ");
        });

        timeCapsuleButton.setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            showTimeCapsuleDialog();
        });
    }

    private void showMediaPickerMenu(View v) {
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(this, R.style.GlassPopupMenuTheme), v);
        popup.getMenu().add("🖼 图片");
        popup.getMenu().add("🎥 视频");
        popup.getMenu().add("🎙 录音");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("🖼 图片")) imagePickerUtil.openGallery();
            else if (item.getTitle().equals("🎥 视频")) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                videoPickerLauncher.launch(intent);
            } else if (item.getTitle().equals("🎙 录音")) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                audioPickerLauncher.launch(intent);
            }
            return true;
        });
        popup.show();
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                triggerAutoSave();
            }
        };
        titleEditText.addTextChangedListener(watcher);
        contentEditText.addTextChangedListener(watcher);
    }

    private synchronized void triggerAutoSave() {
        if (isAutoSaving) return;
        isAutoSaving = true;
        titleEditText.postDelayed(() -> {
            saveNote();
            isAutoSaving = false;
            runOnUiThread(() -> {
                if (savedBadge != null) {
                    savedBadge.setVisibility(View.VISIBLE);
                    savedBadge.setText("● 实时已保存");
                }
            });
        }, 2000);
    }

    /**
     * 将 content:// URI 的媒体文件复制到应用私有目录，避免退出后 URI 权限失效
     */
    private String copyMediaToLocal(Uri uri, String subDir) {
        try {
            String ext = "";
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null) {
                if (mimeType.contains("mp4")) ext = ".mp4";
                else if (mimeType.contains("3gp")) ext = ".3gp";
                else if (mimeType.contains("webm")) ext = ".webm";
                else if (mimeType.contains("mkv")) ext = ".mkv";
                else if (mimeType.contains("mp3") || mimeType.contains("mpeg")) ext = ".mp3";
                else if (mimeType.contains("ogg")) ext = ".ogg";
                else if (mimeType.contains("wav")) ext = ".wav";
                else if (mimeType.contains("m4a") || mimeType.contains("mp4a")) ext = ".m4a";
                else ext = ".bin";
            }
            java.io.File dir = new java.io.File(getFilesDir(), "media/" + subDir);
            if (!dir.exists()) dir.mkdirs();
            java.io.File outFile = new java.io.File(dir, System.currentTimeMillis() + ext);
            try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                 java.io.OutputStream out = new java.io.FileOutputStream(outFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void addMediaSticker(MediaStickerView.Type type, String path, float x, float y, int w, int h) {
        Bitmap thumb = (type == MediaStickerView.Type.IMAGE) ? ImageUtil.loadBitmap(path) : null;
        MediaStickerView sticker = new MediaStickerView(this, type, path, thumb);
        sticker.setPosition(x, y);
        sticker.setSize(w, h);
        sticker.setLayoutChangeListener(this::saveNote);
        stickerContainer.addView(sticker);
        saveNote();
    }

    private String serializeStickers() {
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < stickerContainer.getChildCount(); i++) {
                View v = stickerContainer.getChildAt(i);
                if (v instanceof MediaStickerView) {
                    MediaStickerView sv = (MediaStickerView) v;
                    JSONObject obj = new JSONObject();
                    obj.put("type", sv.getType().name());
                    obj.put("path", sv.getMediaPath());
                    obj.put("x", sv.getX());
                    obj.put("y", sv.getY());
                    obj.put("w", sv.getLayoutParams().width);
                    obj.put("h", sv.getLayoutParams().height);
                    array.put(obj);
                }
            }
            return array.toString();
        } catch (Exception e) { return ""; }
    }

    private void deserializeStickers(String data) {
        if (data == null || data.isEmpty()) return;
        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                addMediaSticker(MediaStickerView.Type.valueOf(obj.getString("type")), 
                    obj.getString("path"), (float) obj.getDouble("x"), (float) obj.getDouble("y"),
                    obj.getInt("w"), obj.getInt("h"));
            }
        } catch (Exception ignored) {}
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String layoutData = serializeStickers();
        
        new Thread(() -> {
            String contentToSave = encryptionUtil.encryptWithMaster(content);
            if (currentNote != null) {
                currentNote.setTitle(title);
                currentNote.setContent(contentToSave);
                currentNote.setImageLayoutData(layoutData);
                currentNote.setTimestamp(System.currentTimeMillis());
                currentNote.setTimeCapsule(isMarkedAsCapsule);
                currentNote.setOpenTimestamp(capsuleOpenTime);
                database.noteDao().update(currentNote);
            } else {
                currentNote = new NoteEntity(title, contentToSave);
                currentNote.setEncrypted(true);
                currentNote.setImageLayoutData(layoutData);
                currentNote.setTimeCapsule(isMarkedAsCapsule);
                currentNote.setOpenTimestamp(capsuleOpenTime);
                long id = database.noteDao().insert(currentNote);
                currentNote.setId((int)id);
            }
        }).start();
    }

    private void showMoreMenu(View v) {
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(this, R.style.GlassPopupMenuTheme), v);
        popup.getMenu().add("🏷 添加标签");
        popup.getMenu().add("🕰 历史版本");
        popup.getMenu().add("📤 分享笔记");
        popup.getMenu().add("🗑 删除笔记");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("添加标签")) showAddTagDialog();
            else if (title.contains("历史版本")) showHistoryVersions();
            else if (title.contains("分享")) { if (currentNote != null) com.example.noteapp.utils.ShareUtil.shareNoteAsText(this, currentNote); }
            else if (title.contains("删除")) deleteNote();
            return true;
        });
        popup.show();
    }

    private void showAddTagDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        EditText input = dialogView.findViewById(R.id.et_tag_input);
        Button btnCancel = dialogView.findViewById(R.id.btn_tag_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_tag_confirm);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String tag = input.getText().toString().trim();
            if (!tag.isEmpty()) {
                if (currentNote == null) currentNote = new NoteEntity(titleEditText.getText().toString(), "");
                currentNote.addTag(tag);
                Toast.makeText(this, "已添加标签: " + tag, Toast.LENGTH_SHORT).show();
                saveNote();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showHistoryVersions() {
        if (currentNote == null) {
            Toast.makeText(this, "新笔记暂无历史版本", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            List<NoteVersionEntity> versions = database.noteVersionDao().getVersionsForNote(currentNote.getId());
            runOnUiThread(() -> {
                if (versions.isEmpty()) { Toast.makeText(this, "暂无历史记录", Toast.LENGTH_SHORT).show(); return; }
                String[] items = new String[versions.size()];
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                for (int i = 0; i < versions.size(); i++) {
                    items[i] = "版本 " + (i + 1) + " (" + sdf.format(new Date(versions.get(i).getTimestamp())) + ")";
                }
                new android.app.AlertDialog.Builder(this).setTitle("恢复到历史版本")
                        .setItems(items, (d, which) -> restoreVersion(versions.get(which))).show();
            });
        }).start();
    }

    private void restoreVersion(NoteVersionEntity version) {
        titleEditText.setText(version.getTitle());
        String rawContent = version.getContent();
        try { contentEditText.setText(encryptionUtil.decryptWithMaster(rawContent)); }
        catch (Exception e) { contentEditText.setText(rawContent); }
        Toast.makeText(this, "已从备份恢复", Toast.LENGTH_SHORT).show();
        saveNote();
    }

    private void showTimeCapsuleDialog() {
        // 使用中文 locale 上下文，确保 DatePicker spinner 显示中文
        android.content.res.Configuration config = new android.content.res.Configuration(getResources().getConfiguration());
        config.setLocale(Locale.CHINA);
        android.content.Context zhContext = createConfigurationContext(config);
        View dialogView = LayoutInflater.from(zhContext).inflate(R.layout.dialog_time_capsule, null);

        DatePicker datePicker = dialogView.findViewById(R.id.date_picker_capsule);
        Button btnCancel = dialogView.findViewById(R.id.btn_capsule_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_capsule_confirm);

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        datePicker.setMinDate(minDate.getTimeInMillis());

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
            isMarkedAsCapsule = true;
            capsuleOpenTime = calendar.getTimeInMillis();
            timeCapsuleButton.setColorFilter(ContextCompat.getColor(this, R.color.ios_orange));
            android.widget.Toast.makeText(this, "时间胶囊已封存", android.widget.Toast.LENGTH_SHORT).show();
            saveNote();
            dialog.dismiss();
        });
        dialog.show();
        // show 之后设置宽度确保生效
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initDatabase() {
        database = NoteDatabase.getInstance(this);
        tagManager = TagManager.getInstance(this);
        encryptionUtil = EncryptionUtil.getInstance(this);
        TimeCapsuleManager.getInstance(this).startBackgroundChecking();
    }

    private void updateDateDisplay(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINESE);
        dateTextView.setText(sdf.format(new Date(timestamp)));
    }

    private void playEditorIntroAnimations() {
        AnimationManager.createSpringInAnimation(titleEditText, 100);
        AnimationManager.createSpringInAnimation(contentEditText, 200);
        AnimationManager.createSpringInAnimation(findViewById(R.id.bottom_toolbar_container), 400);
    }

    private void loadNote() {
        if (getIntent().hasExtra("note")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentNote = getIntent().getSerializableExtra("note", NoteEntity.class);
            } else {
                currentNote = (NoteEntity) getIntent().getSerializableExtra("note");
            }
            if (currentNote != null) {
                titleEditText.setText(currentNote.getTitle());
                String rawContent = currentNote.getContent();
                try { contentEditText.setText(encryptionUtil.decryptWithMaster(rawContent)); }
                catch (Exception e) { contentEditText.setText(rawContent); }
                deserializeStickers(currentNote.getImageLayoutData());
                updateDateDisplay(currentNote.getTimestamp());
                isMarkedAsCapsule = currentNote.isTimeCapsule();
                capsuleOpenTime = currentNote.getOpenTimestamp();
                if (isMarkedAsCapsule) timeCapsuleButton.setColorFilter(ContextCompat.getColor(this, R.color.ios_orange));
                if (savedBadge != null) {
                    savedBadge.setVisibility(View.VISIBLE);
                    savedBadge.setText("● 已加密加载");
                }
            }
        }
    }

    private void deleteNote() {
        if (currentNote != null) {
            new android.app.AlertDialog.Builder(this).setTitle("删除笔记").setMessage("确定要永久删除吗？")
                    .setPositiveButton("删除", (d, w) -> {
                        new Thread(() -> {
                            database.noteDao().delete(currentNote);
                            runOnUiThread(this::finish);
                        }).start();
                    }).setNegativeButton("取消", null).show();
        } else finish();
    }
}
