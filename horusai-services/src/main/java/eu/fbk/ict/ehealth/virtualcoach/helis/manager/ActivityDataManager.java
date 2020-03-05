package eu.fbk.ict.ehealth.virtualcoach.helis.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.User;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.UserActivity;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.reasoner.ReasonerManager;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.ActivityRequest;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.MealRequest;
import eu.fbk.ict.ehealth.virtualcoach.webresponse.UserActivityResponse;

public class ActivityDataManager implements DataManager {

  private static Logger logger;
  
  
  public ActivityDataManager() {
    logger = LoggerFactory.getLogger(ActivityDataManager.class);
  }
  
  
  public String manage(String jsonPars) {
    logger.info("Activity management request received.");
    
    /** Replaces specific request's fields with the generic Pair object */
    jsonPars = jsonPars.replaceAll("activityId", "k").replaceAll("duration", "v");
    Gson gson = new Gson();
    ActivityRequest req = gson.fromJson(jsonPars, ActivityRequest.class);
    
    logger.info("Activity management request parsed. Mode: " + req.getMode());
    return this.manage(req);
  }
  
  

  public String manage(ActivityRequest req) {
    //Gson res = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    Gson gson = new Gson();
    logger.info("Activity request: " + gson.toJson(req));
    
    if(req.getMode().compareTo("save") == 0) {
      ArrayList<UserActivityResponse> a = this.manageAndAdaptUserActivity(req, true);
      ArrayList<String> activityIds = new ArrayList<String>();
      
      for(UserActivityResponse uar : a) {
        activityIds.add(uar.getId());
      }
      
      ReasonerManager.threadPool.submit(new Runnable() {
        @Override
        public void run() {
          ReasonerManager.performLiveReasoning("ACTIVITY", activityIds, req.getUserId());
        }
      });
      
      return gson.toJson(a);
    } else if(req.getMode().compareTo("get") == 0) {
      ArrayList<UserActivityResponse> a = this.getPerformedActivities(req.getUserId(), 0);
      return gson.toJson(a);
    } else if(req.getMode().compareTo("check") == 0) {
      ArrayList<UserActivityResponse> a = this.manageAndAdaptUserActivity(req, false);
      return gson.toJson(a);
    }
    return null;
  }
  
  
  /**
   * Loops over all received activities and store them into the knowledge repository.
   * @param req
   * @return
   */
  private ArrayList<UserActivityResponse> manageAndAdaptUserActivity(ActivityRequest req, boolean storeFlag) {
    
    ArrayList<UserActivityResponse> userActivities = new ArrayList<UserActivityResponse>();
    User u = new User(req.getUserId());
    u.retrieve();
    
    ArrayList<Pair> activities = req.getUserActivity();
    for(Pair a: activities) {
      UserActivity ua = new UserActivity(u, a.getK());
      ua.setDuration(Long.valueOf(a.getV()));
      ua.setTimestamp(req.getTimestamp());
      ua.adaptActivityToUser();
      if(storeFlag) {
        ua.storeUserActivity();
      }
      UserActivityResponse uar = new UserActivityResponse();
      uar.setId(u.getUserId() + "-" + ua.getActivity().getActivityId() + "-" + ua.getTimestamp());
      uar.setLabels(ua.getActivity().getLabels());
      uar.setParentCategories(ua.getActivity().getParentCategories());
      uar.setBaseCalories(ua.getActivity().getBaseCalories());
      uar.setBaseMET(ua.getActivity().getBaseMET());
      uar.setActualCalories(ua.getActualCalories());
      uar.setActualMET(ua.getActualMET());
      uar.setTimestamp(ua.getTimestamp());
      uar.setDuration(ua.getDuration());
      userActivities.add(uar);
    }
    return userActivities;
  }
  
  
  
  
  /**
   * Extracts from the knowledge repository the list of activities performed by a specific user in a specific time span.
   * As default, the list of activities performed during the current day is returned.
   */
  private ArrayList<UserActivityResponse> getPerformedActivities(String userId, long ts) {
    
    ArrayList<UserActivityResponse> userActivities = new ArrayList<UserActivityResponse>();
    User u = new User(userId);
    u.retrieve();
    
    if(ts == 0) { 
      Calendar c = new GregorianCalendar();
      int cY = c.get(Calendar.YEAR);
      int cM = c.get(Calendar.MONTH);
      int cD = c.get(Calendar.DAY_OF_MONTH);
      c.set(cY, cM, cD, 0, 0, 0);
      ts = c.getTimeInMillis();
    }
    
    String query = VC.SPARQL_PREFIX +
                   "SELECT ?activity ?activityDuration ?met ?calories ?timestamp " + 
                   "WHERE { " + 
                   "?performedActivity " + VC.PREFIX + ":hasUserId " + VC.PREFIX + ":" + userId + " ; " +
                   VC.PREFIX + ":hasActivity ?activity ; " +
                   VC.PREFIX + ":activityDuration ?activityDuration ; " +
                   VC.PREFIX + ":hasMETCorrection ?met ; " +
                   VC.PREFIX + ":caloriesConsumed ?calories ; " +
                   VC.PREFIX + ":activityStartTimestamp ?timestamp . " +
                   "}";
        
    
    logger.info(query);
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value activity = bindingSet.getValue("activity");
          Value activityDuration = bindingSet.getValue("activityDuration");
          Value met = bindingSet.getValue("met");
          Value calories = bindingSet.getValue("calories");
          Value timestamp = bindingSet.getValue("timestamp");
          
          UserActivity ua = new UserActivity(u, activity.stringValue().substring(activity.stringValue().indexOf("#") + 1));
          ua.setDuration(Double.valueOf(activityDuration.stringValue()));
          ua.setTimestamp(Long.valueOf(timestamp.stringValue()));
          ua.setActualMET(Double.valueOf(met.stringValue()));
          ua.setActualCalories(Double.valueOf(calories.stringValue()));
          
          UserActivityResponse uar = new UserActivityResponse();
          uar.setId(u.getUserId() + "-" + ua.getActivity().getActivityId());
          uar.setLabels(ua.getActivity().getLabels());
          uar.setParentCategories(ua.getActivity().getParentCategories());
          uar.setBaseCalories(ua.getActivity().getBaseCalories());
          uar.setBaseMET(ua.getActivity().getBaseMET());
          uar.setActualCalories(ua.getActualCalories());
          uar.setActualMET(ua.getActualMET());
          uar.setTimestamp(ua.getTimestamp());
          uar.setDuration(ua.getDuration());
          userActivities.add(uar);
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of the activities for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    return userActivities;
  }
  
  
}
