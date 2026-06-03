package com.matchpaw;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    public static final String BASE_URL =
            "https://matchpaw-api-gxd2eggnhdgefsej.eastus-01.azurewebsites.net";

    private static String token;
    private static int userId;
    private static String role;

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static void setToken(String t) { token = t; }
    public static String getToken() { return token; }
    public static void setSession(String t, int id, String userRole) {
        token = t;
        userId = id;
        role = userRole;
    }
    public static int getUserId() { return userId; }
    public static String getRole() { return role; }
    public static boolean isAdmin() { return role != null && role.equalsIgnoreCase("Admin"); }
    public static boolean isLoggedIn() { return token != null && !token.isEmpty(); }

    public static HttpResponse<String> get(String path) throws Exception {
        return http.send(builder(path).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post(String path, String json) throws Exception {
        return http.send(
                builder(path).POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String path, String json) throws Exception {
        return http.send(
                builder(path).PUT(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> patch(String path, String json) throws Exception {
        return http.send(
                builder(path).method("PATCH", HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> delete(String path) throws Exception {
        return http.send(builder(path).DELETE().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest.Builder builder(String path) {
        var b = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return b;
    }
}
