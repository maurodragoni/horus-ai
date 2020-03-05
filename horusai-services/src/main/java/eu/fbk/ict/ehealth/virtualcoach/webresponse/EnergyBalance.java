package eu.fbk.ict.ehealth.virtualcoach.webresponse;

public class EnergyBalance {

  private double caloriesGoal;
  private double caloriesIntake;
  private double caloriesBurned;
  private double caloriesADS;
  private double remaining;
  
  public double getCaloriesGoal() {
    return caloriesGoal;
  }
  public void setCaloriesGoal(double caloriesGoal) {
    this.caloriesGoal = caloriesGoal;
  }
  public double getCaloriesIntake() {
    return caloriesIntake;
  }
  public void setCaloriesIntake(double caloriesIntake) {
    this.caloriesIntake = caloriesIntake;
  }
  public double getCaloriesBurned() {
    return caloriesBurned;
  }
  public void setCaloriesBurned(double caloriesBurned) {
    this.caloriesBurned = caloriesBurned;
  }
  public double getCaloriesADS() {
    return caloriesADS;
  }
  public void setCaloriesADS(double caloriesADS) {
    this.caloriesADS = caloriesADS;
  }
  public double getRemaining() {
    return remaining;
  }
  public void setRemaining(double remaining) {
    this.remaining = remaining;
  }
  
}
