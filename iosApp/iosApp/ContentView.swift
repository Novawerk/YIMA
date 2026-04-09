import UIKit
import SwiftUI
import ComposeApp

enum ScreenshotMode: String {
    case none
    case home       // --screenshot-mode
    case disclaimer // --screenshot-disclaimer
    case onboarding // --screenshot-onboarding
}

struct ComposeView: UIViewControllerRepresentable {
    let mode: ScreenshotMode

    init(mode: ScreenshotMode = .none) {
        self.mode = mode
    }

    func makeUIViewController(context: Context) -> UIViewController {
        switch mode {
        case .home:
            return MainViewControllerKt.ScreenshotMainViewController()
        case .disclaimer:
            return MainViewControllerKt.ScreenshotDisclaimerViewController()
        case .onboarding:
            return MainViewControllerKt.ScreenshotOnboardingViewController()
        case .none:
            return MainViewControllerKt.MainViewController()
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    private let mode: ScreenshotMode = {
        let args = ProcessInfo.processInfo.arguments
        if args.contains("--screenshot-disclaimer") { return .disclaimer }
        if args.contains("--screenshot-onboarding") { return .onboarding }
        if args.contains("--screenshot-mode") { return .home }
        return .none
    }()

    var body: some View {
        ComposeView(mode: mode)
            .ignoresSafeArea()
    }
}
