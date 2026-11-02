# Android RSS 阅读器

一个基于 Kotlin 的 Android RSS 阅读器课程项目。同一个 Git 仓库中包含主应用与通知提醒中心伴生模块，两者可独立安装运行。

## 功能概览

### 主 App（app）

- 订阅源添加、删除与未读数统计
- RSS 2.0、Atom、RSS 1.0 的 XML 解析
- 文章列表、文章详情、已读/未读、收藏
- Room 本地缓存，按 `feed_id + guid` 去重
- WorkManager 周期后台刷新，可在设置页开启或关闭
- 设置页数据统计、立即刷新、清理已读文章
- ContentProvider 向伴生模块暴露未读文章

### 伴生模块（reminder）

- Android 13 及以上通知运行时权限申请，低版本自动适配
- NotificationChannel 与 NotificationCompat 通知
- WorkManager 定时检查主 App 的新文章
- 通知内直接操作：全部已读、15 分钟后提醒、忽略
- 权限状态展示、立即检查、测试通知

## 强制技术点对应

| 技术点 | 实现位置 |
| --- | --- |
| XML 解析 | `app/src/main/java/com/zpr/rssreader/data/remote/RssParser.kt` |
| 本地数据库 | `app/src/main/java/com/zpr/rssreader/data/local/` |
| WorkManager（主 App） | `app/src/main/java/com/zpr/rssreader/work/` |
| WorkManager（伴生模块） | `reminder/src/main/java/com/zpr/reminder/work/` |
| Android 通知 | `reminder/src/main/java/com/zpr/reminder/notification/` |

## 技术栈

- 语言：Kotlin
- 界面：XML 布局、Material Components、Navigation
- 架构：MVVM、Repository
- 数据：Room、OkHttp、JDOM
- 异步：Kotlin Coroutines、WorkManager
- 测试：JUnit、AndroidX Test、仪器测试

## 目录结构

```text
RssReaderProject/
├── app/                               主应用
│   └── src/main/java/com/zpr/rssreader/
│       ├── data/
│       │   ├── local/                 Room 实体、DAO、数据库
│       │   ├── remote/                OkHttp、XML 解析
│       │   ├── provider/              跨应用数据共享
│       │   ├── repository/            数据仓库
│       │   └── preferences/           本地偏好
│       ├── ui/                        Activity、Fragment、ViewModel、Adapter
│       ├── work/                      WorkManager 后台刷新
│       └── di/                        依赖容器
├── reminder/                          通知提醒中心伴生模块
│   └── src/main/java/com/zpr/reminder/
│       ├── data/                      ContentProvider 客户端与偏好
│       ├── notification/              通知渠道、通知构建、通知操作
│       ├── work/                      定时检查任务
│       └── ui/                        权限与提醒设置界面
├── gradle/libs.versions.toml          依赖版本目录
├── gradle/wrapper/                    Gradle Wrapper
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 构建

环境要求：

- JDK 17 及以上
- Android SDK 35
- 使用仓库自带的 Gradle Wrapper

Windows 示例：

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

构建产物：

- 主 App：`app/build/outputs/apk/debug/app-debug.apk`
- 伴生模块：`reminder/build/outputs/apk/debug/reminder-debug.apk`

首次使用请在 Android Studio 中打开项目，或创建 `local.properties` 并写入本机 SDK 路径。`local.properties` 已被 Git 忽略。

## 运行与演示

1. 分别安装主 App 与伴生模块 APK。
2. 启动主 App，点击右下角“添加订阅”，输入 RSS 地址后添加。
3. 在订阅源列表点击进入文章列表，点击文章查看详情。
4. 在详情页自动标记已读，可通过右上角菜单收藏文章；收藏内容可在底部“收藏”页查看。
5. 在“设置”页可以关闭/开启自动刷新、立即刷新、清理已读文章。
6. 启动伴生模块，授予通知权限后可以发送测试通知，或点击“立即检查一次”读取主 App 未读文章并推送提醒。
7. 收到新文章通知后，可直接在通知上执行“全部已读”“15 分钟后提醒”“忽略”。

说明：

- 主 App 的周期刷新使用网络约束，需要设备网络正常；网络不可用时可在文章列表或设置页手动刷新查看错误提示。
- 主 App 已开启明文流量，支持 HTTP 与 HTTPS 订阅源。
- 伴生模块依赖主 App 已安装且存在未读文章。

## 测试

### 单元测试

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

覆盖 RSS 2.0、Atom、HTML 实体与非法格式解析。

### 仪器测试

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :reminder:connectedDebugAndroidTest
```

如果本机离线环境缺少 Gradle UTP 组件，可直接使用 adb 执行：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb install -r reminder/build/outputs/apk/debug/reminder-debug.apk
adb install -r reminder/build/outputs/apk/androidTest/debug/reminder-debug-androidTest.apk

adb shell pm grant com.zpr.reminder android.permission.POST_NOTIFICATIONS
adb shell am instrument -w -e class com.zpr.rssreader.data.RssSyncIntegrationTest com.zpr.rssreader.test/androidx.test.runner.AndroidJUnitRunner
adb shell am force-stop com.zpr.rssreader
adb shell am instrument -w -e class com.zpr.reminder.integration.ReminderIntegrationTest com.zpr.reminder.test/androidx.test.runner.AndroidJUnitRunner
```

联调测试顺序说明：

1. `RssSyncIntegrationTest` 会在设备内启动本地 HTTP 服务，验证网络拉取、XML 解析、Room 入库与 ContentProvider 查询。
2. `ReminderIntegrationTest` 会冷启动主 App 读取 Provider，触发 WorkManager 并验证通知与 Action 按钮。
3. 若未先运行第一步，伴生模块的联调测试会在没有主 App 测试数据时自动跳过。

## 跨模块数据流

```text
RSS 地址
  -> OkHttp 拉取
  -> RssParser 解析 XML
  -> Room 保存订阅源与文章
  -> RssContentProvider 暴露未读文章
  -> 伴生模块 ReminderCheckWorker 定时读取
  -> NotificationHelper 发送通知
  -> NotificationActionReceiver 处理通知内操作
  -> 通过 ContentProvider 标记已读
```

## 权限与安全

- 主 App：`android.permission.INTERNET`
- 伴生模块：`android.permission.POST_NOTIFICATIONS`
- 主 App 的 ContentProvider authority 为 `com.zpr.rssreader.provider`，当前导出以便伴生模块读取。生产环境建议增加签名级权限。
- 通知操作接收器设置为 `exported=false`，只能通过本应用发出的 PendingIntent 触发。
- 仓库中不包含任何账号凭据、签名文件或本机 SDK 路径。

## Git 约定

- 主分支：`main`
- 伴生模块开发分支：`feature/reminder-notification`
- 提交信息按 `feat:`、`fix:`、`refactor:` 等前缀描述本次修改内容
- 项目包含至少 15 条有意义的开发提交

## 已知限制

- 文章详情使用 TextView 渲染 HTML 文本，正文中的图片不会加载。
- 主 App 周期刷新需要系统认可的有效网络；部分模拟器网络未验证时需要手动刷新。
- 伴生模块的通知基于主 App 的未读数据，需先安装主 App 并添加订阅源。

## 开发进度

- [x] Gradle 工程与双模块结构
- [x] 依赖引入与版本目录
- [x] Room 实体、DAO、数据库与仓库
- [x] RSS / Atom / RDF XML 解析
- [x] OkHttp 网络层与字符集处理
- [x] 主 App WorkManager 后台刷新
- [x] 订阅源、文章、详情、收藏、设置页面
- [x] 主题、启动图标、底部导航与 ViewModel 联调
- [x] 伴生模块通知权限、通知渠道与通知推送
- [x] 伴生模块 WorkManager 定时检查
- [x] 通知内快捷操作
- [x] 跨模块 ContentProvider 数据联动
- [x] 单元测试、仪器测试与联调测试
