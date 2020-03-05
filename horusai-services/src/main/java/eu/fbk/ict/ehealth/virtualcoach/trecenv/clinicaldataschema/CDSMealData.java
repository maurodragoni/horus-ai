package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

import java.util.ArrayList;

public class CDSMealData {

  private String meal_type;
  private ArrayList<CDSMealDataFoods> foods;
  
  public CDSMealData () {}

  public String getMeal_type() {
    return meal_type;
  }

  public void setMeal_type(String meal_type) {
    this.meal_type = meal_type;
  }

  public ArrayList<CDSMealDataFoods> getFoods() {
    return foods;
  }

  public void setFoods(ArrayList<CDSMealDataFoods> foods) {
    this.foods = foods;
  }
  
  
  
}
