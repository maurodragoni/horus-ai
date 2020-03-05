package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSDrinkData {

  private String coding_system;
  private String drink_id;
  private double quantity;
  private String unit;
  
  public CDSDrinkData () {}

  public String getCoding_system() {
    return coding_system;
  }

  public void setCoding_system(String coding_system) {
    this.coding_system = coding_system;
  }

  public String getDrink_id() {
    return drink_id;
  }

  public void setDrink_id(String drink_id) {
    this.drink_id = drink_id;
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
