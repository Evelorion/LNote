# 🎨 笔记本应用 - 完整功能指南

## 📋 目录
1. [笔记分类/标签系统](#笔记分类标签系统)
2. [文本格式化编辑](#文本格式化编辑)
3. [备份与恢复](#备份与恢复)
4. [图片处理](#图片处理)
5. [深色主题支持](#深色主题支持)
6. [Haptic反馈](#haptic反馈)
7. [动画效果](#动画效果)
8. [相机与图库集成](#h相机与图库集成)
9. [笔记分享功能](#笔记分享功能)
10. [搜索优化](#搜索优化)
11. [UI动画增强](#ui动画增强)

---

## 笔记分类/标签系统

### 功能说明
每条笔记支持：
- **分类**: 单个分类，用于主要分组（默认为"默认"）
- **标签**: 多个标签，用逗号分隔，用于灵活分类

### 使用方式
1. 编辑笔记时，点击"分类"下拉菜单
2. 选择现有分类或输入新分类名称
3. 笔记保存时自动创建新分类

### 技术实现
```java
// TagManager 单例类
TagManager tagManager = TagManager.getInstance(context);

// 添加/获取分类
tagManager.addCategory("工作");
List<String> categories = tagManager.getCategoriesList();

// 添加/移除标签
note.addTag("重要");
note.removeTag("重要");
String[] tags = note.getTagsList();
```

### 数据存储
- **分类**: 存储在 SharedPreferences 中，永久保留
- **标签**: 存储在 NoteEntity.tags 字段中（逗号分隔字符串）

---

## 文本格式化编辑

### 支持的格式
| 按钮 | 功能 | 说明 |
|------|------|------|
| **B** | 加粗 | 使用 `Typeface.BOLD` |
| **I** | 斜体 | 使用 `Typeface.ITALIC` |
| **U** | 下划线 | 使用 `UnderlineSpan` |
| **S** | 删除线 | 使用 `StrikethroughSpan` |
| **×** | 清除格式 | 移除所有样式 |

### 使用步骤
1. 在编辑笔记界面，选中要格式化的文本
2. 点击对应的格式按钮
3. 格式将应用于选中的文本
4. 保存笔记时，格式被自动保留

### 技术实现
```java
// 应用格式
TextFormattingUtil.makeBold(editText);      // 加粗
TextFormattingUtil.makeItalic(editText);    // 斜体
TextFormattingUtil.makeUnderline(editText); // 下划线
TextFormattingUtil.makeStrikethrough(editText); // 删除线

// 清除格式
TextFormattingUtil.clearFormatting(editText);
```

### 使用 Android SpannableString
格式化基于 SpannableString API：
- `StyleSpan` - 加粗/斜体
- `UnderlineSpan` - 下划线
- `StrikethroughSpan` - 删除线

---

## 备份与恢复

### 菜单选项
点击顶部菜单 (⋮) 查看备份选项：

#### 1. 备份 (JSON格式)
```
📁 /Documents/NoteApp-Backup/
   └─ notes_backup_2024-01-15-10-30-45.json
```

**包含内容**:
- ✅ 笔记ID
- ✅ 标题和内容
- ✅ 分类和标签
- ✅ 笔记颜色
- ✅ 创建/修改时间戳

**JSON结构示例**:
```json
[
  {
    "id": 1,
    "title": "我的笔记",
    "content": "笔记内容...",
    "timestamp": 1705305045000,
    "tags": "重要,工作",
    "category": "工作",
    "color": "#FF3B30"
  }
]
```

#### 2. 导出为文本
```
📄 notes_backup_2024-01-15-10-30-45.txt
```

**文本格式**:
```
==================================================
标题: 我的笔记
分类: 工作
标签: 重要,工作
时间: 2024-01-15 10:30:45
--------------------------------------------------
笔记内容...

==================================================
```

### 恢复步骤
1. 点击菜单 > 恢复
2. 选择JSON备份文件
3. 所有笔记将导入到应用中
4. 支持重复导入（不会删除现有笔记）

### 技术实现
```java
// 导出JSON
String path = BackupRestoreUtil.exportNotesToJSON(context, database);

// 导出文本
String path = BackupRestoreUtil.exportNotesToText(context, database);

// 导入JSON
int count = BackupRestoreUtil.importNotesFromJSON(context, database, filePath);
```

---

## 图片处理

### 功能概述
> ⚠️ 图片功能框架已完成，等待UI集成

### 支持的功能
- 📸 相机拍照
- 🖼️ 相册选择
- 📐 自动压缩
- 💾 应用内存储
- 🔐 支持 FileProvider

### 技术实现
```java
// 获取图片目录
File imageDir = ImageUtil.getImageDirectory(context);

// 创建新图片文件
File file = ImageUtil.createImageFile(context);

// 保存Bitmap
File saved = ImageUtil.saveBitmapToFile(context, bitmap);

// 加载图片（带压缩）
Bitmap bitmap = ImageUtil.loadBitmap(imagePath);

// 获取FileProvider URI
Uri uri = (Uri) ImageUtil.getImageUri(context, file);
```

### 存储位置
```
/Android/data/com.example.noteapp/files/
└─ Pictures/
   └─ NoteApp-Images/
      ├─ IMG_2024-01-15-10-30-45-123.jpg
      └─ ...
```

---

## 深色主题支持

### 自动切换
应用会**自动跟随系统**主题设置：
- ☀️ 浅色模式（日间）
- 🌙 深色模式（夜间）

### iOS风格深色调色板
| 颜色类型 | 浅色 | 深色 |
|---------|------|------|
| 背景 | `#FFFFFF` | `#FF0A0A0E` |
| 次级背景 | `#F2F2F7` | `#FF1C1C1E` |
| 玻璃 (60%) | `#E0E0E5` | `#3D000000` |
| 玻璃 (50%) | `#D1D1D6` | `#2D000000` |
| 玻璃 (25%) | `#E8E8ED` | `#1A000000` |
| 主文字 | `#000000` | `#FFFFFFFF` |
| 辅文字 | `#999999` | `#FFE5E5EA` |

### 技术实现
```xml
<!-- values/colors.xml - 浅色主题 -->
<color name="colorBackground">#FFFFFF</color>

<!-- values-night/colors.xml - 深色主题 -->
<color name="colorBackground">#FF0A0A0E</color>
```

应用在 `Theme.MaterialComponents.DayNight` 基础上扩展

---

## Haptic反馈

### 反馈类型

| 类型 | 触发场景 | 模式 |
|------|---------|------|
| 🔘 **轻击** (tap) | 按钮按下 | 30ms 单短脉冲 |
| ✅ **成功** (success) | 笔记保存、编辑开启 | 50ms + 100ms 双击模式 |
| ⚠️ **警告** (warning) | 删除操作 | 100ms 双击 |
| ❌ **错误** (error) | 验证失败 | 三次强振 |

### 智能API检测
```java
// 自动选择最佳震动模式
HapticFeedbackUtil.tap(view);       // API 20+ 可用
HapticFeedbackUtil.success(view);   // API 20+ 可用
HapticFeedbackUtil.warning(view);   // API 20+ 可用
HapticFeedbackUtil.error();         // 不需要view上下文
```

### 集成点
- ✅ 浮动按钮添加笔记
- ✅ 笔记项点击编辑
- ✅ 保存笔记
- ✅ 取消操作
- ✅ 删除确认
- ✅ 格式化按钮

### 权限声明
```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 动画效果

### 集成动画库

#### Lottie (JSON动画)
```gradle
implementation 'com.airbnb.android:lottie:5.2.0'
```

**支持的动画**:
- 加载动画
- 成功动画
- 过渡动画

#### 原生Android动画

| 动画类型 | 方法 | 说明 |
|---------|------|------|
| 缩放 | `createScaleAnimation()` | 从0.95缩放到1.0 |
| 淡入淡出 | `createFadeAnimation()` | 透明度变化 |
| 滑动 | `createSlideAnimation()` | X/Y轴平移 |

### 使用示例
```java
// 直接使用动画
AnimationManager.createScaleAnimation(button, 0.95f, 100);

// Lottie动画
AnimationManager.playOnceAnimation(lottieView, "success.json");
AnimationManager.setAnimationSpeed(lottieView, 1.5f);
```

### 集成点
- ✅ 按钮点击缩放
- ✅ 列表项动画
- ✅ 笔记保存动画

---

## 相机与图库集成

### 功能概述
在编辑笔记时，用户可以快速拍摄照片或从设备图库中选择图片附加到笔记。

### 核心特性
- 📸 **实时相机拍照** - 启动设备相机，拍摄新照片
- 🖼️ **图库选择** - 浏览已有图片并选择
- 🔐 **安全存储** - 使用FileProvider确保文件安全
- 📱 **现代API** - 使用ActivityResultContracts替代废弃的onActivityResult

### UI集成
在EditNoteActivity的格式化工具栏添加两个按钮：
- **相机按钮** (🎥) - 点击打开相机应用
- **图库按钮** (🖼️) - 点击打开图库选择器

### 技术实现
```java
// 使用ImagePickerUtil
ImagePickerUtil.openCamera(activity);      // 打开相机
ImagePickerUtil.openGallery();              // 打开图库

// 处理回调
ImagePickerUtil.setOnImagePickedListener((imagePath) -> {
    currentNote.addImagePath(imagePath);
});

// 查看笔记关联的图片
String[] imagePaths = currentNote.getImagePathsList();
```

### 存储位置
```
/Android/data/com.example.noteapp/files/
└─ Pictures/
   └─ IMG_2024-01-15-10-30-45.jpg
```

### 权限要求
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 笔记分享功能

### 功能概述
提供多种方式分享笔记到其他应用，包括即时通讯、邮件、社交媒体等。

### 支持的分享方式

| 分享方式 | 目标应用 | 内容 |
|---------|--------|------|
| 📱 **纯文本分享** | WhatsApp、微信、短信等 | 标题 + 内容 + 元数据 |
| 📄 **文件分享** | 邮件、云盘等 | 格式化纯文本文件 |
| 📸 **多媒体分享** | 照片分享应用 | 笔记 + 关联图片 |
| ✉️ **邮件分享** | Email应用 | 完整笔记内容作为邮件体 |
| 📋 **剪贴板复制** | 任何复制粘贴支持的应用 | 笔记全文 |

### UI集成
在笔记列表项长按，弹出上下文菜单：
- 📱 **分享笔记** - 纯文本分享
- 📸 **分享（附带图片）** - 多媒体分享
- 📋 **复制到剪贴板** - 一键复制
- ✏️ **编辑** - 编辑笔记
- 🗑️ **删除** - 删除笔记

### 技术实现
```java
// 各种分享方法
ShareUtil.shareNoteAsText(activity, note);      // 文本分享
ShareUtil.shareNoteAsFile(activity, note);      // 文件分享
ShareUtil.shareNoteWithImages(activity, note);  // 图片分享
ShareUtil.shareNoteViaEmail(activity, note);    // 邮件分享
ShareUtil.copyToClipboard(activity, note);      // 剪贴板复制
```

### 分享内容格式
```
标题: [笔记标题]
分类: [笔记分类]
标签: [逗号分隔的标签]
时间: [创建/修改时间]
---
[笔记内容]
```

### 安全性
使用🔐 **FileProvider** 实现安全的文件共享：
- ✅ 避免直接恶露外部文件路径
- ✅ 授予临时权限而非永久权限
- ✅ 防止未授权应用访问文件

---

## 搜索优化

### 功能概述
提供高级搜索功能，支持多条件搜索和智能相关性排序。

### 搜索维度

| 维度 | 说明 | 用途 |
|------|------|------|
| 🔍 **关键词** | 标题和内容中的关键词，支持模糊匹配 | 主要搜索方式 |
| 📁 **分类** | 按笔记分类过滤 | 缩小搜索范围 |
| 🏷️ **标签** | 按标签进行过滤 | 精细分类查找 |
| 📅 **日期范围** | 按创建/修改时间范围过滤 | 时间段查询 |

### 相关性排序算法

搜索结果按相关性分数排序（分数越高排名越靠前）：

| 匹配类型 | 分数 | 说明 |
|---------|------|------|
| 标题精确匹配 | 100分 | 标题完全等于搜索词 |
| 标题前缀匹配 | 50分 | 标题以搜索词开头 |
| 标题包含 | 30分 | 标题包含搜索词 |
| 内容包含 | 10分/次 | 内容中每出现一次（最多20分） |

### UI集成
**菜单选项：**
- **高级搜索** - 显示最近10条搜索历史作为快速建议
- **清除搜索历史** - 删除所有已保存的搜索记录

### 搜索历史
- 📝 自动保存最近10条搜索查询
- 🔄 支持快速重复搜索
- 🗑️ 支持逐条删除或全部清除
- 💾 持久化存储在SharedPreferences

### 技术实现
```java
// 创建SearchManager实例
SearchManager searchManager = new SearchManager(context);

// 使用流畅API构建查询
SearchQuery query = new SearchQuery()
    .addKeyword("java编程")           // 添加关键词
    .addCategory("技术")              // 按分类过滤
    .addTag("重要")                  // 按标签过滤
    .setDateRange(startTime, endTime);  // 日期范围过滤

// 执行搜索（返回排序结果）
List<NoteEntity> results = searchManager.advancedSearch(allNotes, query);

// 管理搜索历史
searchManager.addSearchHistory("我的搜索");
List<String> history = searchManager.getSearchHistory();
searchManager.clearSearchHistory();
```

### 性能优化
- ✅ 增量搜索 - 实时过滤，无需等待
- ✅ 缓存优化 - 搜索历史缓存在内存中
- ✅ 轻量级算法 - 相关性计算高效

---

## UI动画增强

### 功能概述
提供13+种专业级动画效果，提升用户体验。

### 动画类型详览

#### 1. 基础动画
| 动画 | 方法 | 说明 |
|-----|------|------|
| 缩放 | `createScaleAnimation()` | 视图大小缩放 |
| 淡入淡出 | `createFadeAnimation()` | 透明度变化 |
| 旋转 | `createRotationAnimation()` | 自定义旋转角度 |

#### 2. 入场动画
| 动画 | 方法 | 说明 |
|-----|------|------|
| 从左滑入 | `createSlideInFromLeftAnimation()` | 从屏幕左边滑进 |
| 上升淡入 | `createSlideUpAnimation()` | 从下向上移动+淡入 |
| 弹出进入 | `createPopInAnimation()` | 缩放+淡入的弹出效果 |

#### 3. 出场动画  
| 动画 | 方法 | 说明 |
|-----|------|------|
| 滑向右边 | `createSlideOutToRightAnimation()` | 滑出到屏幕右边 |
| 下沉淡出 | `createSlideDownAnimation()` | 向下移动+淡出 |
| 弹出退出 | `createPopOutAnimation()` | 缩放+淡出的弹出效果 |

#### 4. 强调动画
| 动画 | 方法 | 说明 |
|-----|------|------|
| 弹跳缩放 | `createBounceScaleAnimation()` | 缩放：1.0→0.9→1.05→1.0 |
| 脉冲效果 | `createPulseAnimation()` | 重复缩放脉冲 |
| 闪烁效果 | `createFlashAnimation()` | 透明度闪烁 |
| 360旋转 | `createSpinAnimation()` | 连续360度旋转 |

#### 5. 列表动画
| 动画 | 方法 | 说明 |
|-----|------|------|
| 交错列表 | `createListItemAnimation()` | 列表项逐个进入 |
| 共享元素 | `createSharedElementAnimation()` | Activity间的共享元素转场 |

### 动画参数配置

```java
// 基础用法
AnimationManager.createScaleAnimation(view, 0.95f, 300);

// 自定义参数
AnimationManager.createRotationAnimation(view, 0f, 360f, 1000);

// 列表动画
AnimationManager.createListItemAnimation(recyclerView, 50);  // 50ms间隔

// 动画链式调用（多个动画同时播放）
AnimationManager.createPopInAnimation(view).start();
```

### 动画曲线（插值器）

| 插值器 | 适用场景 |
|--------|---------|
| **FastOutSlowIn** | 标准Material Design曲线，用于强调 |
| **LinearOutSlowIn** | 入场动画，快速开始后缓慢结束 |
| **LinearInterpolator** | 旋转和连续动画 |

### UI集成点

**在NoteAdapter中：**
- ✅ 列表项点击时应用缩放动画（视觉反馈）
- ✅ 删除按钮反馈动画
- ✅ 列表初始化时应用交错进入动画

**在EditNoteActivity中：**
- ✅ 保存按钮缩放反馈
- ✅ 返回/取消按钮轻击反馈

**在MainActivity中：**
- ✅ FAB浮动按钮弹出动画
- ✅ 搜索结果淡入动画

### 关键特性
- 💪 **性能优化** - 基于ObjectAnimator的硬件加速
- 🎯 **灵活配置** - 可自定义持续时间、插值器和延迟
- 🔄 **可复用** - 动画方法可在应用任何地方调用
- 📱 **API兼容** - 支持API 20+（Android 4.4及以上）

---

### 集成点
- ✅ 按钮点击缩放
- ✅ 列表项动画
- ✅ 笔记保存动画

---

## 📦 新增权限

```xml
<!-- 文件访问 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- 相机 -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- 震动 -->
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 🛠️ 数据库迁移

### Room 自动迁移
NoteEntity 添加新字段时，Room 会自动处理：
```java
// 新字段
private String tags;      // 标签
private String category;  // 分类
private String color;     // 颜色
```

**现有笔记升级**:
- ✅ 自动为新字段设置默认值
- ✅ 无数据丢失
- ✅ 向后兼容

---

## 📱 典型工作流

### 新建笔记并分类
```
1. 点击浮动按钮 (FAB)
   ↓
2. 输入标题和内容
   ↓
3. 从「分类」下拉菜单选择或创建新分类
   ↓
4. 使用格式化工具栏（加粗、斜体等）
   ↓
5. 点击「保存」
```

### 备份笔记
```
1. 点击菜单 (⋮)
   ↓
2. 选择「备份」或「导出为文本」
   ↓
3. 文件自动保存到 /Documents/NoteApp-Backup/
   ↓
4. 可分享或备份
```

### 恢复笔记
```
1. 点击菜单 (⋮)
   ↓
2. 选择「恢复」
   ↓
3. 从文件管理器选择 JSON 备份文件
   ↓
4. 点击导入
```

---

## ✨ 总结

| 功能 | 状态 | 备注 |
|------|------|------|
| 笔记分类/标签 | ✅ 完成 | 完整实现 |
| 文本格式化 | ✅ 完成 | 5种格式支持 |
| 备份恢复 | ✅ 完成 | JSON + 文本格式 |
| 图片处理 | 📐 框架完成 | 等待UI集成 |
| 深色主题 | ✅ 完成 | 自动跟随系统 |
| Haptic反馈 | ✅ 完成 | 4种反馈模式 |
| 动画效果 | ✅ 完成 | Lottie + 原生动画 + 13种新动画 |
| 相机与图库 | ✅ 完成 | ActivityResultContracts + FileProvider |
| 笔记分享 | ✅ 完成 | 5种分享方式 + 上下文菜单 |
| 搜索优化 | ✅ 完成 | 相关性排序 + 搜索历史 |
| UI动画增强 | ✅ 完成 | 13+种专业动画 |

---

## 📞 技术支持

有任何问题？请检查：
1. ✅ 所有权限是否已声明
2. ✅ SDK版本是否符合要求
3. ✅ 设备存储空间是否充足
4. ✅ 系统权限是否已授予

详见主文档: [README.md](README.md)
