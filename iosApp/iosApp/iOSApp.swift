import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        IosModuleKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}