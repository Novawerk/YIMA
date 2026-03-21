package com.haodong.yimalaile.data

import com.haodong.yimalaile.KeyValueStoreFactory

// ---------- Key-value store abstraction ----------

interface KeyValueStore {
    fun getBoolean(key: String, default: Boolean): Boolean
    fun setBoolean(key: String, value: Boolean)
}

class InMemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, Boolean>()
    override fun getBoolean(key: String, default: Boolean): Boolean = map.getOrDefault(key, default)
    override fun setBoolean(key: String, value: Boolean) { map[key] = value }
}

// ---------- Settings repository interface ----------

interface SettingsRepository {
    fun isDisclaimerAccepted(): Boolean
    fun setDisclaimerAccepted(value: Boolean)
}

// ---------- In-memory test double ----------

class InMemorySettingsRepository(private val kv: InMemoryKeyValueStore) : SettingsRepository {
    override fun isDisclaimerAccepted(): Boolean = kv.getBoolean("disclaimer_accepted", false)
    override fun setDisclaimerAccepted(value: Boolean) = kv.setBoolean("disclaimer_accepted", value)
}

// ---------- Production implementation ----------

class DefaultSettingsRepository(private val kv: KeyValueStore) : SettingsRepository {
    override fun isDisclaimerAccepted(): Boolean = kv.getBoolean("disclaimer_accepted", false)
    override fun setDisclaimerAccepted(value: Boolean) = kv.setBoolean("disclaimer_accepted", value)
}

// ---------- App-level singleton (simple DI) ----------

object AppSettings {
    private var repository: SettingsRepository? = null

    fun init(factory: KeyValueStoreFactory) {
        if (repository == null) {
            repository = DefaultSettingsRepository(factory.create())
        }
    }

    fun requireRepository(): SettingsRepository =
        repository ?: error("AppSettings not initialized. Call AppSettings.init() first.")
}
