package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;

public class Profile {

  private String profileId;
  private long startDate;
  private ArrayList<String> forbiddenCategories;
  private ArrayList<String> allowedCategories;
  
  public Profile(String profileId) {
    this.profileId = profileId;
    this.startDate = System.currentTimeMillis();
  }

  public String getProfileId() {
    return this.profileId;
  }

  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }
  
  public long getStartDate() {
    return startDate;
  }

  public void setStartDate(long startDate) {
    this.startDate = startDate;
  }

  public ArrayList<String> getForbiddenCategories() {
    return this.forbiddenCategories;
  }

  public void setForbiddenCategories(ArrayList<String> forbiddenCategories) {
    this.forbiddenCategories = forbiddenCategories;
  }
  
  public void setAllowedCategories(ArrayList<String> allowedCategories) {
    this.allowedCategories = allowedCategories;
  }
  
  public ArrayList<String> getAllowedCategories() {
    return this.allowedCategories;
  }
  
  
  /** 
   * Retrieves from the knowledge repository the list of allowed and forbidden food categories (if any) 
   * associated with the current profile 
   **/
   public void populateProfileEntity() {
     
   }
}
