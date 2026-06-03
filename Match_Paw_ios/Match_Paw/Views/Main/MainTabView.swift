//
//  MainTabView.swift
//  Match_Paw
//

import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var auth: AuthViewModel
    @StateObject private var animalVM = AnimalViewModel()
    @StateObject private var appVM = ApplicationViewModel()

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }

            SearchView()
                .tabItem { Label("Search", systemImage: "magnifyingglass") }

            applicationsTab
                .tabItem { Label("Applications", systemImage: "list.bullet.clipboard.fill") }

            profileTab
                .tabItem { Label("Profile", systemImage: "person.fill") }
        }
        .tint(pawBrown)
        .environmentObject(animalVM)
        .environmentObject(appVM)
        .task { await animalVM.load() }
    }

    @ViewBuilder
    private var applicationsTab: some View {
        if auth.isLoggedIn {
            MyApplicationsView()
        } else {
            LoginPromptView(
                title: "Your Applications",
                message: "Log in to view and track your adoption applications.",
                systemImage: "list.bullet.clipboard.fill"
            )
        }
    }

    @ViewBuilder
    private var profileTab: some View {
        if auth.isLoggedIn {
            ProfileView()
        } else {
            LoginPromptView(
                title: "Profile",
                message: "Log in to manage your account.",
                systemImage: "person.fill"
            )
        }
    }
}

struct LoginPromptView: View {
    let title: String
    let message: String
    let systemImage: String

    @State private var showLogin = false

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Image(systemName: systemImage)
                    .font(.system(size: 52))
                    .foregroundColor(pawBrown.opacity(0.4))

                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 40)

                Button("Log In") {
                    showLogin = true
                }
                .font(.headline)
                .frame(maxWidth: .infinity)
                .padding(14)
                .background(pawBrown)
                .foregroundColor(.white)
                .cornerRadius(12)
                .padding(.horizontal, 40)
            }
            .navigationTitle(title)
            .sheet(isPresented: $showLogin) {
                LoginView()
            }
        }
    }
}
