package com.haodong.yimalaile.domain.settings

/**
 * 应用的显示模式设置。
 */
enum class AppDarkMode(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        /**
         * 从字符串值获取枚举，如果找不到或无效则返回 [SYSTEM]。
         */
        fun fromValue(value: String?): AppDarkMode {
            return entries.find { it.value == value } ?: SYSTEM
        }
    }
}
