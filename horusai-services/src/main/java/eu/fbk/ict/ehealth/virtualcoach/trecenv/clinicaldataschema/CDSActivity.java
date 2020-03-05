package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSActivity {

  private String _id;
  private String type;
  private String patient_id;
  private CDSMetadata metadata;
  private CDSActivityData data;
  
  public CDSActivity() {}

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

  public CDSActivityData getData() {
    return data;
  }

  public void setData(CDSActivityData data) {
    this.data = data;
  }
  
}
