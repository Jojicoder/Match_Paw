//
//  MyApplicationsView.swift
//  Match_Paw
//

import SwiftUI

struct MyApplicationsView: View {
    @EnvironmentObject var appVM: ApplicationViewModel
    @EnvironmentObject var auth: AuthViewModel
    @EnvironmentObject var animalVM: AnimalViewModel

    var body: some View {
        NavigationStack {
            Group {
                if appVM.isLoading {
                    ProgressView()
                } else if let err = appVM.errorMessage {
                    ContentUnavailableView(
                        "Couldn't load",
                        systemImage: "exclamationmark.triangle",
                        description: Text(err)
                    )
                } else if appVM.applications.isEmpty {
                    ContentUnavailableView(
                        "No Applications Yet",
                        systemImage: "list.bullet.clipboard",
                        description: Text("Browse animals and submit an adoption application.")
                    )
                } else {
                    List(appVM.applications) { app in
                        NavigationLink {
                            ApplicationDetailView(
                                application: app,
                                animal: animalVM.animal(id: app.animalId)
                            )
                        } label: {
                            ApplicationRowView(
                                application: app,
                                animal: animalVM.animal(id: app.animalId)
                            )
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("My Applications")
            .task {
                if let id = auth.currentApplicant?.id {
                    await appVM.load(applicantId: id)
                }
            }
            .refreshable {
                if let id = auth.currentApplicant?.id {
                    await appVM.load(applicantId: id)
                }
            }
        }
    }
}

struct ApplicationRowView: View {
    let application: AdoptionApplication
    let animal: Animal?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(animal.map { "\($0.speciesEmoji) \($0.name)" } ?? "Animal #\(application.animalId)")
                    .font(.headline)
                Spacer()
                StatusBadge(status: application.displayStatus, color: application.statusColor)
            }

            if let breed = animal?.breed {
                Text(breed)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            Text("Applied: \(application.applicationDate)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }
}

struct StatusBadge: View {
    let status: String
    let color: Color

    var body: some View {
        Text(status)
            .font(.caption.weight(.semibold))
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(color.opacity(0.15))
            .foregroundColor(color)
            .cornerRadius(8)
    }
}
