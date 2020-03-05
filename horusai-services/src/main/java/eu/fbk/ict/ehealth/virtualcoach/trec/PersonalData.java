package eu.fbk.ict.ehealth.virtualcoach.trec;

import eu.fbk.trec.exceptions.TrecException;
import eu.fbk.trec.personal_data.model.ExtendedUserData;
import retrofit2.Response;

import java.io.IOException;

import static eu.fbk.ict.ehealth.virtualcoach.trec.Clients.getPersonalDataClient;


/**
 * @author Alessandro Valentini
 * Date 1/17/18.
 */
public class PersonalData {

  private PersonalData() {}
  
  /**
   * Retrieve UserData for a given patient handling exception and errors
   *
   * @param patientId patient's TrecUserId
   * @return
   */
  public static ExtendedUserData getUserData(String patientId){
    try {
      final Response<ExtendedUserData> response = getPersonalDataClient().selectUserData(SessionManager.getTokenTrec().getJWT(), patientId).execute();
      if(response.isSuccessful()){
        return response.body();
      } else {
        throw TrecException.serviceUnavailable(null, "Got Http code "+ response.code() +" retrieving UserData from personalData");
      }
    } catch (IOException e) {
      throw TrecException.serviceUnavailable(e, "IOException retrieving UserData from personalData");
    }
  }

}
