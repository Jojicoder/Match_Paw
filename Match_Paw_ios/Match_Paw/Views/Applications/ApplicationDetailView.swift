//
//  ApplicationDetailView.swift
//  Match_Paw
//

import SwiftUI

struct ApplicationDetailView: View {
    let application: AdoptionApplication
    let animal: Animal?

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)
    private let warmBg = Color(red: 0.95, green: 0.91, blue: 0.85)

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Hero photo
                Group {
                    if let urlStr = animal?.photoUrl, let url = URL(string: urlStr) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let img): img.resizable().scaledToFill()
                            default: heroPlaceholder
                            }
                        }
                    } else {
                        heroPlaceholder
                    }
                }
                .frame(height: 220)
                .clipped()

                VStack(alignment: .leading, spacing: 20) {
                    // Animal name + status
                    HStack(alignment: .top) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(animal.map { "\($0.speciesEmoji) \($0.name)" } ?? "Animal #\(application.animalId)")
                                .font(.title2.bold())
                            if let breed = animal?.breed {
                                Text(breed)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                        Spacer()
                        StatusBadge(status: application.displayStatus, color: application.statusColor)
                    }

                    // Status message
                    statusMessageCard

                    Divider()

                    // Application details
                    SectionHeader(title: "Application Details")

                    infoRow(label: "Applied", value: application.applicationDate)
                    if let reviewed = application.reviewedDate {
                        infoRow(label: "Reviewed", value: reviewed)
                    }
                    if let reason = application.reason, !reason.isEmpty {
                        infoRow(label: "Reason", value: reason)
                    }
                    if let living = application.livingSituation, !living.isEmpty {
                        infoRow(label: "Living Situation", value: living)
                    }
                    if let work = application.workSchedule, !work.isEmpty {
                        infoRow(label: "Work Schedule", value: work)
                    }
                    if let yard = application.hasYard {
                        infoRow(label: "Has Yard", value: yard ? "Yes" : "No")
                    }
                    if let landlord = application.landlordApproval {
                        infoRow(label: "Landlord Approval", value: landlord ? "Yes" : "No")
                    }
                    if let pets = application.otherPetsDetails, !pets.isEmpty {
                        infoRow(label: "Other Pets", value: pets)
                    }
                }
                .padding()
            }
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var statusMessageCard: some View {
        HStack(spacing: 12) {
            Image(systemName: application.statusIcon)
                .font(.title2)
                .foregroundColor(application.statusColor)
            VStack(alignment: .leading, spacing: 2) {
                Text(application.displayStatus)
                    .font(.headline)
                    .foregroundColor(application.statusColor)
                Text(application.statusMessage)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
        }
        .padding(14)
        .background(application.statusColor.opacity(0.08))
        .cornerRadius(12)
    }

    private var heroPlaceholder: some View {
        Image(systemName: "pawprint.fill")
            .resizable().scaledToFit().padding(60)
            .foregroundColor(pawBrown.opacity(0.2))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(warmBg)
    }

    private func infoRow(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 2)
    }
}

struct SectionHeader: View {
    let title: String
    var body: some View {
        Text(title)
            .font(.headline)
            .padding(.top, 4)
    }
}
