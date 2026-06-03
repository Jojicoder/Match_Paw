//
//  SearchView.swift
//  Match_Paw
//

import SwiftUI

struct SearchView: View {
    @EnvironmentObject var animalVM: AnimalViewModel
    @State private var query = ""
    @State private var selectedSpecies: String? = nil

    private let speciesOptions = ["Dog", "Cat", "Rabbit", "Bird", "Other"]
    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var results: [Animal] {
        animalVM.filtered(query: query, species: selectedSpecies)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Search bar
                HStack {
                    Image(systemName: "magnifyingglass").foregroundColor(.secondary)
                    TextField("Name, breed, species...", text: $query)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                    if !query.isEmpty {
                        Button { query = "" } label: {
                            Image(systemName: "xmark.circle.fill").foregroundColor(.secondary)
                        }
                    }
                }
                .padding(12)
                .background(Color(.systemGray6))
                .cornerRadius(12)
                .padding()

                // Species filter
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 10) {
                        FilterChip(label: "All", isSelected: selectedSpecies == nil) {
                            selectedSpecies = nil
                        }
                        ForEach(speciesOptions, id: \.self) { s in
                            FilterChip(label: s, isSelected: selectedSpecies == s) {
                                selectedSpecies = selectedSpecies == s ? nil : s
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 8)

                Divider()

                // Results
                if animalVM.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if results.isEmpty {
                    ContentUnavailableView(
                        "No Results",
                        systemImage: "magnifyingglass",
                        description: Text(query.isEmpty ? "No animals found." : "Try a different search.")
                    )
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(results) { animal in
                        NavigationLink(value: animal) {
                            AnimalRowView(animal: animal)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Search")
            .navigationDestination(for: Animal.self) { animal in
                AnimalDetailView(animal: animal)
            }
        }
    }
}

// MARK: - Row used in Search list

struct AnimalRowView: View {
    let animal: Animal

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)
    private let warmBg = Color(red: 0.95, green: 0.91, blue: 0.85)

    var body: some View {
        HStack(spacing: 12) {
            Group {
                if let urlStr = animal.photoUrl, let url = URL(string: urlStr) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let img): img.resizable().scaledToFill()
                        default: pawPlaceholder
                        }
                    }
                } else {
                    pawPlaceholder
                }
            }
            .frame(width: 64, height: 64)
            .background(warmBg)
            .cornerRadius(12)
            .clipped()

            VStack(alignment: .leading, spacing: 4) {
                Text(animal.name).font(.headline)
                Text(animal.breed ?? animal.species)
                    .font(.subheadline).foregroundColor(.secondary)
                Text(animal.displayAge)
                    .font(.caption).foregroundColor(.secondary)
                if !animal.isAvailable {
                    Text(animal.adoptionStatus)
                        .font(.caption.weight(.semibold))
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            Text(animal.speciesEmoji).font(.title2)
        }
        .padding(.vertical, 4)
    }

    private var pawPlaceholder: some View {
        Image(systemName: "pawprint.fill")
            .resizable().scaledToFit().padding(14)
            .foregroundColor(pawBrown.opacity(0.35))
    }
}
