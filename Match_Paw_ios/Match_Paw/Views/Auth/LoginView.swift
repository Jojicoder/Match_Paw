//
//  LoginView.swift
//  Match_Paw
//

import SwiftUI

struct LoginView: View {
    @EnvironmentObject var auth: AuthViewModel
    @State private var email = ""
    @State private var password = ""
    @State private var showSignUp = false

    private let pawBrown = Color(red: 0.42, green: 0.18, blue: 0.04)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 36) {
                    // Logo
                    VStack(spacing: 10) {
                        Image(systemName: "pawprint.fill")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 72, height: 72)
                            .foregroundColor(pawBrown)

                        Text("matchpaw")
                            .font(.system(size: 34, weight: .bold, design: .rounded))
                            .foregroundColor(pawBrown)

                        Text("Find your perfect companion")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.top, 60)

                    // Form
                    VStack(spacing: 14) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Email")
                                .font(.caption.weight(.semibold))
                                .foregroundColor(.secondary)
                            TextField("your@email.com", text: $email)
                                .textContentType(.emailAddress)
                                .keyboardType(.emailAddress)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                                .padding(14)
                                .background(Color(.systemGray6))
                                .cornerRadius(12)
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Password")
                                .font(.caption.weight(.semibold))
                                .foregroundColor(.secondary)
                            SecureField("Password", text: $password)
                                .textContentType(.password)
                                .padding(14)
                                .background(Color(.systemGray6))
                                .cornerRadius(12)
                        }

                        if let err = auth.errorMessage {
                            Text(err)
                                .font(.caption)
                                .foregroundColor(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        Button {
                            Task { await auth.login(email: email, password: password) }
                        } label: {
                            ZStack {
                                if auth.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Log In")
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
                    }
                    .padding(.horizontal, 24)

                    // Sign up
                    HStack(spacing: 4) {
                        Text("New here?")
                            .foregroundColor(.secondary)
                        Button("Create an account") {
                            auth.errorMessage = nil
                            showSignUp = true
                        }
                        .foregroundColor(pawBrown)
                        .fontWeight(.semibold)
                    }
                    .font(.subheadline)
                }
                .padding(.bottom, 40)
            }
            .navigationDestination(isPresented: $showSignUp) {
                SignUpView()
            }
        }
    }

    private var formIsValid: Bool {
        !email.isEmpty && !password.isEmpty
    }
}
