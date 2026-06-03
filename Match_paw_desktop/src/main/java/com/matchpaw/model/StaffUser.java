package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffUser {
    private int userId;
    private String fullName;
    private String email;
    private String role;
    @JsonAlias("isActive")
    private boolean active;
    private String createdAt;

    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
