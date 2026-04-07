import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let screenshotMode: Bool

    init(screenshotMode: Bool = false) {
        self.screenshotMode = screenshotMode
    }

    func makeUIViewController(context: Context) -> UIViewController {
        if screenshotMode {
            return MainViewControllerKt.ScreenshotMainViewController()
        }
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    private let screenshotMode = ProcessInfo.processInfo.arguments.contains("--screenshot-mode")

    var body: some View {
        ComposeView(screenshotMode: screenshotMode)
            .ignoresSafeArea()
    }
}



