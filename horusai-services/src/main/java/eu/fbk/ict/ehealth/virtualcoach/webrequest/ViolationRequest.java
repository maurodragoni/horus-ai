package eu.fbk.ict.ehealth.virtualcoach.webrequest;

public class ViolationRequest {

  private String mode;
  private String userId;
  private String timing;
  private String startDate;
  private String ruleId;
  
  public ViolationRequest() {}

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getTiming() {
    return timing;
  }

  public void setTiming(String timing) {
    this.timing = timing;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }
  
}
