# 智能题库记忆系统 - 平板版 Android App

## 🎯 项目概述

这是基于HTML版本"智能题库记忆系统"的Android原生应用版本，专为10.3英寸平板设备优化设计。采用WebView + 原生接口的混合架构，完美结合了Web技术的灵活性和Android原生功能的强大性。

## ✨ 核心特性

### 🧠 智能学习功能
- **记忆模式**：基于艾宾浩斯遗忘曲线的智能复习提醒
- **练习模式**：随机练习，全面巩固知识点
- **测试模式**：模拟考试环境，检验学习成果
- **复习模式**：专注于错误和未掌握的题目

### 📱 平板优化设计
- **全屏显示**：移除手机外壳，充分利用10.3英寸屏幕
- **横屏优化**：专为平板横屏使用场景设计
- **响应式布局**：自动适应不同屏幕尺寸和分辨率
- **触控友好**：大按钮和适合触控的交互设计

### 💾 数据管理
- **原生存储**：使用SharedPreferences替代localStorage
- **文件导入**：支持JSON、TXT、Excel格式题库导入
- **文件导出**：学习记录和题库数据导出功能
- **数据同步**：可靠的本地数据持久化

### 📊 学习统计
- **学习进度**：详细的答题统计和正确率分析
- **时间统计**：学习时长和效率追踪
- **分类统计**：按题目分类的学习情况分析
- **连续学习**：学习天数连续性统计

## 🏗️ 技术架构

### 核心技术栈
- **开发语言**：Kotlin
- **UI框架**：WebView + HTML/CSS/JavaScript
- **原生接口**：WebAppInterface (JavaScriptInterface)
- **数据存储**：Android SharedPreferences
- **文件操作**：Android Storage Access Framework
- **构建工具**：Gradle 8.4 + Android Gradle Plugin 8.2.0

### 架构设计
```
┌─────────────────────────────────────────┐
│              Android Application         │
├─────────────────────────────────────────┤
│  MainActivity (Kotlin)                  │
│  ├─ WebView 容器 (全屏显示)              │
│  ├─ WebAppInterface (原生桥接)            │
│  ├─ 文件操作 (导入/导出)                  │
│  └─ 权限管理 (存储访问)                   │
├─────────────────────────────────────────┤
│  HTML/CSS/JS (assets/index.html)        │
│  ├─ 用户界面 (Material Design 3)         │
│  ├─ 学习逻辑 (JavaScript)                │
│  ├─ 数据处理 (StorageAdapter)            │
│  └─ 文件处理 (原生接口调用)                │
├─────────────────────────────────────────┤
│  Android System                         │
│  ├─ SharedPreferences (数据存储)         │
│  ├─ Storage Access Framework (文件)      │
│  └─ System UI (全屏控制)                 │
└─────────────────────────────────────────┘
```

## 🚀 构建说明

### 环境要求

- **Android Studio**: Arctic Fox (2020.3.1) 或更高版本
- **JDK**: 17 或更高版本
- **Android SDK**: API Level 34 (Android 14)
- **Kotlin**: 1.9.10
- **Gradle**: 8.4
- **最低支持**: Android 7.0 (API 24)

### 本地构建

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd TKJY3.0
   ```

2. **配置开发环境**
   ```bash
   # 确保 ANDROID_HOME 环境变量已设置
   echo $ANDROID_HOME
   
   # 创建 local.properties (如果不存在)
   echo "sdk.dir=$ANDROID_HOME" > local.properties
   ```

3. **构建APK**
   ```bash
   # Debug版本
   ./gradlew assembleDebug
   
   # Release版本
   ./gradlew assembleRelease
   
   # 清理构建
   ./gradlew clean
   ```

4. **安装到设备**
   ```bash
   # 安装Debug版本
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # 安装Release版本  
   adb install app/build/outputs/apk/release/app-release.apk
   ```

### GitHub Actions 自动构建

项目配置了完整的CI/CD流水线，支持自动构建和发布：

#### 触发构建
- **推送到主分支**：`main`, `master`, `develop`
- **创建Pull Request**：目标分支为 `main` 或 `master`
- **手动触发**：通过GitHub网页界面
- **版本标签**：推送以`v`开头的标签(如`v1.0.0`)

#### 构建产物
- **APK文件**：Debug和Release版本
- **构建日志**：详细的构建过程记录
- **测试报告**：单元测试和Lint检查结果
- **自动发布**：标签推送时自动创建GitHub Release

#### 使用GitHub Actions构建APK

1. **Fork项目**到您的GitHub账户

2. **推送代码**到主分支触发构建：
   ```bash
   git add .
   git commit -m "feat: 初始版本"
   git push origin main
   ```

3. **查看构建状态**：
   - 访问项目的 `Actions` 标签页
   - 查看构建进度和日志
   - 下载构建完成的APK文件

4. **创建正式版本**：
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

## 📁 项目结构

```
TKJY3.0/
├── .github/workflows/          # GitHub Actions工作流
│   └── build.yml              # 自动构建配置
├── app/                       # 应用主模块
│   ├── src/main/
│   │   ├── java/com/tkjy/questionsystem/
│   │   │   ├── MainActivity.kt           # 主活动
│   │   │   └── WebAppInterface.kt        # WebView接口
│   │   ├── res/                         # 资源文件
│   │   │   ├── layout/                  # 布局文件
│   │   │   ├── values/                  # 基础资源
│   │   │   └── xml/                     # 配置文件
│   │   ├── assets/                      # Web资源
│   │   │   ├── index.html               # 主HTML文件
│   │   │   └── libs/                    # JavaScript库
│   │   │       └── xlsx.full.min.js     # Excel处理库
│   │   └── AndroidManifest.xml          # 应用清单
│   ├── build.gradle                     # 模块构建脚本
│   └── proguard-rules.pro               # 混淆规则
├── gradle/wrapper/                      # Gradle包装器
├── build.gradle                         # 项目构建脚本
├── settings.gradle                      # 项目设置
├── gradle.properties                    # Gradle配置
├── gradlew                             # Unix构建脚本
├── gradlew.bat                         # Windows构建脚本
└── README.md                           # 项目文档
```

## 🔧 关键实现细节

### LocalStorage替换方案

原HTML版本使用`localStorage`存储数据，在Android WebView中不够可靠。解决方案：

**JavaScript侧**：
```javascript
const StorageAdapter = {
    saveData: function(key, value) {
        if (window.AndroidBridge) {
            window.AndroidBridge.saveData(key, JSON.stringify(value));
        } else {
            localStorage.setItem(key, JSON.stringify(value)); // 后备方案
        }
    },
    
    loadData: function(key, defaultValue = null) {
        let data;
        if (window.AndroidBridge) {
            data = window.AndroidBridge.loadData(key);
        } else {
            data = localStorage.getItem(key);
        }
        return data ? JSON.parse(data) : defaultValue;
    }
};
```

**Android侧**：
```kotlin
@JavascriptInterface
fun saveData(key: String, value: String) {
    prefs.edit().putString(key, value).apply()
}

@JavascriptInterface
fun loadData(key: String): String? {
    return prefs.getString(key, null)
}
```

### 文件操作原生化

原HTML版本的文件选择和下载在Android中无效，使用原生实现：

**文件导入**：
```kotlin
@JavascriptInterface
fun openFilePicker() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
    }
    filePickerLauncher.launch(intent)
}
```

**文件导出**：
```kotlin
@JavascriptInterface
fun saveFile(fileName: String, data: String, mimeType: String) {
    pendingFileData = data
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        type = mimeType
        putExtra(Intent.EXTRA_TITLE, fileName)
    }
    fileSaverLauncher.launch(intent)
}
```

### 平板全屏优化

删除原HTML中的手机外壳样式，优化为平板全屏显示：

**CSS优化**：
```css
/* 移除手机外壳 */
.phone-mockup { display: none; }

/* 全屏容器 */
.container {
    width: 100%;
    height: 100vh;
    margin: 0;
    padding: 16px;
}

/* 平板优化的网格布局 */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
}
```

**Android全屏配置**：
```kotlin
private fun setupFullscreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(
            WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
        )
    } else {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}
```

## 📦 APK签名配置

### 自动签名 (推荐)
GitHub Actions支持自动签名，需要配置以下Secrets：

```bash
KEYSTORE_FILE=<base64编码的keystore文件>
KEYSTORE_PASSWORD=<keystore密码>
KEY_ALIAS=<密钥别名>
KEY_PASSWORD=<密钥密码>
```

### 手动签名
```bash
# 生成签名密钥
keytool -genkey -v -keystore release-key.keystore \
    -alias tkjy-key -keyalg RSA -keysize 2048 -validity 10000

# 签名APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore release-key.keystore app-release-unsigned.apk tkjy-key

# 对齐APK
zipalign -v 4 app-release-unsigned.apk TKJY-QuestionSystem-v1.0.0.apk
```

## 🎯 使用说明

### 系统要求
- **Android版本**：7.0 (API 24) 或更高
- **设备类型**：推荐10.3英寸或12.3英寸平板
- **存储空间**：至少100MB可用空间
- **权限需求**：文件读写权限（用于导入/导出功能）

### 安装步骤
1. 从GitHub Releases下载最新APK文件
2. 在设备上启用"未知来源"应用安装
3. 使用文件管理器安装APK
4. 首次运行时授予必要的权限

### 题库格式

**支持的导入格式**：

JSON格式（推荐）：
```json
{
  "name": "题库名称",
  "description": "题库描述",
  "questions": [
    {
      "id": 1,
      "question": "题目内容",
      "answer": "答案内容",
      "type": "text",
      "category": "分类名称",
      "difficulty": "normal"
    }
  ]
}
```

TXT格式：
```text
Q:第一道题目的内容？
A:第一道题目的答案

Q:第二道题目的内容？
A:第二道题目的答案
```

### 学习模式使用

1. **记忆模式**：
   - 首先会询问"记得/不记得"
   - 系统根据遗忘曲线智能安排复习时间
   - 适合长期记忆巩固

2. **练习模式**：
   - 随机显示题目进行练习
   - 可以查看答案并自我评估
   - 适合日常练习

3. **测试模式**：
   - 模拟考试环境
   - 计时答题，测试掌握程度
   - 适合考前检验

4. **复习模式**：
   - 专注于错题和未掌握题目
   - 系统自动筛选需要复习的内容
   - 提高学习效率

## 🔍 故障排除

### 常见问题

**Q: APK安装失败**
A: 
- 检查Android版本是否为7.0或更高
- 确保已启用"未知来源"安装选项
- 检查设备存储空间是否充足

**Q: 文件导入/导出功能无法使用**
A:
- 确保已授予应用文件访问权限
- 检查文件格式是否正确
- 尝试重新启动应用

**Q: 界面显示异常或过小**
A:
- 确认设备为平板设备
- 检查系统显示缩放设置
- 尝试横屏使用

**Q: 学习数据丢失**
A:
- 数据存储在SharedPreferences中，通常不会丢失
- 检查是否清除了应用数据
- 可以通过设置中的"重置数据"功能重新开始

### 调试方法

**启用WebView调试**：
```bash
# 连接设备并启用USB调试
adb devices

# Chrome浏览器中访问
chrome://inspect/#devices
```

**查看应用日志**：
```bash
# 实时查看日志
adb logcat | grep "TKJY\|WebView\|WebApp"

# 过滤ERROR级别日志
adb logcat *:E | grep "com.tkjy.questionsystem"
```

## 🤝 贡献指南

欢迎提交Bug报告、功能建议和代码贡献！

### 开发环境设置
1. Fork本项目
2. 创建特性分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -m 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建Pull Request

### 代码规范
- 遵循Kotlin官方编码规范
- JavaScript代码使用ES6+语法
- CSS使用BEM命名规范
- 提交信息使用约定式提交格式

## 📄 许可证

本项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件

## 🙏 致谢

- [SheetJS](https://sheetjs.com/) - Excel文件处理库
- [Material Design 3](https://m3.material.io/) - UI设计系统
- [Android Jetpack](https://developer.android.com/jetpack) - Android开发框架

---

**项目维护者**: TKJY开发团队
**最后更新**: 2024年1月
**版本**: v1.0.0