package com.example.noteapp.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import com.example.noteapp.db.NoteDatabase;
import com.example.noteapp.db.NoteEntity;
import com.example.noteapp.db.NoteVersionEntity;
import com.example.noteapp.db.TimeCapsuleEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 备份和恢复工具类
 * 支持密码加密的导出和导入笔记数据（含所有关联数据）
 */
public class BackupRestoreUtil {

    private static final String BACKUP_DIR = "NoteApp-Backup";
    private static final String BACKUP_EXTENSION = ".nbk";
    private static final String MAGIC_HEADER = "NOTEAPP_ENCRYPTED_BACKUP_V2";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * 导出所有数据为加密备份文件（使用SAF输出流）
     */
    public static boolean exportAllData(Context context, NoteDatabase database, String password, OutputStream outputStream) {
        try {
            JSONObject root = buildFullBackupJson(database);
            String jsonData = root.toString();

            // 加密
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            byte[] key = deriveKey(password, salt);

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(jsonData.getBytes("UTF-8"));

            // 构建加密文件结构: HEADER + \n + salt(base64) + \n + iv(base64) + \n + data(base64)
            StringBuilder sb = new StringBuilder();
            sb.append(MAGIC_HEADER).append("\n");
            sb.append(Base64.encodeToString(salt, Base64.NO_WRAP)).append("\n");
            sb.append(Base64.encodeToString(iv, Base64.NO_WRAP)).append("\n");
            sb.append(Base64.encodeToString(encrypted, Base64.NO_WRAP));

            outputStream.write(sb.toString().getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 导出所有数据为加密备份文件（传统文件路径方式）
     * @return 备份文件路径，如果失败返回null
     */
    public static String exportAllData(Context context, NoteDatabase database, String password) {
        try {
            File dir = getBackupDirectory();
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
            File backupFile = new File(dir, "backup_" + timestamp + BACKUP_EXTENSION);

            java.io.FileOutputStream fos = new java.io.FileOutputStream(backupFile);
            boolean success = exportAllData(context, database, password, fos);
            return success ? backupFile.getAbsolutePath() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 构建包含所有数据的JSON
     */
    private static JSONObject buildFullBackupJson(NoteDatabase database) throws Exception {
        JSONObject root = new JSONObject();
        root.put("version", 2);
        root.put("exportTime", System.currentTimeMillis());

        // 笔记
        List<NoteEntity> notes = database.noteDao().getAllNotesSync();
        JSONArray notesArray = new JSONArray();
        for (NoteEntity note : notes) {
            JSONObject obj = new JSONObject();
            obj.put("id", note.getId());
            obj.put("title", note.getTitle());
            obj.put("content", note.getContent());
            obj.put("timestamp", note.getTimestamp());
            obj.put("tags", note.getTags());
            obj.put("category", note.getCategory());
            obj.put("color", note.getColor());
            obj.put("imagePaths", note.getImagePaths());
            obj.put("imageLayoutData", note.getImageLayoutData());
            obj.put("isEncrypted", note.isEncrypted());
            obj.put("encryptedContent", note.getEncryptedContent());
            obj.put("isTimeCapsule", note.isTimeCapsule());
            obj.put("openTimestamp", note.getOpenTimestamp());
            notesArray.put(obj);
        }
        root.put("notes", notesArray);

        // 时间胶囊
        List<TimeCapsuleEntity> capsules = database.timeCapsuleDao().getAllSync();
        JSONArray capsulesArray = new JSONArray();
        for (TimeCapsuleEntity cap : capsules) {
            JSONObject obj = new JSONObject();
            obj.put("id", cap.getId());
            obj.put("noteId", cap.getNoteId());
            obj.put("title", cap.getTitle());
            obj.put("content", cap.getContent());
            obj.put("createdTime", cap.getCreatedTime());
            obj.put("scheduledTime", cap.getScheduledTime());
            obj.put("openedTime", cap.getOpenedTime());
            obj.put("status", cap.getStatus());
            obj.put("tags", cap.getTags());
            obj.put("hasNotification", cap.isHasNotification());
            obj.put("reminderMinutesBefore", cap.getReminderMinutesBefore());
            capsulesArray.put(obj);
        }
        root.put("timeCapsules", capsulesArray);

        // 版本历史
        List<NoteVersionEntity> versions = database.noteVersionDao().getAllSync();
        JSONArray versionsArray = new JSONArray();
        for (NoteVersionEntity ver : versions) {
            JSONObject obj = new JSONObject();
            obj.put("id", ver.getId());
            obj.put("noteId", ver.getNoteId());
            obj.put("title", ver.getTitle());
            obj.put("content", ver.getContent());
            obj.put("timestamp", ver.getTimestamp());
            versionsArray.put(obj);
        }
        root.put("noteVersions", versionsArray);

        return root;
    }

    /**
     * 从加密备份文件导入所有数据（使用URI）
     * @return 导入成功的笔记数，密码错误返回-2，其他失败返回-1
     */
    public static int importAllData(Context context, NoteDatabase database, Uri uri, String password) {
        if (uri == null) return -1;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new BufferedInputStream(context.getContentResolver().openInputStream(uri)), "UTF-8"
                    )
            );
            return importFromReader(database, reader, password);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 从Reader解析并导入加密备份
     */
    private static int importFromReader(NoteDatabase database, BufferedReader reader, String password) {
        try {
            // 读取全部内容
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (content.length() > 0) content.append("\n");
                content.append(line);
            }
            reader.close();

            String fileContent = content.toString();

            // 检查是否为加密格式
            if (!fileContent.startsWith(MAGIC_HEADER)) {
                // 尝试作为旧版未加密JSON导入（需要密码验证，但旧格式不支持，直接拒绝）
                return -2;
            }

            // 解析加密结构
            String[] parts = fileContent.split("\n", 4);
            if (parts.length < 4) return -1;

            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] iv = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] encryptedData = Base64.decode(parts[3], Base64.NO_WRAP);

            // 派生密钥并解密
            byte[] key = deriveKey(password, salt);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted;
            try {
                decrypted = cipher.doFinal(encryptedData);
            } catch (Exception e) {
                // 密码错误导致解密失败
                return -2;
            }

            String jsonStr = new String(decrypted, "UTF-8");
            JSONObject root = new JSONObject(jsonStr);

            int imported = 0;

            // 导入笔记
            if (root.has("notes")) {
                JSONArray notesArray = root.getJSONArray("notes");
                for (int i = 0; i < notesArray.length(); i++) {
                    JSONObject obj = notesArray.getJSONObject(i);
                    NoteEntity note = new NoteEntity();
                    note.setTitle(obj.optString("title", ""));
                    note.setContent(obj.optString("content", ""));
                    note.setTimestamp(obj.optLong("timestamp", System.currentTimeMillis()));
                    note.setTags(obj.optString("tags", ""));
                    note.setCategory(obj.optString("category", "默认"));
                    note.setColor(obj.optString("color", "#FFFFFF"));
                    note.setImagePaths(obj.optString("imagePaths", ""));
                    note.setImageLayoutData(obj.optString("imageLayoutData", ""));
                    note.setEncrypted(obj.optBoolean("isEncrypted", false));
                    note.setEncryptedContent(obj.optString("encryptedContent", ""));
                    note.setTimeCapsule(obj.optBoolean("isTimeCapsule", false));
                    note.setOpenTimestamp(obj.optLong("openTimestamp", 0));

                    int oldId = obj.optInt("id", -1);
                    long newId = database.noteDao().insert(note);

                    // 导入关联的时间胶囊（根据旧noteId匹配）
                    if (root.has("timeCapsules")) {
                        JSONArray capsulesArray = root.getJSONArray("timeCapsules");
                        for (int j = 0; j < capsulesArray.length(); j++) {
                            JSONObject capObj = capsulesArray.getJSONObject(j);
                            if (capObj.optInt("noteId", -1) == oldId) {
                                TimeCapsuleEntity cap = new TimeCapsuleEntity();
                                cap.setNoteId((int) newId);
                                cap.setTitle(capObj.optString("title", ""));
                                cap.setContent(capObj.optString("content", ""));
                                cap.setCreatedTime(capObj.optLong("createdTime", 0));
                                cap.setScheduledTime(capObj.optLong("scheduledTime", 0));
                                cap.setOpenedTime(capObj.optLong("openedTime", 0));
                                cap.setStatus(capObj.optInt("status", 0));
                                cap.setTags(capObj.optString("tags", ""));
                                cap.setHasNotification(capObj.optBoolean("hasNotification", true));
                                cap.setReminderMinutesBefore(capObj.optInt("reminderMinutesBefore", 0));
                                database.timeCapsuleDao().insert(cap);
                            }
                        }
                    }

                    // 导入关联的版本历史
                    if (root.has("noteVersions")) {
                        JSONArray versionsArray = root.getJSONArray("noteVersions");
                        for (int j = 0; j < versionsArray.length(); j++) {
                            JSONObject verObj = versionsArray.getJSONObject(j);
                            if (verObj.optInt("noteId", -1) == oldId) {
                                NoteVersionEntity ver = new NoteVersionEntity((int) newId, verObj.optString("title", ""), verObj.optString("content", ""));
                                ver.setTimestamp(verObj.optLong("timestamp", 0));
                                database.noteVersionDao().insert(ver);
                            }
                        }
                    }

                    imported++;
                }
            }

            // 导入不关联笔记的时间胶囊（noteId==0）
            if (root.has("timeCapsules")) {
                JSONArray capsulesArray = root.getJSONArray("timeCapsules");
                for (int j = 0; j < capsulesArray.length(); j++) {
                    JSONObject capObj = capsulesArray.getJSONObject(j);
                    if (capObj.optInt("noteId", 0) == 0) {
                        TimeCapsuleEntity cap = new TimeCapsuleEntity();
                        cap.setNoteId(0);
                        cap.setTitle(capObj.optString("title", ""));
                        cap.setContent(capObj.optString("content", ""));
                        cap.setCreatedTime(capObj.optLong("createdTime", 0));
                        cap.setScheduledTime(capObj.optLong("scheduledTime", 0));
                        cap.setOpenedTime(capObj.optLong("openedTime", 0));
                        cap.setStatus(capObj.optInt("status", 0));
                        cap.setTags(capObj.optString("tags", ""));
                        cap.setHasNotification(capObj.optBoolean("hasNotification", true));
                        cap.setReminderMinutesBefore(capObj.optInt("reminderMinutesBefore", 0));
                        database.timeCapsuleDao().insert(cap);
                    }
                }
            }

            return imported;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 从密码和盐值派生AES-256密钥（PBKDF2-like with SHA-256 iterations）
     */
    private static byte[] deriveKey(String password, byte[] salt) throws Exception {
        // 使用多轮SHA-256模拟PBKDF2
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] input = password.getBytes("UTF-8");
        byte[] combined = new byte[input.length + salt.length];
        System.arraycopy(input, 0, combined, 0, input.length);
        System.arraycopy(salt, 0, combined, input.length, salt.length);
        byte[] hash = digest.digest(combined);
        for (int i = 0; i < 10000; i++) {
            hash = digest.digest(hash);
        }
        return hash;
    }

    /**
     * 获取备份目录
     */
    private static File getBackupDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_DIR);
    }

    /**
     * 导出为文本格式（不加密，仅纯文本，不含敏感数据）
     */
    public static String exportNotesToText(Context context, NoteDatabase database) {
        try {
            File dir = getBackupDirectory();
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
            File backupFile = new File(dir, "notes_export_" + timestamp + ".txt");

            List<NoteEntity> notes = database.noteDao().getAllNotesSync();
            StringBuilder content = new StringBuilder();
            for (NoteEntity note : notes) {
                content.append("=".repeat(50)).append("\n");
                content.append("标题: ").append(note.getTitle()).append("\n");
                content.append("分类: ").append(note.getCategory()).append("\n");
                content.append("标签: ").append(note.getTags()).append("\n");
                content.append("时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date(note.getTimestamp()))).append("\n");
                if (note.isTimeCapsule()) {
                    content.append("时间胶囊: 是 (开启时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(new Date(note.getOpenTimestamp()))).append(")\n");
                }
                content.append("-".repeat(50)).append("\n");
                content.append(note.getContent()).append("\n\n");
            }

            FileWriter writer = new FileWriter(backupFile);
            writer.write(content.toString());
            writer.close();
            return backupFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
