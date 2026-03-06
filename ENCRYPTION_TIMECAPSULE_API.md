# 🔐 加密和时间胶囊 API 参考指南

本文档提供加密功能和时间胶囊功能的完整 API 参考和代码示例。

---

## 📖 目录

1. [EncryptionUtil API](#encryptionutil-api)
2. [TimeCapsuleManager API](#timecapsulemanager-api)
3. [TimeCapsuleEntity API](#timecapsuleentity-api)
4. [数据访问层 (DAO)](#数据访问层-dao)
5. [集成示例](#集成示例)
6. [常见实现模式](#常见实现模式)

---

## 🔐 EncryptionUtil API

### 简介

`EncryptionUtil` 是笔记加密的核心工具类，提供 AES-256 加密/解密功能。

**位置**: `com.example.noteapp.utils.EncryptionUtil`

### 初始化

#### 获取单例实例

```java
EncryptionUtil encryptionUtil = EncryptionUtil.getInstance(context);
```

**参数**:
- `context`: Android Context 对象

**返回**: EncryptionUtil 单例实例

**异常**: 无

**线程安全**: 是（单例实现）

### 核心方法

#### 1. encrypt() - 使用特定密码加密

```java
public String encrypt(String plainText, String password) throws Exception
```

**功能**: 使用指定密码加密文本

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| plainText | String | 需要加密的明文 |
| password | String | 加密密码 |

**返回**: 
- Base64 编码的加密文本
- 格式: `[Base64-encoded-ciphertext]`

**异常**:
- `Exception` - 加密过程中出错

**示例**:
```java
try {
    String plainText = "这是我的私密笔记";
    String password = "mySecurePassword";
    String encrypted = encryptionUtil.encrypt(plainText, password);
    // encrypted = "U2FsdGVkX1/1K2d..."
} catch (Exception e) {
    Log.e("Encryption", "加密失败: " + e.getMessage());
}
```

#### 2. decrypt() - 使用特定密码解密

```java
public String decrypt(String encryptedText, String password) throws Exception
```

**功能**: 使用指定密码解密文本

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| encryptedText | String | Base64 编码的加密文本 |
| password | String | 解密密码 |

**返回**: 
- 解密后的明文
- 如果返回 `null` 表示密码错误或解密失败

**异常**:
- `Exception` - 解密过程中出错

**示例**:
```java
try {
    String encrypted = "U2FsdGVkX1/1K2d...";
    String password = "mySecurePassword";
    String decrypted = encryptionUtil.decrypt(encrypted, password);
    // decrypted = "这是我的私密笔记"
} catch (Exception e) {
    Log.e("Decryption", "解密失败: " + e.getMessage());
}
```

#### 3. setMasterPassword() - 设置主密钥

```java
public void setMasterPassword(String password) throws Exception
```

**功能**: 设置应用全局主密钥

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| password | String | 主密钥 |

**返回**: void

**异常**:
- `Exception` - 密钥设置失败

**持久化**: 主密钥存储在 SharedPreferences 中

**示例**:
```java
try {
    encryptionUtil.setMasterPassword("AppMasterKey123!");
} catch (Exception e) {
    Toast.makeText(context, "密钥设置失败", Toast.LENGTH_SHORT).show();
}
```

#### 4. encryptWithMaster() - 使用主密钥加密

```java
public String encryptWithMaster(String plainText) throws Exception
```

**功能**: 使用设置的主密钥加密文本（无需传入密码）

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| plainText | String | 明文 |

**返回**: Base64 编码的加密文本

**异常**:
- `Exception` - 加密失败或主密钥未设置

**示例**:
```java
String plainText = "笔记内容";
String encrypted = encryptionUtil.encryptWithMaster(plainText);
note.setEncryptedContent(encrypted);
note.setEncrypted(true);
```

#### 5. decryptWithMaster() - 使用主密钥解密

```java
public String decryptWithMaster(String encryptedText) throws Exception
```

**功能**: 使用主密钥解密文本

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| encryptedText | String | 加密文本 |

**返回**: 解密后的明文

**异常**:
- `Exception` - 解密失败

**示例**:
```java
try {
    String decrypted = encryptionUtil.decryptWithMaster(note.getEncryptedContent());
    contentTextView.setText(decrypted);
} catch (Exception e) {
    Toast.makeText(context, "解密失败", Toast.LENGTH_SHORT).show();
}
```

#### 6. verifyPassword() - 验证密码

```java
public boolean verifyPassword(String encryptedText, String password)
```

**功能**: 检查密码是否与加密文本匹配，无需完整解密

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| encryptedText | String | 加密文本 |
| password | String | 要验证的密码 |

**返回**: 
- `true` - 密码正确
- `false` - 密码错误或文本被损坏

**异常**: 无

**示例**:
```java
String password = userInput.getText().toString();
if (encryptionUtil.verifyPassword(encryptedText, password)) {
    // 密码正确，可以解密
    String decrypted = encryptionUtil.decrypt(encryptedText, password);
} else {
    Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
}
```

#### 7. deriveKeyFromPassword() - 密钥派生

```java
public SecretKey deriveKeyFromPassword(String password) throws Exception
```

**功能**: 从密码派生加密密钥（SHA-256）

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| password | String | 输入密码 |

**返回**: 256 位 SecretKey 对象

**异常**:
- `Exception` - 密钥派生失败

**说明**: 此方法内部使用，一般不需要直接调用

---

## ⏰ TimeCapsuleManager API

### 简介

`TimeCapsuleManager` 管理时间胶囊的生命周期、后台检查和通知。

**位置**: `com.example.noteapp.utils.TimeCapsuleManager`

### 初始化

#### 获取单例

```java
TimeCapsuleManager manager = TimeCapsuleManager.getInstance(context);
```

**参数**:
- `context`: Android Context

**返回**: TimeCapsuleManager 单例

**推荐位置**: `MainActivity.onCreate()`

### 生命周期方法

#### 1. startBackgroundChecking() - 启动后台监控

```java
public void startBackgroundChecking()
```

**功能**: 启动 60 秒间隔的后台计时器

**行为**:
- 每 60 秒检查一次所有待打开的胶囊
- 如果胶囊到期，自动标记为已打开
- 发送通知给用户

**线程**: 在后台线程上运行

**调用方式**:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    TimeCapsuleManager manager = TimeCapsuleManager.getInstance(this);
    manager.startBackgroundChecking();  // 在 onCreate 中启动
    // ...
}
```

**注意**: 此方法应在应用启动时调用一次

#### 2. stopBackgroundChecking() - 停止后台监控

```java
public void stopBackgroundChecking()
```

**功能**: 停止后台计时器

**调用场景**:
- 应用进入后台（可选）
- 应用销毁时（推荐）

**示例**:
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    TimeCapsuleManager manager = TimeCapsuleManager.getInstance(this);
    manager.stopBackgroundChecking();  // 清理资源
}
```

### CRUD 方法

#### 3. createCapsule() - 创建新的时间胶囊

```java
public void createCapsule(TimeCapsuleEntity capsule)
```

**功能**: 创建并保存新的时间胶囊

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| capsule | TimeCapsuleEntity | 要创建的胶囊对象 |

**返回**: void

**示例**:
```java
TimeCapsuleEntity capsule = new TimeCapsuleEntity(
    "给一年后的自己",  // title
    "记录当前的想法",   // content
    futureTime          // scheduledTime (毫秒)
);
capsule.setHasNotification(true);
capsule.setReminderMinutesBefore(30);  // 提前 30 分钟提醒

manager.createCapsule(capsule);
Toast.makeText(context, "时间胶囊已创建", Toast.LENGTH_SHORT).show();
```

#### 4. updateCapsule() - 更新胶囊

```java
public void updateCapsule(TimeCapsuleEntity capsule)
```

**功能**: 更新现有胶囊信息

**参数**: TimeCapsuleEntity 对象

**示例**:
```java
capsule.setTitle("新标题");
capsule.setContent("更新内容");
manager.updateCapsule(capsule);
```

#### 5. deleteCapsule() - 删除胶囊

```java
public void deleteCapsule(TimeCapsuleEntity capsule)
```

**功能**: 从数据库删除胶囊

**参数**: TimeCapsuleEntity 对象

**示例**:
```java
manager.deleteCapsule(capsule);
Toast.makeText(context, "胶囊已删除", Toast.LENGTH_SHORT).show();
```

### 查询方法

#### 6. getPendingCapsules() - 获取待打开的胶囊

```java
public LiveData<List<TimeCapsuleEntity>> getPendingCapsules()
```

**功能**: 获取所有状态为"等待中"的胶囊

**返回**: 
- `LiveData<List<TimeCapsuleEntity>>`
- 列表按计划打开时间升序排序

**订阅示例**:
```java
manager.getPendingCapsules().observe(this, capsules -> {
    if (capsules != null && !capsules.isEmpty()) {
        for (TimeCapsuleEntity capsule : capsules) {
            String remaining = manager.getTimeRemainingString(capsule);
            Log.d("Capsule", capsule.getTitle() + " - " + remaining);
        }
    }
});
```

#### 7. getOpenedCapsules() - 获取已打开的胶囊

```java
public LiveData<List<TimeCapsuleEntity>> getOpenedCapsules()
```

**功能**: 获取已打开的胶囊列表

**返回**: 
- `LiveData<List<TimeCapsuleEntity>>`
- 列表按打开时间降序排序

**限制**: 最多返回最近 5 个已打开的胶囊

**示例**:
```java
manager.getOpenedCapsules().observe(this, opened -> {
    if (opened != null) {
        for (TimeCapsuleEntity capsule : opened) {
            String openDate = formatTime(capsule.getOpenedTime());
            Log.d("Opened", capsule.getTitle() + " - 打开于: " + openDate);
        }
    }
});
```

### 工具方法

#### 8. getTimeRemainingString() - 格式化剩余时间

```java
public String getTimeRemainingString(TimeCapsuleEntity capsule)
```

**功能**: 将剩余毫秒数转换为易读格式

**参数**: TimeCapsuleEntity 对象

**返回**: 格式化时间字符串，示例：
- `"365 天"`
- `"30 天 12 小时"`
- `"2 小时 30 分钟"`
- `"15 分钟"`

**示例**:
```java
String remaining = manager.getTimeRemainingString(capsule);
textView.setText("将在 " + remaining + " 后打开");
```

#### 9. showOpenNotification() - 显示打开通知

```java
public void showOpenNotification(TimeCapsuleEntity capsule)
```

**功能**: 向用户显示胶囊已打开的通知

**参数**: TimeCapsuleEntity 对象

**通知内容**:
```
[通知]
⏰ 时间胶囊已打开！
标题：给一年后的自己
```

#### 10. showReminderNotification() - 显示提醒通知

```java
public void showReminderNotification()
```

**功能**: 显示即将打开的胶囊提醒

**触发条件**: 胶囊的 `reminderMinutesBefore` 时间到达

#### 11. createNotificationChannel() - 创建通知频道

```java
private void createNotificationChannel()
```

**功能**: 为 Android 8.0+ 创建通知频道

**频道信息**:
| 属性 | 值 |
|-----|-----|
| Channel ID | "time_capsule_channel" |
| 名称 | "时间胶囊" |
| 重要性 | NotificationManager.IMPORTANCE_DEFAULT |

**自动调用**: 在 `getInstance()` 时自动创建

#### 12. cleanupOldCapsules() - 清理旧胶囊

```java
public void cleanupOldCapsules()
```

**功能**: 删除已打开超过 30 天的胶囊

**清理规则**:
- 只清理状态为"已打开"的胶囊
- 已打开超过 30 天的胶囊将被删除
- 待打开的胶囊不会被清理

**调用建议**:
```java
// 定期清理，例如应用启动时
if (shouldPeriodicCleanup()) {
    manager.cleanupOldCapsules();
}
```

---

## ⏰ TimeCapsuleEntity API

### 简介

`TimeCapsuleEntity` 是时间胶囊的数据模型，使用 Room 框架保存到数据库。

**位置**: `com.example.noteapp.db.TimeCapsuleEntity`

### 构造函数

#### 标准构造函数

```java
public TimeCapsuleEntity(String title, String content, long scheduledTime)
```

**参数**:
| 参数 | 类型 | 说明 |
|-----|------|------|
| title | String | 胶囊标题 |
| content | String | 胶囊内容 |
| scheduledTime | long | 计划打开时间（毫秒时间戳） |

**示例**:
```java
long futureTime = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L);
TimeCapsuleEntity capsule = new TimeCapsuleEntity(
    "一年回顾",
    "回顾这一年的成长和学习",
    futureTime
);
```

### 属性字段

#### 主键和外键

```java
@PrimaryKey(autoGenerate = true)
public int id;

@ForeignKey(entity = NoteEntity.class, 
    parentColumns = "id", 
    childColumns = "noteId",
    onDelete = ForeignKey.CASCADE)
public int noteId;  // 可选：关联的笔记 ID
```

#### 核心字段

| 字段 | 类型 | 说明 | 示例值 |
|-----|------|------|--------|
| `id` | int | 胶囊唯一 ID | 1 |
| `noteId` | int | 关联的笔记 ID（可选） | 101 |
| `title` | String | 胶囊标题 | "给一年后的自己" |
| `content` | String | 胶囊内容 | "记录当前的想法..." |
| `createdTime` | long | 创建时间戳 | 1704067200000 |
| `scheduledTime` | long | 计划打开时间戳 | 1735689600000 |
| `openedTime` | long | 实际打开时间戳 (0 = 未打开) | 0 或时间戳 |
| `status` | int | 胶囊状态 | 0(等待), 1(已打开), 2(已删除) |
| `tags` | String | 标签（JSON 格式）| "[\"生活\", \"回顾\"]" |
| `hasNotification` | boolean | 是否启用通知 | true |
| `reminderMinutesBefore` | int | 提前几分钟提醒 | 30 |

### 方法

#### 1. shouldOpen() - 检查胶囊是否应该打开

```java
public boolean shouldOpen()
```

**功能**: 检查当前时间是否已超过计划打开时间

**返回**:
- `true` - 应该打开（当前时间 >= 计划时间）
- `false` - 还未到打开时间

**示例**:
```java
if (capsule.shouldOpen() && capsule.getStatus() == 0) {
    capsule.markAsOpened();
    manager.updateCapsule(capsule);
    showNotification(capsule);
}
```

#### 2. markAsOpened() - 标记为已打开

```java
public void markAsOpened()
```

**功能**: 将胶囊状态改为"已打开"并记录打开时间

**影响**:
- 设置 `status = 1`
- 设置 `openedTime = System.currentTimeMillis()`

**示例**:
```java
capsule.markAsOpened();
manager.updateCapsule(capsule);
```

#### 3. getTimeRemaining() - 获取剩余毫秒数

```java
public long getTimeRemaining()
```

**功能**: 计算从现在到计划打开时间的毫秒数

**返回**: 
- 正数 - 剩余毫秒数
- 负数 - 已超期毫秒数
- 0 - 恰好到达时间

**示例**:
```java
long remainingMs = capsule.getTimeRemaining();
long days = remainingMs / (24 * 60 * 60 * 1000);
long hours = (remainingMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
Log.d("Time", "剩余: " + days + "天 " + hours + "小时");
```

#### 4. getFormattedScheduledTime() - 获取格式化的计划时间

```java
public String getFormattedScheduledTime()
```

**功能**: 将时间戳格式化为可读的日期时间字符串

**返回**: 格式为 `"yyyy-MM-dd HH:mm"` 的字符串

**示例**:
```java
String formatted = capsule.getFormattedScheduledTime();
// 返回: "2025-12-25 10:30"
textView.setText("将在 " + formatted + " 打开");
```

#### 5. Getter/Setter 方法

**常用 Setters**:
```java
capsule.setTitle("新标题");
capsule.setContent("新内容");
capsule.setStatus(1);  // 0=等待, 1=已打开, 2=已删除
capsule.setHasNotification(true);
capsule.setReminderMinutesBefore(30);
```

**常用 Getters**:
```java
String title = capsule.getTitle();
String content = capsule.getContent();
long createdTime = capsule.getCreatedTime();
long scheduledTime = capsule.getScheduledTime();
long openedTime = capsule.getOpenedTime();
int status = capsule.getStatus();
boolean hasNotif = capsule.isHasNotification();
int reminderMins = capsule.getReminderMinutesBefore();
```

### 标签管理

#### 添加标签

```java
capsule.addTag("生活");
capsule.addTag("回顾");
```

#### 获取标签列表

```java
List<String> tags = capsule.getTagsList();
// 返回: ["生活", "回顾"]
```

#### 说明

- 标签以 JSON 数组格式存储在 `tags` 字段
- 默认为空列表

---

## 📊 数据访问层 (DAO)

### TimeCapsuleDao API

**位置**: `com.example.noteapp.db.TimeCapsuleDao`

#### 1. insert() - 插入新胶囊

```java
@Insert
public abstract void insert(TimeCapsuleEntity capsule);
```

#### 2. update() - 更新胶囊

```java
@Update
public abstract void update(TimeCapsuleEntity capsule);
```

#### 3. delete() - 删除胶囊

```java
@Delete
public abstract void delete(TimeCapsuleEntity capsule);
```

#### 4. getPendingCapsules() - 查询待打开的胶囊

```java
@Query("SELECT * FROM time_capsules WHERE status = 0 ORDER BY scheduledTime ASC")
public abstract LiveData<List<TimeCapsuleEntity>> getPendingCapsules();
```

#### 5. getOpenedCapsules() - 查询已打开的胶囊

```java
@Query("SELECT * FROM time_capsules WHERE status = 1 ORDER BY openedTime DESC LIMIT 5")
public abstract LiveData<List<TimeCapsuleEntity>> getOpenedCapsules();
```

#### 6. getCapsulesReadyToOpen() - 查询到期的胶囊

```java
@Query("SELECT * FROM time_capsules WHERE status = 0 AND scheduledTime <= :currentTime ORDER BY scheduledTime ASC")
public abstract LiveData<List<TimeCapsuleEntity>> getCapsulesReadyToOpen(long currentTime);
```

#### 7. searchCapsules() - 搜索胶囊

```java
@Query("SELECT * FROM time_capsules WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
public abstract LiveData<List<TimeCapsuleEntity>> searchCapsules(String query);
```

#### 8. countPendingCapsules() - 统计待打开胶囊数

```java
@Query("SELECT COUNT(*) FROM time_capsules WHERE status = 0")
public abstract LiveData<Integer> countPendingCapsules();
```

---

## 🔗 集成示例

### 完整的加密笔记工作流

```java
// 1. 在 MainActivity 中初始化
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // 初始化数据库
    NoteDatabase database = Room.databaseBuilder(
        this, NoteDatabase.class, "notes-db"
    ).build();
    
    // 初始化加密工具
    EncryptionUtil encryptionUtil = EncryptionUtil.getInstance(this);
    try {
        encryptionUtil.setMasterPassword("AppDefaultKey123!");
    } catch (Exception e) {
        Log.e("Encryption", "密钥设置失败", e);
    }
    
    // 初始化时间胶囊管理器
    TimeCapsuleManager capsuleManager = TimeCapsuleManager.getInstance(this);
    capsuleManager.startBackgroundChecking();
}

// 2. 在 EditNoteActivity 中加密笔记
private void saveNote() {
    String title = titleEdit.getText().toString();
    String content = contentEdit.getText().toString();
    
    NoteEntity note = new NoteEntity(title, content);
    
    // 如果启用了加密
    if (isEncrypted) {
        try {
            String encrypted = encryptionUtil.encryptWithMaster(content);
            note.setEncrypted(true);
            note.setEncryptedContent(encrypted);
        } catch (Exception e) {
            Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    
    // 保存到数据库
    new Thread(() -> {
        database.noteDao().insert(note);
        runOnUiThread(() -> {
            Toast.makeText(this, "笔记已保存", Toast.LENGTH_SHORT).show();
            finish();
        });
    }).start();
}

// 3. 打开加密笔记
private void loadNote(int noteId) {
    new Thread(() -> {
        NoteEntity note = database.noteDao().getNoteById(noteId);
        
        if (note != null && note.isEncrypted()) {
            // 需要输入密码
            runOnUiThread(() -> showPasswordDialog(note.getEncryptedContent()));
        } else {
            // 直接显示
            runOnUiThread(() -> {
                if (note != null) {
                    titleEdit.setText(note.getTitle());
                    contentEdit.setText(note.getContent());
                }
            });
        }
    }).start();
}

// 4. 创建时间胶囊
private void createTimeCapsule() {
    String title = "给一年后的自己";
    String content = "拍摄了这个美丽的时刻，希望一年后能记起...";
    
    long futureTime = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L);
    
    TimeCapsuleEntity capsule = new TimeCapsuleEntity(title, content, futureTime);
    capsule.setHasNotification(true);
    capsule.setReminderMinutesBefore(60);  // 提前 1 小时提醒
    
    TimeCapsuleManager manager = TimeCapsuleManager.getInstance(this);
    manager.createCapsule(capsule);
    
    Toast.makeText(this, "时间胶囊已创建！将在 365 天后打开", Toast.LENGTH_LONG).show();
}

// 5. 观察待打开的胶囊
private void observePendingCapsules() {
    TimeCapsuleManager manager = TimeCapsuleManager.getInstance(this);
    
    manager.getPendingCapsules().observe(this, capsules -> {
        if (capsules != null && !capsules.isEmpty()) {
            for (int i = 0; i < capsules.size(); i++) {
                TimeCapsuleEntity capsule = capsules.get(i);
                String remaining = manager.getTimeRemainingString(capsule);
                String text = (i + 1) + ". " + capsule.getTitle() + " - " + remaining;
                
                // 添加到列表视图
                listAdapter.add(text);
            }
        } else {
            Toast.makeText(MainActivity.this, "暂无待打开的胶囊", Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

## 🎯 常见实现模式

### 模式 1：加密敏感笔记

```java
public void encryptSensitiveNote(NoteEntity note, String content) 
    throws Exception {
    
    EncryptionUtil util = EncryptionUtil.getInstance(this);
    
    // 加密内容
    String encrypted = util.encryptWithMaster(content);
    
    // 更新笔记
    note.setEncrypted(true);
    note.setEncryptedContent(encrypted);
    note.setTitle("🔒 " + note.getTitle());  // 视觉指示
    
    // 保存
    database.noteDao().update(note);
}
```

### 模式 2：批量创建多个胶囊

```java
public void createMultipleCapsules() {
    String[] titles = {"1月回顾", "3月回顾", "6月回顾", "年度回顾"};
    int[] daysFromNow = {30, 90, 180, 365};
    
    TimeCapsuleManager manager = TimeCapsuleManager.getInstance(this);
    long now = System.currentTimeMillis();
    
    for (int i = 0; i < titles.length; i++) {
        long scheduledTime = now + (daysFromNow[i] * 24L * 60 * 60 * 1000);
        
        TimeCapsuleEntity capsule = new TimeCapsuleEntity(
            titles[i],
            "在 " + daysFromNow[i] + " 天后回顾这段时期...",
            scheduledTime
        );
        capsule.setHasNotification(true);
        
        manager.createCapsule(capsule);
    }
    
    Toast.makeText(this, "已创建 4 个定期回顾胶囊", Toast.LENGTH_SHORT).show();
}
```

### 模式 3：自定义密码加密

```java
public void encryptWithCustomPassword(NoteEntity note, String content, String password) 
    throws Exception {
    
    EncryptionUtil util = EncryptionUtil.getInstance(this);
    
    // 使用自定义密码加密
    String encrypted = util.encrypt(content, password);
    
    note.setEncrypted(true);
    note.setEncryptedContent(encrypted);
    
    // 保存密码提示（不保存密码本身！）
    // note.setPasswordHint("我最喜欢的书的名字");
    
    database.noteDao().update(note);
}

// 使用自定义密码打开笔记
public void decryptWithCustomPassword(NoteEntity note, String password) 
    throws Exception {
    
    EncryptionUtil util = EncryptionUtil.getInstance(this);
    
    String decrypted = util.decrypt(note.getEncryptedContent(), password);
    contentTextView.setText(decrypted);
}
```

### 模式 4：定期清理管理

```java
public void schedulePeriodicMaintenance() {
    // 每周清理一次旧胶囊
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    scheduler.scheduleAtFixedRate(() -> {
        TimeCapsuleManager manager = TimeCapsuleManager.getInstance(MainActivity.this);
        manager.cleanupOldCapsules();
        Log.i("Maintenance", "已清理旧的时间胶囊");
    }, 0, 7, TimeUnit.DAYS);
}
```

---

## 📞 常见问题

**Q: 如何更改主密钥？**
```java
encryptionUtil.setMasterPassword("NewMasterKey123!");
```
⚠️ 注意：更改后老笔记如果使用旧密钥加密则无法解密！

**Q: 加密会影响搜索吗？**
A: 不会。搜索对加密和未加密笔记一视同仁。

**Q: 胶囊能否编辑？**
```java
// 目前不支持直接编辑，需要先删除再重建
manager.deleteCapsule(oldCapsule);
manager.createCapsule(newCapsule);
```

**Q: 如何导出加密笔记？**
```java
// 解密后导出
String decrypted = encryptionUtil.decryptWithMaster(note.getEncryptedContent());
shareUtil.shareText(decrypted);
```

---

**API 版本**: 1.0  
**更新时间**: 2024 年  
**相关类**: EncryptionUtil, TimeCapsuleManager, TimeCapsuleEntity, TimeCapsuleDao

