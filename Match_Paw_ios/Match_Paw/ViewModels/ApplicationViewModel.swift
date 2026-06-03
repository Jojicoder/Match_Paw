//
//  ApplicationViewModel.swift
//  Match_Paw
//

import SwiftUI
import Combine

@MainActor
final class ApplicationViewModel: ObservableObject {
    @Published var applications: [AdoptionApplication] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var submitSuccess = false

    func load(applicantId: Int) async {
        isLoading = true
        errorMessage = nil
        do {
            let all = try await APIService.shared.fetchApplications()
            applications = all.filter { $0.applicantId == applicantId }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func submit(
        animalId: Int, applicantId: Int,
        reason: String, livingSituation: String, workSchedule: String,
        hasYard: Bool, landlordApproval: Bool, otherPets: String
    ) async {
        isLoading = true
        errorMessage = nil
        submitSuccess = false
        let today = String(ISO8601DateFormatter().string(from: Date()).prefix(10))
        let body = CreateApplicationRequest(
            animalId: animalId, applicantId: applicantId,
            applicationDate: today, status: "Pending",
            reason: reason, livingSituation: livingSituation,
            workSchedule: workSchedule, hasYard: hasYard,
            landlordApproval: landlordApproval, otherPetsDetails: otherPets,
            reviewedBy: nil, reviewedDate: nil
        )
        do {
            _ = try await APIService.shared.createApplication(body)
            await load(applicantId: applicantId)
            submitSuccess = true
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func reset() {
        submitSuccess = false
        errorMessage = nil
    }
}
