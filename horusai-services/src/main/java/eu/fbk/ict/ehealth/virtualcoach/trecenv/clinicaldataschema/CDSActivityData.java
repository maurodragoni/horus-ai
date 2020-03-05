package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSActivityData {
  
  private String coding_system;
  private String activity_id;
  
  public CDSActivityData () {}

  public String getCoding_system() {
    return coding_system;
  }

  public void setCoding_system(String coding_system) {
    this.coding_system = coding_system;
  }

  public String getActivity_id() {
    return activity_id;
  }

  public void setActivity_id(String activity_id) {
    this.activity_id = activity_id;
  }
  
}
