package eu.fbk.ict.ehealth.virtualcoach.webresponse;

import java.util.ArrayList;
import java.util.HashMap;


public class UserActivityResponse {
  
  private String id;
  private HashMap<String, String> labels;
  private ArrayList<String> parentCategories;
  private ArrayList<String> activeCategory;
  private double baseCalories;
  private double baseMET;
  private double actualCalories;
  private double actualMET;
  private long timestamp;
  private double duration;
  
  public UserActivityResponse() {}

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public HashMap<String, String> getLabels() {
    return labels;
  }
  
  public void setLabels(HashMap<String, String> labels) {
    this.labels = labels;
  }
  
  public ArrayList<String> getParentCategories() {
    return parentCategories;
  }
  
  public void setParentCategories(ArrayList<String> parentCategories) {
    this.parentCategories = parentCategories;
  }
  
  public ArrayList<String> getActiveCategory() {
    return activeCategory;
  }
  
  public void setActiveCategory(ArrayList<String> activeCategory) {
    this.activeCategory = activeCategory;
  }
  
  public double getBaseCalories() {
    return baseCalories;
  }
  
  public void setBaseCalories(double baseCalories) {
    this.baseCalories = baseCalories;
  }
  
  public double getBaseMET() {
    return baseMET;
  }
  
  public void setBaseMET(double baseMET) {
    this.baseMET = baseMET;
  }
  
  public double getActualCalories() {
    return actualCalories;
  }
  
  public void setActualCalories(double actualCalories) {
    this.actualCalories = actualCalories;
  }
  
  public double getActualMET() {
    return actualMET;
  }
  
  public void setActualMET(double actualMET) {
    this.actualMET = actualMET;
  }
  
  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public double getDuration() {
    return duration;
  }
  
  public void setDuration(double duration) {
    this.duration = duration;
  }
}
