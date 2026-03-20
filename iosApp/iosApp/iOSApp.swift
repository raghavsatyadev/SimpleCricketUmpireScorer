import SwiftUI
import SCUSLib

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.InitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}