package eu.fbk.ict.ehealth.virtualcoach.messages.receive;

public class PersonalDataNotification {
  public String trec_user_id;
  public String operation;
  
  public String getTrec_user_id() {
    return trec_user_id;
  }
  public void setTrec_user_id(String trec_user_id) {
    this.trec_user_id = trec_user_id;
  }
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
}
