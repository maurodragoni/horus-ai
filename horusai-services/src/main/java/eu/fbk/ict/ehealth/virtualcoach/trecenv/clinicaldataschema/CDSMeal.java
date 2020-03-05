package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSMeal {
  
  private String _id;
  private String type;
  private String patient_id;
  private CDSMetadata metadata;
  private CDSMealData data;
  private String version;
  
  
  public CDSMeal() {}
  
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
  
  public CDSMealData getData() {
    return data;
  }
  
  public void setData(CDSMealData data) {
    this.data = data;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
  
  
  
}
