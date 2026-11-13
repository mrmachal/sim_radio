# AGENTS.md

Android 网络收音机 App。Kotlin + Jetpack Compose + Media3，电台目录取自 radio-browser 开放接口。

本目录是代码仓库，与开发文档仓库 `../sim_radio_docs` 并列，两者各自独立提交。

## 目录

```
app/src/main/java/com/simradio/
  SimRadioApp.kt          应用容器：单例依赖与播放入口
  MainActivity.kt         唯一 Activity，edge-to-edge
  data/                   目录接口、数据模型、本地收藏存储
  playback/               Media3 会话服务与播放控制器
  ui/                     根组件、主题、发现/搜索/收藏/播放四个界面
app/src/main/res/         字符串、颜色、图标
gradle/libs.versions.toml 依赖版本目录
```

## 约定

- 依赖版本只在 `gradle/libs.versions.toml` 里改，不在模块脚本里写死。
- 用户可见文案一律进 `res/values/strings.xml`，代码里用 `stringResource`。
- 颜色走 Material 颜色角色（`MaterialTheme.colorScheme.*`），不直接引用 `ui/theme/Color.kt` 的常量；常量只用于组装两套 ColorScheme。
- 尺寸用 `dp`，文字用 `sp`；触控目标不小于 48×48dp。
- 注释只写这个函数做什么，一行以内。不写变更历史、计划或推理过程。
- 状态集中在 ViewModel 的 `StateFlow`，界面只读状态与转发意图。
- 播放一律经 `SimRadioApp.play()`，它负责连接控制器并记入最近收听。

## 命令

```bash
./gradlew :app:assembleDebug     # 构建 debug APK
./gradlew :app:assembleRelease   # 构建签名 release APK（2.7 MB）
./gradlew :app:lint              # 静态检查，应为零告警
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

release 签名需要 `app/keystore/simradio.jks`（不入库），生成：

```bash
keytool -genkeypair -keystore app/keystore/simradio.jks -alias simradio \
  -keyalg RSA -keysize 2048 -validity 10950 \
  -storepass simradio -keypass simradio -dname "CN=SimRadio"
```

`local.properties` 里写 `sdk.dir` 指向本机 Android SDK，已在 `.gitignore` 中。Gradle wrapper 的 `distributionUrl` 指向腾讯云镜像（官方源在部分网络下不可达），换网络环境时改回 `https://services.gradle.org/distributions/gradle-8.10.2-bin.zip`。

## 数据来源

radio-browser 免费开放目录，无需 key。四个镜像 `de1` / `si1` / `fr1` / `fi1` 按序故障转移，见 `data/RadioBrowserClient.kt`。接口返回的 `url_resolved` 是实际播放地址，`hls=1` 的台需要 HLS 支持。