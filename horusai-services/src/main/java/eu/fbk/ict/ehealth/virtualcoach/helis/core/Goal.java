package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;

public class Goal {

  private String userId;
  private String goalId;
  private String status;
  private ArrayList<String> profiles;
  private long timestamp;

  public Goal() {
    this.profiles = new ArrayList<String>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGoalId() {
    return goalId;
  }

  public void setGoalId(String goalId) {
    this.goalId = goalId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
  
  public void addProfile(String profile) {
    this.profiles.add(profile);
  }
  
  public void setProfiles(ArrayList<String> profiles) {
    this.profiles = profiles;
  }

  public ArrayList<String> getProfiles() {
    return this.profiles;
  }
}
