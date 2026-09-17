# 智能Markdown笔记 (SmartMarkdownNotes)

一个功能强大的Android Markdown笔记应用，集成AI辅助写作功能。

## 功能特性

### 核心功能
- ✨ **Markdown支持** - 使用Markwon库渲染Markdown格式
- 📝 **笔记管理** - 创建、编辑、删除、搜索笔记
- 💾 **本地存储** - 使用Room数据库存储数据
- 🌙 **暗黑模式** - 支持Material3动态主题

### AI功能
- 🤖 **AI续写** - 根据已有内容智能续写
- ✨ **AI润色** - 优化文本表达，提升专业性
- 📋 **AI总结** - 自动生成内容摘要

## 技术栈

- **语言**: Kotlin
- **架构**: MVVM + Repository Pattern
- **UI**: Material Design 3
- **数据库**: Room
- **网络**: Retrofit + OkHttp + Gson
- **异步**: Kotlin Coroutines + Flow
- **Markdown**: Markwon

## 项目结构

```
├── data/
│   ├── dao/         # Room DAO
│   ├── db/          # 数据库
│   ├── model/       # 数据模型
│   ├── network/     # OpenAI API
│   └── repository/  # 仓库层
├── ui/
│   ├── adapter/     # RecyclerView适配器
│   ├── viewmodel/   # ViewModel
│   └── *.kt         # Activity
└── util/            # 工具类
```

## 配置AI功能

1. 获取OpenAI API Key
   - 访问 [platform.openai.com](https://platform.openai.com)
   - 登录并进入 API Keys 页面
   - 创建新的 Secret Key

2. 在应用中设置
   - 打开应用，点击右上角菜单
   - 选择 "AI设置"
   - 输入您的 API Key
   - API Key 仅存储在本地，不会上传

## 编译运行

### 环境要求
- Android Studio Giraffe (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 编译
```bash
./gradlew assembleDebug
```

### 安装
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 注意事项

- AI功能需要配置有效的OpenAI API Key
- 请确保您的API Key有足够的使用额度
- 所有数据存储在本地，建议定期备份

## 开源协议

MIT License
