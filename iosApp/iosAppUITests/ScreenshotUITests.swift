import XCTest

/// Captures screenshots of all screens in EN/ZH × Light/Dark combinations.
/// Run with: xcodebuild test -project iosApp.xcodeproj -scheme iosApp
///           -destination 'platform=iOS Simulator,name=iPhone 16'
///           -only-testing iosAppUITests
final class ScreenshotUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    // MARK: - English Light

    func test_en_light() throws {
        try captureAllScreens(language: "en", locale: "en_US", appearance: .light, suffix: "en_light")
    }

    // MARK: - English Dark

    func test_en_dark() throws {
        try captureAllScreens(language: "en", locale: "en_US", appearance: .dark, suffix: "en_dark")
    }

    // MARK: - Chinese Light

    func test_zh_light() throws {
        try captureAllScreens(language: "zh-Hans", locale: "zh_CN", appearance: .light, suffix: "zh_light")
    }

    // MARK: - Chinese Dark

    func test_zh_dark() throws {
        try captureAllScreens(language: "zh-Hans", locale: "zh_CN", appearance: .dark, suffix: "zh_dark")
    }

    // MARK: - Helpers

    private func captureAllScreens(
        language: String,
        locale: String,
        appearance: XCUIDevice.Appearance,
        suffix: String
    ) throws {
        let app = XCUIApplication()
        app.launchArguments = ["--screenshot-mode"]
        app.launchArguments += ["-AppleLanguages", "(\(language))"]
        app.launchArguments += ["-AppleLocale", locale]

        XCUIDevice.shared.appearance = appearance
        app.launch()

        // Wait for the app to fully render
        sleep(3)

        // 1. Home Calendar (default view on launch)
        saveScreenshot(name: "home_calendar_\(suffix)")

        // 2. Home Detail — tap detail tab
        let detailTab = app.buttons["nav_detail"]
        if detailTab.waitForExistence(timeout: 5) {
            detailTab.tap()
            sleep(1)
            saveScreenshot(name: "home_detail_\(suffix)")
        }

        // 3. Home Stats — tap stats tab
        let statsTab = app.buttons["nav_stats"]
        if statsTab.waitForExistence(timeout: 5) {
            statsTab.tap()
            sleep(1)
            saveScreenshot(name: "home_stats_\(suffix)")
        }

        // 4. Settings — tap settings icon
        let settingsButton = app.buttons["nav_settings"]
        if settingsButton.waitForExistence(timeout: 5) {
            settingsButton.tap()
            sleep(1)
            saveScreenshot(name: "settings_\(suffix)")
        }
    }

    private func saveScreenshot(name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
