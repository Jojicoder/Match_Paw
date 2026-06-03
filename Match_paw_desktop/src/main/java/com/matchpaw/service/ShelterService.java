package com.matchpaw.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchpaw.ApiClient;
import com.matchpaw.model.AdoptionApplication;
import com.matchpaw.model.Applicant;
import com.matchpaw.model.CareLog;
import com.matchpaw.model.MedicalRecord;
import com.matchpaw.model.StaffUser;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public class ShelterService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<AdoptionApplication> getApplications() throws Exception {
        var resp = ApiClient.get("/api/adoptionapplications");
        requireSuccess(resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<AdoptionApplication>>() {});
    }

    public static List<Applicant> getApplicants() throws Exception {
        var resp = ApiClient.get("/api/applicants");
        requireSuccess(resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<Applicant>>() {});
    }

    public static List<MedicalRecord> getMedicalRecords() throws Exception {
        var resp = ApiClient.get("/api/medicalrecords");
        requireSuccess(resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<MedicalRecord>>() {});
    }

    public static List<CareLog> getCareLogs() throws Exception {
        var resp = ApiClient.get("/api/carelogs");
        requireSuccess(resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<CareLog>>() {});
    }

    public static List<StaffUser> getUsers() throws Exception {
        var resp = ApiClient.get("/api/users");
        requireSuccess(resp.statusCode());
        return mapper.readValue(resp.body(), new TypeReference<List<StaffUser>>() {});
    }

    public static StaffUser createUser(StaffUser user, String password) throws Exception {
        var request = staffUserRequest(user, password);
        var resp = ApiClient.post("/api/users", mapper.writeValueAsString(request));
        requireSuccess(resp.statusCode());
        if (resp.body() != null && !resp.body().isBlank()) {
            var node = mapper.readTree(resp.body());
            if (node.has("userId")) user.setUserId(node.get("userId").asInt());
        }
        return user;
    }

    public static StaffUser updateUser(StaffUser user, String password) throws Exception {
        var request = staffUserRequest(user, password == null || password.isBlank() ? null : password);
        var resp = ApiClient.put("/api/users/" + user.getUserId(), mapper.writeValueAsString(request));
        requireSuccess(resp.statusCode());
        return user;
    }

    public static void updateApplicationStatus(int applicationId, String status) throws Exception {
        var request = new LinkedHashMap<String, Object>();
        request.put("status", status);
        request.put("reviewedBy", ApiClient.getUserId() == 0 ? null : ApiClient.getUserId());
        request.put("reviewedDate", LocalDate.now().toString());
        var body = mapper.writeValueAsString(request);
        var resp = ApiClient.patch("/api/adoptionapplications/" + applicationId + "/status", body);
        requireSuccess(resp.statusCode());
    }

    public static void updateApplication(AdoptionApplication application) throws Exception {
        var resp = ApiClient.put(
                "/api/adoptionapplications/" + application.getApplicationId(),
                mapper.writeValueAsString(application));
        requireSuccess(resp.statusCode());
    }

    public static void createMedicalRecord(MedicalRecord record) throws Exception {
        var resp = ApiClient.post("/api/medicalrecords", mapper.writeValueAsString(record));
        requireSuccess(resp.statusCode());
    }

    public static void createCareLog(CareLog log) throws Exception {
        var resp = ApiClient.post("/api/carelogs", mapper.writeValueAsString(log));
        requireSuccess(resp.statusCode());
    }

    private static LinkedHashMap<String, Object> staffUserRequest(StaffUser user, String password) {
        var request = new LinkedHashMap<String, Object>();
        request.put("fullName", user.getFullName());
        request.put("email", user.getEmail());
        request.put("password", password);
        request.put("role", user.getRole());
        request.put("isActive", user.isActive());
        return request;
    }

    private static void requireSuccess(int statusCode) {
        if (statusCode < 200 || statusCode >= 300)
            throw new RuntimeException("Server error: " + statusCode);
    }
}
