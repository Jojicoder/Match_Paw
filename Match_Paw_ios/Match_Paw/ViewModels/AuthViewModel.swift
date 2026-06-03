//
//  AuthViewModel.swift
//  Match_Paw
//

import SwiftUI
import Combine

@MainActor
final class AuthViewModel: ObservableObject {
    @Published var currentApplicant: Applicant?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let applicantKey = "matchpaw_applicant"
    private let tokenKey = "matchpaw_token"

    init() { restore() }

    var isLoggedIn: Bool { currentApplicant != nil }

    // MARK: - Auth

    func login(email: String, password: String) async {
        guard !email.trimmingCharacters(in: .whitespaces).isEmpty, !password.isEmpty else {
            errorMessage = "Please enter your email and password."
            return
        }
        isLoading = true
        errorMessage = nil
        do {
            let response = try await APIService.shared.login(email: email, password: password)
            APIService.shared.authToken = response.token
            UserDefaults.standard.set(response.token, forKey: tokenKey)

            let applicant = try await APIService.shared.fetchApplicant(id: response.applicantId)
            persist(applicant)
            currentApplicant = applicant
        } catch {
            errorMessage = error.localizedDescription
            APIService.shared.authToken = nil
            UserDefaults.standard.removeObject(forKey: tokenKey)
        }
        isLoading = false
    }

    func signUp(fullName: String, email: String, password: String) async {
        guard !fullName.trimmingCharacters(in: .whitespaces).isEmpty,
              !email.trimmingCharacters(in: .whitespaces).isEmpty,
              !password.isEmpty else {
            errorMessage = "All fields are required."
            return
        }
        isLoading = true
        errorMessage = nil
        do {
            let body = CreateApplicantRequest(
                fullName: fullName, email: email, password: password,
                phone: "", address: "", housingType: "Other",
                hasPets: false, hasChildren: false,
                experienceWithPets: "", preferredContactMethod: "Email"
            )
            _ = try await APIService.shared.createApplicant(body)
            await login(email: email, password: password)
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }

    func logout() {
        UserDefaults.standard.removeObject(forKey: applicantKey)
        UserDefaults.standard.removeObject(forKey: tokenKey)
        APIService.shared.authToken = nil
        currentApplicant = nil
    }

    // MARK: - Private

    private func persist(_ applicant: Applicant) {
        if let data = try? JSONEncoder().encode(applicant) {
            UserDefaults.standard.set(data, forKey: applicantKey)
        }
    }

    private func restore() {
        if let token = UserDefaults.standard.string(forKey: tokenKey) {
            APIService.shared.authToken = token
        }
        if let data = UserDefaults.standard.data(forKey: applicantKey),
           let applicant = try? JSONDecoder().decode(Applicant.self, from: data) {
            currentApplicant = applicant
        }
    }
}
