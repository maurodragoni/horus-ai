package eu.fbk.ict.ehealth.virtualcoach.helis.core;

public class ConsumedFood {
  
  //private String mealId;
  private long timestamp;
  private String foodId;
  private double foodQuantity;
  private String foodLabel;

  public ConsumedFood() {
  }

  /*
   * public String getMealId() { return mealId; }
   * 
   * public void setMealId(String mealId) { this.mealId = mealId; }
   */

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getFoodId() {
    return foodId;
  }

  public void setFoodId(String foodId) {
    this.foodId = foodId;
  }

  public String getFoodLabel() {
    return foodLabel;
  }

  public void setFoodLabel(String foodLabel) {
    this.foodLabel = foodLabel;
  }

  public double getFoodQuantity() {
    return foodQuantity;
  }

  public void setFoodQuantity(double foodQuantity) {
    this.foodQuantity = foodQuantity;
  }
}
