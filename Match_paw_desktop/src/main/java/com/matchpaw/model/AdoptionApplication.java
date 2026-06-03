package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdoptionApplication {
    private int applicationId;
    private int animalId;
    private int applicantId;
    private String applicationDate;
    private String status;
    private String reason;
    private String livingSituation;
    private String workSchedule;
    private boolean hasYard;
    private boolean landlordApproval;
    private String otherPetsDetails;
    private Integer reviewedBy;
    private String reviewedDate;

    public int getApplicationId() { return applicationId; }
    public int getAnimalId() { return animalId; }
    public int getApplicantId() { return applicantId; }
    public String getApplicationDate() { return applicationDate; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getLivingSituation() { return livingSituation; }
    public String getWorkSchedule() { return workSchedule; }
    public boolean isHasYard() { return hasYard; }
    public boolean isLandlordApproval() { return landlordApproval; }
    public String getOtherPetsDetails() { return otherPetsDetails; }
    public Integer getReviewedBy() { return reviewedBy; }
    public String getReviewedDate() { return reviewedDate; }

    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setLivingSituation(String livingSituation) { this.livingSituation = livingSituation; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
    public void setHasYard(boolean hasYard) { this.hasYard = hasYard; }
    public void setLandlordApproval(boolean landlordApproval) { this.landlordApproval = landlordApproval; }
    public void setOtherPetsDetails(String otherPetsDetails) { this.otherPetsDetails = otherPetsDetails; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedDate(String reviewedDate) { this.reviewedDate = reviewedDate; }
}
