package eu.fbk.ict.ehealth.virtualcoach.messages.send;

import java.util.ArrayList;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.Goal;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Violation;

public class ReasonerThreadResult {
  
  public String userId;
  public String type;
  public ArrayList<Violation> violations;
  public ArrayList<Goal> goals;

  public ReasonerThreadResult() {}

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ArrayList<Violation> getViolations() {
    return violations;
  }

  public void setViolations(ArrayList<Violation> violations) {
    this.violations = violations;
  }

  public ArrayList<Goal> getGoals() {
    return goals;
  }

  public void setGoals(ArrayList<Goal> goals) {
    this.goals = goals;
  }
}
