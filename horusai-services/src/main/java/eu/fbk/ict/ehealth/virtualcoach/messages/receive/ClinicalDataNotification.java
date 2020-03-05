package eu.fbk.ict.ehealth.virtualcoach.messages.receive;

public class ClinicalDataNotification {
  
  public String _id;
  public String event_type;
  public String document_type;
  public String patient_id;
  public String source;
  
  public String get_id() {
    return _id;
  }
  public void set_id(String _id) {
    this._id = _id;
  }
  public String getEvent_type() {
    return event_type;
  }
  public void setEvent_type(String event_type) {
    this.event_type = event_type;
  }
  public String getDocument_type() {
    return document_type;
  }
  public void setDocument_type(String document_type) {
    this.document_type = document_type;
  }
  public String getPatient_id() {
    return patient_id;
  }
  public void setPatient_id(String patient_id) {
    this.patient_id = patient_id;
  }
  public String getSource() {
    return source;
  }
  public void setSource(String source) {
    this.source = source;
  }
}
