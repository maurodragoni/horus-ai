package eu.fbk.ict.ehealth.virtualcoach.webresponse;

public class Message {
  
  public String messageType;
  public String message;
  
  public Message() {
    this.messageType = "text";
    this.message = new String("");
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
