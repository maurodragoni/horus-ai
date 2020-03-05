package eu.fbk.ict.ehealth.virtualcoach.messages.send;

import java.util.ArrayList;


public class UserProfilesStatusNotification {
  
  public String userId;
  public ArrayList<ProfileStatus> profilesStatus;

  public UserProfilesStatusNotification() {
    this.profilesStatus = new ArrayList<ProfileStatus>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public ArrayList<ProfileStatus> getProfilesStatus() {
    return profilesStatus;
  }

  public void addProfileStatus(ProfileStatus p) {
    this.profilesStatus.add(p);
  }
  
  
  public class ProfileStatus {
    
    private String rule;
    private String category;
    private String operator;
    private String monitoredValue;
    private String timing;
    private String occurrences;
    
    public ProfileStatus() {}
      
    public String getRule() {
      return rule;
    }
    
    public void setRule(String rule) {
      this.rule = rule;
    }
    
    public String getCategory() {
      return category;
    }
    
    public void setCategory(String category) {
      this.category = category;
    }
    
    public String getOperator() {
      return operator;
    }
    
    public void setOperator(String operator) {
      this.operator = operator;
    }
    
    public String getMonitoredValue() {
      return monitoredValue;
    }
    
    public void setMonitoredValue(String monitoredValue) {
      this.monitoredValue = monitoredValue;
    }
    
    public String getTiming() {
      return timing;
    }
    
    public void setTiming(String timing) {
      this.timing = timing;
    }
    
    public String getOccurrences() {
      return occurrences;
    }
    
    public void setOccurrences(String occurrences) {
      this.occurrences = occurrences;
    }
  }
}
