package com.example.noteapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.utils.BackupRestoreUtil;
import com.example.noteapp.utils.HapticFeedbackUtil;
import com.example.noteapp.utils.SearchManager;
import com.example.noteapp.utils.ThemeModeManager;
import com.example.noteapp.utils.TimeCapsuleManager;

/**
 * 设置页面 - 采用卡片式布局
 */
public class ProfileActivity extends AppCompatActivity {

    private NoteDatabase database;
    private SearchManager searchManager;
    private TimeCapsuleManager timeCapsuleManager;
    private ActivityResultLauncher<String[]> importBackupLauncher;
    private ActivityResultLauncher<String> exportBackupLauncher;
    private String pendingExportPassword;
    private String pendingImportPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeModeManager.applySavedMode(this);
        ThemeModeManager.applyTheme(this);

        importBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::handleImportUri
        );

        exportBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(),
            this::handleExportUri
        );

        setContentView(R.layout.activity_profile);

        database = NoteDatabase.getInstance(this);
        searchManager = new SearchManager(this);
        timeCapsuleManager = TimeCapsuleManager.getInstance(this);

        initViews();
    }

    private void initViews() {
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            HapticFeedbackUtil.tap(v);
            finish();
        });

        findViewById(R.id.btn_backup_layout).setOnClickListener(v -> showExportPasswordDialog());
        findViewById(R.id.btn_import_layout).setOnClickListener(v -> showImportPasswordDialog());
        findViewById(R.id.btn_export_layout).setOnClickListener(v -> performExportText());
        findViewById(R.id.btn_clear_history_layout).setOnClickListener(v -> clearSearchHistory());

        findViewById(R.id.btn_settings_layout).setOnClickListener(v -> showSettings());
        findViewById(R.id.btn_advanced_search_layout).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_search", true);
            startActivity(intent);
        });
    }

    private void showSettings() {
        int currentTheme = ThemeModeManager.getCustomTheme(this);
        CharSequence[] themeOptions = {"☀️ 白天模式", "🌙 深色模式", "📱 跟随系统", "🌿 护眼模式"};
        new AlertDialog.Builder(this)
                .setTitle("显示模式")
                .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                    ThemeModeManager.setCustomTheme(this, which);
                    dialog.dismiss();
                    recreate();
                })
                .show();
    }

    // ==================== 导出（加密备份） ====================

    private void showExportPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView hint = new TextView(this);
        hint.setText("请设置备份密码，导入时需要此密码才能解开数据");
        hint.setTextSize(13);
        hint.setPadding(0, 0, 0, 20);
        layout.addView(hint);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("输入密码");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        EditText confirmInput = new EditText(this);
        confirmInput.setHint("再次确认密码");
        confirmInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmInput);

        new AlertDialog.Builder(this)
                .setTitle("🔐 设置备份密码")
                .setView(layout)
                .setPositiveButton("导出", (dialog, which) -> {
                    String pwd = passwordInput.getText().toString();
                    String confirm = confirmInput.getText().toString();
                    if (pwd.isEmpty()) {
                        Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!pwd.equals(confirm)) {
                        Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pendingExportPassword = pwd;
                    exportBackupLauncher.launch("NoteApp_backup.nbk");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void handleExportUri(Uri uri) {
        if (uri == null || pendingExportPassword == null) return;
        String password = pendingExportPassword;
        pendingExportPassword = null;

        new Thread(() -> {
            try {
                java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                boolean success = BackupRestoreUtil.exportAllData(this, database, password, os);
                runOnUiThread(() -> {
                    if (success) {
                        HapticFeedbackUtil.success();
                        Toast.makeText(this, "✅ 加密备份导出成功", Toast.LENGTH_SHORT).show();
                    } else {
                        HapticFeedbackUtil.error();
                        Toast.makeText(this, "❌ 导出失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    HapticFeedbackUtil.error();
                    Toast.makeText(this, "❌ 导出失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // ==================== 导入（解密备份） ====================

    private void showImportPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView hint = new TextView(this);
        hint.setText("请输入备份文件的密码，密码错误将无法导入");
        hint.setTextSize(13);
        hint.setPadding(0, 0, 0, 20);
        layout.addView(hint);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("输入备份密码");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        new AlertDialog.Builder(this)
                .setTitle("🔑 输入备份密码")
                .setView(layout)
                .setPositiveButton("选择文件并导入", (dialog, which) -> {
                    String pwd = passwordInput.getText().toString();
                    if (pwd.isEmpty()) {
                        Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pendingImportPassword = pwd;
                    importBackupLauncher.launch(new String[]{"*/*"});
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void handleImportUri(Uri uri) {
        if (uri == null || pendingImportPassword == null) return;
        String password = pendingImportPassword;
        pendingImportPassword = null;

        new Thread(() -> {
            int result = BackupRestoreUtil.importAllData(this, database, uri, password);
            runOnUiThread(() -> {
                if (result == -2) {
                    HapticFeedbackUtil.error();
                    Toast.makeText(this, "❌ 密码错误，无法解密备份文件", Toast.LENGTH_LONG).show();
                } else if (result >= 0) {
                    HapticFeedbackUtil.success();
                    Toast.makeText(this, "✅ 成功导入 " + result + " 条笔记", Toast.LENGTH_SHORT).show();
                } else {
                    HapticFeedbackUtil.error();
                    Toast.makeText(this, "❌ 导入失败，文件格式不正确", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // ==================== 纯文本导出 ====================

    private void performExportText() {
        new Thread(() -> {
            String exportPath = BackupRestoreUtil.exportNotesToText(this, database);
            runOnUiThread(() -> {
                if (exportPath != null) {
                    HapticFeedbackUtil.success();
                    Toast.makeText(this, "✅ 文本导出成功\n" + exportPath, Toast.LENGTH_LONG).show();
                } else {
                    HapticFeedbackUtil.error();
                    Toast.makeText(this, "❌ 导出失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // ==================== 清除搜索历史 ====================

    private void clearSearchHistory() {
        new AlertDialog.Builder(this)
                .setTitle("清除历史")
                .setMessage("确定要清除搜索历史吗？")
                .setPositiveButton("清除", (dialog, which) -> {
                    searchManager.clearSearchHistory();
                    HapticFeedbackUtil.success();
                    Toast.makeText(this, "✅ 已清除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
