@file:Suppress("unused")
package com.haodong.yimalaile.fakes

// Re-export from commonMain for backwards compatibility.
// All test data and FakeRecordsRepository are now in:
// commonMain/kotlin/com/haodong/yimalaile/fakes/ScreenshotTestData.kt

/**
 * Alias for the shared [createScreenshotTestData] function.
 */
fun createBeautifulTestData() = createScreenshotTestData()
