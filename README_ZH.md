# 姨妈来了 (YIMA) — 经期日历

[English Version](README.md)

一款隐私优先、开源的经期追踪应用，基于 Kotlin Multiplatform 和 Compose Multiplatform 构建。所有数据始终留在你的设备上。无账号、无云同步、无联网、无追踪、无广告。

**由 [Novawerk](https://github.com/Novawerk) 打造** — 开源应用，用心制作。

## 为什么有这款应用

很多小工具类 App 本来是很好用的。但在某个时间点之后，它们就被"必须盈利"这件事慢慢拖垮了 —— 核心功能被藏进付费墙，界面塞满广告和各种推销，设置页面的一半功能只是为了暗中把你推向订阅。慢慢地，留下来的那些功能已经很难让人想起当初为什么会下载它。

YIMA 是一次反向实验：**尝试以"不盈利"为前提去设计一个小工具，看看这样子它的体验能不能重新变好**。没有商业模式，就没有理由去阉割核心体验；没有广告，就没有理由想办法让你反复打开；没有账号，就没有理由去收集你的数据。把这些全部剥掉之后，剩下来的就只有工具本身 —— 一个尽量真正好用、然后安静走开的经期日历。

如果这个实验成立，也许更多的小工具都可以按这个思路被重新做一遍。

## 功能特色

### 记录
- **一键经期记录** — 主页一键点击"姨妈来了 / 姨妈走了"，默认会根据你的平均经期长度自动估算结束日期，等你确认
- **补录过去经期** — 在日历上选择区间即可补录任意历史经期
- **日历内直接编辑** — 在详细日历上点击任意一天，可直接添加、删除、延长或缩短经期

### 预测与周期阶段
- **预测接下来 3 次经期** — 基于你的历史平均周期长度，初期回落到入门设置中的周期长度
- **异常过滤** — 周期小于 14 天的会被视为异常，不计入预测平均值，避免一次偶发的不规律周期影响后续预测
- **智能自动确认** — 如果预测经期已经过了预期结束日 3 天以上，且你没有记录任何数据，YIMA 会静默将其自动确认为真实记录，确保即使你忘记点也不会乱
- **4 个周期阶段** — 经期、卵泡期、排卵期、黄体期 — 采用医学倒推法计算（排卵日为"周期长度 − 14"，排卵窗口为 6 天）
- **每日阶段详情** — 在详细日历上点击任意一天，可查看当前是周期的第几天、所处阶段、排卵日标记及贴士

### 主页三种视图
- **概览** — 大圆形日历一眼看清当前周期及阶段指示
- **详细日历** — 按月滑动的日历，彩色标记经期、预测经期、排卵期、排卵日花朵图标，以及经期起止标记
- **统计** — 最近几个周期的柱状图（异常会突出标记），最近 6 个周期的平均经期时长和周期长度，以及可滚动的历史记录

### 每日记录（弹窗）
- **经量** — 少量 / 中量 / 大量
- **心情** — 😊 😐 😔 😢
- **症状** — 痛经、腰酸、头痛、胸胀、疲劳
- **自由文字笔记**

### 通知提醒（默认关闭）
- **经期提醒** — 下次预测经期前 1–7 天
- **排卵日提醒** — 排卵高峰日前 1–7 天
- **每日提醒** — 在你设定的时间

三种提醒默认都是关闭的，且需要你在系统层级明确授权。

### 健康数据同步
- **Apple 健康 & Google 健康互联** — 通过 [HealthKMP](https://github.com/vitoksmile/HealthKMP) 双向同步经期和经量数据
- **导入** — 从健康平台读取经期记录并与现有数据合并（自动跳过重叠的记录）
- **导出** — 将手动记录的经期数据回写到健康平台
- **用户自主开启** — 默认关闭，可在设置中"健康数据"里开启

### 周期报告导出
- **长图 PNG 报告** — 一键生成一张可分享的长图，包含你的总览（记录总数、平均周期、平均经期）和每一条记录的每日详情（经量、心情、症状、笔记）
- **报告语言可选** — 中文或英文，独立于应用的语言设置
- **系统原生分享面板** — Android 和 iOS 都使用系统分享面板，你可以自己保存、发给医生或任何你想分享的人 — YIMA 完全不经手这个文件

### 入门引导
- 欢迎页（带应用 Logo）
- 在日历上选择上一次经期的区间
- 滑动条设置经期时长（2–10 天）和周期长度（20–45 天）
- YIMA 会基于你的设置自动生成 5 个过去的周期，让预测和统计**从第一天起就能工作** — 不用等到你记满 2 次真实周期

### 设置
- 显示模式 — 跟随系统 / 浅色 / 深色
- 语言 — 自动 / English / 中文
- 经期时长 & 周期长度（滑动条，可随时修改）
- 健康数据同步（Apple 健康 / Google 健康互联）
- 通知
- 周期报告导出
- 关于（版本、作者）
- 清除所有数据（危险操作，带确认）

### 隐私
- **仅本地存储** — 所有数据通过 Jetpack DataStore 存在你自己的设备上
- **无账号、无登录、无手机号**
- **零用户数据联网** — 应用不会发送任何包含你经期信息的 HTTP 请求
- **无统计、无追踪、无广告**
- **开源** — 代码完全公开，欢迎审计
- **首次启动显示免责声明** — YIMA 是一个追踪工具，不是医疗设备

### 平台与设计
- **Android**（最低 SDK 26）和 **iOS** — 共享代码库
- **Material Design 3 Expressive** 设计，搭配 Comfortaa 字体
- 完整中英双语，包括本地化的月份和星期名称

## 截图

<!-- TODO: 添加截图 -->

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 2.2 |
| UI | Compose Multiplatform 1.8 (Material 3 Expressive) |
| 导航 | Compose Navigation，类型安全 `@Serializable` 路由 |
| 依赖注入 | kotlin-inject + KSP (`@KmpComponentCreate`) |
| 健康数据 | [HealthKMP](https://github.com/vitoksmile/HealthKMP)（Apple HealthKit + Google Health Connect）|
| 存储 | Jetpack DataStore Preferences + kotlinx.serialization |
| 日期时间 | kotlinx-datetime + kotlinx-datetime-names |
| 通知 | 通过 `expect` / `actual` 的平台专属调度器 |
| 目标平台 | Android (最低 SDK 26，编译/目标 36)、iOS |
| 构建 | Gradle 8.7 + 版本目录 |

## 架构

单模块 (`:composeApp`)，清晰分层：

```
composeApp/src/commonMain/kotlin/com/haodong/yimalaile/
├── App.kt                            # 根 Composable + NavHost
├── di/                               # kotlin-inject 根组件
├── domain/
│   ├── menstrual/                    # MenstrualService、周期逻辑、模型
│   ├── health/                       # HealthService、HealthSyncManager
│   ├── notifications/                # 提醒调度契约
│   └── settings/                     # SettingsRepository + AppDarkMode
├── infrastructure/
│   └── persistence/                  # DataStore 实现
└── ui/
    ├── theme/                        # M3 Expressive 主题 + Comfortaa 字体
    ├── navigation/                   # 类型安全路由
    ├── components/                   # 共享日历及布局组件
    └── pages/
        ├── disclaimer/               # 首次启动医疗免责声明
        ├── onboarding/               # 4 步入门引导
        ├── home/                     # 概览 / 详细 / 统计三种模式
        ├── settings/                 # 设置 + 通知设置
        └── sheet/                    # 共享的 BottomSheet（记录、详情、选择器）
```

## 快速开始

```bash
# 前置条件：JDK 17+、Android SDK

# 构建 Android
./gradlew :composeApp:assembleDebug

# 安装到设备 / 模拟器
./gradlew :composeApp:installDebug
```

iOS 端请在 Xcode 中打开 `iosApp/` 并正常构建。首次构建前请先将 `iosApp/Configuration/Config.xcconfig.template` 复制为 `Config.xcconfig` 并填入你的 Team ID。

## 业务规则

1. 当前经期进行中（尚未确认结束）时不能开始新的经期
2. 经期日期范围不能重叠
3. 补录不受当前活跃经期影响
4. 预测至少需要一条记录；入门引导会自动生成 5 个历史周期，让应用从第一天就可用
5. 周期短于 14 天会被视为异常，不计入预测平均值和统计平均值
6. 预测经期超过预期结束日 3 天仍无记录时，会自动确认为真实记录
7. 结束或缩短经期时，会自动裁剪范围外的每日记录
8. 所有数据仅本地存储；首次启动显示免责声明
9. YIMA 是一个追踪工具，不是医疗设备 — 不提供任何医疗建议

## 下载

- **App Store** — 搜索「姨妈来了」或 "YIMA"
- **Google Play** — 搜索「姨妈来了」或 "YIMA"

## 参与贡献

欢迎贡献代码！请提交 Issue 或 Pull Request。

## 许可协议

本项目基于 [MIT 许可协议](LICENSE) 开源。

Copyright (c) 2025–2026 [Novawerk](https://github.com/Novawerk)。你可以自由使用、修改和分发本软件，只需保留原始版权声明。
