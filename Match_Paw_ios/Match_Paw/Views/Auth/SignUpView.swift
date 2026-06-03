//
//  SignUpView.swift
//  Match_Paw
//

import SwiftUI

struct SignUpView: View {
    @EnvironmentObject var auth: AuthViewModel
    @Environment(\.dismiss) var dismiss

    @State private var fullName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                formCard("Account Info") {
                    inputField("Full Name", text: $fullName, contentType: .name)
                    inputField("Email", text: $email, contentType: .emailAddress, capitalization: .never)
                    secureField("Password", text: $password, contentType: .newPassword)
                    secureField("Confirm Password", text: $confirmPassword, contentType: .newPassword)
                }

                if let err = auth.errorMessage {
                    Text(err)
                        .font(.caption)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                }

                if !passwordsMatch && !confirmPassword.isEmpty {
                    Text("Passwords do not match.")
                        .font(.caption)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                }

                Button {
                    Task {
                        await auth.signUp(fullName: fullName, email: email, password: password)
                    }
                } label: {
                    ZStack {
                        if auth.isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Text("Create Account")
                                .font(.headline)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(16)
                    .background(formIsValid ? pawBrown : pawBrown.opacity(0.4))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .disabled(auth.isLoading || !formIsValid)
                .padding(.horizontal)
                .padding(.bottom, 40)
            }
            .padding(.top)
        }
        .navigationTitle("Create Account")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var passwordsMatch: Bool { password == confirmPassword }
    private var formIsValid: Bool {
        !fullName.trimmingCharacters(in: .whitespaces).isEmpty &&
        !email.trimmingCharacters(in: .whitespaces).isEmpty &&
        !password.isEmpty && passwordsMatch
    }

    @ViewBuilder
    private func formCard<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .padding(.horizontal)

            VStack(spacing: 12) {
                content()
            }
            .padding()
            .background(Color(.systemBackground))
            .cornerRadius(14)
            .shadow(color: .black.opacity(0.06), radius: 6, x: 0, y: 2)
            .padding(.horizontal)
        }
    }

    @ViewBuilder
    private func inputField(
        _ label: String,
        text: Binding<String>,
        contentType: UITextContentType,
        capitalization: TextInputAutocapitalization = .words,
        keyboard: UIKeyboardType = .default
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption.weight(.semibold))
                .foregroundColor(.secondary)
            TextField(label, text: text)
                .textContentType(contentType)
                .textInputAutocapitalization(capitalization)
                .keyboardType(keyboard)
                .autocorrectionDisabled(contentType == .emailAddress)
                .padding(10)
                .background(Color(.systemGray6))
                .cornerRadius(8)
        }
    }

    @ViewBuilder
    private func secureField(
        _ label: String,
        text: Binding<String>,
        contentType: UITextContentType
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption.weight(.semibold))
                .foregroundColor(.secondary)
            SecureField(label, text: text)
                .textContentType(contentType)
                .padding(10)
                .background(Color(.systemGray6))
                .cornerRadius(8)
        }
    }
}
