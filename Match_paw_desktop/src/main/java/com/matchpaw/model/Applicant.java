package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Applicant {
    private int applicantId;
    private String fullName;
    private String email;
    @JsonAlias("isActive")
    private boolean active;
    private String phone;
    private String address;
    private String housingType;
    private boolean hasPets;
    private boolean hasChildren;
    private String experienceWithPets;
    private String preferredContactMethod;

    public int getApplicantId() { return applicantId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getHousingType() { return housingType; }
    public boolean isHasPets() { return hasPets; }
    public boolean isHasChildren() { return hasChildren; }
    public String getExperienceWithPets() { return experienceWithPets; }
    public String getPreferredContactMethod() { return preferredContactMethod; }

    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setActive(boolean active) { this.active = active; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setHousingType(String housingType) { this.housingType = housingType; }
    public void setHasPets(boolean hasPets) { this.hasPets = hasPets; }
    public void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }
    public void setExperienceWithPets(String experienceWithPets) { this.experienceWithPets = experienceWithPets; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
}
