package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSDrink {
  
  private String _id;
  private String type;
  private String patient_id;
  private CDSMetadata metadata;
  private CDSDrinkData data;
  
  
  public CDSDrink() {}
  
  public String get_id() {
    return _id;
  }
  
  public void set_id(String _id) {
    this._id = _id;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getPatient_id() {
    return patient_id;
  }
  
  public void setPatient_id(String patient_id) {
    this.patient_id = patient_id;
  }
  
  public CDSMetadata getMetadata() {
    return metadata;
  }
  
  public void setMetadata(CDSMetadata metadata) {
    this.metadata = metadata;
  }
  
  public CDSDrinkData getData() {
    return data;
  }
  
  public void setData(CDSDrinkData data) {
    this.data = data;
  }
  
  
  
}
