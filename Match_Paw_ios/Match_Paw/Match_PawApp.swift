//
//  Match_PawApp.swift
//  Match_Paw
//
//  Created by Joji Kashimura on 5/25/26.
//

import SwiftUI

@main
struct Match_PawApp: App {
    @StateObject private var auth = AuthViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(auth)
        }
    }
}
