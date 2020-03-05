package eu.fbk.ict.ehealth.virtualcoach.webrequest;

import java.util.ArrayList;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.Meal;


public class MealRequest {
  private String mode;
  private String userId;
  private String timestamp;
  private ArrayList<Meal> meals;

  public MealRequest() {
  }

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

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
  
  public ArrayList<Meal> getMeals() {
    return meals;
  }

  public void setMeals(ArrayList<Meal> meals) {
    this.meals = meals;
  }
}
