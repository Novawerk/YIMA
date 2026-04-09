import XCTest

/// Captures screenshots of all screens in EN/ZH × Light/Dark combinations.
/// Covers: Home (3 tabs), Settings, Disclaimer, Onboarding (4 steps), Record Detail sheet.
final class ScreenshotUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    // ═══════════════════════════════════════════════════
    // MARK: - Home Screens (Calendar, Detail, Stats, Settings)
    // ═══════════════════════════════════════════════════

    func test_en_light() throws {
        try captureHomeScreens(language: "en", locale: "en_US", appearance: .light, suffix: "en_light")
    }

    func test_en_dark() throws {
        try captureHomeScreens(language: "en", locale: "en_US", appearance: .dark, suffix: "en_dark")
    }

    func test_zh_light() throws {
        try captureHomeScreens(language: "zh-Hans", locale: "zh_CN", appearance: .light, suffix: "zh_light")
    }

    func test_zh_dark() throws {
        try captureHomeScreens(language: "zh-Hans", locale: "zh_CN", appearance: .dark, suffix: "zh_dark")
    }

    // ═══════════════════════════════════════════════════
    // MARK: - Disclaimer Screen
    // ═══════════════════════════════════════════════════

    func test_disclaimer_en_light() throws {
        try captureDisclaimer(language: "en", locale: "en_US", appearance: .light, suffix: "en_light")
    }

    func test_disclaimer_en_dark() throws {
        try captureDisclaimer(language: "en", locale: "en_US", appearance: .dark, suffix: "en_dark")
    }

    func test_disclaimer_zh_light() throws {
        try captureDisclaimer(language: "zh-Hans", locale: "zh_CN", appearance: .light, suffix: "zh_light")
    }

    func test_disclaimer_zh_dark() throws {
        try captureDisclaimer(language: "zh-Hans", locale: "zh_CN", appearance: .dark, suffix: "zh_dark")
    }

    // ═══════════════════════════════════════════════════
    // MARK: - Onboarding Flow
    // ═══════════════════════════════════════════════════

    func test_onboarding_en_light() throws {
        try captureOnboarding(language: "en", locale: "en_US", appearance: .light, suffix: "en_light")
    }

    func test_onboarding_en_dark() throws {
        try captureOnboarding(language: "en", locale: "en_US", appearance: .dark, suffix: "en_dark")
    }

    func test_onboarding_zh_light() throws {
        try captureOnboarding(language: "zh-Hans", locale: "zh_CN", appearance: .light, suffix: "zh_light")
    }

    func test_onboarding_zh_dark() throws {
        try captureOnboarding(language: "zh-Hans", locale: "zh_CN", appearance: .dark, suffix: "zh_dark")
    }

    // ═══════════════════════════════════════════════════
    // MARK: - Record Detail Sheet (opened from Stats)
    // ═══════════════════════════════════════════════════

    func test_record_detail_en_light() throws {
        try captureRecordDetail(language: "en", locale: "en_US", appearance: .light, suffix: "en_light")
    }

    func test_record_detail_en_dark() throws {
        try captureRecordDetail(language: "en", locale: "en_US", appearance: .dark, suffix: "en_dark")
    }

    func test_record_detail_zh_light() throws {
        try captureRecordDetail(language: "zh-Hans", locale: "zh_CN", appearance: .light, suffix: "zh_light")
    }

    func test_record_detail_zh_dark() throws {
        try captureRecordDetail(language: "zh-Hans", locale: "zh_CN", appearance: .dark, suffix: "zh_dark")
    }

    // ═══════════════════════════════════════════════════
    // MARK: - Helpers
    // ═══════════════════════════════════════════════════

    /// Launch the app in screenshot-home mode and capture all home tabs + settings.
    private func captureHomeScreens(
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance,
        suffix: String
    ) throws {
        let app = launchApp(mode: "--screenshot-mode", language: language, locale: locale, appearance: appearance)

        // Verify the app actually launched
        let calendarTab = app.buttons["nav_calendar"]
        let launched = calendarTab.waitForExistence(timeout: 30)
        XCTAssertTrue(launched, "App failed to launch — nav_calendar button not found")

        // 1. Home Calendar (default view on launch)
        saveScreenshot(name: "home_calendar_\(suffix)")

        // 2. Home Detail
        let detailTab = app.buttons["nav_detail"]
        XCTAssertTrue(detailTab.waitForExistence(timeout: 10), "nav_detail not found")
        detailTab.tap()
        sleep(2)
        saveScreenshot(name: "home_detail_\(suffix)")

        // 3. Home Stats
        let statsTab = app.buttons["nav_stats"]
        XCTAssertTrue(statsTab.waitForExistence(timeout: 10), "nav_stats not found")
        statsTab.tap()
        sleep(2)
        saveScreenshot(name: "home_stats_\(suffix)")

        // 4. Settings
        let settingsButton = app.buttons["nav_settings"]
        XCTAssertTrue(settingsButton.waitForExistence(timeout: 10), "nav_settings not found")
        settingsButton.tap()
        sleep(2)
        saveScreenshot(name: "settings_\(suffix)")
    }

    /// Launch in disclaimer mode and capture the disclaimer screen.
    private func captureDisclaimer(
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance,
        suffix: String
    ) throws {
        let app = launchApp(mode: "--screenshot-disclaimer", language: language, locale: locale, appearance: appearance)

        let acceptBtn = app.buttons["btn_accept"]
        let launched = acceptBtn.waitForExistence(timeout: 30)
        XCTAssertTrue(launched, "Disclaimer screen failed to load — btn_accept not found")

        saveScreenshot(name: "disclaimer_\(suffix)")
    }

    /// Launch in onboarding mode and capture each step.
    private func captureOnboarding(
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance,
        suffix: String
    ) throws {
        let app = launchApp(mode: "--screenshot-onboarding", language: language, locale: locale, appearance: appearance)

        // Step 0: Welcome
        let nextBtn = app.buttons["onboarding_next"]
        let launched = nextBtn.waitForExistence(timeout: 30)
        XCTAssertTrue(launched, "Onboarding failed to load — onboarding_next not found")
        saveScreenshot(name: "onboarding_welcome_\(suffix)")

        // Advance to step 1: Calendar
        nextBtn.tap()
        sleep(2)
        saveScreenshot(name: "onboarding_calendar_\(suffix)")
    }

    /// Launch in home mode, navigate to stats, tap a record card to open the detail sheet.
    private func captureRecordDetail(
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance,
        suffix: String
    ) throws {
        let app = launchApp(mode: "--screenshot-mode", language: language, locale: locale, appearance: appearance)

        // Navigate to Stats tab
        let statsTab = app.buttons["nav_stats"]
        let launched = statsTab.waitForExistence(timeout: 30)
        XCTAssertTrue(launched, "App failed to launch")
        statsTab.tap()
        sleep(2)

        // Tap the most recent record card (r7 is the latest in test data)
        let recordCard = app.buttons["record_card_r7"]
        if recordCard.waitForExistence(timeout: 10) {
            recordCard.tap()
            sleep(2)
            saveScreenshot(name: "record_detail_\(suffix)")
        }
    }

    // ── Shared launch helper ──

    private func launchApp(
        mode: String,
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = [mode]
        app.launchArguments += ["-AppleLanguages", "(\(language))"]
        app.launchArguments += ["-AppleLocale", locale]

        XCUIDevice.shared.appearance = appearance
        app.launch()
        return app
    }

    private func saveScreenshot(name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
