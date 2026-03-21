package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryTest {
    @Test
    fun disclaimer_acceptance_toggles() {
        val kv = InMemoryKeyValueStore()
        val repo = InMemorySettingsRepository(kv)
        assertFalse(repo.isDisclaimerAccepted())
        repo.setDisclaimerAccepted(true)
        assertTrue(repo.isDisclaimerAccepted())
    }
}
