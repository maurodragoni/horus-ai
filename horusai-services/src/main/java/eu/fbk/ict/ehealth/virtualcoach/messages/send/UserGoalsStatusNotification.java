package eu.fbk.ict.ehealth.virtualcoach.messages.send;

import java.util.ArrayList;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;

public class UserGoalsStatusNotification {
  
  public String userId;
  public ArrayList<Pair> goalsStatus;

  public UserGoalsStatusNotification() {}

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public ArrayList<Pair> getGoalsStatus() {
    return goalsStatus;
  }

  public void setGoalsStatus(ArrayList<Pair> goalsStatus) {
    this.goalsStatus = goalsStatus;
  }
}
