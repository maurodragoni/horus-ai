package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.VC.EngineMode;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Goal;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Profile;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Violation;
import eu.fbk.ict.ehealth.virtualcoach.messages.send.ReasonerThreadResult;
import eu.fbk.ict.ehealth.virtualcoach.queue.Publisher;
import eu.fbk.rdfpro.util.QuadModel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;


public class ReasonerThread {

  private static Logger logger;

  private QuadModel userData;
  
  private String userId;
  private ArrayList<String> eventId;
  private String eventType;
  private String reasoningType;
  private String timingType;
  
  private Engine engine;
  
  private long violationLowerTimestamp = 0;
  
  //metrics
  private static final Counter HELIS_REASONING_COUNT = Counter.build()
      .name("helis_reasoning_counter_total")
      .help("Total number of reasoner executions, including the status (ok / fail).")
      .labelNames("reasoning", "status")
      .register();
  private static final Histogram HELIS_TIME = Histogram.build()
      .name("helis_reasoning_duration_seconds")
      .help("Time requested to perform a reasoning operation.")
      .labelNames("reasoning")
      .register();

  
  
  
  public ReasonerThread(Engine preEngine, String eventType, ArrayList<String> eventId, String userId) {
    //this.reasonOnData(userId, userMeals, EngineMode.QB, false);
    
    final Histogram.Timer timer = HELIS_TIME.labels("reasoner-init").startTimer();
    
    // TODO: variabile d'ambiente per indicare il tipo di reasoning da supportare (NODATA, DAY-ONLY, ecc.)
    // TODO: fare il check delle regole in merito alla mancanza di inferenze sulle attivita', esempio su Engine.activity.day.ttl
    // TODO: per sicurezza fare un check anche sulle regole degli alimenti
    // TODO: fixare le regole sull'alimentazione in modo che filtrino le quantity = 0.0 per il conteggio delle porzioni
    //       esempio su Engine.food.monitoring regola check_contains_food_portion_less_day
    // TODO: fixare il tipo di alimento tipo nella regola :check_contains_food_portion_greater_day
    //       modificare ontologia in modo da non fare piu' regole su individui specifici
    
    try {
      logger = LoggerFactory.getLogger(ReasonerThread.class);
      this.userId = userId;
      this.eventId = eventId;
      this.violationLowerTimestamp = Long.valueOf(eventId.get(0).split("-")[eventId.get(0).split("-").length - 1]);
      for (int i = 1; i < eventId.size(); i++) {
        long curTs = Long.valueOf(eventId.get(i).split("-")[eventId.get(i).split("-").length - 1]);
        if (curTs < this.violationLowerTimestamp) {
          this.violationLowerTimestamp = curTs;
        }
      }
      this.eventType = eventType;
      this.engine = preEngine.clone();
      //this.userData = getUserData(eventType, eventId, userId);
      this.userData = getUserDataWeekly(eventType, userId);
      this.engine.setEngineMode(EngineMode.QB);
      //this.engine.setEngineMode(EngineMode.CHECK);
      HELIS_REASONING_COUNT.labels("reasoner-init", "OK").inc();
    } catch (Exception e) {
      HELIS_REASONING_COUNT.labels("reasoner-init", "FAILED").inc();
      throw e;
    } finally {
      timer.observeDuration();
    }
  }
  
  
  
  
  
  public ReasonerThread(Engine preEngine, String eventType, String userId, EngineMode mode) {
    
    final Histogram.Timer timer = HELIS_TIME.labels("reasoner-init").startTimer();
    
    try {
      logger = LoggerFactory.getLogger(ReasonerThread.class);
      this.userId = userId;
      
      //this.eventId = eventId;
      
      
      /*
      this.violationLowerTimestamp = Long.valueOf(eventId.get(0).split("-")[eventId.get(0).split("-").length - 1]);
      for (int i = 1; i < eventId.size(); i++) {
        long curTs = Long.valueOf(eventId.get(i).split("-")[eventId.get(i).split("-").length - 1]);
        if (curTs < this.violationLowerTimestamp) {
          this.violationLowerTimestamp = curTs;
        }
      }
      */
      this.violationLowerTimestamp = System.currentTimeMillis() - (86400000 * 7);
      
      this.eventType = eventType;
      this.engine = preEngine.clone();
      /*
      if(mode == EngineMode.WEEK) {
        this.userData = getUserDataWeekly(eventType, userId);
      }
      */
      this.userData = this.getGoalBasedUserData(eventType, userId, mode);
      this.engine.setEngineMode(mode);
      HELIS_REASONING_COUNT.labels("reasoner-init", "OK").inc();
    } catch (Exception e) {
      HELIS_REASONING_COUNT.labels("reasoner-init", "FAILED").inc();
      throw e;
    } finally {
      timer.observeDuration();
    }
  }
  
  
  
  
  public String getReasoningType() {
    return reasoningType;
  }

  public void setReasoningType(String reasoningType) {
    this.reasoningType = reasoningType;
  }
  
  public String getTimingType() {
    return timingType;
  }

  public void setTimingType(String timingType) {
    this.timingType = timingType;
  }

  
  
  /*
  public ReasonerThread(RepositoryConnection c, QuadModel ontology, String mode, String mealId, String userId, 
                        boolean force, int y, int m, int d) {
    this.userId = userId;
    this.mealId = mealId;
  }
  */
  
  
  




  public void run() {
    
    HeLiSConfigurator hc = VC.config;
    
    if(this.userData == null) return;
    
    final Histogram.Timer timer = HELIS_TIME.labels("reasoner-run").startTimer();
    
    /*
    logger.info("Request received by the Reasoner");
    if (this.mode.compareTo("singlemeal") == 0) {
      //this.runSingleMealReasoning(userId, mealId);
      this.runDailyReasoning();
    } else if (mode.compareTo("daily") == 0) {
      this.runDailyReasoning();
    } else if (mode.compareTo("weekly") == 0) {
      this.runWeeklyReasoning();
    }
    */

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      long ts = System.currentTimeMillis();
      QuadModel inf = engine.process(userData);

      ValueFactory factory = SimpleValueFactory.getInstance();
      IRI rulesContext = factory.createIRI(VC.PREFIX + ":CXT-VIOLATIONS");
      Iterator<Statement> iS = inf.iterator();
      while (iS.hasNext()) {
        Statement s = iS.next();
        // logger.info("Inferred statement: {} {} {}",
        // s.getSubject().toString(), s.getPredicate().toString(),
        // s.getObject().toString());
        if (s.getSubject().toString().contains("violation_")) {
          conn.add(s, rulesContext);
        }
      }

      long time = System.currentTimeMillis() - ts;
      int infTriples = inf.size();
      int infViolations = inf.filter(null, RDF.TYPE, VC.VIOLATION).size();
      logger.info("Reasoner inferred {} triples in {} ms.", infTriples, time);
      logger.info("Reasoner found {} violations.", infViolations);
      logger.info("***********************************************************");
      this.engine = null;
      
      this.sendNotifications(hc, userId, eventId);
      // this.checkGoalSatisfaction(userId, eventId);
      HELIS_REASONING_COUNT.labels("reasoner-run", "OK").inc();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the save of violation data. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
      
      HELIS_REASONING_COUNT.labels("reasoner-run", "FAILED").inc();
      throw e;
    } finally {
      timer.observeDuration();
    } 
  }
  
  
  
  
  
  private void sendNotifications(HeLiSConfigurator hc, String userId, ArrayList<String> eventId) {
    
    try (RepositoryConnection conn = VC.r.getConnection()) {
  
      ReasonerThreadResult rtr = new ReasonerThreadResult();
      rtr.setUserId(userId);
      rtr.setType(this.reasoningType);
      
      String entityFilter = "Food";
      if(this.eventType.compareTo("ACTIVITY") == 0) entityFilter = "Activity";
      
      String timingFilter = "";
      Calendar c = new GregorianCalendar();
      c.set(Calendar.HOUR_OF_DAY, 0);
      if(this.timingType.compareTo("Day") == 0 ||
         this.timingType.compareTo("Week") == 0) {
        timingFilter = "FILTER EXISTS {?id " + VC.PREFIX + ":hasViolationTiming " + VC.PREFIX + ":" + this.timingType + " } ";
        c.set(Calendar.HOUR_OF_DAY, 0);
        if(this.timingType.compareTo("Week") == 0) {
          c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        this.violationLowerTimestamp = c.getTimeInMillis();
      }

      
      String queryString = VC.SPARQL_PREFIX +
          "SELECT ?id ?user ?rule ?ruleId ?timestamp ?entityType ?entity ?timing ?quantity ?expectedQuantity ?priority " +
          "       ?meal ?goal ?goalconstraint ?level ?constraint ?history WHERE {" + 
          " ?id rdf:type " + VC.PREFIX + ":Violation ; " +
          VC.PREFIX + ":hasViolationUser " + VC.PREFIX + ":" + userId + " ; " + 
          VC.PREFIX + ":hasViolationRule ?rule ; " +
          VC.PREFIX + ":hasViolationRuleId ?ruleId ; " + 
          VC.PREFIX + ":hasTimestamp ?timestamp ; " +
          VC.PREFIX + ":hasViolationEntityType ?entityType ; " + 
          VC.PREFIX + ":hasViolationEntity ?entity ; " +
          VC.PREFIX + ":hasViolationQuantity ?quantity ; " + 
          VC.PREFIX + ":hasViolationExpectedQuantity ?expectedQuantity ; " + 
          VC.PREFIX + ":hasViolationPriority ?priority ; " +
          VC.PREFIX + ":hasViolationLevel ?level ; " + 
          VC.PREFIX + ":hasViolationConstraint ?constraint ; " +
          VC.PREFIX + ":hasViolationTiming ?timing ; " +
          VC.PREFIX + ":hasViolationHistory ?history . " +
          "?rule " + VC.PREFIX + ":monitoredEntityType " + VC.PREFIX + ":" + entityFilter + " . " +
          " OPTIONAL {" +
          "  ?id " + VC.PREFIX + ":hasViolationGoal ?goal . " +
          "  ?goal rdf:type " + VC.PREFIX + ":Goal ; " + 
                   VC.PREFIX + ":hasMonitoredValue ?goalconstraint . } " +
          " OPTIONAL {?id " + VC.PREFIX + ":hasViolationMeal ?meal } " + 
          "FILTER (?timestamp >= " + this.violationLowerTimestamp + ") " + 
          timingFilter +
          " } " + 
          "ORDER BY DESC(?timestamp) ?priority "; 
          //LIMIT 1";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      HashMap<String, Violation> tempViolations = new HashMap<String, Violation>();
      ArrayList<Violation> vl = new ArrayList<Violation>();
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          if (userId == null) {
            userId = bindingSet.getValue("user").stringValue();
          }
          Value id = bindingSet.getValue("id");
          Value rule = bindingSet.getValue("rule");
          Value ruleId = bindingSet.getValue("ruleId");
          Value timestamp = bindingSet.getValue("timestamp");
          Value entityType = bindingSet.getValue("entityType");
          Value entity = bindingSet.getValue("entity");
          Value timing = bindingSet.getValue("timing");
          Value quantity = bindingSet.getValue("quantity");
          Value expectedQuantity = bindingSet.getValue("expectedQuantity");
          Value meal = bindingSet.getValue("meal");
          Value goal = bindingSet.getValue("goal");
          Value goalConstraint = bindingSet.getValue("goalconstraint");
          Value priority = bindingSet.getValue("priority");
          Value level = bindingSet.getValue("level");
          Value constraint = bindingSet.getValue("constraint");
          Value history = bindingSet.getValue("history");

          String violationId = id.stringValue().substring(id.stringValue().indexOf("#") + 1);
          Violation v = tempViolations.get(violationId);
          if(v == null) {
            v = new Violation();
            v.setViolationId(id.stringValue().substring(id.stringValue().indexOf("#") + 1));
            v.setUser(userId.substring(userId.indexOf("#") + 1));
            v.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
            v.setRuleId(ruleId.stringValue());
            v.setTimestamp(timestamp.stringValue());
            v.setEntityType(entityType.stringValue());
            v.setEntity(entity.stringValue().substring(entity.stringValue().indexOf("#") + 1));
            v.setTiming(timing.stringValue().substring(timing.stringValue().indexOf("#") + 1));
            v.setQuantity(quantity.stringValue());
            v.setExpectedQuantity(expectedQuantity.stringValue());
            if(meal != null) v.addMeal(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
            else v.addMeal("ACTIVITY");
            if(goal != null && goalConstraint != null)
              v.addGoal(goal.stringValue().substring(goal.stringValue().indexOf("#") + 1), goalConstraint.stringValue());
            //v.addMeals(eventId);
            v.setPriority(priority.stringValue());
            v.setLevel(level.stringValue());
            v.setConstraint(constraint.stringValue());
            v.setHistory(history.stringValue());
            //this.violations.put(id.stringValue().substring(id.stringValue().indexOf("#") + 1), v);   
            //if(eventId.contains("-Running-") && id.stringValue().contains("SMOKE")) continue;
            //if(eventId.contains("-7024-") && id.stringValue().contains("ACTIVITY")) continue;   
          } else {
            if(meal != null) v.addMeal(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
            else v.addMeal("ACTIVITY");
            
            if(goal != null && goalConstraint != null)
              v.addGoal(goal.stringValue().substring(goal.stringValue().indexOf("#") + 1), goalConstraint.stringValue());
          }
          tempViolations.put(violationId, v);
        }
        result.close();
      }
      
      Iterator<String> itV = tempViolations.keySet().iterator();
      while(itV.hasNext()) {
        String violationId = itV.next();
        vl.add(tempViolations.get(violationId));  
      }
      
      
      ArrayList<Goal> gl = new ArrayList<Goal>();
      if(hc.getVirtualCoachUseGoalCheckLive().compareTo("1") == 0) {
        gl = this.checkGoalSatisfaction(userId, eventId);
      }
      
      
      rtr.setViolations(vl);
      rtr.setGoals(gl);
      Gson gson = new Gson();
      try {
        logger.info(gson.toJson(rtr));
        
        if(hc.getVirtualCoachUseReasonerLive().compareTo("1") == 0 &&
           hc.getVirtualCoachUseRabbit().compareTo("1") == 0) {
          Publisher.sendReasonerResult(gson.toJson(rtr));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      conn.close();
    }
  }
  
  
  
  
  private ArrayList<Goal> checkGoalSatisfaction(String userId, ArrayList<String> eventId) {
     
    ArrayList<Goal> goals = new ArrayList<Goal>();
    ArrayList<Pair> goalsActivationTimestamp = this.getLastUserGoalTimestamp(userId);
    RepositoryConnection conn = null;
    
    try {
     
      conn = VC.r.getConnection();
      
      HashMap<String, Boolean> goalDataValidator = new HashMap<String, Boolean>();
      HashMap<String, Long> goalsLastData = new HashMap<String, Long>();
      //HashMap<String, ArrayList<String>> goalsToProfiles = new HashMap<String, ArrayList<String>>();
      
      String checkDataQuery = new String();
      /*
      if(this.eventType.compareTo("ACTIVITY") == 0) {
        checkDataQuery = VC.SPARQL_PREFIX + 
            "SELECT ?user ?goal ?profile (COUNT(?pa) AS ?availableData) (MAX(?timestamp) AS ?last) (MIN(?timestamp) AS ?first) WHERE { " +  
            "  ?goal a vc:Goal ; vc:hasMonitoredRule ?rule ; vc:appliesTo ?profile . " + 
            "  {SELECT DISTINCT ?goal ?user WHERE {?goal vc:appliesTo ?profile. ?profile ^vc:belongsProfile ?user}} " + 
            "  ?rule vc:monitoredEntity ?activity . " + 
            "  ?rule vc:monitoredEntityType vc:Activity . " + 
            "  ?pa rdf:type vc:PerformedActivity ; vc:hasUserId ?user ;  " + 
            "      vc:hasActivity ?activity ; vc:activityStartTimestamp ?timestamp . } " +
            "GROUP BY ?user ?goal ?profile " +
            "ORDER BY ?user ?goal ?profile ";
      } else if(this.eventType.compareTo("MEAL") == 0) {
        checkDataQuery = VC.SPARQL_PREFIX + 
            "SELECT ?user ?goal ?profile (COUNT(DISTINCT ?meal) AS ?availableData) (MAX(?timestamp) AS ?last) (MIN(?timestamp) AS ?first) WHERE { " +  
            "  ?goal a vc:Goal ; vc:hasMonitoredRule ?rule ; vc:appliesTo ?profile . " + 
            "  {SELECT DISTINCT ?goal ?user WHERE {?goal vc:appliesTo ?profile. ?profile ^vc:belongsProfile ?user}} " + 
            "  ?rule vc:monitoredEntity ?food . " + 
            "  ?rule vc:monitoredEntityType vc:Food . " + 
            "  ?user vc:consumed ?meal .  " + 
            "  ?meal vc:hasTimestamp ?timestamp . } " +
            "GROUP BY ?user ?goal ?profile " +
            "ORDER BY ?user ?goal ?profile ";
      }
      */
      
      // TODO: variabile d'ambiente per decidere se fare il check del goal sul singolo evento oppure no.
      
      String e = eventId.get(0);
      
      if(this.eventType.compareTo("ACTIVITY") == 0) {
		checkDataQuery = VC.SPARQL_PREFIX
				+ "SELECT ?user ?goal ?timing (COUNT(?pa) AS ?availableData) (MAX(?timestamp) AS ?last) (MIN(?timestamp) AS ?first) WHERE { "
				+ "  ?goal a vc:Goal ; vc:timing ?timing ; vc:hasMonitoredRule ?rule . "
				+ "  {SELECT DISTINCT ?goal ?user WHERE {?goal ^vc:belongsProfile ?user}} "
				+ "  ?rule vc:monitoredEntity ?activity . " + "  ?rule vc:monitoredEntityType vc:Activity . "
				//+ "  ?pa rdf:type vc:PerformedActivity ; vc:hasUserId ?user ;  "
				//+ "      vc:hasActivity ?a ; vc:activityStartTimestamp ?timestamp . "
				//+ "  { ?a a ?activity . } UNION { ?a rdfs:subClassOf vc:Activity . } "
				+ " ?pa rdf:type vc:PerformedActivity ; "
				+ " vc:hasUserId ?user ; "  
		        + " vc:activityStartTimestamp ?timestamp . "
		        + " { "
		        + "   ?pa vc:hasActivity ?a . "
		        + "   { ?a a ?activity . } UNION { ?a rdfs:subClassOf vc:Activity . } "
		        + " } "
		        //+ " UNION "
		        //+ " { "
		        //+ "   ?pa vc:hasActivity vc:ACTIVITY-7024 . "
		        //+ " } "
				+ "  ?user vc:hasUserId \"" + userId + "\" . } "
				+ "GROUP BY ?user ?goal ?timing ORDER BY ?user ?goal ";
      } else if(this.eventType.compareTo("MEAL") == 0) {
		checkDataQuery = VC.SPARQL_PREFIX
				+ "SELECT ?user ?goal ?timing (COUNT(DISTINCT ?meal) AS ?availableData) (MAX(?timestamp) AS ?last) (MIN(?timestamp) AS ?first) WHERE { "
				+ "  ?goal a vc:Goal ; vc:timing ?timing ; vc:hasMonitoredRule ?rule . "
				+ "  {SELECT DISTINCT ?goal ?user WHERE {?goal ^vc:belongsProfile ?user}} "
				+ "  ?rule vc:monitoredEntity ?food . " 
				+ "  ?rule vc:monitoredEntityType vc:Food . "
				+ "  ?user vc:consumed ?meal . "
				+ "  ?user vc:consumed vc:MEAL-" + e + " . "
				//+ "  vc:MEAL-" + e + " vc:hasConsumedFood ?cf . "
			    + "  { ?cf vc:hasFood ?f . ?f a ?food . } UNION { ?cf vc:hasFood ?food . } "
				+ "  ?user vc:hasUserId \"" + userId + "\" . ?meal vc:hasTimestamp ?timestamp . } "
				+ "GROUP BY ?user ?goal ?timing ORDER BY ?user ?goal ";
      }          
      
      
      
      logger.info(checkDataQuery);
      Long lastTsStr = 0L;
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, checkDataQuery);
      tupleQuery.setIncludeInferred(true);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value goal = bindingSet.getValue("goal");
          //Value profile = bindingSet.getValue("profile");
          Value availableData = bindingSet.getValue("availableData");
          Value timing = bindingSet.getValue("timing");
          Value lastTs = bindingSet.getValue("last");
          Value firstTs = bindingSet.getValue("first");
          
          // TODO: vedere se si riesce ad ottimizzare questa parte relativa al monitoraggio del goal
          if(goal == null) break;
          
          String goalStr = goal.stringValue().substring(goal.stringValue().indexOf("#") + 1);
          //String profileStr = profile.stringValue().substring(profile.stringValue().indexOf("#") + 1);
          String timeStr = timing.stringValue().substring(timing.stringValue().indexOf("#") + 1);
          Double availableDataStr = Double.valueOf(availableData.stringValue());
          lastTsStr = Long.valueOf(lastTs.stringValue());
          Double firstTsStr = Double.valueOf(firstTs.stringValue());
          
                   
          /*
          ArrayList<String> goalProfiles = goalsToProfiles.get(goalStr);
          if(goalProfiles == null) {
            goalProfiles = new ArrayList<String>();
          }
          goalProfiles.add(profileStr);
          goalsToProfiles.put(goalStr, goalProfiles);
          */
          goalsLastData.put(goalStr, lastTsStr);
          
          /*
          String[] goalStringTokens = goalStr.split("-");
          double threshold = 0.0;
          int goalType = Integer.valueOf(goalStringTokens[3]);
          if(goalType < 200) threshold = 28.0;
          else if(goalType < 300) threshold = 56.0;
          else threshold = 84.0;
          */
          double threshold = Double.valueOf(timeStr.replace("DAY", ""));
          
          logger.info("Validating GOAL {} - Goal Duration {} days.", goalStr, threshold);
          
          
          if(availableDataStr >= threshold && (((lastTsStr - firstTsStr) / 86400000) >= threshold)) {
            goalDataValidator.put(goalStr, true);
          } else {
            goalDataValidator.put(goalStr, false);
          }
        }
        result.close();
      }
      
      logger.info("{}", goalsLastData.toString());
      logger.info("{} {}", goalDataValidator.toString(), lastTsStr);
      
      
      Iterator<String> itData = goalsLastData.keySet().iterator();
      while(itData.hasNext()) {
        String currentGoal = itData.next();
        Long lastTs = goalsLastData.get(currentGoal);
        
        String[] goalStringTokens = currentGoal.split("-");
        double threshold = 0.0;
        int goalType = Integer.valueOf(goalStringTokens[3]);
        if(goalType < 200) threshold = 28.0;
        else if(goalType < 300) threshold = 56.0;
        else threshold = 84.0;
        
        String queryString = VC.SPARQL_PREFIX + 
            "SELECT ?goal (SUM(?et) AS ?actual) (SUM(?mv) AS ?monitored) WHERE { " +
            " ?goal a " + VC.PREFIX + ":Goal ; " +
            "         " + VC.PREFIX + ":hasGoalId \"" + currentGoal + "\" ; " +
            "         " + VC.PREFIX + ":hasMonitoredRule ?rule ; " +
            "         " + VC.PREFIX + ":hasMonitoredValue ?mv ; " +
            "         " + VC.PREFIX + ":hasOperator ?operator.  " +
            " FILTER EXISTS {?goal " + VC.PREFIX + ":hasOperator \"equal\"} " +
            //" FILTER EXISTS {?goal " + VC.PREFIX + ":timing " + VC.PREFIX + ":DAY28} " + 
            " {SELECT DISTINCT ?goal ?user WHERE {{?goal ^" + VC.PREFIX + ":belongsProfile " + 
            									   VC.PREFIX + ":" + userId + "}}} " +
            " OPTIONAL { ?v a " + VC.PREFIX + ":Violation ; " +
            "      " + VC.PREFIX + ":hasViolationRule ?rule ; " +
            "      " + VC.PREFIX + ":hasViolationUser " + VC.PREFIX + ":" + userId + " ; " +
            "      " + VC.PREFIX + ":hasViolationLevel ?et ; " +
            "      " + VC.PREFIX + ":hasTimestamp ?timestamp. " + 
            "      FILTER (?timestamp >= " + (lastTs.longValue() - (86400000 * threshold)) + ")} " +
            "} " +
            "GROUP BY ?goal ?user";
        
        logger.info("{} {} {}", lastTs.longValue(), ((int)threshold), (lastTs.longValue() - (86400000 * threshold)));
        logger.info(queryString);
        Double actualD = 0.0;
        Double monitoredD = 0.0;
        tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        tupleQuery.setIncludeInferred(false);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
          while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            
            Value goal = bindingSet.getValue("goal");
            //Value rule = bindingSet.getValue("rule");
            Value actual = bindingSet.getValue("actual");
            Value monitored = bindingSet.getValue("monitored");
            
            String goalStr = goal.stringValue().substring(goal.stringValue().indexOf("#") + 1);
            //String ruleStr = rule.stringValue().substring(rule.stringValue().indexOf("#") + 1);
            actualD = Double.valueOf(actual.stringValue());
            monitoredD = Double.valueOf(monitored.stringValue());

            //logger.info("{} {} {}", goalStr, actualD, monitoredD);
            if(actualD.doubleValue() != monitoredD.doubleValue()) {
              goalDataValidator.put(goalStr, false);
            }
          }
          result.close();
        }
      }
      
      
      logger.info(goalDataValidator.toString());
      
      Iterator<String> it = goalDataValidator.keySet().iterator();
      while(it.hasNext()) {
        String currentGoal = it.next();
        Boolean goalSatisfied = Boolean.valueOf(goalDataValidator.get(currentGoal));
        Goal g = new Goal();
        g.setUserId(userId);
        g.setGoalId(currentGoal);
        //g.setProfiles(goalsToProfiles.get(currentGoal));
        if(goalSatisfied) {
          g.setStatus("SATISFIED");
        } else {
          g.setStatus("UNSATISFIED"); 
        }
        g.setTimestamp(System.currentTimeMillis());
        //Gson gson = new Gson();
        //Publisher.sendGoal(gson.toJson(g));
        goals.add(g);
      }
      
      conn.close();
    } catch (Exception e) {
      logger.info("Error during goal checking. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    return goals;
  }
  
  
  
  
  private HashMap<String, Integer> checkDataAvailability(ArrayList<Pair> goalsActivationTimestamp) {
    
    HashMap<String, Integer> goalDataAvailability = new HashMap<String, Integer>();
    HashMap<String, Integer> dataCheck = new HashMap<String, Integer>();
    String checkDataQuery;
    long oldestMealGoalActivation = Long.MAX_VALUE;
    long oldestActivityGoalActivation = Long.MAX_VALUE;
    RepositoryConnection conn = null;
    Calendar c = new GregorianCalendar();
    
    
    /* Get the oldest goal activation timestamp for each kind of monitored events.
     * This information is used for retrieving all the data that have to be checked.
     */
    for(Pair p : goalsActivationTimestamp) {
      String goalId = p.getK();
      long timestamp = Long.parseLong(p.getV());
      
      if(goalId.indexOf("-A-") != -1) {
        if(timestamp < oldestActivityGoalActivation) oldestActivityGoalActivation = timestamp;
      } else if(goalId.indexOf("-D-") != -1) {
        if(timestamp < oldestMealGoalActivation) oldestMealGoalActivation = timestamp;
      }
    }
    
    
    /* Get and manage ACTIVITY event data */
    checkDataQuery = VC.SPARQL_PREFIX
        + "SELECT DISTINCT ?activityTs WHERE { "
        + "  ?pa rdf:type vc:PerformedActivity ; "
        + "      vc:hasUserId ?user ; "  
        + "      vc:activityStartTimestamp ?activityTs ; "
        + "      vc:hasActivity ?a . "
        + "  ?a a vc:Activity . "
        + "  ?user vc:hasUserId \"" + this.userId + "\" . "
        + "}";    
    
    logger.info(checkDataQuery);
    TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, checkDataQuery);
    tupleQuery.setIncludeInferred(true);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value activityTs = bindingSet.getValue("activityTs");
        long timestamp = Long.valueOf(activityTs.stringValue());
        c.setTimeInMillis(timestamp);
        String checkString = new String(c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH));
        dataCheck.put(checkString, 0);
      }
      result.close();
    }
    for(Pair p : goalsActivationTimestamp) {
      String goalId = p.getK();
      if(goalId.indexOf("-A-") != -1) {
        long timestamp = Long.parseLong(p.getV());
        
      }
    }
    
    
    
    
    
    
    /* Get and manage MEAL event data */
    checkDataQuery = VC.SPARQL_PREFIX
        + "SELECT DISTINCT ?mealTs WHERE { "
        + "  ?user vc:consumed ?meal . "
        + "  ?user vc:hasUserId \"" + this.userId + "\" . "
        + "  ?meal vc:hasTimestamp ?mealTs . "
        + "}+";
    
    logger.info(checkDataQuery);
    tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, checkDataQuery);
    tupleQuery.setIncludeInferred(true);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value activityTs = bindingSet.getValue("mealTs");
        long timestamp = Long.valueOf(activityTs.stringValue());
        c.setTimeInMillis(timestamp);
        String checkString = new String(c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH));
        dataCheck.put(checkString, 0);
      }
      result.close();
    }
     
    
    return goalDataAvailability;
  }
  
  
  
  
  
  /*
  private void runSingleMealReasoning() {
    Calendar c = new GregorianCalendar();

    // Monitors potential violations associated with Meal upper limits
    logger.info("Monitoring single meal.");
    //long ts = c.getTimeInMillis() - 36000000;
    //ArrayList<String> userMeals = this.getUserMeals(userId, ts);
    ArrayList<String> userMeals = new ArrayList<String>();
    userMeals.add("MEAL-" + this.mealId);
    this.reasonOnData(this.userId, userMeals, EngineMode.QB, false);
    logger.info("***********************************************************");
  }
  */
  
  
  /*
  private void runDailyReasoning() {

    // Gets all the active users of the system and analyze daily activities
    Calendar c = new GregorianCalendar();
    int cY = c.get(Calendar.YEAR);
    int cM = c.get(Calendar.MONTH);
    int cD = c.get(Calendar.DAY_OF_MONTH);
    if (this.force) {
      cY = this.y;
      cM = this.m;
      cD = this.d;
    }
    c.set(cY, cM, cD, 0, 0, 0);
    long ts = c.getTimeInMillis();
    ArrayList<String> users = this.getUsers();
    for (String userId : users) {
      logger.info("***********************************************************");
      ArrayList<String> userMeals = this.getUserMeals(userId, ts);
      this.reasonOnData(userId, userMeals, EngineMode.DAY, true);
      logger.info("***********************************************************");
    }
  }
  */

  
  
  /*
  private void runWeeklyReasoning() {

    // Gets all the active users of the system and analyze weekly activities
    Calendar c = new GregorianCalendar();
    System.out.println(Calendar.DAY_OF_WEEK + " - " + Calendar.MONDAY);
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    System.out.println(c.getTime());
    long ts = c.getTimeInMillis();
    ArrayList<String> users = this.getUsers();
    for (String userId : users) {
      logger.info("***********************************************************");
      ArrayList<String> userMeals = this.getUserMeals(userId, ts);
      this.reasonOnData(userId, userMeals, EngineMode.WEEK, true);
      logger.info("***********************************************************");
    }
  }
  */

  
  
  
  
  /*
  private void reasonOnData(String userId, ArrayList<String> userMeals, EngineMode e, boolean aggregate) {

    long ts = 0;
    this.rules = QuadModel.create();
    this.users = QuadModel.create();
    this.getRulesData();
    QuadModel ontologyNormalized = QuadModel.create();
    for (final QuadModel model : new QuadModel[] { this.ontology, this.rules, this.users }) {
      for (final Statement s : model) {
        ontologyNormalized.add(s.getSubject(), s.getPredicate(), s.getObject());
      }
    }

    QuadModel userData = QuadModel.create();

    if (aggregate) {

      for (String m : userMeals) {
        QuadModel singleMealData = this.getUserData(userId, m);
        for (final Statement s : singleMealData) {
          userData.add(s.getSubject(), s.getPredicate(), s.getObject());
        }
      }
      Engine engine = new Engine(ontologyNormalized, false, e);
      ts = System.currentTimeMillis();
      QuadModel inf = engine.process(userData);

      ValueFactory factory = SimpleValueFactory.getInstance();
      IRI rulesContext = factory.createIRI("vc:CXT-VIOLATIONS");
      Iterator<Statement> iS = inf.iterator();
      while (iS.hasNext()) {
        Statement s = iS.next();
        // logger.info("Inferred statement: {} {} {}",
        // s.getSubject().toString(), s.getPredicate().toString(),
        // s.getObject().toString());
        if (s.getSubject().toString().contains("violation_")) {
          this.c.add(s, rulesContext);
        }
      }

      long time = System.currentTimeMillis() - ts;
      int infTriples = inf.size();
      int infViolations = inf.filter(null, RDF.TYPE, VC.VIOLATION).size();
      logger.info("Reasoner inferred {} triples in {} ms.", infTriples, time);
      logger.info("Reasoner found {} violations.", infViolations);

    } else {

      for (String m : userMeals) {
        logger.info("Reasoning parameters - userId: {} - mealId: {}", userId, m);
        // Engine engine = new Engine(this.ontology, false);
        Engine engine = new Engine(ontologyNormalized, false, e);
        
        // QuadModel userData = this.getUserData(userId, mealId);
        userData = this.getUserData(userId, m);
        // QuadModel userData = loadRDF("safetest-userdata.rdf");

        ts = System.currentTimeMillis();
        QuadModel inf = engine.process(userData);

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI rulesContext = factory.createIRI("vc:CXT-VIOLATIONS");
        Iterator<Statement> iS = inf.iterator();
        while (iS.hasNext()) {
          Statement s = iS.next();
          if (s.getSubject().toString().contains("violation_")) {
            this.c.add(s, rulesContext);
          }
        }

        long time = System.currentTimeMillis() - ts;
        int infTriples = inf.size();
        int infViolations = inf.filter(null, RDF.TYPE, VC.VIOLATION).size();
        logger.info("Reasoner inferred {} triples in {} ms.", infTriples, time);
        logger.info("Reasoner found {} violations.", infViolations);
      }
    }
  }
  */
  
  
  
  
  /*
  private void getRulesData() {

    ValueFactory factory = SimpleValueFactory.getInstance();

    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    IRI rulesContext = factory.createIRI("vc:CXT-RULES");
    RepositoryResult<Statement> rulesStatements = this.c.getStatements(null, null, null, false, rulesContext);
    logger.info("Extracting rules from the {} context.", rulesContext.toString());
    while (rulesStatements.hasNext()) {
      this.rules.add((Statement) rulesStatements.next());
      extractedStatements++;
    }
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        rulesContext.toString(), System.currentTimeMillis() - ts);

    extractedStatements = 0;
    ts = System.currentTimeMillis();
    IRI intervalsContext = factory.createIRI("vc:CXT-INTERVALS");
    RepositoryResult<Statement> intervalsStatements = this.c.getStatements(null, null, null, false, intervalsContext);
    logger.info("Extracting intervals from the {} context.", intervalsContext.toString());
    while (intervalsStatements.hasNext()) {
      this.rules.add((Statement) intervalsStatements.next());
      extractedStatements++;
    }
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        intervalsContext.toString(), System.currentTimeMillis() - ts);

    extractedStatements = 0;
    ts = System.currentTimeMillis();
    IRI usersContext = factory.createIRI("vc:CXT-USERS");
    logger.info("Extracting users from the {} context.", usersContext.toString());
    RepositoryResult<Statement> usersStatements = this.c.getStatements(null, null, null, false, usersContext);
    while (usersStatements.hasNext()) {
      this.users.add((Statement) usersStatements.next());
      extractedStatements++;
    }
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        usersContext.toString(), System.currentTimeMillis() - ts);

  }
  */
  
  
  // TODO: caricare le regole ogni volta
  /*
  private QuadModel getUserData(String userId, String mealId) {
    QuadModel userData = QuadModel.create();

    ValueFactory factory = SimpleValueFactory.getInstance();
    long ts = System.currentTimeMillis();
    int extractedStatements = 0;
    //IRI usersContext = factory.createIRI("vc:" + userId + "-" + mealId);
    IRI usersContext = factory.createIRI("vc:" + userId + "-CONSUMED-MEALS");
    logger.info("Extracting user data from the {} context.", usersContext.toString());
    RepositoryResult<Statement> usersStatements = this.c.getStatements(null, null, null, false, usersContext);
    while (usersStatements.hasNext()) {
      Statement s = (Statement) usersStatements.next();
      if(s.getSubject().stringValue().contains(mealId.substring(5)) || s.getObject().stringValue().contains(mealId)) {
        userData.add(s.getSubject(), s.getPredicate(), s.getObject());
        // userData.add((Statement) usersStatements.next());
        //logger.info("{} {} {}", s.getSubject(), s.getPredicate(), s.getObject());
        extractedStatements++;
      }
    }
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        usersContext.toString(), System.currentTimeMillis() - ts);
    return userData;
  }
  */
  
  
  
  
  public synchronized QuadModel getUserData(String eventType, ArrayList<String> eventId, String userId) {
    QuadModel userData = QuadModel.create();

    //ValueFactory factory = SimpleValueFactory.getInstance();
    long ts = System.currentTimeMillis();
    int extractedStatements = 0;
    //IRI usersContext = factory.createIRI("vc:" + userId + "-" + mealId);
    //IRI usersContext = factory.createIRI(VC.PREFIX + ":" + userId + "-" + eventType);
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();  
    } catch (Exception e) {
      logger.info("Error retrieving the connection. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    GraphQuery graphQuery = null;
    
    //String query = VC.SPARQL_PREFIX + 
    //    "CONSTRUCT { ?s ?p ?o . } WHERE{?s ?p ?o . FILTER (?s = " + VC.PREFIX + ":" + userId + ")}";
    String query = VC.SPARQL_PREFIX + " CONSTRUCT { ?s ?p ?o . ?a ?p1 ?o1 . } " + 
        "WHERE{?s ?p ?o . ?s vc:belongsProfile ?a . ?a ?p1 ?o1 . FILTER (?s = " + VC.PREFIX + ":" + userId + ")}";
    
    logger.info(query);
    try {  
      graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setIncludeInferred(false);
      try (GraphQueryResult result = graphQuery.evaluate()) {
        while (result.hasNext()) {
          Statement s = result.next();
          userData.add(s.getSubject(), s.getPredicate(), s.getObject());
          //System.out.println(s.getSubject() + " - " + s.getPredicate() + " - " + s.getObject());
          extractedStatements++;
        }
        result.close();
      }
    } catch (Exception e) {
      logger.info("Error during the retrieval of the data for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    
    
    query = new String("");
    for (String event : eventId) {
      
      if(eventType.compareTo("MEAL") == 0) {
        query = VC.SPARQL_PREFIX +
            "CONSTRUCT { " + VC.PREFIX + ":" + eventType + "-" + event + " ?p1 ?o1 . " + 
            "?s2 ?p2 " + VC.PREFIX + ":" + eventType + "-" + event + " . ?f ?p3 ?o3 . } " +
            "WHERE { GRAPH ?g { " +
            "{ " + VC.PREFIX + ":" + eventType + "-" + event + " ?p1 ?o1 . } UNION " +
            "{ ?s2 ?p2 " + VC.PREFIX + ":" + eventType + "-" + event + " . } UNION " +
            "{ " + VC.PREFIX + ":" + eventType + "-" + event + " vc:hasConsumedFood ?f . ?f ?p3 ?o3 . } " +
            "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-CONSUMED-" + eventType + "\")) }";
        
        logger.info(query);
        
        try {
          graphQuery = conn.prepareGraphQuery(query);
          graphQuery.setIncludeInferred(false);
          try (GraphQueryResult result = graphQuery.evaluate()) {
            // we just iterate over all solutions in the result...
            while (result.hasNext()) {
              Statement s = result.next();
              userData.add(s.getSubject(), s.getPredicate(), s.getObject());
              //System.out.println(s.getSubject() + " - " + s.getPredicate() + " - " + s.getObject());
              extractedStatements++;
            }
            result.close();
          }
        } catch (Exception e) {
          logger.info("Error during the retrieval of the meals for the user {}. {} {}", userId, e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
          if(conn != null) {
            conn.close();
          }
        }
        
        
      } else if(eventType.compareTo("ACTIVITY") == 0) {
        /*
        query = VC.SPARQL_PREFIX +
            "SELECT ?s ?p ?o " +
            "WHERE { ?s ?p ?o " +
            "FILTER (contains(str(?s), \"" + eventId + "\")) }";
        */
        
        query = VC.SPARQL_PREFIX +
            "CONSTRUCT { " + VC.PREFIX + ":" + event + " ?p1 ?o1 . } " +
            "WHERE { GRAPH ?g { " +
            "{ " + VC.PREFIX + ":" + event + " ?p1 ?o1 . }}}";
        
        logger.info(query);
        
        /*
        TupleQuery tupleQuery = VC.c.prepareTupleQuery(QueryLanguage.SPARQL, query);
        tupleQuery.setIncludeInferred(false);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
          while (result.hasNext()) {
            Statement s = (Statement) result.next();
            userData.add(s.getSubject(), s.getPredicate(), s.getObject());
            //System.out.println(s.getSubject() + " - " + s.getPredicate() + " - " + s.getObject());
            extractedStatements++;
          }
        }
        */
        try {
          graphQuery = conn.prepareGraphQuery(query);
          graphQuery.setIncludeInferred(false);
          logger.info("Executing query.");
          try (GraphQueryResult result = graphQuery.evaluate()) {
            // we just iterate over all solutions in the result...
            logger.info("Query executed.");
            while (result.hasNext()) {
              Statement s = result.next();
              userData.add(s.getSubject(), s.getPredicate(), s.getObject());
              //System.out.println(s.getSubject() + " - " + s.getPredicate() + " - " + s.getObject());
              extractedStatements++;
            }
            result.close();
          }
        } catch (Exception e) {
          logger.info("Error during the retrieval of the activities for the user {}. {} {}", userId, e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
          if(conn != null) {
            conn.close();
          }
        }
        
      }
    }

    /*
    logger.info(query);
    TupleQuery tupleQuery = VC.c.prepareTupleQuery(QueryLanguage.SPARQL, query);
    tupleQuery.setIncludeInferred(false);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value subject = bindingSet.getValue("subject");
        Value predicate = bindingSet.getValue("predicate");
        Value object = bindingSet.getValue("object");
        System.out.println(subject.stringValue() + " - " + predicate.stringValue() + " - " + object.stringValue());
      }
    }
    */
    
    

    
    
    //logger.info("Extracting user data from the {} context.", usersContext.toString());
    //RepositoryResult<Statement> usersStatements = VC.c.getStatements(null, null, null, false, usersContext);
    /*
    while (usersStatements.hasNext()) {
      Statement s = (Statement) usersStatements.next();
      if(s.getSubject().stringValue().contains(eventId.substring(5)) || s.getObject().stringValue().contains(eventId)) {
        userData.add(s.getSubject(), s.getPredicate(), s.getObject());
        // userData.add((Statement) usersStatements.next());
        //logger.info("{} {} {}", s.getSubject(), s.getPredicate(), s.getObject());
        extractedStatements++;
      }
    }
    */
    
    
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        eventType + "-" + userId, System.currentTimeMillis() - ts);
    
    conn.close();
    return userData;
  }
  
  
  
  public synchronized ArrayList<String> getEventIdWeekly() {
    ArrayList<String> events = new ArrayList<String>();
    /* TODO: continuare qui */
    
    return events;
  }
  
  
  
  public synchronized QuadModel getUserDataWeekly(String eventType, String userId) {
    QuadModel userData = QuadModel.create();

    long ts = System.currentTimeMillis() - (86400000 * 7);
    int extractedStatements = 0;  
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();  
    } catch (Exception e) {
      logger.info("Error retrieving the connection. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    GraphQuery graphQuery = null;
    
    //String query = VC.SPARQL_PREFIX + " CONSTRUCT { ?s ?p ?o . } WHERE{?s ?p ?o . FILTER (?s = " + VC.PREFIX + ":" + userId + ")}";
    String query = VC.SPARQL_PREFIX + " CONSTRUCT { ?s ?p ?o . ?a ?p1 ?o1 . } " + 
                   "WHERE{?s ?p ?o . ?s vc:belongsProfile ?a . ?a ?p1 ?o1 . FILTER (?s = " + VC.PREFIX + ":" + userId + ")}";
    logger.info(query);
    try {  
      graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setIncludeInferred(false);
      try (GraphQueryResult result = graphQuery.evaluate()) {
        while (result.hasNext()) {
          Statement s = result.next();
          userData.add(s.getSubject(), s.getPredicate(), s.getObject());
          extractedStatements++;
        }
        result.close();
      }
    } catch (Exception e) {
      logger.info("Error during the retrieval of the data for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    
    
    query = new String("");
    //for (String event : eventId) {
      
      if(eventType.compareTo("MEAL") == 0) {
        /*
        query = VC.SPARQL_PREFIX +
            "CONSTRUCT { " + VC.PREFIX + ":" + eventType + "-" + event + " ?p1 ?o1 . " + 
            "?s2 ?p2 " + VC.PREFIX + ":" + eventType + "-" + event + " . ?f ?p3 ?o3 . } " +
            "WHERE { GRAPH ?g { " +
            "{ " + VC.PREFIX + ":" + eventType + "-" + event + " ?p1 ?o1 . } UNION " +
            "{ ?s2 ?p2 " + VC.PREFIX + ":" + eventType + "-" + event + " . } UNION " +
            "{ " + VC.PREFIX + ":" + eventType + "-" + event + " vc:hasConsumedFood ?f . ?f ?p3 ?o3 . } " +
            "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-CONSUMED-" + eventType + "\")) }";
        */
        query = VC.SPARQL_PREFIX +
            "CONSTRUCT { ?meal ?p1 ?o1 . ?s2 ?p2 ?meal . ?f ?p3 ?o3 . } " +
            "WHERE { GRAPH ?g { " +
            "{ " +
              "?meal ?p1 ?o1 . ?s2 ?p2 ?meal . ?meal vc:hasConsumedFood ?f . ?f ?p3 ?o3 . ?meal vc:hasTimestamp ?timestamp . " +
              "FILTER (?timestamp > " + ts + ") " +
            "} " +
            "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-CONSUMED-" + eventType + "\")) }";
        
        logger.info(query);
        
        try {
          graphQuery = conn.prepareGraphQuery(query);
          graphQuery.setIncludeInferred(false);
          try (GraphQueryResult result = graphQuery.evaluate()) {
            while (result.hasNext()) {
              Statement s = result.next();
              userData.add(s.getSubject(), s.getPredicate(), s.getObject());
              extractedStatements++;
            }
            result.close();
          }
        } catch (Exception e) {
          logger.info("Error during the retrieval of the meals for the user {}. {} {}", userId, e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
          if(conn != null) {
            conn.close();
          }
        }
        
        
      } else if(eventType.compareTo("ACTIVITY") == 0) {
        
        query = VC.SPARQL_PREFIX +
            "CONSTRUCT { ?s1 ?p1 ?o1 . } " +
            "WHERE { GRAPH ?g { " +
            "{ " +
              "?s1 a " + VC.PREFIX + ":PerformedActivity . ?s1 " + VC.PREFIX + ":activityStartTimestamp ?timestamp . ?s1 ?p1 ?o1 . " +
              "FILTER (?timestamp > " + ts + ") " +
              "} " +
          "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-PERFORMED-ACTIVITIES\")) }";
                
        logger.info(query);

        try {
          graphQuery = conn.prepareGraphQuery(query);
          graphQuery.setIncludeInferred(false);
          logger.info("Executing query.");
          try (GraphQueryResult result = graphQuery.evaluate()) {
            logger.info("Query executed.");
            while (result.hasNext()) {
              Statement s = result.next();
              userData.add(s.getSubject(), s.getPredicate(), s.getObject());
              extractedStatements++;
            }
            result.close();
          }
        } catch (Exception e) {
          logger.info("Error during the retrieval of the activities for the user {}. {} {}", userId, e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
          if(conn != null) {
            conn.close();
          }
        }
        
      }
    //}    
    
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        eventType + "-" + userId, System.currentTimeMillis() - ts);
    
    conn.close();
    return userData;
  }
  
  
  
  
  public synchronized QuadModel getGoalBasedUserData(String eventType, String userId, EngineMode mode) {
    QuadModel userData = QuadModel.create();
    
    int dayMultiplier = 7;
    if(mode == EngineMode.WEEKFOOD || mode == EngineMode.WEEKACTIVITY) dayMultiplier = 7;
    long ts = System.currentTimeMillis() - (86400000 * dayMultiplier);
    ArrayList<Pair> goalsTs = this.getLastUserGoalTimestamp(userId);
    if(goalsTs.size() == 0) {
      logger.info("User " + userId + " does not have active goals.");
      return null;
    }
    Pair cg = goalsTs.get(0);
    long goalOldTs = Long.parseLong(cg.getV());
    if(goalOldTs > ts) ts = goalOldTs;
    
    
    int extractedStatements = 0;

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();  
    } catch (Exception e) {
      logger.info("Error retrieving the connection. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    
    /* Get user profile data */
    GraphQuery graphQuery = null;
    String query = VC.SPARQL_PREFIX + " CONSTRUCT { ?s ?p ?o . ?a ?p1 ?o1 . } " + 
                   "WHERE{?s ?p ?o . ?s vc:belongsProfile ?a . ?a ?p1 ?o1 . FILTER (?s = " + VC.PREFIX + ":" + userId + ")}";
    logger.info(query);
    try {  
      graphQuery = conn.prepareGraphQuery(query);
      graphQuery.setIncludeInferred(false);
      try (GraphQueryResult result = graphQuery.evaluate()) {
        while (result.hasNext()) {
          Statement s = result.next();
          userData.add(s.getSubject(), s.getPredicate(), s.getObject());
          extractedStatements++;
        }
        result.close();
      }
    } catch (Exception e) {
      logger.info("Error during the retrieval of the data for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    
    
    query = new String("");
     
    if(eventType.compareTo("MEAL") == 0) {
      query = VC.SPARQL_PREFIX +
          "CONSTRUCT { ?meal ?p1 ?o1 . ?s2 ?p2 ?meal . ?f ?p3 ?o3 . } " +
          "WHERE { GRAPH ?g { " +
          "{ " +
            "?meal ?p1 ?o1 . ?s2 ?p2 ?meal . ?meal vc:hasConsumedFood ?f . ?f ?p3 ?o3 . ?meal vc:hasTimestamp ?timestamp . " +
            "FILTER (?timestamp > " + ts + ") " +
          "} " +
          "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-CONSUMED-" + eventType + "\")) }";
      
      logger.info(query);
      
      try {
        graphQuery = conn.prepareGraphQuery(query);
        graphQuery.setIncludeInferred(false);
        try (GraphQueryResult result = graphQuery.evaluate()) {
          while (result.hasNext()) {
            Statement s = result.next();
            userData.add(s.getSubject(), s.getPredicate(), s.getObject());
            extractedStatements++;
          }
          result.close();
        }
      } catch (Exception e) {
        logger.info("Error during the retrieval of the meals for the user {}. {} {}", userId, e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
      }
      
      
    } else if(eventType.compareTo("ACTIVITY") == 0) {
      
      query = VC.SPARQL_PREFIX +
          "CONSTRUCT { ?s1 ?p1 ?o1 . } " +
          "WHERE { GRAPH ?g { " +
          "{ " +
            "?s1 a " + VC.PREFIX + ":PerformedActivity . ?s1 " + VC.PREFIX + ":activityStartTimestamp ?timestamp . ?s1 ?p1 ?o1 . " +
            "FILTER (?timestamp > " + ts + ") " +
            "} " +
        "} FILTER (contains(str(?g), \"" + VC.PREFIX + ":" + userId + "-PERFORMED-ACTIVITIES\")) }";
              
      logger.info(query);

      try {
        graphQuery = conn.prepareGraphQuery(query);
        graphQuery.setIncludeInferred(false);
        logger.info("Executing query.");
        try (GraphQueryResult result = graphQuery.evaluate()) {
          logger.info("Query executed.");
          while (result.hasNext()) {
            Statement s = result.next();
            userData.add(s.getSubject(), s.getPredicate(), s.getObject());
            extractedStatements++;
          }
          result.close();
        }
      } catch (Exception e) {
        logger.info("Error during the retrieval of the activities for the user {}. {} {}", userId, e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
      }
      
    }
    
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        eventType + "-" + userId, System.currentTimeMillis() - ts);
    
    conn.close();

    return userData;
  }
  
  
  
  
  
  public synchronized ArrayList<Pair> getLastUserGoalTimestamp(String userId) {
    ArrayList<Pair> goalsLastTimestamps = new ArrayList<Pair>();
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();  
    } catch (Exception e) {
      logger.info("Error retrieving the connection. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    try {
      String queryString = VC.SPARQL_PREFIX + "SELECT ?goal (MAX(?timestamp) as ?ts) (COUNT(?goal) as ?c) WHERE { " +
                     VC.PREFIX + ":" + userId + " a vc:User . " +
                     VC.PREFIX + ":" + userId + " " + VC.PREFIX + ":belongsProfile ?assignedGoal . " +
                     "?assignedGoal " + VC.PREFIX + ":hasProfile ?goal ; " + 
                     VC.PREFIX + ":hasStartTimestamp ?timestamp . } GROUP BY(?goal)";
      
      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
  
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value goalId = bindingSet.getValue("goal");
          Value timestamp = bindingSet.getValue("ts");
          Value check = bindingSet.getValue("c");
          logger.info(check.stringValue());
          if(check.stringValue().compareTo("0") == 0) continue;
          Pair p = new Pair(goalId.stringValue().substring(goalId.stringValue().indexOf("#") + 1), timestamp.stringValue());
          goalsLastTimestamps.add(p);
        }
        result.close();
      }
    } catch(Exception e) {
      logger.info("Error in retrieving user data. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }
    
    return goalsLastTimestamps;
  }
  
  
  
  
  public ArrayList<String> getRules(String filter) {
    ArrayList<String> rules = new ArrayList<String>();
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();  
    } catch (Exception e) {
      logger.info("Error retrieving the connection. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    
    try {
      String queryString = VC.SPARQL_PREFIX + "SELECT ?ruleid WHERE { " +
                     "?rule a " + VC.PREFIX + ":MonitoringRule ; " +
                     VC.PREFIX + ":hasRuleId ?ruleId ; " + 
                     VC.PREFIX + ":hasOperator ?operator ; " +
                     VC.PREFIX + ":timing ?timing . " +
                     "FILTER (?operator = \"" + filter + "\") " +
                     "FILTER EXISTS {?rule " + VC.PREFIX + ":timing " + VC.PREFIX + ":" + this.timingType + "} ";
                     
      
      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
  
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value ruleId = bindingSet.getValue("rule");
          rules.add(ruleId.stringValue().substring(ruleId.stringValue().indexOf("#") + 1));        
        }
        result.close();
      }
    } catch(Exception e) {
      logger.info("Error in retrieving user data. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if (conn != null) {
        conn.close();
      }
    }
    
    return rules;
  }
  
  
  
  
  /*
  private ArrayList<String> getUserMeals(String userId, long ts) {
    ArrayList<String> meals = new ArrayList<String>();

    String query = VC.SPARQL_PREFIX + 
        "SELECT ?meal WHERE { " + 
        VC.PREFIX + ":" + userId + VC.PREFIX + ":consumed ?meal . " + 
        "?meal " + VC.PREFIX + ":hasTimestamp ?timestamp . " + 
        "FILTER(?timestamp > " + ts + ") }";

    logger.info(query);
    TupleQuery tupleQuery = this.c.prepareTupleQuery(QueryLanguage.SPARQL, query);
    tupleQuery.setIncludeInferred(false);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value meal = bindingSet.getValue("meal");
        meals.add(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
      }
    }
    // this.c.close();

    return meals;
  }
  */
  
  
  
  
  
  protected void finalize() {
    this.engine = null;
    /*
    this.ontology.clear();
    this.rules.clear();
    this.users.clear();
    */
    this.userData.clear();
    logger.info("Reasoning operation terminated. {}", Thread.currentThread());
  }
  
}
