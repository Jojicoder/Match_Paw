//
//  AdoptionApplication.swift
//  Match_Paw
//

import Foundation
import SwiftUI

struct AdoptionApplication: Codable, Identifiable {
    let id: Int
    let animalId: Int
    let applicantId: Int

    enum CodingKeys: String, CodingKey {
        case id = "applicationId"
        case animalId, applicantId, applicationDate, status, reason
        case livingSituation, workSchedule, hasYard, landlordApproval
        case otherPetsDetails, reviewedBy, reviewedDate
    }
    let applicationDate: String
    let status: String
    let reason: String?
    let livingSituation: String?
    let workSchedule: String?
    let hasYard: Bool?
    let landlordApproval: Bool?
    let otherPetsDetails: String?
    let reviewedBy: Int?
    let reviewedDate: String?

    var displayStatus: String {
        status == "UnderReview" ? "Under Review" : status
    }

    var statusColor: Color {
        switch status {
        case "Approved": return .green
        case "Rejected": return .red
        case "UnderReview": return .orange
        default: return .blue
        }
    }

    var statusIcon: String {
        switch status {
        case "Approved": return "checkmark.circle.fill"
        case "Rejected": return "xmark.circle.fill"
        case "UnderReview": return "clock.fill"
        default: return "hourglass"
        }
    }

    var statusMessage: String {
        switch status {
        case "Approved": return "Congratulations! Your application has been approved."
        case "Rejected": return "Unfortunately, your application was not approved this time."
        case "UnderReview": return "Your application is currently being reviewed by our team."
        default: return "Your application has been submitted and is awaiting review."
        }
    }
}

struct CreateApplicationRequest: Codable {
    let animalId: Int
    let applicantId: Int
    let applicationDate: String
    let status: String
    let reason: String
    let livingSituation: String
    let workSchedule: String
    let hasYard: Bool
    let landlordApproval: Bool
    let otherPetsDetails: String
    let reviewedBy: Int?
    let reviewedDate: String?
}
