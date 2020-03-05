package eu.fbk.ict.ehealth.virtualcoach.webrequest;

public class FoodDetailRequest {
  
  private String id;
  private double quantity;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }
}
