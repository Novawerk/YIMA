package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryTest {

    // --- InMemorySettingsRepository ---

    @Test
    fun disclaimer_is_false_by_default() {
        // Given / When
        val repo = InMemorySettingsRepository(InMemoryKeyValueStore())

        // Then
        assertFalse(repo.isDisclaimerAccepted())
    }

    @Test
    fun disclaimer_acceptance_toggles() {
        // Given
        val repo = InMemorySettingsRepository(InMemoryKeyValueStore())

        // When / Then: false → true → false
        assertFalse(repo.isDisclaimerAccepted())
        repo.setDisclaimerAccepted(true)
        assertTrue(repo.isDisclaimerAccepted())
        repo.setDisclaimerAccepted(false)
        assertFalse(repo.isDisclaimerAccepted())
    }

    @Test
    fun two_repos_sharing_same_kv_store_share_state() {
        // Given: two repo instances backed by the same KV store
        val kv = InMemoryKeyValueStore()
        val repo1 = InMemorySettingsRepository(kv)
        val repo2 = InMemorySettingsRepository(kv)

        // When: write via repo1
        repo1.setDisclaimerAccepted(true)

        // Then: repo2 observes the change (simulates cross-instance persistence)
        assertTrue(repo2.isDisclaimerAccepted())
    }

    // --- DefaultSettingsRepository ---

    @Test
    fun default_repo_disclaimer_is_false_by_default() {
        // Given / When
        val repo = DefaultSettingsRepository(InMemoryKeyValueStore())

        // Then
        assertFalse(repo.isDisclaimerAccepted())
    }

    @Test
    fun default_repo_disclaimer_acceptance_toggles() {
        // Given
        val repo = DefaultSettingsRepository(InMemoryKeyValueStore())

        // When / Then
        repo.setDisclaimerAccepted(true)
        assertTrue(repo.isDisclaimerAccepted())
        repo.setDisclaimerAccepted(false)
        assertFalse(repo.isDisclaimerAccepted())
    }
}
