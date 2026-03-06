package com.example.noteapp.utils;

import android.annotation.SuppressLint;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

/**
 * 笔记加密工具类
 * 使用AES-256加密算法
 */
public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";
    private static final String PREFS_NAME = "NoteEncryption";
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    
    private static EncryptionUtil instance;
    private SharedPreferences sharedPreferences;
    protected byte[] encryptionKey;
    
    private EncryptionUtil(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadOrCreateKey();
    }
    
    /**
     * 获取单例实例
     */
    public static EncryptionUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (EncryptionUtil.class) {
                if (instance == null) {
                    instance = new EncryptionUtil(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载或创建加密密钥
     */
    private void loadOrCreateKey() {
        String savedKey = sharedPreferences.getString("encryption_key", null);
        if (savedKey != null) {
            // 解码已保存的密钥
            encryptionKey = Base64.decode(savedKey, Base64.DEFAULT);
        } else {
            // 生成新密钥
            encryptionKey = generateKey();
            String encodedKey = Base64.encodeToString(encryptionKey, Base64.DEFAULT);
            sharedPreferences.edit().putString("encryption_key", encodedKey).apply();
        }
    }
    
    /**
     * 生成256位AES密钥
     */
    private byte[] generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate encryption key", e);
            throw new RuntimeException("Encryption key generation failed", e);
        }
    }
    
    /**
     * 使用密码加密文本
     * @param plainText 明文
     * @param password 密码（可选，如果提供则使用密码生成密钥）
     * @return 加密后的文本（Base64编码）
     */
    public String encrypt(String plainText, String password) {
        try {
            byte[] key = password != null ? deriveKeyFromPassword(password) : encryptionKey;
            SecretKeySpec secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
            
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * 使用密码解密文本
     * @param encryptedText 加密文本（Base64编码）
     * @param password 密码（可选，如果提供则使用密码生成密钥）
     * @return 解密后的明文
     */
    public String decrypt(String encryptedText, String password) {
        try {
            byte[] key = password != null ? deriveKeyFromPassword(password) : encryptionKey;
            SecretKeySpec secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
            
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encrypted = Base64.decode(encryptedText, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * 从密码派生256位密钥
     * 使用简单的摘要算法，实际应用应使用PBKDF2
     */
    private byte[] deriveKeyFromPassword(String password) {
        try {
            // 使用SHA-256哈希密码
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            
            // 确保密钥长度为32字节（256位）
            byte[] key = new byte[32];
            System.arraycopy(hash, 0, key, 0, Math.min(hash.length, 32));
            
            return key;
        } catch (Exception e) {
            Log.e(TAG, "Key derivation failed", e);
            throw new RuntimeException("Key derivation failed", e);
        }
    }
    
    /**
     * 验证密码是否正确
     * @param encryptedText 加密文本
     * @param password 要验证的密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(String encryptedText, String password) {
        try {
            String decrypted = decrypt(encryptedText, password);
            return decrypted != null && !decrypted.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 设置主密码（用于快速加密/解密）
     */
    public void setMasterPassword(String password) {
        try {
            encryptionKey = deriveKeyFromPassword(password);
            String encodedKey = Base64.encodeToString(encryptionKey, Base64.DEFAULT);
            sharedPreferences.edit().putString("encryption_key", encodedKey).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to set master password", e);
        }
    }
    
    /**
     * 使用主密码加密
     */
    public String encryptWithMaster(String plainText) {
        return encrypt(plainText, null);
    }
    
    /**
     * 使用主密码解密
     */
    public String decryptWithMaster(String encryptedText) {
        return decrypt(encryptedText, null);
    }
    
    /**
     * 清空加密密钥（注意：将无法解密使用此密钥加密的内容）
     * 该操作应谨慎使用
     */
    public void clearMasterPassword() {
        try {
            encryptionKey = generateKey();
            String encodedKey = Base64.encodeToString(encryptionKey, Base64.DEFAULT);
            sharedPreferences.edit().putString("encryption_key", encodedKey).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear master password", e);
        }
    }
}
