# 姨妈周期记录与数据显示：数据模型与原理详解

目前的系统中，姨妈数据的管理遵循典型的 **数据存储 -> 业务逻辑计算 -> 视图模型封装 -> UI展示** 的流程。以下是详细的技术细节：

---

### 1. 核心数据模型 (Data Model)

系统的核心数据类是 `MenstrualRecord`，它定义在 `MenstrualRecord.kt` 中：

```kotlin
data class MenstrualRecord(
    val id: String,                    // 唯一标识符 (例如: MANUAL_1712050000_123)
    val startDate: LocalDateKey,       // 经期开始日期 (自定义日期对象)
    val endDate: LocalDateKey? = null, // 经期结束日期 (如果还在进行中则为 null)
    val intensity: Intensity? = null,  // 流量 (LIGHT, MEDIUM, HEAVY)
    val mood: Mood? = null,            // 心情 (HAPPY, NEUTRAL, SAD, VERY_SAD)
    val symptoms: List<String>,        // 症状列表
    val notes: String? = null,         // 备注
    val source: RecordSource,          // 来源 (MANUAL 手动录入 或 PREDICTION 预测)
    val isDeleted: Boolean = false     // 软删除标记
)
```

**关键设计点：**
- **LocalDateKey**: 这是一个自定义的轻量级日期对象 `(year, month, day)`，避免了不同平台（Android/iOS）原生日期库的兼容性问题。
- **软删除**: 数据不会被物理删除，而是标记 `isDeleted = true`，这有利于数据恢复和同步。

---

### 2. 计算原理与算法 (CycleCalculator)

所有的周期统计和预测逻辑都封装在 `CycleCalculator.kt` 中，采用 **平均值算法**：

#### A. 平均周期长度 (Average Cycle Length)
1. **排序**: 将所有未删除的记录按 `startDate` 升序排列。
2. **计算间距 (Gaps)**: 计算相邻两次记录 `startDate` 之间的天数。
3. **求均值**: `(间距1 + 间距2 + ... + 间距N) / N`。
   * *注意：至少需要 2 条记录才能计算周期。*

#### B. 平均经期长度 (Average Period Length)
1. **筛选**: 只提取 `endDate` 不为空的记录。
2. **计算时长**: 每次经期的持续天数为 `daysBetween(startDate, endDate) + 1`。
3. **求均值**: 所有持续天数的平均值。

#### C. 下次经期预测 (Prediction)
- **公式**: `最后一次开始日期 + 平均周期长度`。
- 如果没有足够的历史数据（少于 2 条记录），则无法进行预测。

---

### 3. 数据流架构 (Architecture)

应用采用了清晰的分层架构：

1.  **持久层 (SQLDelight)**:
    - 使用 `SqlDelightRecordsRepository` 进行数据库操作。
    - **冲突检测**: 在插入新记录时，系统会检查：
        - 是否存在相同 `startDate` 的记录。
        - 两次记录的间隔是否小于 **15天**（防止短时间内重复录入不合理的周期）。

2.  **业务层 (HomeViewModel)**:
    - 从 Repository 获取原始记录列表。
    - 调用 `CycleCalculator` 计算统计数据（平均值、预测日期等）。
    - 将结果封装进 `HomeState` 流中。

3.  **表示层 (Compose UI)**:
    - 监听 `HomeState` 的变化。
    - 在首页显示“距离下次还有 X 天”的倒计时。
    - 在统计页显示历史周期的趋势图表。

---

### 4. 数据流向图 (Diagram)

```mermaid
graph TD
    User([用户录入]) -->|点击保存| VM[HomeViewModel]
    VM -->|冲突检测/15天规则| REPO[SqlDelight Repository]
    REPO -->|持久化| DB[(SQLite 数据库)]
    
    DB -->|读取所有记录| REPO
    REPO -->|List of MenstrualRecord| VM
    
    subgraph "计算核心 (CycleCalculator)"
        VM -->|原始数据| CALC[平均值计算/预测算法]
        CALC -->|统计结果| VM
    }
    
    VM -->|封装 HomeState| UI[Compose UI 界面]
    UI -->|显示| Dash[首页进度环 / 倒计时]
    UI -->|显示| Stats[统计图表 / 历史列表]
```

---

### 5. 总结

- **存储**: 使用 SQLite 存储每一段经期的起止时间和附加信息。
- **逻辑**: 基于历史数据的 **移动平均值** 来预测未来。
- **验证**: 严格的 **15天间隔保护** 确保了数据的生理合理性。
- **显示**: 动态计算当前日期与预测日期的差值，实现实时更新的 UI。
