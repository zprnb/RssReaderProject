# Android RSS 阅读器

一个基于 Kotlin 的 Android RSS 阅读器课程项目，同一个 Git 仓库中包含主应用与通知提醒中心伴生模块。

## 项目组成

| 模块 | 说明 | 包名 |
| --- | --- | --- |
| app | RSS 阅读器主应用，负责订阅源管理、文章解析、本地缓存与后台刷新 | com.zpr.rssreader |
| reminder | 通知提醒中心伴生应用，可独立安装运行，负责定时检查与通知操作 | com.zpr.reminder |

## 强制技术点

- XML 解析：自行实现 RSS 2.0 与 Atom 解析器，不依赖第三方解析库
- 本地数据库：使用 Room 保存订阅源、文章、已读与收藏状态
- WorkManager：主应用周期刷新订阅源，伴生模块周期检查新文章
- Android 通知：通知渠道、Android 13 通知权限、通知内快捷操作

## 技术栈

- 语言：Kotlin
- 界面：XML 布局、Material Components、Navigation
- 架构：MVVM、Repository
- 数据：Room、OkHttp
- 异步：Kotlin Coroutines、WorkManager

## 目录结构

```text
RssReaderProject/
├── app/                     主应用模块
│   ├── src/main/java/com/zpr/rssreader/
│   └── src/main/res/
├── reminder/                通知提醒中心模块
│   ├── src/main/java/com/zpr/reminder/
│   └── src/main/res/
├── gradle/
│   ├── libs.versions.toml   依赖版本目录
│   └── wrapper/             Gradle Wrapper
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 构建环境

- JDK 17 及以上
- Android SDK 35
- Gradle 9.3.0（使用仓库自带 Wrapper）

Windows 命令行构建示例：

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

构建产物位于 `app/build/outputs/apk/debug/` 与 `reminder/build/outputs/apk/debug/`。

## 开发进度

- [x] 初始化 Gradle 工程与模块结构
- [ ] 模块清单、主题与资源
- [ ] Room 数据层
- [ ] XML 解析
- [ ] 网络与仓储层
- [ ] 主应用界面与后台刷新
- [ ] 通知提醒中心
- [ ] 单元测试与联调
