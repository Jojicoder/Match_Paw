package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalRecord {
    private int medicalRecordId;
    private int animalId;
    private String recordDate;
    private String treatmentType;
    private String description;
    private String veterinarianName;
    private String nextAppointment;

    public int getMedicalRecordId() { return medicalRecordId; }
    public int getAnimalId() { return animalId; }
    public String getRecordDate() { return recordDate; }
    public String getTreatmentType() { return treatmentType; }
    public String getDescription() { return description; }
    public String getVeterinarianName() { return veterinarianName; }
    public String getNextAppointment() { return nextAppointment; }

    public void setMedicalRecordId(int medicalRecordId) { this.medicalRecordId = medicalRecordId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }
    public void setDescription(String description) { this.description = description; }
    public void setVeterinarianName(String veterinarianName) { this.veterinarianName = veterinarianName; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }
}
