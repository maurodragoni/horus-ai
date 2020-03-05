package eu.fbk.ict.ehealth.virtualcoach.webrequest;

import java.util.ArrayList;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;

public class ActivityRequest {
  
  private String mode;
  private String userId;
  private long timestamp;
  private ArrayList<Pair> activities;

  public ActivityRequest() {}

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public ArrayList<Pair> getUserActivity() {
    return activities;
  }

  public void setUserActivity(ArrayList<Pair> activities) {
    this.activities = activities;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
