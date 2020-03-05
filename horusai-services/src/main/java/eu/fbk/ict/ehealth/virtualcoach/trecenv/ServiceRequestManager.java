package eu.fbk.ict.ehealth.virtualcoach.trecenv;

import eu.fbk.ict.ehealth.virtualcoach.HttpCURLClient;

import java.util.HashMap;


public class ServiceRequestManager {

  private ServiceRequestManager() {}
  
  /*
  {
  "JWT":"eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIxMFVDNGJ0MHRzMnAyMjZwNW40Z3ZteUZGMUxCYWtpZVM1Y01SQWNwM2NraXNNZFZoN2V6NHB2c2pCNlNZNG9NT2k5OE1aZTl0NmoyWkpDNXJwdTcxREszMHRBeGNETjh0djE1IiwiaWF0IjoxNTAzNjcyNTA1LCJleHAiOjE1MDQ3OTU3MDUsInN1YiI6IjZHVnIyR2VXWTl2bDg1WDFBMzB6NzNPd1YxQ3RyMjFkNkZva1VaZTgyNTFiSHJoVGs1IiwiaXNzIjoiQVBTUyIsImF1ZCI6IlRyZUMiLCJUcmVjVXNlclR5cGUiOiJ1c2VyIiwibG9hIjoxfQ.0qMOE39vfR_xr9gpgArZ1na6RWKqzPESCUrtigE30ObsyfIfJm5J5nDOiEX8EccTWrlt8SWjKXblwhFD2WCD4zHyy9ZQmu_VLAlLfa2MsIe_TNk6e3fG50hgZRUQN2PuPn8DiXNGJ2XvV0srriq4GOdl13SmCn61UGHc-7qoCJqupAmOt52ev4iX92UoE6W_8nexyvirQgWMzbN7bYcauNoDv2XC70gp2aBl3kOFQAXrN-6FSwS3YM0HGk6MOEX2TOTBar1qUKpFOchCzjUW-oThfK32zwIdqBXszH94xIOB9BOXQX-KaYQoh2EEH1uUv1wTv-bhf42VBBcRCBu9ha2UIgWSDJw47fjo5HL9am5hLGpxdUrh6DIfp8s-aEFzOPN_h_ht63cC_su0Rc1Jz490ZMQlZrB15ILMpNQffGf9XsDbGmuu3V0Fh8phe7TYbLchrZ28c_Y4drMSafjH3RR5YJ7Xr0L-k7iNEHo6KqcNV9navey7jR4ynyoI8cfBHxOP7n_eJtwk6FBrntgN7z1mbgGICc5P1jlup66I7yXD1YZWxT84gKPCJVeMRRBpd9xx1GcQC6ZvwGB5WN7FkkSfoXcraXhBuBsd-PWE2MmyTKrlHmfWzvfDmDaw4tFPhpxHhw6tEb4_kYCv4ZCbvDu0uEcuKEe3hmhkP5yywAc",
  "trecUserId":"6GVr2GeWY9vl85X1A30z73OwV1Ctr21d6FokUZe8251bHrhTk5"
  }
   */
  
  public void getToken(String jsonRequest) {
    
  }
  
  
  public String readActivies(String jsonRequest) throws Exception {
    HashMap<String, String> header = new HashMap<String, String>();
    header.put("accept", "application/json");
    header.put("content-type", "application/json");
    header.put("X-TokenTreC", "application/json");
    
    String url = "";
    String activities = HttpCURLClient.post(url, header, jsonRequest);
    return activities;
  }
  
  
  public String readMeals(String jsonRequest) throws Exception {
    HashMap<String, String> header = new HashMap<String, String>();
    header.put("accept", "application/json");
    header.put("content-type", "application/json");
    header.put("X-TokenTreC", "application/json");
    
    String url = "";
    String meals = HttpCURLClient.post(url, header, jsonRequest);
    return meals;
  }
  
}
