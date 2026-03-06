# LNote - 轻盈笔记 📝

> 一款采用 iOS 风格毛玻璃设计的 Android 原生笔记应用，以优雅的方式记录你的想法。

---

## ✨ 应用简介

LNote（轻盈笔记）是一款精心设计的 Android 笔记应用，融合了 **iOS Glassmorphism 毛玻璃美学** 与强大的功能体验。无论是日常随笔、待办清单，还是加密日记、时间胶囊，LNote 都能以简洁优雅的方式满足你的需求。

### 🎨 设计理念
- **毛玻璃 UI** — 全局 Glassmorphism 视觉风格，磨砂质感卡片与通透层次
- **四套主题** — 深色 / 浅色 / 跟随系统 / 护眼模式，随心切换
- **iOS 配色** — 参考 Apple 系统级配色方案，柔和舒适
- **流畅动画** — 13+ 种精心调校的交互动画与触感反馈

---

## 📋 功能特性

### 核心功能
- 📝 创建、编辑、删除笔记
- 🔍 全文搜索（标题 + 内容），支持语音搜索
- 🏷️ 笔记分类（全部 / 收藏 / 日记 / 待办）与标签管理
- ✏️ 富文本编辑 — 加粗、斜体、下划线、删除线
- 📋 长按菜单 — 编辑、分享文本/图片、复制、删除

### 媒体功能
- 📷 拍照 / 相册导入图片，点击放大查看
- 🎬 视频导入，点击全屏播放（支持暂停 / 快进 / 进度条）
- 🎵 音频导入与播放
- 🖼️ 可拖拽缩放的媒体贴纸系统

### 安全与加密
- 🔐 AES-256 内容加密，保护隐私
- 💾 密码保护的备份与恢复（`.nbk` 加密格式）
- 📥 导出所有数据（笔记 + 时间胶囊 + 版本历史）

### 时间胶囊 🕰️
- 将笔记封存至指定日期，届时前内容不可查看
- 精美的胶囊徽章与封存遮罩 UI
- 到期自动通知解锁

### 主题系统 🎭
| 模式 | 说明 |
|------|------|
| 🌙 深色 | 暗夜毛玻璃，护眼低亮度 |
| ☀️ 浅色 | 明亮清透，日间舒适 |
| 🔄 跟随系统 | 自动匹配系统深浅色 |
| 👁️ 护眼 | 暖色调低蓝光，长时间阅读友好 |

---

## 🛠️ 技术栈

| 技术 | 说明 |
|------|------|
| **语言** | Java |
| **数据库** | Room (SQLite) |
| **UI 框架** | Material Design + iOS Glassmorphism |
| **最低 API** | 21 (Android 5.0) |
| **目标 API** | 34 (Android 14) |
| **加密** | AES-256-CBC |
| **动画** | Lottie + 自定义 Spring 动画 |

---

## 📁 项目结构

```
app/src/main/java/com/example/noteapp/
├── MainActivity.java              # 主页面 — 笔记列表、搜索、分类
├── EditNoteActivity.java          # 编辑页面 — 富文本编辑、媒体插入
├── ProfileActivity.java           # 设置页面 — 主题、备份恢复
├── adapter/
│   └── NoteAdapter.java           # 笔记列表适配器
├── db/
│   ├── NoteEntity.java            # 笔记实体
│   ├── NoteDao.java               # 笔记 DAO
│   ├── NoteDatabase.java          # Room 数据库
│   ├── TimeCapsuleEntity.java     # 时间胶囊实体
│   └── NoteVersionEntity.java     # 版本历史实体
├── ui/components/
│   ├── GlassContainerView.java    # 毛玻璃卡片容器
│   ├── MediaStickerView.java      # 媒体贴纸（图片/视频/音频）
│   └── StickerView.java           # 图片贴纸
└── utils/
    ├── EncryptionUtil.java        # AES-256 加密工具
    ├── BackupRestoreUtil.java     # 备份恢复工具
    ├── ThemeModeManager.java      # 主题管理器
    ├── TimeCapsuleManager.java    # 时间胶囊管理器
    ├── TagManager.java            # 标签管理器
    ├── ImagePickerUtil.java       # 图片选择工具
    ├── AnimationManager.java      # 动画管理器
    └── HapticFeedbackUtil.java    # 触感反馈工具
```

---

## 🚀 构建与运行

```bash
# 克隆项目
git clone https://github.com/Evelorion/LNote.git

# 使用 Android Studio 打开项目，或命令行构建
cd LNote
./gradlew assembleDebug

# APK 输出路径
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 许可证

本项目仅供学习参考使用。

**Q: 可以给笔记添加标签吗？**
A: 可以！在编辑笔记时，从分类下拉菜单选择或创建新分类

**Q: 支持格式化编辑吗？**
A: 支持！在编辑笔记时，使用文本格式化工具栏：
- 加粗：选中文本后点击"B"按钮
- 斜体：选中文本后点击"I"按钮
- 下划线：选中文本后点击"U"按钮
- 删除线：选中文本后点击"S"按钮

**Q: 黑暗模式是自动的吗？**
A: 是的！应用会自动跟随系统的深色/浅色主题设置

## 开发许可证

MIT License - 自由使用和修改

## 技术支持

如有问题，请检查：
1. Android Studio Gradle同步状态
2. SDK版本是否满足要求
3. 编译错误日志
4. 设备或模拟器是否正确连接

详细的iOS风格玻璃化UI设计文档，请查看 [iOS_GLASS_DESIGN.md](iOS_GLASS_DESIGN.md)

## 新增功能详解

### 笔记分类和标签系统
每条笔记可以绑定一个**分类**和多个**标签**：
- 分类：在编辑笔记时从下拉菜单选择，用于主要分类
- 标签：支持多个标签，用逗号分隔，便于灵活分组

### 文本格式化编辑
在编辑笔记时，使用编辑页面顶部的格式化工具栏：
```
| B | I | U | S | × |
```
- **B** (加粗): 选中文本后点击使用 StyleSpan(Typeface.BOLD)
- **I** (斜体): 选中文本后点击使用 StyleSpan(Typeface.ITALIC)
- **U** (下划线): 使用 UnderlineSpan 装饰文本
- **S** (删除线): 使用 StrikethroughSpan 标记文本
- **×** (清除格式): 移除选中文本的所有格式

### 笔记备份与恢复
点击菜单 (⋯) 选择备份选项：

**JSON备份** (`action_backup`)
- 导出所有笔记为JSON格式
- 包含所有字段：标题、内容、标签、分类、颜色、时间戳
- 格式化输出便于阅读
- 文件位置: `/Documents/NoteApp-Backup/notes_backup_[timestamp].json`

**文本导出** (`action_export_text`)
- 导出为可读的文本格式
- 每条笔记用 "=" 分隔
- 包含标题、分类、标签、时间信息
- 文件位置: `/Documents/NoteApp-Backup/notes_backup_[timestamp].txt`

**恢复导入**
- 从JSON备份文件导入（需实现文件选择器）
- 支持批量恢复所有笔记
