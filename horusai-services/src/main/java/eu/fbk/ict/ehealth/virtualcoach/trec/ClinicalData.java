package eu.fbk.ict.ehealth.virtualcoach.trec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.fbk.trec.clinical_data.model.Search;
import eu.fbk.trec.exceptions.TrecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

import static eu.fbk.ict.ehealth.virtualcoach.trec.Clients.clinicalDataClient;

/**
 * @author Alessandro Valentini
 * Date 1/16/18.
 */
public class ClinicalData {
  
  private static final Logger logger = LoggerFactory.getLogger(ClinicalData.class);

  private ClinicalData() {}  
  
  /**
   * Retrieve the most recent Weight for the specified patient
   * @param patientId patient's TrecUserId
   * @return
   */
  public static double getWeight(String patientId) {
    String entity = "observation/weight/1";

    try {
      final JsonNode jsonNode = mostRecentValue(entity, patientId);
      return jsonNode.asDouble();
    } catch (NullPointerException e) {
      return 80.0;
    }
    
  }

  /**
   * Retrieve the most recent Height for the specified patient
   * @param patientId patient's TrecUserId
   * @return
   */
  public static int getHeight(String patientId) {
    String entity = "observation/height/1";

    try {
      final JsonNode jsonNode = mostRecentValue(entity, patientId);
      return jsonNode.asInt();
    } catch (NullPointerException e) {
      return 180;
    }
  }
  
  /**
   * Retrieve the data contained in the specified meal
   * @param patientId patient's TrecUserId
   * @param eventId event's id of the data to retrieve
   * @return
   */
  public static String getMeal(String patientId, String eventId) {
    String entity = "observation/meal/1";

    try {
      //final JsonNode jsonNode = mostRecentObject(entity, patientId, eventId);
      //logger.info("JsonNode Content: " + jsonNode.toString());
      //return jsonNode.toString();
      final String jsonNode = mostRecentObject(entity, patientId, eventId);
      return jsonNode;
    } catch (NullPointerException e) {
      return "";
    }
  }
  
  /**
   * Retrieve the data contained in the specified activity
   * @param patientId patient's TrecUserId
   * @param eventId event's id of the data to retrieve
   * @return
   */
  public static String getActivity(String patientId, String eventId) {
    String entity = "observation/activity/1";

    try {
      //final JsonNode jsonNode = mostRecentObject(entity, patientId, eventId);
      //logger.info("JsonNode Content: " + jsonNode.toString());
      //return jsonNode.toString();
      final String jsonNode = mostRecentObject(entity, patientId, eventId);
      return jsonNode;
    } catch (NullPointerException e) {
      return "";
    }
  }
  
  /**
   * Retrieve the data contained in the specified meal
   * @param patientId patient's TrecUserId
   * @param eventId event's id of the data to retrieve
   * @return
   */
  public static String getDrink(String patientId, String eventId) {
    String entity = "observation/drink/1";

    try {
      //final JsonNode jsonNode = mostRecentObject(entity, patientId, eventId);
      //logger.info("JsonNode Content: " + jsonNode.toString());
      //return jsonNode.toString();
      final String jsonNode = mostRecentObject(entity, patientId, eventId);
      return jsonNode;
    } catch (NullPointerException e) {
      return "";
    }
  }

  /**
   * Execute a search request to retrieve the most value of the specified entity
   * @param entity ClinicalData required type
   * @param patientId patient's TrecUserId
   * @return JsonNode containing the required value
   */
  private static JsonNode mostRecentValue(String entity, String patientId) {
    LinkedHashMap<String, Integer> sort = new LinkedHashMap<String, Integer>();
    sort.put("metadata.end_date", -1);

    String field = "data.value";

    List<String> fields = new ArrayList<>();
    fields.add(field);

    final ObjectNode emptyQuery = new ObjectMapper().createObjectNode();

    final Call<ArrayNode> search = clinicalDataClient().search(
      SessionManager.getTokenTrec().getJWT(), entity, Search.create(patientId, emptyQuery, fields, sort, 1, 0)
    );

    try {
      final Response<ArrayNode> response = search.execute();
      if(response.isSuccessful()){
        final ArrayNode body = response.body();
        logger.info("Body: " + body.get(0).get("data").get("value"));
        return body.get(0).get("data").get("value");   
      } else {
        throw TrecException.serviceUnavailable(null, "Got Http code "+ response.code() +" retrieving " + entity + " from clinicalData");
      }
    } catch (IOException e) {
      throw TrecException.serviceUnavailable(e, "IOException retrieving " + entity + " from clinicalData");
    }
  }
  
  /**
   * Execute a select request to retrieve the most recent object of the specified event
   * @param entity ClinicalData required type
   * @param patientId patient's TrecUserId
   * @param eventId data's id
   * @return JsonNode containing the required value
   */
  //private static JsonNode mostRecentObject(String entity, String patientId, String eventId) {
  private static String mostRecentObject(String entity, String patientId, String eventId) {
    LinkedHashMap<String, Integer> sort = new LinkedHashMap<String, Integer>();
    sort.put("metadata.end_date", -1);

    String field = "data.value";

    List<String> fields = new ArrayList<>();
    fields.add(field);

    //final ObjectNode emptyQuery = new ObjectMapper().createObjectNode();
   
    final Call<ObjectNode> select = clinicalDataClient().select(SessionManager.getTokenTrec().getJWT(), entity, eventId, false);

    try {
      final Response<ObjectNode> response = select.execute();
      if(response.isSuccessful()){
        final ObjectNode body = response.body();
        //logger.info("Body:");
        //logger.info(body.toString());
        //return body.get(0).get("data");
        return body.toString();
      } else {
        throw TrecException.serviceUnavailable(null, "Got Http code "+ response.code() +" retrieving " + entity + " from clinicalData");
      }
    } catch (IOException e) {
      throw TrecException.serviceUnavailable(e, "IOException retrieving " + entity + " from clinicalData");
    }
  }
}
