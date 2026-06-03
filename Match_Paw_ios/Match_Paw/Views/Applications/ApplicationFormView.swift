//
//  ApplicationFormView.swift
//  Match_Paw
//

import SwiftUI

struct ApplicationFormView: View {
    let animal: Animal
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appVM: ApplicationViewModel
    @EnvironmentObject var auth: AuthViewModel

    @State private var reason = ""
    @State private var livingSituation = ""
    @State private var workSchedule = ""
    @State private var hasYard = false
    @State private var landlordApproval = false
    @State private var otherPets = ""

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Animal summary
                    HStack(spacing: 14) {
                        Text(animal.speciesEmoji)
                            .font(.largeTitle)
                            .frame(width: 52, height: 52)
                            .background(pawBrown.opacity(0.1))
                            .cornerRadius(12)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(animal.name).font(.headline)
                            Text(animal.breed ?? animal.species)
                                .font(.subheadline).foregroundColor(.secondary)
                        }
                        Spacer()
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(14)
                    .padding(.horizontal)

                    formSection("Why do you want to adopt \(animal.name)?") {
                        editor(text: $reason, placeholder: "Tell us about why this animal is a great match for you...")
                    }

                    formSection("Living Situation") {
                        editor(text: $livingSituation, placeholder: "Describe your home environment...")
                    }

                    formSection("Work Schedule") {
                        TextField("e.g. Work from home, 9–5 office...", text: $workSchedule)
                            .padding(12)
                            .background(Color(.systemGray6))
                            .cornerRadius(10)
                    }

                    formSection("Additional Details") {
                        Toggle("Do you have a yard?", isOn: $hasYard).tint(pawBrown)
                        Divider()
                        Toggle("Landlord approval obtained?", isOn: $landlordApproval).tint(pawBrown)
                        Divider()
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Other pets at home")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            TextField("Describe any current pets...", text: $otherPets)
                                .padding(12)
                                .background(Color(.systemGray6))
                                .cornerRadius(10)
                        }
                    }

                    if let err = appVM.errorMessage {
                        Text(err)
                            .font(.caption)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal)
                    }

                    Button {
                        guard let applicant = auth.currentApplicant else { return }
                        Task {
                            await appVM.submit(
                                animalId: animal.id,
                                applicantId: applicant.id,
                                reason: reason,
                                livingSituation: livingSituation,
                                workSchedule: workSchedule,
                                hasYard: hasYard,
                                landlordApproval: landlordApproval,
                                otherPets: otherPets
                            )
                        }
                    } label: {
                        ZStack {
                            if appVM.isLoading {
                                ProgressView().tint(.white)
                            } else {
                                Text("Submit Application").font(.headline)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(16)
                        .background(reason.isEmpty ? pawBrown.opacity(0.4) : pawBrown)
                        .foregroundColor(.white)
                        .cornerRadius(14)
                    }
                    .disabled(appVM.isLoading || reason.isEmpty)
                    .padding(.horizontal)
                    .padding(.bottom, 40)
                }
                .padding(.top)
            }
            .navigationTitle("Adoption Application")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .onAppear { appVM.reset() }
            .onChange(of: appVM.submitSuccess) { _, success in
                if success { dismiss() }
            }
        }
    }

    @ViewBuilder
    private func formSection<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(.headline).padding(.horizontal)
            VStack(spacing: 10) { content() }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(14)
                .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
                .padding(.horizontal)
        }
    }

    private func editor(text: Binding<String>, placeholder: String) -> some View {
        ZStack(alignment: .topLeading) {
            if text.wrappedValue.isEmpty {
                Text(placeholder)
                    .foregroundColor(.secondary)
                    .padding(12)
            }
            TextEditor(text: text)
                .frame(minHeight: 90)
                .padding(8)
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .opacity(text.wrappedValue.isEmpty ? 0.9 : 1)
        }
    }
}
