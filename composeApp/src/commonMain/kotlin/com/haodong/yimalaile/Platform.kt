package com.haodong.yimalaile

import app.cash.sqldelight.db.SqlDriver
import com.haodong.yimalaile.data.KeyValueStore

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect class KeyValueStoreFactory {
    fun create(): KeyValueStore
}