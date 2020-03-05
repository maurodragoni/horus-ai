package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.UserGoalsStatusNotification;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.UserProfilesStatusNotification;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.UserProfilesStatusNotification.ProfileStatus;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.UserProfileRequest;
import eu.fbk.ict.ehealth.virtualcoach.webresponse.EnergyBalance;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

public class User {

  private static Logger logger = LoggerFactory.getLogger(User.class);
  private String userId;
  private String username;
  private String gender;
  private int height;
  private int weight;
  private long age;
  private int yearsAge;
  private double dci;
  private double metCorrection;
  private ArrayList<Profile> profiles;
  private ArrayList<Pair> properties;

  private EnergyBalance energyBalance;

  public User(String userId) {
    this.setUserId(userId);
    this.setUsername(userId);
    this.setGender("M");
    this.setHeight(178);
    this.setHeight(70);
    this.setAge(347963400000L);
    this.setDci(0.0);
    this.setMetCorrection(0.0);
    this.profiles = new ArrayList<Profile>();
    this.properties = new ArrayList<Pair>();
  }

  public String getUserId() {
    return this.userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getGender() {
    return this.gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public int getHeight() {
    return this.height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWeight() {
    return this.weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public long getAge() {
    return this.age;
  }

  public void setAge(long age) {
    this.age = age;
  }

  public int getYearsAge() {
    return yearsAge;
  }

  public void setYearsAge(int yearsAge) {
    this.yearsAge = yearsAge;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public double getDci() {
    return this.dci;
  }

  public void setDci(double dci) {
    this.dci = dci;
  }

  public double getMetCorrection() {
    return metCorrection;
  }

  public void setMetCorrection(double metCorrection) {
    this.metCorrection = metCorrection;
  }

  public EnergyBalance getEnergyBalance() {
    return energyBalance;
  }

  public void setEnergyBalance(EnergyBalance energyBalance) {
    this.energyBalance = energyBalance;
  }

  public ArrayList<Profile> getProfiles() {
    return this.profiles;
  }

  public void setProfiles(ArrayList<Profile> profiles) {
    this.profiles = profiles;
  }

  public void addProfile(Profile p) {
    this.profiles.add(p);
  }

  public ArrayList<Pair> getProperties() {
    return this.properties;
  }

  public void setProperties(ArrayList<Pair> properties) {
    this.properties = properties;
  }

  /**
   * Stores the current entity in the knowledge store
   */
  public boolean store() {
    boolean storeFlag = false;
    logger.info("Update user profile: " + this.getUserId() + " - " + this.getUsername() + ".");
    String userContext = "<vc:CXT-USERS>";

    RepositoryConnection conn = null;
    String query = null;

    /* Remove all information of the current User before saving the new ones. */
    try {
      conn = VC.r.getConnection();
      
      query = VC.SPARQL_PREFIX + "WITH <" + VC.PREFIX + ":CXT-USERS> DELETE { ?s ?p ?v } WHERE { ?u vc:belongsProfile ?s . ?s ?p ?v . " +
      // "FILTER (?s = " + VC.PREFIX + ":" + this.getUserId() + " && ?p = " +
      // VC.PREFIX + ":belongsProfile)}";
         "FILTER (?u = " + VC.PREFIX + ":" + this.getUserId() + ")}";
      logger.info(query);
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      
      query = VC.SPARQL_PREFIX + "WITH <" + VC.PREFIX + ":CXT-USERS> DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . " +
      // "FILTER (?s = " + VC.PREFIX + ":" + this.getUserId() + " && ?p = " +
      // VC.PREFIX + ":belongsProfile)}";
         "FILTER (?s = " + VC.PREFIX + ":" + this.getUserId() + ")}";
      logger.info(query);
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the data deletion for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    
    
    /* Remove all properties of the current User before saving the new ones. */
    try {
      conn = VC.r.getConnection();
      query = VC.SPARQL_PREFIX + "WITH <" + VC.PREFIX + ":CXT-USERS> " + "DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . "
          + "?s " + VC.PREFIX + ":hasUser ?u . " + "?s rdf:type " + VC.PREFIX + ":UserProperty . " + "FILTER (?u = "
          + VC.PREFIX + ":" + this.getUserId() + ")}";
      logger.info(query);
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the properties data deletion for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    
    
    /* Saves the new User profile information. */
    try {
      conn = VC.r.getConnection();
      query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ GRAPH " + userContext + " " + "{ " + VC.PREFIX + ":"
          + this.getUserId() + " " + VC.PREFIX + ":hasUserId \"" + this.getUserId() + "\" ; " + " " + VC.PREFIX
          + ":hasUsername \"" + this.getUsername() + "\" ; " + " " + VC.PREFIX + ":hasGender \"" + this.getGender()
          + "\" ; " + " " + VC.PREFIX + ":hasHeight " + this.getHeight() + " ; " + " " + VC.PREFIX + ":hasWeight "
          + this.getWeight() + " ; " + " " + VC.PREFIX + ":hasAge " + this.getAge() + " ; " + " rdf:type " + VC.PREFIX
          + ":User . " + "}}";
      logger.info(query);
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the data insertion for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    /* Saves the goals and/or profiles associated with the current User. */
    try {
      conn = VC.r.getConnection();
      String userId = VC.PREFIX + ":" + this.getUserId();
      for (Profile p : this.getProfiles()) {
        String profileId = p.getProfileId();
        String userProfile = userId + "-" + profileId + " ";
        // query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ GRAPH " + userContext
        // + " " + "{ " + VC.PREFIX + ":"
        // + this.getUserId() + " " + VC.PREFIX + ":belongsProfile " + VC.PREFIX
        // + ":" + profileId + "." + "}}";
        query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ GRAPH " + userContext + " " + "{ " + userProfile + "rdf:type "
            + VC.PREFIX + ":AssignedProfile . " + userProfile + VC.PREFIX + ":hasProfile " + VC.PREFIX + ":" + profileId
            + " . " + userProfile + VC.PREFIX + ":hasStartTimestamp " + p.getStartDate() + " . " + userId
            + " " + VC.PREFIX + ":belongsProfile " + userProfile + ". " + "}}";
        logger.info(query);
        conn.begin();
        UpdateUtil.executeUpdate(conn, query);
        conn.commit();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the profile data insertion for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    /* Saves the properties associated with the current User. */
    try {
      conn = VC.r.getConnection();
      for (Pair p : this.getProperties()) {
        String propertyName = p.getK();
        String propertyValue = p.getV();
        query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ GRAPH " + userContext + " " + "{ " + VC.PREFIX + ":"
            + this.getUserId() + " " + VC.PREFIX + ":hasProperty " + VC.PREFIX + ":" + this.getUserId() + "-"
            + propertyName + " . " + VC.PREFIX + ":" + this.getUserId() + "-" + propertyName + " rdf:type " + VC.PREFIX
            + ":UserProperty . " + VC.PREFIX + ":" + this.getUserId() + "-" + propertyName + " vc:hasUser " + VC.PREFIX
            + ":" + this.getUserId() + " . " + VC.PREFIX + ":" + this.getUserId() + "-" + propertyName
            + " vc:hasName \"" + propertyName + "\" . " + VC.PREFIX + ":" + this.getUserId() + "-" + propertyName
            + " vc:hasValue \"" + propertyValue + "\" . " + "}}";
        logger.info(query);
        conn.begin();
        UpdateUtil.executeUpdate(conn, query);
        conn.commit();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the properties data insertion for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    storeFlag = true;
    logger.info("User updated.");
    return storeFlag;
  }

  /**
   * Imports user data from the knowledge repository
   */
  public void retrieve() {

    RepositoryConnection conn = null;

    try {

      conn = VC.r.getConnection();

      /* Retrieves user data and associated profiles. */
      String queryString = VC.SPARQL_PREFIX + "SELECT ?userId ?username ?gender ?height ?weight ?age ?profileName ?profileTS "
          + "WHERE " + "{ " + "?user rdf:type vc:User ; " + " vc:hasUserId \"" + this.userId + "\" ; "
          + " vc:hasUsername ?username ; " + " vc:hasGender ?gender ; " + " vc:hasHeight ?height ; "
          + " vc:hasWeight ?weight ; " + " vc:hasAge ?age . " 
          + " OPTIONAL { ?user vc:belongsProfile ?assignedProfile . "
          + " ?assignedProfile vc:hasProfile ?profileName; vc:hasStartTimestamp ?profileTS . } "
          + "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      boolean saveUserData = true;
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          if (saveUserData == true) {
            Value userName = bindingSet.getValue("username");
            Value gender = bindingSet.getValue("gender");
            Value height = bindingSet.getValue("height");
            Value weight = bindingSet.getValue("weight");
            Value age = bindingSet.getValue("age");
            this.setUsername(userName.stringValue());
            this.setGender(gender.stringValue());
            this.setHeight(Integer.parseInt(height.stringValue()));
            this.setWeight(Integer.parseInt(weight.stringValue()));
            this.setAge(Long.parseLong(age.stringValue()));
            saveUserData = false;
          }

          Value profileName = bindingSet.getValue("profileName");
          if (profileName != null) {
            Value profileTs = bindingSet.getValue("profileTS");
            Profile p = new Profile(profileName.stringValue().substring(profileName.stringValue().indexOf("#") + 1));
            p.setStartDate(Long.parseLong(profileTs.stringValue()));
            p.populateProfileEntity();
            this.addProfile(p);
          }
        }
        result.close();
      }

      /* Retrieves user properties. */
      queryString = VC.SPARQL_PREFIX + "SELECT ?propertyName ?propertyValue " + "WHERE " + "{ "
          + "?p rdf:type vc:UserProperty ; " + " " + VC.PREFIX + ":hasUser " + VC.PREFIX + ":" + this.userId + " ; "
          + " " + VC.PREFIX + ":hasName ?propertyName ; " + " " + VC.PREFIX + ":hasValue ?propertyValue . " + "}";

      logger.info(queryString);
      tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      ArrayList<Pair> properties = new ArrayList<Pair>();
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          Value propertyName = bindingSet.getValue("propertyName");
          Value propertyValue = bindingSet.getValue("propertyValue");

          Pair p = new Pair(propertyName.stringValue(), propertyValue.stringValue());
          properties.add(p);
        }
        result.close();
      }
      this.addProperty(properties);

      conn.close();
    } catch (Exception e) {
      logger.info("Error in retrieving user. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    try {
      this.computeDCI();
    } catch (Exception e) {
      logger.info("Daily calories intakes cannot be computed.");
    }
  }

  /**
   * Populates a UserEntity object from a UserRequest
   */
  public void populate(UserProfileRequest req) {
    this.setUserId(req.getUserId());
    if (StringUtils.isNotEmpty(req.getUsername()))
      this.setUsername(req.getUsername());
    if (StringUtils.isNotEmpty(req.getGender()))
      this.setGender(req.getGender());
    if (req.getHeight() > 0)
      this.setHeight(req.getHeight());
    if (req.getWeight() > 0)
      this.setWeight(req.getWeight());
    if (req.getAge() > 0)
      this.setAge(req.getAge());
    /*
    if (req.getProfiles() != null)
      this.setProfiles(req.getProfiles());
    */
  }

  
  /**
   * Computes the daily calorie intake by using the Harris-Benedict metabolic
   * equation
   **/
  public void computeDCI() {

    if (this.weight == 0)
      this.weight = 78;
    if (this.height == 0)
      this.height = 178;

    Calendar c = new GregorianCalendar();
    long now = c.getTimeInMillis();
    double days = ((double) now - this.getAge()) / (1000 * 60 * 60 * 24);
    double leaps = days / (365.0 * 4.0);
    days += leaps;
    double years = days / 365.0;
    this.setYearsAge((int) Math.floor(years));
    logger.info("{} {} {} {}", days, leaps, years, this.getYearsAge());

    if (years < 2.0 || years > 110.0)
      years = 35.0;

    if (this.getGender().charAt(0) == 'M' || this.getGender().compareTo("1") == 0) {
      this.setDci(66.4730 + (5.0033 * this.getHeight()) + (13.7516 * this.getWeight()) - (6.7550 * years));
    } else if (this.getGender().charAt(0) == 'F' || this.getGender().compareTo("0") == 0) {
      this.setDci(655.0955 + (1.8496 * this.getHeight()) + (9.5634 * this.getWeight()) - (4.6756 * years));
    }

    this.setMetCorrection(3.5 / (this.getDci() / 1440.0 / 5.0 / this.getWeight() * 1000));
  }

  
  
  /** Computes the energy report for a specific day **/
  public void computeEnergyReport(long timestamp) {
    this.energyBalance = new EnergyBalance();
    this.energyBalance.setCaloriesGoal(1800.0);
    this.energyBalance.setCaloriesADS(0.0);

    Calendar c = new GregorianCalendar();
    int cY = c.get(Calendar.YEAR);
    int cM = c.get(Calendar.MONTH);
    int cD = c.get(Calendar.DAY_OF_MONTH);
    c.set(cY, cM, cD, 0, 0, 0);
    long ts = c.getTimeInMillis();

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();

      /* Retrieves intake calories. */
      String queryString = VC.SPARQL_PREFIX + "SELECT (SUM(?calories) as ?c) WHERE { " + VC.PREFIX + ":" + this.userId
          + " " + VC.PREFIX + ":consumed ?meal . " + "?meal " + VC.PREFIX + ":hasConsumedFood ?cf ; " + " " + VC.PREFIX
          + ":hasTimestamp ?ts . " + "?cf " + VC.PREFIX + ":hasFood ?f ; " + "    " + VC.PREFIX + ":amountFood ?a . "
          + "?f  " + VC.PREFIX + ":amountCalories ?fc . " + "BIND(((?a / 100) * ?fc) as ?calories) " + "FILTER (?ts > "
          + ts + ")" + "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value calories = bindingSet.getValue("c");
          this.energyBalance.setCaloriesIntake(Double.valueOf(calories.stringValue()));
        }
        result.close();
      }

      /* Retrieves burned calories. */
      queryString = VC.SPARQL_PREFIX + "SELECT (SUM(?calories) as ?c) WHERE { " + "?activity a " + VC.PREFIX
          + ":PerformedActivity ; " + VC.PREFIX + ":hasUserId " + VC.PREFIX + ":" + this.userId + " ; " + VC.PREFIX
          + ":caloriesConsumed ?calories ; " + VC.PREFIX + ":activityStartTimestamp ?ts . " + "FILTER (?ts > " + ts
          + ")" + "}";

      logger.info(queryString);
      tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value calories = bindingSet.getValue("c");
          this.energyBalance.setCaloriesBurned(Double.valueOf(calories.stringValue()));
        }
        result.close();
      }

      this.energyBalance.setRemaining((this.energyBalance.getCaloriesIntake() - this.energyBalance.getCaloriesGoal()
          - this.energyBalance.getCaloriesBurned()) * -1);

      conn.close();

    } catch (Exception e) {
      logger.info("Error in retrieving user goals status. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

  }

  /** Check the status of the user with respect to all goals assigned **/
  public UserGoalsStatusNotification checkUserGoalStatus() {

    UserGoalsStatusNotification ugsn = new UserGoalsStatusNotification();
    ugsn.setUserId(this.userId);
    ArrayList<Pair> goalsStatus = new ArrayList<Pair>();

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();

      /* Retrieves user data and associated profiles. */
      String queryString = VC.SPARQL_PREFIX + "SELECT ?user ?goal ?goalId ?mv ?timing "
          + "(COUNT(?violation) as ?v) (SUM(ABS(?level)) as ?l) WHERE { "
          + "?user " + VC.PREFIX + ":hasUserId \"" + this.userId + "\" . ?user " + VC.PREFIX + ":belongsProfile ?a . "
          + "?a " + VC.PREFIX + ":hasProfile ?goal . "
          + "?goal a " + VC.PREFIX + ":Goal ; " + VC.PREFIX + ":hasGoalId ?goalId ; " + VC.PREFIX + ":hasMonitoredValue ?mv ; "
          + VC.PREFIX + ":hasMonitoredRule ?rule . "
          + "?rule vc:timing ?timing . "
          + "OPTIONAL {?violation a " + VC.PREFIX + ":Violation ; " + VC.PREFIX + ":hasViolationGoal ?goal ; "
          + VC.PREFIX + ":hasViolationLevel ?level ; "
          + VC.PREFIX + ":hasViolationUser " + VC.PREFIX + ":" + this.userId + " . } } "
          + "GROUP BY ?user ?goal ?goalId ?mv ?timing ORDER BY DESC(?v)";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          Value goal = bindingSet.getValue("goalId");
          Value v = bindingSet.getValue("v");
          Value mv = bindingSet.getValue("mv");
          Value l = bindingSet.getValue("l");
          Value timing = bindingSet.getValue("timing");
          
          Double multiplier = 4.0;
          Double violationsLevelSum = Double.parseDouble(l.stringValue());
          
          String timingString = timing.stringValue().substring(timing.stringValue().indexOf("#") + 1);
          if(timingString.compareTo("Day") == 0) multiplier = 28.0;
          Double referenceValue = Double.parseDouble(mv.stringValue()) * 4;
          
          logger.info("{} {} {}", multiplier, violationsLevelSum, referenceValue);
          
          Double goalSatisfactionLevel = (1.0 - ((violationsLevelSum - referenceValue) / (multiplier - referenceValue))) * 100.0;
          
          Pair p = new Pair(goal.stringValue(), goalSatisfactionLevel.toString());
          goalsStatus.add(p);
        }
        result.close();
      }
      ugsn.setGoalsStatus(goalsStatus);
      conn.close();

    } catch (Exception e) {
      logger.info("Error in retrieving user goals status. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    return ugsn;
  }

  
  
  
  /**
   * Check the status of the user with respect to all profiles associated with
   * him/her
   **/
  public UserProfilesStatusNotification checkUserProfilesStatus() {

    UserProfilesStatusNotification upsn = new UserProfilesStatusNotification();
    upsn.setUserId(this.userId);

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();

      /* Retrieves user data and associated profiles. */
      String queryString = VC.SPARQL_PREFIX + "SELECT DISTINCT ?user ?m ?e ?o ?mv ?t (COUNT(?cf) AS ?c) WHERE " + "{ "
          + " ?user vc:hasUserId \"" + this.userId + "\". " + " {" + " SELECT DISTINCT ?m WHERE {"
          //+ " ?goal a vc:Goal ;" + " vc:hasMonitoredRule ?m . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + " ?m a vc:MonitoringRule . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + "   vc:monitoredEntityType vc:Food ; " + "   vc:hasOperator ?o ; " + "   vc:hasMonitoredValue ?mv ; "
          + "   vc:timing ?t . " + " OPTIONAL { " + "    ?user vc:consumed ?meal . " + "    ?user vc:hasUserId \""
          + this.userId + "\" . " + "    ?meal vc:hasConsumedFood ?cf . " + "    ?cf vc:hasFood ?f . "
          + "    ?f a ?e . " + "} " + "}  " + "GROUP BY ?user ?m ?e ?o ?mv ?t ";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(true);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          Value rule = bindingSet.getValue("m");
          Value category = bindingSet.getValue("e");
          Value operator = bindingSet.getValue("o");
          Value monitoredValue = bindingSet.getValue("mv");
          Value timing = bindingSet.getValue("t");
          Value occurrences = bindingSet.getValue("c");

          ProfileStatus p = upsn.new ProfileStatus();
          p.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
          p.setCategory(category.stringValue().substring(category.stringValue().indexOf("#") + 1));
          p.setOperator(operator.stringValue());
          p.setMonitoredValue(monitoredValue.stringValue());
          p.setTiming(timing.stringValue().substring(timing.stringValue().indexOf("#") + 1));
          p.setOccurrences(occurrences.stringValue());
          upsn.addProfileStatus(p);
        }
        result.close();
      }
      conn.close();

    } catch (Exception e) {
      logger.info("Error in retrieving user goals status. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    return upsn;
  }
  
  
  
  /**
   * Check the status of the user with respect to all profiles associated with
   * him/her by using parametric information for filtering based on data and rules type
   **/
  public UserProfilesStatusNotification checkUserProfilesStatusParametric() {

    UserProfilesStatusNotification upsn = new UserProfilesStatusNotification();
    upsn.setUserId(this.userId);

    RepositoryConnection conn = null;
    try {
      Calendar c = new GregorianCalendar();
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      long tsLimit = c.getTimeInMillis();
      
      conn = VC.r.getConnection();

      /* Retrieves user data and associated profiles. */
      String queryString = VC.SPARQL_PREFIX + "SELECT DISTINCT ?user ?m ?e ?o ?mv ?t (COUNT(?cf) AS ?c) WHERE " + "{ "
          + " ?user vc:hasUserId \"" + this.userId + "\". " + " {" + " SELECT DISTINCT ?m WHERE {"
          //+ " ?goal a vc:Goal ;" + " vc:hasMonitoredRule ?m . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + " ?m a vc:MonitoringRule . ?m vc:timing vc:Day . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + "   vc:monitoredEntityType vc:Food ; " + "   vc:hasOperator ?o ; " + " "
          + "   vc:timing ?t . "
          + " {{?m vc:hasMonitoredValue ?mv . } UNION {?m vc:hasMonitoredValueInterval ?mvint . ?mvint vc:upperBound ?mv . }} "
          + " OPTIONAL { " + "    ?user vc:consumed ?meal . " + "    ?user vc:hasUserId \""
          + this.userId + "\" . " + "    ?meal vc:hasConsumedFood ?cf . ?meal vc:hasTimestamp ?ts . " + "    ?cf vc:hasFood ?f . "
          + "    ?f a ?e . FILTER (?ts >= " + tsLimit + ") " + "} " + "}  " + "GROUP BY ?user ?m ?e ?o ?mv ?t ";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(true);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          Value rule = bindingSet.getValue("m");
          Value category = bindingSet.getValue("e");
          Value operator = bindingSet.getValue("o");
          Value monitoredValue = bindingSet.getValue("mv");
          Value timing = bindingSet.getValue("t");
          Value occurrences = bindingSet.getValue("c");

          ProfileStatus p = upsn.new ProfileStatus();
          p.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
          p.setCategory(category.stringValue().substring(category.stringValue().indexOf("#") + 1));
          p.setOperator(operator.stringValue());
          p.setMonitoredValue(monitoredValue.stringValue());
          p.setTiming(timing.stringValue().substring(timing.stringValue().indexOf("#") + 1));
          p.setOccurrences(occurrences.stringValue());
          upsn.addProfileStatus(p);
        }
        result.close();
      }
      
      
      c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      tsLimit = c.getTimeInMillis();
      
      /* Retrieves user data and associated profiles. */
      queryString = VC.SPARQL_PREFIX + "SELECT DISTINCT ?user ?m ?e ?o ?mv ?t (COUNT(?cf) AS ?c) WHERE " + "{ "
          + " ?user vc:hasUserId \"" + this.userId + "\". " + " {" + " SELECT DISTINCT ?m WHERE {"
          //+ " ?goal a vc:Goal ;" + " vc:hasMonitoredRule ?m . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + " ?m a vc:MonitoringRule . ?m vc:timing vc:Week . " + " } " + " } " + " ?m vc:monitoredEntity ?e ; "
          + "   vc:monitoredEntityType vc:Food ; " + "   vc:hasOperator ?o ; " + " "
          + "   vc:timing ?t . "
          + " {{?m vc:hasMonitoredValue ?mv . } UNION {?m vc:hasMonitoredValueInterval ?mvint . ?mvint vc:upperBound ?mv . }} "
          + " OPTIONAL { " + "    ?user vc:consumed ?meal . " + "    ?user vc:hasUserId \""
          + this.userId + "\" . " + "    ?meal vc:hasConsumedFood ?cf . ?meal vc:hasTimestamp ?ts . " + "    ?cf vc:hasFood ?f . "
          + "    ?f a ?e . FILTER (?ts >= " + tsLimit + ") " + "} " + "}  " + "GROUP BY ?user ?m ?e ?o ?mv ?t ";

      logger.info(queryString);
      tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(true);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          Value rule = bindingSet.getValue("m");
          Value category = bindingSet.getValue("e");
          Value operator = bindingSet.getValue("o");
          Value monitoredValue = bindingSet.getValue("mv");
          Value timing = bindingSet.getValue("t");
          Value occurrences = bindingSet.getValue("c");

          ProfileStatus p = upsn.new ProfileStatus();
          p.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
          p.setCategory(category.stringValue().substring(category.stringValue().indexOf("#") + 1));
          p.setOperator(operator.stringValue());
          p.setMonitoredValue(monitoredValue.stringValue());
          p.setTiming(timing.stringValue().substring(timing.stringValue().indexOf("#") + 1));
          p.setOccurrences(occurrences.stringValue());
          upsn.addProfileStatus(p);
        }
        result.close();
      }
      
      conn.close();
      
    } catch (Exception e) {
      logger.info("Error in retrieving user goals status. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    return upsn;
  }
  
  
  

  /** Add a property to the user profile **/
  public void addProperty(ArrayList<Pair> p) {
    this.properties.addAll(p);
  }

  /**
   * Read a property to the user profile
   * 
   * @return
   **/
  public String getProperty(String p) {
    for (Pair prp : this.getProperties()) {
      if (prp.getK().compareTo(p) == 0)
        return prp.getV();
    }
    return "";
  }

  /** Remove a property to the user profile **/
  public void removeProperty(ArrayList<Pair> p) {
    for (Pair prpToRemove : p) {
      for (Iterator<Pair> iterator = this.getProperties().iterator(); iterator.hasNext();) {
        Pair prp = iterator.next();
        if (prp.getK().compareTo(prpToRemove.getK()) == 0)
          iterator.remove();
      }
    }
  }

  /** Adds a new ruleset/goal to the current User profile **/
  public boolean addAllRuleSetGoals() {

    RepositoryConnection conn = null;

    try {
      conn = VC.r.getConnection();

      this.profiles = new ArrayList<Profile>();

      /* Add all profiles defined in the repository to the current user. */
      String query = VC.SPARQL_PREFIX + "INSERT { GRAPH <" + VC.PREFIX + ":CXT-USERS> { " + VC.PREFIX + ":"
          + this.userId + " " + VC.PREFIX + ":belongsProfile ?p . } } " + " WHERE {  { ?p rdf:type " + VC.PREFIX
          + ":Goal . } UNION { ?p rdf:type " + VC.PREFIX + ":Profile . }" + "}";

      logger.info(query);
      try {
        conn.begin();
        UpdateUtil.executeUpdate(conn, query);
        conn.commit();
        conn.close();
      } catch (Exception e) {
        logger.info("Error in saving User data.");
        logger.info(Arrays.toString(e.getStackTrace()));
      }

    } catch (Exception e) {
      logger.info("Error during the assignments of all profiles to {}. {} {}", this.userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    return true;
  }

  /** Clean the current User profile from all ruleset/goal **/
  public boolean removeAllRuleSetGoals() {

    RepositoryConnection conn = null;
    HeLiSConfigurator hc = VC.config;

    try {

      conn = VC.r.getConnection();

      this.profiles = new ArrayList<Profile>();
      String defaultProfiles = hc.getVirtualCoachUserDefaultProfiles();
      String[] p = defaultProfiles.split(";");
      for(int i = 0; i < p.length; i++) {
        this.profiles.add(new Profile(p[i]));
      }
      

      /* Deletes all profiles associated with the current user. */
      /*
      String  deleteQuery = VC.SPARQL_PREFIX + "WITH <" + VC.PREFIX + ":CXT-USERS> DELETE { ?s ?p ?v } " +
                            "WHERE { ?u vc:belongsProfile ?s . ?s ?p ?v . " +
                            "FILTER (?u = " + VC.PREFIX + ":" + this.getUserId() + ")}";
      logger.info(deleteQuery);
      try {
        conn.begin();
        UpdateUtil.executeUpdate(conn, deleteQuery);
        conn.commit();
        conn.close();
      } catch (Exception e) {
        logger.info("Error in saving User data.");
        logger.info(Arrays.toString(e.getStackTrace()));
      }
      
      
      
      deleteQuery = VC.SPARQL_PREFIX + "DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . " + "?s " + VC.PREFIX
          + ":belongsProfile ?v . " + "FILTER (?s = " + VC.PREFIX + ":" + this.getUserId() + ")}";
      logger.info(deleteQuery);
      try {
        conn.begin();
        UpdateUtil.executeUpdate(conn, deleteQuery);
        conn.commit();
        conn.close();
      } catch (Exception e) {
        logger.info("Error in saving User data.");
        logger.info(Arrays.toString(e.getStackTrace()));
      }
      */
      

    } catch (Exception e) {
      logger.info("Error during the removal of all profiles to {}. {} {}", this.userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }

    return true;
  }

  /** Adds a new ruleset/goal to the current User profile **/
  public void addRuleSetGoal(String ruleset) {
    this.profiles.add(new Profile(ruleset));
  }

  /** Removes a new ruleset/goal to the current User profile **/
  public void removeRuleSetGoal(String ruleset) {
    Iterator<Profile> iter = this.profiles.iterator();
    while (iter.hasNext()) {
      Profile p = iter.next();
      if (p.getProfileId().compareTo(ruleset) == 0) {
        iter.remove();
      }
    }
  }
}
