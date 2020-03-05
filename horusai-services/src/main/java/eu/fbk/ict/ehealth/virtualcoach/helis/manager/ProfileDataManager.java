package eu.fbk.ict.ehealth.virtualcoach.helis.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.spi.DirStateFactory.Result;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.fbk.ict.ehealth.virtualcoach.GsonFactory;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Profile;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.User;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.UserGoalsStatusNotification;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.UserProfilesStatusNotification;
import eu.fbk.ict.ehealth.virtualcoach.queue.Publisher;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.UserProfileRequest;
import eu.fbk.ict.ehealth.virtualcoach.webresponse.Message;

public class ProfileDataManager implements DataManager {

  private static Logger logger;
  
  public ProfileDataManager() {
    logger = LoggerFactory.getLogger(ProfileDataManager.class);
  }
  
  
  public String manage(String jsonPars) {
    logger.info("User management request received.");
    Gson gson = new Gson();
    UserProfileRequest req = gson.fromJson(jsonPars, UserProfileRequest.class);
    logger.info("User management request parsed. Mode: " + req.getMode());
    return this.manage(req);
  }
  
  
  public String manage(UserProfileRequest req) {
    logger.info("User management request received.");
    Gson gsonFiltered = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    Gson gson = new Gson();
    logger.info("User management request parsed. Mode: " + req.getMode());
    
    /* Checks if the user exists */
    if (req.getMode().compareTo("check") == 0) {
      boolean userExist = this.checkUserExist(req);
      Message m = new Message();
      m.setMessage(String.valueOf(userExist));
      return gson.toJson(m);
      
    /* Creates a new user */
    } else if (req.getMode().compareTo("new") == 0) {
      //boolean createFlag = this.createUserProfile(req);
      User u = new User(req.getUserId());
      u.populate(req);
      boolean createFlag = u.store();
      if(createFlag) {
        User res = this.get(req);
        return gsonFiltered.toJson(res);
      } else {
        Message m = new Message();
        m.setMessage(String.valueOf(createFlag));
        return gson.toJson(m);
      }
      
    /* Updates an existing user */
    } else if (req.getMode().compareTo("update") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      u.populate(req);
      boolean updateFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Adds a new ruleset/goal to an existing user */
    } else if (req.getMode().compareTo("addgoal") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      ArrayList<Profile> profiles = req.getProfiles();
      for(Profile p : profiles) {
        u.addRuleSetGoal(p.getProfileId()); 
      }
      boolean updateFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Adds all ruleset/goal defined within the current instance to an existing user */
    } else if (req.getMode().compareTo("addallgoals") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      boolean updateFlag = u.addAllRuleSetGoals();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Removes a new ruleset/goal to an existing user */
    } else if (req.getMode().compareTo("removegoal") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      ArrayList<Profile> profiles = req.getProfiles();
      for(Profile p : profiles) {
        u.removeRuleSetGoal(p.getProfileId()); 
      }
      boolean updateFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Removes all ruleset/goal defined within the current instance to an existing user  */
    } else if (req.getMode().compareTo("removeallgoals") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      boolean updateFlag = u.removeAllRuleSetGoals();
      boolean storeFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag) + " - " + String.valueOf(storeFlag));
      return gsonFiltered.toJson(m);
    
    /* Removes all ruleset/goal defined within the current instance to an existing user  */
    } else if (req.getMode().compareTo("proposegoals") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      //boolean updateFlag = u.removeAllRuleSetGoals(); 
      //Message m = new Message();
      //m.setMessage(String.valueOf(updateFlag));

      /*
      ArrayList<Pair> goalsStatus = new ArrayList<Pair>();
      Pair p = new Pair("KTH-GOAL-D-101", "95.6");
      goalsStatus.add(p);
      p = new Pair("KTH-GOAL-D-124", "91.6");
      goalsStatus.add(p);
      p = new Pair("KTH-GOAL-D-130", "84.2");
      goalsStatus.add(p);
      p = new Pair("KTH-GOAL-D-190", "71.9");
      //return gsonFiltered.toJson(goalsStatus);
      */
      UserGoalsStatusNotification ugsn = u.checkUserGoalStatus();
      Publisher.sendGoal(gson.toJson(ugsn));
      //return gsonFiltered.toJson(gson.toJson(ugsn));
      return gson.toJson(ugsn);
      
    /* Add a property within the profile of an existing user */
    } else if (req.getMode().compareTo("addproperty") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      u.addProperty(req.getProperties()); 
      boolean updateFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Remove a property from the profile of an existing user */
    } else if (req.getMode().compareTo("removeproperty") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      u.removeProperty(req.getProperties());  
      boolean updateFlag = u.store();
      Message m = new Message();
      m.setMessage(String.valueOf(updateFlag));
      return gsonFiltered.toJson(m);
      
    /* Return a specific property from the profiles of an existing user */
    } else if (req.getMode().compareTo("getproperty") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      ArrayList<Pair> reqP = req.getProperties();
      String p = u.getProperty(reqP.get(0).getK());  
      Message m = new Message();
      m.setMessage(gsonFiltered.toJson(p));
      return gsonFiltered.toJson(m);
      
    /* Return the whole set of properties from the profiles of an existing user */
    } else if (req.getMode().compareTo("getallproperties") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      ArrayList<Pair> p = u.getProperties();  
      Message m = new Message();
      m.setMessage(gsonFiltered.toJson(p));
      return gsonFiltered.toJson(m);      
      
    /* Gets forbidden categories for the specified profile */
    } else if (req.getMode().compareTo("getforbiddencategories") == 0) {
      CategoryResponse cr = this.getForbiddenCategories(req.getUserId());
      return gsonFiltered.toJson(cr);
      
    /* Gets the situation of a User with respect to all rules contained in the associated profile */
    } else if (req.getMode().compareTo("getprofilestatus") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      UserProfilesStatusNotification upsn = null;
      if(VC.config.getVirtualCoachRepositoryId().compareTo("inmp") == 0) {
        upsn = u.checkUserProfilesStatusParametric();
      } else {
        upsn = u.checkUserProfilesStatus();
      }
      logger.info(gson.toJson(upsn));
      Publisher.sendUserData(gson.toJson(upsn));
      //return gsonFiltered.toJson(gson.toJson(ugsn));
      return gson.toJson(upsn);
    
    /* Gets goal status information */
    } else if (req.getMode().compareTo("getgoalstatus") == 0) {
      logger.info(req.getUserId());
      logger.info(req.getProfiles().get(0).getProfileId());
      
      User u = new User(req.getUserId());
      u.retrieve();
      UserGoalsStatusNotification ugsn = u.checkUserGoalStatus();
      logger.info(gson.toJson(ugsn));
      //Publisher.sendUserData(gson.toJson(ugsn));
      Publisher.sendGoal(gson.toJson(ugsn));
      //return gsonFiltered.toJson(gson.toJson(ugsn));
      return gson.toJson(ugsn);
      
    /* Gets user information */
    }else if (req.getMode().compareTo("getenergyreport") == 0) {
      User u = new User(req.getUserId());
      u.retrieve();
      u.computeEnergyReport(10000000000L);
      
      List<String> fieldExclusions = Arrays.asList("logger", "username", "gender", "height", "weight", "age", "yearsAge", 
                                                   "dci", "metCorrection", "profiles", "properties");

      gson = GsonFactory.build(fieldExclusions, null);
      Publisher.sendUserData(gson.toJson(u));
      return gson.toJson(u);    
      
    /* Gets user information */
    } else if (req.getMode().compareTo("get") == 0) {
      User res = this.get(req);
      return gsonFiltered.toJson(res);    
    }
    return null;
  }
  
  
  
  /**
   * Checks if the received userId is already present in the repository or not.
   * @param req
   * @return
   */
  private boolean checkUserExist(UserProfileRequest req) {
    logger.info("Check user existence.");
    String userId = req.getUserId();
    boolean userExists = true;
    try (RepositoryConnection conn = VC.r.getConnection()) {
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?userId WHERE " + 
          "{ " + VC.PREFIX + ":" + userId + " " + VC.PREFIX + ":hasUserId ?userId .}";

      userExists = false;
      System.out.println(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          userExists = true;
          break;
        }
        result.close();
      }
      conn.close();
    }
    return userExists;
  }

   
  
  /** Extracts the user profile from the knowledge repository */
  private User get(UserProfileRequest req) {
    User u = new User(req.getUserId());
    u.retrieve();
    return u;
  }
  
  
  
  private CategoryResponse getForbiddenCategories(String userId) {

    ArrayList<String> forbiddenCategories = new ArrayList<String>();
    String userProfile = new String("");

    CategoryResponse cr = new CategoryResponse();
    if (userId == null)
      return cr;

    logger.info("Retrieve forbidden categories for user {}.", userId);
    try (RepositoryConnection conn = VC.r.getConnection()) {
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?profile ?category WHERE { " +
          VC.PREFIX + ":" + userId + " " + VC.PREFIX + ":belongsProfile ?profile . " + 
          "?profile " + VC.PREFIX + ":hasForbiddenCategory ?category .}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value profile = bindingSet.getValue("profile");
          Value category = bindingSet.getValue("category");

          if (profile != null && userProfile.compareTo("") == 0) {
            userProfile = profile.stringValue().substring(profile.stringValue().indexOf("#") + 1);
          }

          forbiddenCategories.add(category.stringValue().substring(category.stringValue().indexOf('#') + 1));
        }
        result.close();
      }
      conn.close();
    }

    cr.setProfileId(userProfile);
    cr.setForbiddenCategories(forbiddenCategories);
    return cr;
  }
  
  
 
  private class CategoryResponse {
    private String profileId;
    private ArrayList<String> forbiddenCategories;

    public CategoryResponse() {
    }

    @SuppressWarnings("unused")
    public String getProfileId() {
      return profileId;
    }

    public void setProfileId(String profileId) {
      this.profileId = profileId;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getForbiddenCategories() {
      return forbiddenCategories;
    }

    public void setForbiddenCategories(ArrayList<String> forbiddenCategories) {
      this.forbiddenCategories = forbiddenCategories;
    }
  }
  
}
