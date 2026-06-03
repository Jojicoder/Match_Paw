//
//  ContentView.swift
//  Match_Paw
//

import SwiftUI

struct ContentView: View {
    @State private var splashDone = false

    var body: some View {
        ZStack {
            if !splashDone {
                SplashView {
                    withAnimation(.easeInOut(duration: 0.4)) {
                        splashDone = true
                    }
                }
                .transition(.opacity)
                .zIndex(1)
            } else {
                MainTabView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: splashDone)
    }
}

#Preview {
    ContentView()
        .environmentObject(AuthViewModel())
}
