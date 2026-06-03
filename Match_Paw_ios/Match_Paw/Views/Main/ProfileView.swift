//
//  ProfileView.swift
//  Match_Paw
//

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var auth: AuthViewModel
    @State private var showLogoutAlert = false

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        NavigationStack {
            List {
                // Avatar + name
                Section {
                    HStack(spacing: 16) {
                        ZStack {
                            Circle()
                                .fill(pawBrown.opacity(0.12))
                                .frame(width: 64, height: 64)
                            Text(auth.currentApplicant?.initials ?? "?")
                                .font(.title2.bold())
                                .foregroundColor(pawBrown)
                        }
                        VStack(alignment: .leading, spacing: 4) {
                            Text(auth.currentApplicant?.fullName ?? "")
                                .font(.headline)
                            Text(auth.currentApplicant?.email ?? "")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.vertical, 6)
                }

                // Contact info
                Section("Contact") {
                    if let phone = auth.currentApplicant?.phone, !phone.isEmpty {
                        LabeledContent("Phone", value: phone)
                    }
                    if let address = auth.currentApplicant?.address, !address.isEmpty {
                        LabeledContent("Address", value: address)
                    }
                    if let housing = auth.currentApplicant?.housingType {
                        LabeledContent("Housing", value: housing)
                    }
                }

                // Household info
                Section("Household") {
                    LabeledContent("Has Pets", value: (auth.currentApplicant?.hasPets ?? false) ? "Yes" : "No")
                    LabeledContent("Has Children", value: (auth.currentApplicant?.hasChildren ?? false) ? "Yes" : "No")
                    if let exp = auth.currentApplicant?.experienceWithPets, !exp.isEmpty {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Pet Experience")
                                .font(.subheadline)
                            Text(exp)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding(.vertical, 2)
                    }
                }

                // Logout
                Section {
                    Button(role: .destructive) {
                        showLogoutAlert = true
                    } label: {
                        Label("Log Out", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                }
            }
            .navigationTitle("Profile")
            .alert("Log Out", isPresented: $showLogoutAlert) {
                Button("Log Out", role: .destructive) { auth.logout() }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("Are you sure you want to log out?")
            }
        }
    }
}
