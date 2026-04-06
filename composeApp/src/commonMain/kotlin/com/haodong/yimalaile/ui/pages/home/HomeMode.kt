package com.haodong.yimalaile.ui.pages.home

enum class HomeMode(val key: String) {
    CALENDAR("calendar"),
    DETAIL("detail"),
    STATS("stats");

    companion object {
        fun fromKey(key: String): HomeMode =
            entries.find { it.key == key } ?: CALENDAR
    }
}
