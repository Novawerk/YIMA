package com.haodong.yimalaile

import androidx.compose.ui.window.ComposeUIViewController
import com.haodong.yimalaile.data.AppDatabase

fun MainViewController() = ComposeUIViewController {
    AppDatabase.init(DatabaseDriverFactory())
    App()
}
