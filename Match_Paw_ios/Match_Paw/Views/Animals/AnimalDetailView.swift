//
//  AnimalDetailView.swift
//  Match_Paw
//

import SwiftUI

struct AnimalDetailView: View {
    let animal: Animal
    @State private var showApplication = false
    @State private var showLogin = false
    @EnvironmentObject var auth: AuthViewModel

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)
    private let warmBg = Color(red: 0.95, green: 0.91, blue: 0.85)

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Hero photo
                Group {
                    if let urlStr = animal.photoUrl, let url = URL(string: urlStr) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let img): img.resizable().scaledToFill()
                            default: heroPlacer
                            }
                        }
                    } else {
                        heroPlacer
                    }
                }
                .frame(height: 300)
                .clipped()
                .background(warmBg)

                VStack(alignment: .leading, spacing: 20) {
                    // Name row
                    HStack(alignment: .top) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(animal.name)
                                .font(.title.bold())
                            Text(animal.breed ?? animal.species)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Text("\(animal.speciesEmoji) \(animal.species)")
                            .font(.caption.weight(.semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(pawBrown.opacity(0.1))
                            .foregroundColor(pawBrown)
                            .cornerRadius(20)
                    }

                    // Stats
                    HStack(spacing: 12) {
                        if let age = animal.age {
                            statPill(icon: "calendar", text: "\(age) yr")
                        }
                        if let sex = animal.sex {
                            statPill(icon: "person.fill", text: sex)
                        }
                        if let health = animal.healthStatus {
                            statPill(icon: "heart.fill", text: health)
                        }
                    }

                    Divider()

                    // Notes
                    if let notes = animal.notes, !notes.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("About \(animal.name)")
                                .font(.headline)
                            Text(notes)
                                .font(.body)
                                .foregroundColor(.secondary)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }

                    // Intake date
                    if let date = animal.intakeDate {
                        HStack(spacing: 6) {
                            Image(systemName: "calendar.badge.plus")
                                .foregroundColor(pawBrown)
                            Text("At shelter since \(date)")
                                .font(.footnote)
                                .foregroundColor(.secondary)
                        }
                    }

                    // CTA
                    if animal.isAvailable {
                        Button {
                            if auth.isLoggedIn {
                                showApplication = true
                            } else {
                                showLogin = true
                            }
                        } label: {
                            Label("Apply for Adoption", systemImage: "heart.fill")
                                .font(.headline)
                                .frame(maxWidth: .infinity)
                                .padding(16)
                                .background(pawBrown)
                                .foregroundColor(.white)
                                .cornerRadius(14)
                        }
                        .padding(.top, 4)
                    } else {
                        HStack(spacing: 8) {
                            Image(systemName: "info.circle.fill")
                            Text("\(animal.name) is currently \(animal.adoptionStatus.lowercased()).")
                        }
                        .font(.subheadline.weight(.medium))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(Color(.systemGray6))
                        .foregroundColor(.secondary)
                        .cornerRadius(12)
                        .padding(.top, 4)
                    }
                }
                .padding()
            }
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showApplication) {
            ApplicationFormView(animal: animal)
        }
        .sheet(isPresented: $showLogin) {
            LoginView()
        }
        .onChange(of: auth.isLoggedIn) { _, loggedIn in
            if loggedIn {
                showLogin = false
                showApplication = true
            }
        }
    }

    private var heroPlacer: some View {
        Image(systemName: "pawprint.fill")
            .resizable().scaledToFit().padding(80)
            .foregroundColor(pawBrown.opacity(0.2))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(warmBg)
    }

    private func statPill(icon: String, text: String) -> some View {
        HStack(spacing: 4) {
            Image(systemName: icon).font(.caption)
            Text(text).font(.caption.weight(.medium))
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}
