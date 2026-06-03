//
//  APIService.swift
//  Match_Paw
//

import Foundation

enum APIError: LocalizedError {
    case invalidResponse
    case serverError(Int, String)
    case decodingFailed(String)

    var errorDescription: String? {
        switch self {
        case .invalidResponse: return "Invalid server response."
        case .serverError(let code, let body): return "Server error \(code): \(body)"
        case .decodingFailed(let msg): return "Could not read response: \(msg)"
        }
    }
}

final class APIService {
    static let shared = APIService()
    private init() {}

    private let base = URL(string: "https://matchpaw-api-gxd2eggnhdgefsej.eastus-01.azurewebsites.net/api")!
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    var authToken: String?

    // MARK: - Private helpers

    private func get<T: Decodable>(_ path: String) async throws -> T {
        var req = URLRequest(url: base.appendingPathComponent(path))
        if let token = authToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        let (data, response) = try await URLSession.shared.data(for: req)
        try validate(response, data)
        return try decode(data)
    }

    private func post<T: Decodable, B: Encodable>(_ path: String, body: B) async throws -> T {
        var req = URLRequest(url: base.appendingPathComponent(path))
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = authToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        req.httpBody = try encoder.encode(body)
        let (data, response) = try await URLSession.shared.data(for: req)
        try validate(response, data)
        return try decode(data)
    }

    private func validate(_ response: URLResponse, _ data: Data) throws {
        guard let http = response as? HTTPURLResponse else { throw APIError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw APIError.serverError(http.statusCode, body)
        }
    }

    private func decode<T: Decodable>(_ data: Data) throws -> T {
        do {
            return try decoder.decode(T.self, from: data)
        } catch {
            throw APIError.decodingFailed(error.localizedDescription)
        }
    }

    // MARK: - Auth

    func login(email: String, password: String) async throws -> ApplicantLoginResponse {
        try await post("applicants/login", body: ApplicantLoginRequest(email: email, password: password))
    }

    // MARK: - Animals

    func fetchAnimals() async throws -> [Animal] {
        try await get("animals")
    }

    // MARK: - Applicants

    func fetchApplicants() async throws -> [Applicant] {
        try await get("applicants")
    }

    func fetchApplicant(id: Int) async throws -> Applicant {
        try await get("applicants/\(id)")
    }

    func createApplicant(_ body: CreateApplicantRequest) async throws -> Int {
        let r: CreatedResponse = try await post("applicants", body: body)
        return r.applicantId
    }

    // MARK: - Adoption Applications

    func fetchApplications() async throws -> [AdoptionApplication] {
        try await get("adoptionapplications")
    }

    func createApplication(_ body: CreateApplicationRequest) async throws -> Int {
        let r: CreatedApplicationResponse = try await post("adoptionapplications", body: body)
        return r.applicationId
    }
}
