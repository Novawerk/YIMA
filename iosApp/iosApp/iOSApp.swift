import SwiftUI
import UserNotifications

@main
struct iOSApp: App {
    // Bridge UIKit lifecycle into SwiftUI so we can set a
    // UNUserNotificationCenterDelegate. Without this, local notifications
    // scheduled from Kotlin (IosNotificationScheduler) will NOT show a
    // banner when the app is in the foreground.
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // Show notification banner + play sound when the app is in the
    // foreground. Without this the notification is silently delivered
    // to Notification Center with no visible alert.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .list, .sound])
        } else {
            completionHandler([.alert, .sound])
        }
    }
}
