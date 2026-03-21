package com.haodong.yimalaile

import androidx.compose.ui.window.ComposeUIViewController
import com.haodong.yimalaile.data.AppDatabase
import com.haodong.yimalaile.data.AppSettings

fun MainViewController() = ComposeUIViewController {
    AppDatabase.init(DatabaseDriverFactory())
    AppSettings.init(KeyValueStoreFactory())
    App()
}
