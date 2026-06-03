//
//  AnimalCardView.swift
//  Match_Paw
//

import SwiftUI

struct AnimalCardView: View {
    let animal: Animal

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)
    private let warmBg = Color(red: 0.95, green: 0.91, blue: 0.85)

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Photo
            Group {
                if let urlStr = animal.photoUrl, let url = URL(string: urlStr) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable().scaledToFill()
                        default:
                            placeholder
                        }
                    }
                } else {
                    placeholder
                }
            }
            .frame(height: 140)
            .clipped()
            .background(warmBg)

            // Info
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(animal.name)
                        .font(.headline)
                        .lineLimit(1)
                    Spacer()
                    Text(animal.speciesEmoji)
                }

                Text(animal.breed ?? animal.species)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)

                Text(animal.displayAge + (animal.sex.map { " · \($0)" } ?? ""))
                    .font(.caption2)
                    .foregroundColor(.secondary)

                if !animal.isAvailable {
                    Text(animal.adoptionStatus)
                        .font(.caption2.weight(.semibold))
                        .foregroundColor(.secondary)
                }
            }
            .padding(10)
        }
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(Color(.secondarySystemBackground))
                .shadow(color: .black.opacity(0.12), radius: 6, x: 0, y: 2)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private var placeholder: some View {
        Image(systemName: "pawprint.fill")
            .resizable().scaledToFit().padding(32)
            .foregroundColor(pawBrown.opacity(0.25))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(warmBg)
    }
}
