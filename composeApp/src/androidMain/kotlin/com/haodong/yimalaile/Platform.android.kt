package com.haodong.yimalaile

import android.content.Context
import android.os.Build
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.haodong.yimalaile.data.KeyValueStore
import com.haodong.yimalaile.data.YimalaileDatabase

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(YimalaileDatabase.Schema, context, "yimalaile.db")
}

private class SharedPrefsKeyValueStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("yimalaile_settings", Context.MODE_PRIVATE)
    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    override fun setBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
}

actual class KeyValueStoreFactory(private val context: Context) {
    actual fun create(): KeyValueStore = SharedPrefsKeyValueStore(context)
}