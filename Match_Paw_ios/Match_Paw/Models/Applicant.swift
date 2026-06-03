//
//  Applicant.swift
//  Match_Paw
//

import Foundation

struct Applicant: Codable, Identifiable {
    let id: Int
    let fullName: String
    let email: String
    let phone: String?
    let address: String?
    let housingType: String?
    let hasPets: Bool?
    let hasChildren: Bool?
    let experienceWithPets: String?
    let preferredContactMethod: String?

    enum CodingKeys: String, CodingKey {
        case id = "applicantId"
        case fullName, email, phone, address, housingType
        case hasPets, hasChildren, experienceWithPets, preferredContactMethod
    }

    var initials: String {
        fullName.split(separator: " ")
            .prefix(2)
            .compactMap { $0.first }
            .map { String($0).uppercased() }
            .joined()
    }
}

struct CreateApplicantRequest: Codable {
    let fullName: String
    let email: String
    let password: String
    let phone: String
    let address: String
    let housingType: String
    let hasPets: Bool
    let hasChildren: Bool
    let experienceWithPets: String
    let preferredContactMethod: String
}

struct ApplicantLoginRequest: Codable {
    let email: String
    let password: String
}

struct ApplicantLoginResponse: Codable {
    let token: String
    let applicantId: Int
}

struct CreatedResponse: Codable {
    let applicantId: Int
}

struct CreatedApplicationResponse: Codable {
    let applicationId: Int
}
