# 姨妈来了 | YIMA: Period Calendar

[English Version](README.md)

一款隐私优先、开源的经期周期追踪应用，基于 Kotlin Multiplatform 和 Compose Multiplatform 构建。所有数据始终留在你的设备上。

**由 [Novawerk](https://github.com/Novawerk) 打造** — 开源应用，用心制作。

## 功能特色

- **经期记录** — 记录开始/结束日期、每日经量、心情、症状和笔记
- **智能预测** — 只需 2 个完整周期，自动预测接下来 3 次经期
- **周期阶段** — 实时显示当前阶段（经期、卵泡期、排卵期、黄体期）及说明
- **可视化日历** — 彩色标记经期天数、预测日期和今天
- **统计数据** — 胶囊形柱状图展示周期趋势、异常检测、平均值和完整历史
- **补录功能** — 通过可视化日历批量补录过去的经期
- **隐私优先** — 所有数据通过 DataStore 存储在本地。无账号、无云同步、无追踪、无广告
- **双语支持** — 完整的中文和英文支持，日期和星期本地化显示
- **主题适配** — 浅色/深色/跟随系统模式，Material Design 3 Expressive 设计

## 截图

<!-- TODO: 添加截图 -->

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 2.2 |
| UI | Compose Multiplatform (Material Design 3 Expressive) |
| 导航 | Compose Navigation (类型安全 `@Serializable` 路由) |
| 存储 | Jetpack DataStore Preferences |
| 依赖注入 | kotlin-inject (KSP, `@KmpComponentCreate`) |
| 日期时间 | kotlinx-datetime + kotlinx-datetime-names |
| 目标平台 | Android (最低 SDK 24) / iOS |
| 构建 | Gradle + 版本目录 |

## 架构

单模块 (`:composeApp`)，清晰分层：

```
composeApp/src/commonMain/kotlin/com/haodong/yimalaile/
├── App.kt                          # NavHost + 启动逻辑
├── domain/
│   ├── menstrual/                   # MenstrualService、模型、仓库
│   └── settings/                    # SettingsRepository
├── infrastructure/
│   └── persistence/                 # DataStore 实现
└── ui/
    ├── theme/                       # M3 Expressive 主题
    ├── navigation/                  # 类型安全路由
    ├── components/                  # 共享组件
    ├── pages/
    │   ├── disclaimer/              # 首次启动免责声明
    │   ├── onboarding/              # 初始数据录入（3 个周期）
    │   ├── home/                    # 日历、内嵌统计、阶段信息
    │   ├── settings/                # 主题、语言、关于、清除数据
    │   └── record/                  # 经期开始/结束/补录/详情弹窗
    └── locale/                      # 应用级语言管理
```

## 快速开始

```bash
# 前置条件：JDK 17+、Android SDK

# 构建 Android
./gradlew :composeApp:assembleDebug

# 安装到设备/模拟器
./gradlew :composeApp:installDebug
```

iOS 端请在 Xcode 中打开 `iosApp/` 并正常构建。

## 业务规则

1. 当前经期进行中时不能开始新的经期
2. 经期日期范围不能重叠
3. 补录不受当前活跃经期影响
4. 预测功能需要至少 2 个完整记录
5. 结束经期时自动裁剪范围外的每日记录
6. 所有数据仅存储在本地；首次启动显示免责声明
7. 不提供任何医疗建议

## 参与贡献

欢迎贡献代码！请提交 Issue 或 Pull Request。

iOS 开发请将 `iosApp/Configuration/Config.xcconfig.template` 复制为 `Config.xcconfig` 并填入你的 Team ID。

## 许可协议

本项目基于 [MIT 许可协议](LICENSE) 开源。

Copyright (c) 2025-2026 [Novawerk](https://github.com/Novawerk)。你可以自由使用、修改和分发本软件，只需保留原始版权声明。
