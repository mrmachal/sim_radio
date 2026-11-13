# SimRadio

Android 上的网络收音机。打开即听，不用注册、不用登录，也不上传任何个人数据。

电台目录来自 [radio-browser.info](https://www.radio-browser.info/) —— 一个由社区维护的开放电台数据库，免费、无需 API key。应用本身不托管任何音频，播放的是各电台公开的直播流。

## 功能

- **发现**：中文电台与全球热门两个范围，按新闻、音乐、交通、财经、文艺、评书、体育等主题分组，列表按社区票数排序，滚动到底自动加载下一页。
- **搜索**：按名称检索，也可以从热门标签或国家/地区直接进入。
- **播放**：Media3 + ExoPlayer，支持 MP3、AAC 与 HLS 直播流；通知栏与控制中心可控制播放。
- **刻度盘切台**：播放页的调谐刻度盘左右滑动即可切换上一个/下一个电台。
- **收藏与最近**：收藏与最近收听的电台保存在本地，随时回到。
- **定时关闭**：15 / 30 / 60 分钟后自动停止播放。
- 深色与浅色主题跟随系统，界面按 Material 3 构建。

## 环境要求

- Android 7.0（API 24）及以上
- 构建需要 JDK 17 与 Android SDK Platform 34

## 构建

```bash
git clone <repo-url> sim_radio
cd sim_radio
echo 'sdk.dir=<你的 Android SDK 路径>' > local.properties
./gradlew :app:assembleDebug
```

产物在 `app/build/outputs/apk/debug/app-debug.apk`。

安装到已连接的设备：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` 保存本机 SDK 路径，不属于仓库内容。Gradle wrapper 的 distributionUrl 指向腾讯云镜像，因为 services.gradle.org 在部分网络下不可达；如果你的网络可以直接访问官方源，改回 `https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip` 即可。

## 技术栈

| 组成 | 选择 |
| --- | --- |
| 语言与 UI | Kotlin 2.0、Jetpack Compose、Material 3 |
| 播放 | AndroidX Media3（ExoPlayer + MediaSessionService） |
| 网络 | OkHttp + kotlinx.serialization |
| 本地存储 | DataStore Preferences |
| 图片 | Coil |

## 数据来源

目录查询走 radio-browser 的 `stations/search`、`countries` 与 `tags` 接口。官方发现端点 `all.api.radio-browser.info` 在部分网络下不稳，因此内置 `de1` / `si1` / `fr1` / `fi1` 四个镜像并按序做故障转移，探测结果缓存 24 小时。

接口与用法见 [radio-browser API 文档](https://de1.api.radio-browser.info/)。

## 版权与使用边界

- 目录数据为 radio-browser 社区维护的开放数据，遵循其站点条款。
- 音频流来自各电台的公开直播，版权归电台及版权方所有。应用不录制、不转存、不二次分发，也不绕过任何付费墙或 DRM。
- 仅限个人收听与学习使用。收听与再分发受各地法律及各电台服务条款约束。

## 许可

代码以 MIT 许可发布，详见 [LICENSE](LICENSE)。