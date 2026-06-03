package com.matchpaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CareLog {
    private int careLogId;
    private int animalId;
    private int userId;
    private String logDate;
    private String feedingNotes;
    private String cleaningNotes;
    private String behaviorNotes;

    public int getCareLogId() { return careLogId; }
    public int getAnimalId() { return animalId; }
    public int getUserId() { return userId; }
    public String getLogDate() { return logDate; }
    public String getFeedingNotes() { return feedingNotes; }
    public String getCleaningNotes() { return cleaningNotes; }
    public String getBehaviorNotes() { return behaviorNotes; }

    public void setCareLogId(int careLogId) { this.careLogId = careLogId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setLogDate(String logDate) { this.logDate = logDate; }
    public void setFeedingNotes(String feedingNotes) { this.feedingNotes = feedingNotes; }
    public void setCleaningNotes(String cleaningNotes) { this.cleaningNotes = cleaningNotes; }
    public void setBehaviorNotes(String behaviorNotes) { this.behaviorNotes = behaviorNotes; }
}
