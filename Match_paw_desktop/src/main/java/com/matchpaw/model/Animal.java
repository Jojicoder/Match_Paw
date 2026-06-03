package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Animal {

    private int animalId;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String sex;
    private String intakeDate;
    private String adoptionStatus;
    private String healthStatus;
    private String notes;
    private String photoUrl;
    private String createdAt;

    public int getAnimalId() { return animalId; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public Integer getAge() { return age; }
    public String getSex() { return sex; }
    public String getIntakeDate() { return intakeDate; }
    public String getAdoptionStatus() { return adoptionStatus; }
    public String getHealthStatus() { return healthStatus; }
    public String getNotes() { return notes; }
    public String getPhotoUrl() { return photoUrl; }
    public String getCreatedAt() { return createdAt; }

    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public void setName(String name) { this.name = name; }
    public void setSpecies(String species) { this.species = species; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAge(Integer age) { this.age = age; }
    public void setSex(String sex) { this.sex = sex; }
    public void setIntakeDate(String intakeDate) { this.intakeDate = intakeDate; }
    public void setAdoptionStatus(String adoptionStatus) { this.adoptionStatus = adoptionStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name == null || name.isBlank() ? "Animal #" + animalId : name;
    }
}
