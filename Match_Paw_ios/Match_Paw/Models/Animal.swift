//
//  Animal.swift
//  Match_Paw
//

import Foundation

struct Animal: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let species: String
    let breed: String?
    let age: Int?
    let sex: String?
    let intakeDate: String?
    let adoptionStatus: String
    let healthStatus: String?
    let notes: String?
    let photoUrl: String?

    enum CodingKeys: String, CodingKey {
        case id = "animalId"
        case name, species, breed, age, sex, intakeDate
        case adoptionStatus, healthStatus, notes, photoUrl
    }

    var isAvailable: Bool { adoptionStatus == "Available" }
    var isVisibleToApplicants: Bool { adoptionStatus != "Unavailable" }

    var displayAge: String {
        guard let age else { return "Unknown age" }
        return age == 1 ? "1 year old" : "\(age) years old"
    }

    var speciesEmoji: String {
        switch species.lowercased() {
        case "dog": return "🐶"
        case "cat": return "🐱"
        case "rabbit": return "🐰"
        case "bird": return "🐦"
        default: return "🐾"
        }
    }
}
