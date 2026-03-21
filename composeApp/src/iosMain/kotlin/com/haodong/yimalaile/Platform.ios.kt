package com.haodong.yimalaile

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.haodong.yimalaile.data.KeyValueStore
import com.haodong.yimalaile.data.YimalaileDatabase
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(YimalaileDatabase.Schema, "yimalaile.db")
}

private class NsUserDefaultsKeyValueStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default
    override fun setBoolean(key: String, value: Boolean) = defaults.setBool(value, forKey = key)
}

actual class KeyValueStoreFactory {
    actual fun create(): KeyValueStore = NsUserDefaultsKeyValueStore()
}