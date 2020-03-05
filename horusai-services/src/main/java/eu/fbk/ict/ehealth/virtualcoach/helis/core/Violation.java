package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;

public class Violation {
  
  private String violationId;
  private String user;
  private String rule;
  private String ruleId;
  private String timestamp;
  private String startTime;
  private String endTime;
  private String entityType;
  private String entity;
  private String timing;
  private String quantity;
  private String expectedQuantity;
  private ArrayList<String> meals;
  private ArrayList<String> goals;
  private ArrayList<String> goalsConstraints;
  private String priority;
  private String level;
  private String constraint;
  private String history;

  public Violation() {
    this.meals = new ArrayList<String>();
    this.goals = new ArrayList<String>();
    this.goalsConstraints = new ArrayList<String>();
  }

  public String getViolationId() {
    return violationId;
  }

  public void setViolationId(String violationId) {
    this.violationId = violationId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public String getTiming() {
    return timing;
  }

  public void setTiming(String timing) {
    this.timing = timing;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public String getQuantity() {
    return quantity;
  }

  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public String getExpectedQuantity() {
    return expectedQuantity;
  }

  public void setExpectedQuantity(String expectedQuantity) {
    this.expectedQuantity = expectedQuantity;
  }

  public ArrayList<String> getMeals() {
    return meals;
  }

  public void addMeal(String meal) {
    this.meals.add(meal);
  }
  
  public void addMeals(ArrayList<String> meals) {
    this.meals.addAll(meals);
  }
  
  public ArrayList<String> getGoals() {
    return goals;
  }
  
  public ArrayList<String> getGoalsConstraints() {
    return goalsConstraints;
  }

  public void addGoal(String goal, String constraint) {
    for(String s : this.goals) {
      if(s.compareTo(goal) == 0) {
        return;
      }
    }
    this.goals.add(goal);
    this.goalsConstraints.add(constraint);
  }
  
  public void addGoals(ArrayList<String> goals) {
    this.goals.addAll(goals);
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getConstraint() {
    return constraint;
  }

  public void setConstraint(String constraint) {
    this.constraint = constraint;
  }

  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }
}
