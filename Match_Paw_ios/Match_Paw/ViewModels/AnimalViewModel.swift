//
//  AnimalViewModel.swift
//  Match_Paw
//

import SwiftUI
import Combine

@MainActor
final class AnimalViewModel: ObservableObject {
    @Published var animals: [Animal] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    var visibleAnimals: [Animal] {
        animals
            .filter { $0.isVisibleToApplicants }
            .sorted { lhs, rhs in
                if statusRank(lhs) != statusRank(rhs) {
                    return statusRank(lhs) < statusRank(rhs)
                }
                return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
            }
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            animals = try await APIService.shared.fetchAnimals()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func filtered(query: String, species: String?) -> [Animal] {
        visibleAnimals.filter { a in
            let q = query.lowercased()
            let matchQuery = q.isEmpty
                || a.name.lowercased().contains(q)
                || (a.breed?.lowercased().contains(q) ?? false)
                || a.species.lowercased().contains(q)
            let matchSpecies = species == nil || a.species == species
            return matchQuery && matchSpecies
        }
    }

    func animal(id: Int) -> Animal? {
        animals.first { $0.id == id }
    }

    private func statusRank(_ animal: Animal) -> Int {
        switch animal.adoptionStatus {
        case "Available": return 0
        case "Pending": return 1
        case "Adopted": return 2
        default: return 3
        }
    }
}
