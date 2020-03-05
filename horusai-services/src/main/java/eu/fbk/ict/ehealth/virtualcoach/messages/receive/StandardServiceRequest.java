package eu.fbk.ict.ehealth.virtualcoach.messages.receive;

public class StandardServiceRequest {
  
  public String serviceRequestName;
  public Object serviceRequestData;
  
  public StandardServiceRequest() {}

  public String getServiceRequestName() {
    return serviceRequestName;
  }

  public void setServiceRequestName(String serviceRequestName) {
    this.serviceRequestName = serviceRequestName;
  }

  public Object getServiceRequestData() {
    return serviceRequestData;
  }

  public void setServiceRequestData(Object serviceRequestData) {
    this.serviceRequestData = serviceRequestData;
  }
}
