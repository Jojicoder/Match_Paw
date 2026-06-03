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
        GeometryReader { proxy in
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
                .frame(width: proxy.size.width, height: 140)
                .clipped()
                .background(warmBg)

                // Info
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(animal.name)
                            .font(.headline)
                            .lineLimit(1)
                            .layoutPriority(1)
                        Spacer(minLength: 0)
                        Text(animal.speciesEmoji)
                            .fixedSize()
                    }
                    .frame(maxWidth: .infinity)

                    Text(animal.breed ?? animal.species)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Text(animal.displayAge + (animal.sex.map { " · \($0)" } ?? ""))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    if !animal.isAvailable {
                        Text(animal.adoptionStatus)
                            .font(.caption2.weight(.semibold))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(10)
                .frame(width: proxy.size.width, alignment: .leading)
            }
            .frame(
                width: proxy.size.width,
                height: proxy.size.height,
                alignment: .topLeading
            )
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: .black.opacity(0.12), radius: 6, x: 0, y: 2)
        }
    }

    private var placeholder: some View {
        Image(systemName: "pawprint.fill")
            .resizable().scaledToFit().padding(32)
            .foregroundColor(pawBrown.opacity(0.25))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(warmBg)
    }
}
