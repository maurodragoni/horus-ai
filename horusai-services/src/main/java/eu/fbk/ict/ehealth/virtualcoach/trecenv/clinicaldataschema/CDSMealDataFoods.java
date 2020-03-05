package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSMealDataFoods {

  private String coding_system;
  private String food_id;
  private double quantity;
  private String unit;
  
  public CDSMealDataFoods () {}

  public String getCoding_system() {
    return coding_system;
  }

  public void setCoding_system(String coding_system) {
    this.coding_system = coding_system;
  }

  public String getFood_id() {
    return food_id;
  }

  public void setFood_id(String food_id) {
    this.food_id = food_id;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
  
  
  
}
