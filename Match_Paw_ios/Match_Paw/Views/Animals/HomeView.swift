//
//  HomeView.swift
//  Match_Paw
//

import SwiftUI

struct HomeView: View {
    @EnvironmentObject var animalVM: AnimalViewModel
    @State private var selectedSpecies: String? = nil

    private let speciesOptions = ["Dog", "Cat", "Rabbit", "Bird", "Other"]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Species filter chips
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

                    if animalVM.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, minHeight: 200)
                    } else if let err = animalVM.errorMessage {
                        ContentUnavailableView(
                            "Couldn't load animals",
                            systemImage: "exclamationmark.triangle",
                            description: Text(err)
                        )
                        .padding(.top, 40)
                    } else {
                        let list = animalVM.filtered(query: "", species: selectedSpecies)
                        if list.isEmpty {
                            ContentUnavailableView(
                                "No animals found",
                                systemImage: "pawprint",
                                description: Text("Check back soon!")
                            )
                            .padding(.top, 40)
                        } else {
                            LazyVGrid(
                                columns: [GridItem(.flexible()), GridItem(.flexible())],
                                spacing: 16
                            ) {
                                ForEach(list) { animal in
                                    NavigationLink(value: animal) {
                                        AnimalCardView(animal: animal)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal)
                        }
                    }
                }
                .padding(.vertical)
            }
            .navigationTitle("Animals")
            .navigationDestination(for: Animal.self) { animal in
                AnimalDetailView(animal: animal)
            }
            .refreshable { await animalVM.load() }
        }
    }
}

// MARK: - FilterChip (shared with SearchView)

struct FilterChip: View {
    let label: String
    let isSelected: Bool
    let action: () -> Void

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.subheadline.weight(.medium))
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? pawBrown : Color(.systemGray5))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}
