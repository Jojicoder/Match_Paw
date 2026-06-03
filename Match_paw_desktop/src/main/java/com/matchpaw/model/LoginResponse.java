package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    private String token;
    private int userId;
    private String role;

    public String getToken() { return token; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }

    public void setToken(String token) { this.token = token; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setRole(String role) { this.role = role; }
}
