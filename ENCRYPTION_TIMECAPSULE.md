# 🔐 笔记加密与时间胶囊指南

完整的两个新高级功能说明和集成指南。

---

## 📚 目录

1. [笔记加密](#笔记加密)
2. [时间胶囊功能](#时间胶囊功能)
3. [技术实现细节](#技术实现细节)
4. [故障排除](#故障排除)

---

## 🔐 笔记加密

### 功能概述

笔记加密功能使用 **AES-256** 加密算法来保护您的笔记内容。只有正确的主密钥才能解密笔记。

### 核心特性

- 🛡️ **AES-256加密** - 军事级别的加密标准
- 🔑 **主密钥管理** - 由应用生成和存储的密钥
- 🔄 **透明加密/解密** - 自动处理加密和解密
- 📁 **文件大小** - 加密不会显著增加文件大小
- ⚡ **高性能** - 加密/解密速度快

### 使用步骤

#### 1. 创建加密笔记

1. 点击 **+** 按钮创建新笔记
2. 输入标题和内容
3. 在工具栏中找到 **🔒 加密** 复选框
4. ✅ 勾选加密复选框
5. 点击 **保存** 按钮

笔记内容将自动加密存储在数据库中。

#### 2. 打开加密笔记

1. 在笔记列表中选择一条加密笔记（会有 🔒 标记）
2. 系统将弹出 **密码输入对话框**
3. 输入主密钥（与加密时使用的密钥一致）
4. 点击 **确定** 来解密

如果主密钥正确，笔记内容将被解密并显示。

#### 3. 编辑加密笔记

编辑加密笔记时：
- ✅ 内容在内存中保持解密状态（便于编辑）
- 🔒 保存时内容会重新加密
- 🔄 您可以随时取消加密（取消勾选加密框）

#### 4. 取消加密

1. 打开加密笔记
2. 输入主密钥解密
3. 在工具栏中 **取消勾选** 加密复选框
4. 点击 **保存**

笔记将以明文形式保存，不再需要密钥打开。

### 技术实现

#### EncryptionUtil 类

```java
// 获取加密工具实例
EncryptionUtil encryptionUtil = EncryptionUtil.getInstance(context);

// 加密文本
String encrypted = encryptionUtil.encryptWithMaster(plainText);

// 解密文本
String decrypted = encryptionUtil.decryptWithMaster(encryptedText);

// 使用自定义密码加密
String encrypted = encryptionUtil.encrypt(plainText, "customPassword");

// 验证密码
boolean isCorrect = encryptionUtil.verifyPassword(encryptedText, password);
```

#### NoteEntity 扩展

```java
NoteEntity note = new NoteEntity("标题", "内容");

// 设置加密
note.setEncrypted(true);
note.setEncryptedContent(encryptionUtil.encryptWithMaster("内容"));

// 检查是否加密
if (note.isEncrypted()) {
    String decrypted = encryptionUtil.decryptWithMaster(note.getEncryptedContent());
}
```

### 安全建议

⚠️ **重要提示：**

1. **不要重置密钥** - 重置密钥后，旧的加密笔记将无法解密
2. **备份密钥** - 定期导出数据库确保备份
3. **强密钥** - 应用会自动生成 256 位强密钥
4. **设备锁定** - 建议启用设备加密以保护数据库文件

---

## ⏰ 时间胶囊功能

### 功能概述

时间胶囊是一种特殊的笔记，可以在指定的未来时间自动"打开"。就像历史上的时间胶囊一样，您可以创建一条笔记，在一年后（或任何时间）收到通知提醒。

### 核心特性

- ⏱️ **灵活的计时** - 可以设置任何未来时间
- 🔔 **通知提醒** - 在胶囊打开时收到通知
- 📝 **后台监控** - 应用在后台监控所有胶囊
- 🎯 **自动打开** - 到达指定时间时自动标记为已打开
- 📊 **历史记录** - 保留所有已打开的胶囊

### 使用步骤

#### 1. 创建新的时间胶囊

1. 点击菜单 (⋮) → **时间胶囊**
2. 选择 **创建新胶囊**
3. 输入胶囊标题（如："给一年后的自己"）
4. 输入胶囊内容（写想对未来自己的话）
5. 点击 **下一步（选择时间）**
6. 输入天数（如：365 表示一年后）
7. 点击 **创建**

时间胶囊将被存储并在后台监控。

#### 2. 查看待打开的胶囊

1. 点击菜单 (⋮) → **时间胶囊**
2. 选择 **查看待打开的胶囊**
3. 系统显示所有待打开的胶囊列表
4. 每个胶囊都显示剩余时间

```
1. 给一年后的自己 - 365 天
2. 六个月回顾 - 180 天 12 小时
3. 新年计划 - 260 天
```

#### 3. 查看已打开的胶囊

1. 点击菜单 (⋮) → **时间胶囊**
2. 选择 **查看已打开的胶囊**
3. 系统显示已打开的胶囊列表（按打开时间排序）

#### 4. 手动打开胶囊

如果时间到达，系统会自动发送通知。您也可以：
1. 在待打开列表中，点击胶囊查看内容
2. 系统会提前解密并显示内容（如果已设置的话）

### 常见场景

#### 场景 1：写给一年后的自己

```
标题：一年后的回顾
内容：
去年我设定的目标是：
1. 学习新技能
2. 完成项目
3. 健身计划

现在回看，我完成了吗？让我检视进度...

时间：365 天后
```

#### 场景 2：节日或特殊日期提醒

```
标题：母亲节提醒
内容：记得给妈妈打电话和准备礼物！

时间：到母亲节（如：60 天）
```

#### 场景 3：项目里程碑

```
标题：V2.0 功能回顾
内容：到时候评估这些功能的使用情况：
- 加密功能使用率
- 分享功能反馈
- 搜索功能有效性

时间：180 天（产品发布后）
```

### 技术实现

#### TimeCapsuleManager 类

```java
// 获取时间胶囊管理器
TimeCapsuleManager timeCapsuleManager = TimeCapsuleManager.getInstance(context);

// 启动后台检查
timeCapsuleManager.startBackgroundChecking();

// 创建新胶囊
TimeCapsuleEntity capsule = new TimeCapsuleEntity("标题", "内容", scheduledTime);
capsule.setHasNotification(true);
long capsuleId = timeCapsuleManager.createCapsule(capsule);

// 获取待打开的胶囊
LiveData<List<TimeCapsuleEntity>> pending = timeCapsuleManager.getPendingCapsules();
pending.observe(this, capsules -> {
    // 处理胶囊列表
});

// 获取剩余时间字符串
String remaining = timeCapsuleManager.getTimeRemainingString(capsule);
// 输出例子："365 天 12 小时"
```

#### TimeCapsuleEntity 数据模型

```java
TimeCapsuleEntity capsule = new TimeCapsuleEntity("标题", "内容", scheduledTime);

// 属性
capsule.setCreatedTime(System.currentTimeMillis());  // 创建时间
capsule.setScheduledTime(futureTime);               // 计划打开时间
capsule.setStatus(0);                               // 0=等待中, 1=已打开
capsule.setHasNotification(true);                  // 打开时是否通知
capsule.setReminderMinutesBefore(30);              // 提前30分钟提醒

// 查询
if (capsule.shouldOpen()) {
    // 胶囊应该打开
}

String formatted = capsule.getFormattedScheduledTime();
// 输出：2025-12-25 10:30
```

### 通知系统

#### 通知权限

为了接收时间胶囊打开的通知，需要以下权限：

```xml
<!-- 已在 AndroidManifest.xml 中声明 -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

#### 通知频道

应用创建一个 `时间胶囊` 通知频道：
- **频道 ID**: `time_capsule_channel`
- **重要性**: 默认
- **声音和振动**: 已启用

#### 后台检查

TimeCapsuleManager 在后台运行定时器，每 **60 秒** 检查一次：
- ✅ 是否有胶囊应该打开
- ✅ 是否需要发送提醒通知
- ✅ 自动标记已打开的胶囊

### 与笔记加密的结合

时间胶囊和笔记加密可以结合使用：

```java
// 创建加密的时间胶囊内容
String content = "给一年后的隐私内容...";
String encrypted = encryptionUtil.encryptWithMaster(content);

TimeCapsuleEntity capsule = new TimeCapsuleEntity("私密胶囊", encrypted, futureTime);
capsule.setEncrypted(true);  // 标记为加密
```

---

## 🔧 技术实现细节

### 数据库架构

#### 数据库版本

已升级至 **版本 2**，包含新的时间胶囊表：

```sql
CREATE TABLE time_capsules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    noteId INTEGER,
    title TEXT,
    content TEXT,
    createdTime INTEGER,
    scheduledTime INTEGER,
    openedTime INTEGER,
    status INTEGER,
    tags TEXT,
    hasNotification INTEGER,
    reminderMinutesBefore INTEGER,
    FOREIGN KEY(noteId) REFERENCES notes(id)
);
```

#### 加密字段存储

笔记表扩展：
```sql
ALTER TABLE notes ADD COLUMN isEncrypted INTEGER DEFAULT 0;
ALTER TABLE notes ADD COLUMN encryptedContent TEXT;
```

### 线程管理

#### 后台计时线程

```java
// TimeCapsuleManager 启动后台计时器
private Timer checkTimer;

public void startBackgroundChecking() {
    checkTimer = new Timer();
    checkTimer.scheduleAtFixedRate(
        new TimerTask() { ... },
        0,
        60 * 1000  // 每 60 秒检查一次
    );
}
```

#### 数据库操作线程

所有数据库操作都在后台线程上运行：

```java
new Thread(() -> {
    // 加密/解密操作
    String encrypted = encryptionUtil.encryptWithMaster(content);
    
    // 数据库操作
    database.noteDao().update(note);
    
    // 返回到主线程更新UI
    runOnUiThread(() -> {
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
    });
}).start();
```

### 内存管理

- ✅ **LeakCanary 兼容** - 所有单例实现了正确的生命周期
- ✅ **临时解密** - 解密后的内容仅在内存中保留，不落盘存储
- ✅ **及时释放** - 加密工具在使用完毕后自动释放敏感数据

---

## 🆘 故障排除

### 笔记加密问题

#### Q: 密码错误提示

**问题**: 输入密钥后收到"密码错误"提示

**解决方案**:
1. 检查键盘布局（中英文）
2. 检查大小写
3. 尝试重新启动应用
4. 如果仍无法打开，笔记可能被损坏

#### Q: 加密的笔记无法打开

**问题**: 加密笔记显示但无法解密

**解决方案**:
1. 确保使用了正确的主密钥
2. 检查记录的密钥是否正确
3. 尝试从备份恢复
4. 如果是应用升级后出现，可能需要清除应用缓存

#### Q: 加密性能下降

**问题**: 加密大量笔记时应用变慢

**解决方案**:
- 这是正常的，AES-256 加密需要时间
- 单个笔记加密通常需要 100-500 毫秒
- 系统设计了进度显示和后台处理

### 时间胶囊问题

#### Q: 胶囊不在指定时间打开

**问题**: 设定的时间到了，但没有通知

**解决方案**:
1. 检查应用是否被关闭或卸载
2. 检查通知权限是否启用
3. 检查系统时间是否正确
4. 重启应用以重新启动后台检查器

#### Q: 无法创建时间胶囊

**问题**: 创建按钮不可用或无反应

**解决方案**:
1. 检查存储空间是否充足
2. 检查数据库权限
3. 尝试重启应用
4. 清除应用缓存后重试

#### Q: 旧胶囊未显示

**问题**: 已打开的胶囊列表中没有看到旧胶囊

**解决方案**:
- 应用自动清理 30 天以上的已打开胶囊
- 如果需要保留，请考虑将其转换为普通笔记
- 最多显示最近 5 个已打开的胶囊

### 性能优化

#### 建议

1. **定期清理** - 使用"清除搜索历史"菜单项
2. **及时备份** - 定期备份重要笔记
3. **管理胶囊** - 删除不需要的已打开胶囊
4. **更新系统** - 保持 Android 系统最新

---

## 📞 技术支持

### 常见问题

**Q: 加密的密钥存储在哪里？**
A: 密钥存储在应用的 SharedPreferences 中（应用私有存储），受 Android 系统保护。

**Q: 可以更改主密钥吗？**
A: 可以使用 `EncryptionUtil.setMasterPassword(newPassword)` 更改，但旧密钥加密的内容将无法解密。

**Q: 时间胶囊支持多少天？**
A: 理论上支持任意大整数天数，建议不超过 50 年（18250 天）。

**Q: 胶囊可以编辑吗？**
A: 不可以直接编辑，但可以删除后重新创建。

---

## 📖 相关文档

- [FEATURES.md](FEATURES.md) - 完整功能列表
- [INTEGRATION.md](INTEGRATION.md) - 所有功能的集成指南
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - 项目技术总结

---

**文档版本**: 1.0  
**更新时间**: 2024 年  
**许可证**: MIT

