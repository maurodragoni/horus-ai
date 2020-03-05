package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import eu.fbk.ict.ehealth.virtualcoach.VC;

public class UserActivity {
  
  private static Logger logger = LoggerFactory.getLogger(Activity.class);
  private User user;
  private Activity activity;
  private double actualCalories;
  private double actualMET;
  private double duration;
  private long timestamp;
  private int numberOfAggregatedActivities;

  
  public UserActivity(User u, String activityId) {
    this.user = u;
    this.activity = new Activity(activityId);
    this.activity.retrieve();
    this.setActualCalories(0.0);
    this.setActualMET(0.0);
    this.setDuration(0.0);
    this.numberOfAggregatedActivities = 0;    
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Activity getActivity() {
    return activity;
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  public double getActualCalories() {
    return actualCalories;
  }

  public void setActualCalories(double actualCalories) {
    this.actualCalories = actualCalories;
  }

  public double getActualMET() {
    return actualMET;
  }

  public void setActualMET(double actualMET) {
    this.actualMET = actualMET;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  
  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getJSONDataObject() {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    String jsonObjectString;
    jsonObjectString = gson.toJson(this);
    return jsonObjectString;
  }

  
  
  
  
  public boolean storeUserActivity() {
    boolean storeFlag = false;
    try {
      //String ts = String.valueOf(System.currentTimeMillis());
      //this.setTimestamp(Long.valueOf(ts));
      String user = VC.PREFIX + ":" + this.user.getUserId();
      String context = "GRAPH <" + user + "-PERFORMED-ACTIVITIES>";
      String performedActivityId = user + "-" + this.activity.getActivityId() + "-" + this.getTimestamp();
  
      String query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ " + context + " " + "{ " +
                     performedActivityId + " rdf:type " + VC.PREFIX + ":PerformedActivity ; " +
                     VC.PREFIX + ":hasActivity " + VC.PREFIX + ":" + this.activity.getActivityId() + " ; " +
                     VC.PREFIX + ":activityDuration " + this.getDuration() + " ; " +
                     VC.PREFIX + ":hasMETCorrection " + this.getActualMET() + " ; " +
                     VC.PREFIX + ":caloriesConsumed " + this.getActualCalories() + " ; " +
                     VC.PREFIX + ":activityStartTimestamp " + this.getTimestamp() + " ; " +
                     VC.PREFIX + ":hasUserId " + user + " . " +
                     "}}";
  
      logger.info(query);
      RepositoryConnection conn = null;
      try {
        conn = VC.r.getConnection();
        conn.begin();
        UpdateUtil.executeUpdate(conn, query);
        conn.commit();
        conn.close();
      } catch (Exception e) {
        logger.info("Error in saving user activity. {} {}", e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
      }
      
    } catch (Exception e) {
      return storeFlag;
    }
    storeFlag = true;
    return storeFlag;
  }

  
  
  public void aggregateActivityEntity(UserActivity ae) {
    this.duration += ae.getDuration();
    this.actualCalories += ae.getActualCalories();
    this.actualMET = (this.actualMET * this.numberOfAggregatedActivities) + ae.getActualMET();
    this.numberOfAggregatedActivities++;
    this.actualMET /= this.numberOfAggregatedActivities;
  }

  
  public void adaptActivityToUser() {
    double aC = this.activity.getBaseCalories() * duration * this.user.getWeight();
    double aM = this.activity.getBaseMET() * this.user.getMetCorrection();
    DecimalFormat formatter = new DecimalFormat("#0.00");
    this.setDuration(duration);
    this.setActualCalories(Double.valueOf(formatter.format(aC)));
    this.setActualMET(Double.valueOf(formatter.format(aM)));
  }
}
