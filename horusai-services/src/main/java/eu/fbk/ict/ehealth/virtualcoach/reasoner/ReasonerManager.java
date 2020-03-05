package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.VC.EngineMode;
import eu.fbk.rdfpro.util.QuadModel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.*;


/**
 * Servlet implementation class Reasoner
 */
public class ReasonerManager {

  private static Logger logger;

  private static QuadModel ontology;
  private static QuadModel rules;
  private static QuadModel users;
  public static Engine preEngine;
  
  private static ValueFactory factory = SimpleValueFactory.getInstance();
  
  public static ExecutorService threadPool;
  public static ScheduledExecutorService scheduler;
  public static ScheduledFuture<?> batchDailyHandler;
  public static ScheduledFuture<?> batchWeeklyHandler;
  
  public static HeLiSConfigurator prp;

  
  private ReasonerManager() { }

  
  
  public static void init() {
    logger = LoggerFactory.getLogger(ReasonerManager.class); 
    logger.info("Reasoner initialization...");
    
    prp = VC.config;
    
    threadPool = new ThreadPoolExecutor(2, 20, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    scheduler = Executors.newScheduledThreadPool(1);
    
    ontology = QuadModel.create();
    rules = QuadModel.create();
    users = QuadModel.create();
    
    if(prp.getVirtualCoachOntologyVersion().compareTo("1") == 0) {
      getOntologyStatements();
      getRulesStatements();
      //getUsersStatements();
    } else if(prp.getVirtualCoachOntologyVersion().compareTo("2") == 0) {
      getOntologyModuleStatements();
    }
    preInitReasoningEngine();
    
    HeLiSConfigurator hc = VC.config;
    if(hc.getVirtualCoachUseReasonerBatch().compareTo("1") == 0) {
      initScheduler();
    }
  }
  
  
  
  /**
   * Loads ontology statements.
   */
  private static void getOntologyModuleStatements() {
    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    RepositoryConnection conn = null;
   
    IRI coreContext = factory.createIRI("vc:CXT-CORE");
    IRI activityContext = factory.createIRI("vc:CXT-ACTIVITY");
    
    try {
      conn = VC.r.getConnection();
      RepositoryResult<Statement> coreStatements = conn.getStatements(null, null, null, false, coreContext);
      logger.info("Extracting statements from the {} context.", coreContext.toString());
      while (coreStatements.hasNext()) {
        ontology.add((Statement) coreStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", 
                  extractedStatements, coreContext.toString(), System.currentTimeMillis() - ts);
      coreStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of the {} context. {}", coreContext.toString(), e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    try {
      conn = VC.r.getConnection();
      extractedStatements = 0;
      ts = System.currentTimeMillis();
      RepositoryResult<Statement> activityStatements = conn.getStatements(null, null, null, false, activityContext);
      logger.info("Extracting statements from the {} context.", activityContext.toString());
      while (activityStatements.hasNext()) {
        ontology.add((Statement) activityStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
          activityContext.toString(), System.currentTimeMillis() - ts);
      activityStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of the {} context. {}", activityContext.toString(), e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }
  
  
  
  
  /**
   * Loads ontology statements.
   */
  private static void getOntologyStatements() {
    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    IRI ontologyContext = factory.createIRI("vc:CXT-ONTOLOGY");
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      RepositoryResult<Statement> ontologyStatements = conn.getStatements(null, null, null, false, ontologyContext);
      logger.info("Extracting statements from the {} context.", ontologyContext.toString());
      while (ontologyStatements.hasNext()) {
        ontology.add((Statement) ontologyStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", 
                  extractedStatements, ontologyContext.toString(), System.currentTimeMillis() - ts);
      ontologyStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of ontology statements. {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }
  
  
  
  /**
   * Loads rules statements.
   */
  private static void getRulesStatements() {
    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    IRI rulesContext = factory.createIRI("vc:CXT-RULES");
    RepositoryConnection conn = null;
    
    try {
      conn = VC.r.getConnection();
      RepositoryResult<Statement> rulesStatements = conn.getStatements(null, null, null, false, rulesContext);
      logger.info("Extracting rules from the {} context.", rulesContext.toString());
      while (rulesStatements.hasNext()) {
        rules.add((Statement) rulesStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
          rulesContext.toString(), System.currentTimeMillis() - ts);
      rulesStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of rules statements. {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    try {
      conn = VC.r.getConnection();
      extractedStatements = 0;
      ts = System.currentTimeMillis();
      IRI intervalsContext = factory.createIRI("vc:CXT-GOALS");
      RepositoryResult<Statement> intervalsStatements = conn.getStatements(null, null, null, false, intervalsContext);
      logger.info("Extracting intervals from the {} context.", intervalsContext.toString());
      while (intervalsStatements.hasNext()) {
        rules.add((Statement) intervalsStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
          intervalsContext.toString(), System.currentTimeMillis() - ts);
      intervalsStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of intervals statements. {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    try {
      conn = VC.r.getConnection();
      extractedStatements = 0;
      ts = System.currentTimeMillis();
      IRI intervalsContext = factory.createIRI("vc:CXT-INTERVALS");
      RepositoryResult<Statement> intervalsStatements = conn.getStatements(null, null, null, false, intervalsContext);
      logger.info("Extracting intervals from the {} context.", intervalsContext.toString());
      while (intervalsStatements.hasNext()) {
        rules.add((Statement) intervalsStatements.next());
        extractedStatements++;
      }
      logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
          intervalsContext.toString(), System.currentTimeMillis() - ts);
      intervalsStatements.close();
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of intervals statements. {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }
  
  
  
  /**
   * Loads users statements.
   */
  /*
  private static void getUsersStatements() {
    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    IRI usersContext = factory.createIRI("vc:CXT-USERS");
    logger.info("Extracting users from the {} context.", usersContext.toString());
    RepositoryResult<Statement> usersStatements = VC.c.getStatements(null, null, null, false, usersContext);
    while (usersStatements.hasNext()) {
      users.add((Statement) usersStatements.next());
      extractedStatements++;
    }
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        usersContext.toString(), System.currentTimeMillis() - ts);
  }
  */
  
  
  /**
   * Creates the reasoning engine bean that is stored in memory and cloned every time a reasoning operation has to be performed.
   */
  private static void preInitReasoningEngine() {
    QuadModel ontologyNormalized = QuadModel.create();
    for (final QuadModel model : new QuadModel[] { ontology, rules, users }) {
      for (final Statement s : model) {
        ontologyNormalized.add(s.getSubject(), s.getPredicate(), s.getObject());
      }
    }
    preEngine = new Engine(ontologyNormalized);
    preEngine.preInit();
  }
  
  
  
  
  public static void performLiveReasoning(String eventType, ArrayList<String> eventId, String userId) {
    
    if(prp.getVirtualCoachUseReasonerLive().compareTo("1") == 0)
    {    
      logger.info("***********************************************************");
      logger.info("Reasoning on data provided by {} on event {}", userId, eventId);
  
      ReasonerThread rt = new ReasonerThread(preEngine, eventType, eventId, userId);
      rt.setReasoningType("LIVE");
      rt.setTimingType("LIVE");
      rt.run();
      //rt.finalize();
      logger.info("Memory usage {}", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      rt = null;
      System.gc();
      logger.info("Memory usage {}", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      
      
      /*
      long weekCheckTs = getUserWeeklyCheckTs(userId);
      if(weekCheckTs < Long.MAX_VALUE) {
        long weeklyCheck = (((System.currentTimeMillis() - weekCheckTs) / 86400000) % 7);
        logger.info("Weekly check result {} {} {}.", System.currentTimeMillis(), weekCheckTs, weeklyCheck);
        if(weeklyCheck == 0) {
          rt = new ReasonerThread(preEngine, eventType, userId, EngineMode.WEEK);
          rt.run();
        }
      }
      */
    }
    
    
    
    /*
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(new ReasonerThread(VC.c, preEngine, eventType, eventId, userId));
    executor.shutdown();
    logger.info("Shutting down thread.");
    try {
      if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
        logger.info("Waiting for shutting down thread.");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }
    */
    
    /*
    Thread rt = new Thread(new ReasonerThread(VC.c, preEngine, eventType, eventId, userId));
    rt.start();
    */
    /*
    try {
      rt.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    */
  }

  
  
  protected static void initScheduler() {
    
    final Runnable batchDailyReasoner = new Runnable() {
      public void run() { performBatchReasoning("DAY"); }
    };
    
    final Runnable batchWeeklyReasoner = new Runnable() {
      public void run() { performBatchReasoning("WEEK"); }
    };
    
    Calendar c = new GregorianCalendar();
    long currentTimestamp = c.getTimeInMillis();
    c.set(Calendar.HOUR_OF_DAY, 22);
    long lateTimestamp = c.getTimeInMillis();
    long delay = lateTimestamp - currentTimestamp;
    if(delay < 0) delay += 86400000;
    
    
    batchDailyHandler = scheduler.scheduleAtFixedRate(batchDailyReasoner, (delay / 1000), 86400, SECONDS);
    //batchDailyHandler = scheduler.scheduleAtFixedRate(batchDailyReasoner, 60, 3600, SECONDS);
    logger.info("Daily reasoner set to start in {} seconds.", (delay / 1000));
    
    batchWeeklyHandler = scheduler.scheduleAtFixedRate(batchWeeklyReasoner, ((delay / 1000) + 1800), 86400, SECONDS);
    //batchWeeklyHandler = scheduler.scheduleAtFixedRate(batchWeeklyReasoner, 720, 3600, SECONDS);
    logger.info("Weekly reasoner set to start in {} seconds.", ((delay / 1000) + 1800));
    
    
    //batchDailyHandler = scheduler.scheduleAtFixedRate(batchDailyReasoner, 10, 86400, SECONDS);
    //logger.info("Daily reasoner set to start in {} seconds.", (delay / 1000));
    
    //batchWeeklyHandler = scheduler.scheduleAtFixedRate(batchWeeklyReasoner, ((delay / 1000) + 1800) , 86400, SECONDS);
    //logger.info("Weekly reasoner set to start in {} seconds.", ((delay / 1000) + 1800));
  }
  
  
  
  
  
  protected static void performBatchReasoning(String mode) {
    
    logger.info("Getting eligible users for reasoning operations.");
    ArrayList<String> users = null;
    if(mode.compareTo("WEEK") == 0) {
      users = getUsersByActiveGoalTimestamp((86400000 * 7));
    } else {
      users = getUsers();
    }
    
    logger.info("Running scheduled reasoner in {} mode.", mode);
    
    //users.clear();
    //users.add("6FsX9CQn90TDj60KAUGE40SwdshylW5VXaZWf6MTsNUhhz1xjV");
    
    for(String userId : users) {
      ReasonerThread rt; 
      
      EngineMode m = EngineMode.DAYFOOD;
      if(mode.compareTo("WEEK") == 0) m = EngineMode.WEEKFOOD;
      
      EngineMode a = EngineMode.DAYACTIVITY;
      if(mode.compareTo("WEEK") == 0) a = EngineMode.WEEKACTIVITY;
      
      String timing = "Day";
      if(mode.compareTo("WEEK") == 0) timing = "Week";
      
      //m = EngineMode.CHECK;
      logger.info("Reasoning over food data of user {}.", userId);
      rt = new ReasonerThread(preEngine, "MEAL", userId, m);
      rt.setReasoningType("SUMMARY");
      rt.setTimingType(timing);
      rt.run();
      rt = null;
      
      
      logger.info("Reasoning over activity data of user {}.", userId);
      rt = new ReasonerThread(preEngine, "ACTIVITY", userId, a);
      rt.setReasoningType("SUMMARY");
      rt.setTimingType(timing);
      rt.run();
      rt = null;
      
    }
    
  }

  
  
  private static long getUserWeeklyCheckTs(String userId) {
    
    long startTs = Long.MAX_VALUE;
    String query = VC.SPARQL_PREFIX + "SELECT ?ts WHERE { "
        + "?user " + VC.PREFIX + ":registrationDate  ?ts . "
        + "?user " + VC.PREFIX + ":hasUserId \"" + userId + "\" . "
        + "}";
    
    logger.info(query);
    RepositoryConnection conn = VC.getConnection();
    try {
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value userTs = bindingSet.getValue("ts");
          if(userTs != null) {
            startTs = Long.parseLong(userTs.stringValue());
          }
        }
        result.close();
      }
      conn.close();
      
      
      if(startTs == Long.MAX_VALUE) {
        
        query = VC.SPARQL_PREFIX + "SELECT (MIN(?mt) AS ?mts) (MIN(?at) AS ?ats)  WHERE { "
            + "OPTIONAL { ?user " + VC.PREFIX + ":consumed ?meal . ?meal " + VC.PREFIX + ":hasTimestamp ?mt . } "
            + "OPTIONAL { ?activity " + VC.PREFIX + ":hasUserId ?user . ?activity " + VC.PREFIX + ":activityStartTimestamp ?at . } "
            + "?user " + VC.PREFIX + ":hasUserId \"" + userId + "\" . "
            + "}";
        
        logger.info(query);
        conn = VC.getConnection();
        tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
        tupleQuery.setIncludeInferred(false);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
          while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value mealTs = bindingSet.getValue("mts");
            Value activityTs = bindingSet.getValue("ats");
            
            long mts = Long.MAX_VALUE;
            long ats = Long.MAX_VALUE;
            if(mealTs != null) {
              mts = Long.parseLong(mealTs.stringValue());
            }
            if(activityTs != null) {
              ats = Long.parseLong(activityTs.stringValue());
            }
            
            if(mts < startTs) startTs = mts;
            if(ats < mts) startTs = ats;
          }
          result.close();
        }
        conn.close();
      }
            
    } catch (Exception e) {
        logger.info("Error during the retrieval of users list. {} {}", e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
    }
    
    return startTs;
  }
  
  
  
  
  private static ArrayList<String> getUsers() {
    ArrayList<String> users = new ArrayList<String>();
    String query = VC.SPARQL_PREFIX + " SELECT ?user WHERE { ?user rdf:type vc:User . }";

    logger.info(query);
    RepositoryConnection conn = VC.r.getConnection();
    try {
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value user = bindingSet.getValue("user");
          users.add(user.stringValue().substring(user.stringValue().indexOf("#") + 1));
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
        logger.info("Error during the retrieval of users list. {} {}", e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
    }
    
    return users;
  }
  
  
  
  private static ArrayList<String> getUsersByActiveGoalTimestamp(long timespan) {
    ArrayList<String> users = new ArrayList<String>();
    long currentTimestamp = System.currentTimeMillis();
    String query = VC.SPARQL_PREFIX + " SELECT ?user WHERE { "
        + " ?user rdf:type vc:User ; vc:belongsProfile ?g. "
        + " ?g rdf:type vc:AssignedProfile ; vc:hasStartTimestamp ?ts . "
        + "FILTER((" + currentTimestamp + " - ?ts) > " + timespan + ") . "
        + "}";

    logger.info(query);
    RepositoryConnection conn = VC.r.getConnection();
    try {
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value user = bindingSet.getValue("user");
          users.add(user.stringValue().substring(user.stringValue().indexOf("#") + 1));
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
        logger.info("Error during the retrieval of users list. {} {}", e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
        if(conn != null) {
          conn.close();
        }
    }
    
    return users;
  }
  
  
  
  
  
  
  //TODO: caricare le regole ogni volta
  
  
  
  /*
  PREFIX vc: <http://www.fbk.eu/ontologies/virtualcoach#> 
  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
  
  CONSTRUCT {
      vc:MEAL-test010 ?p1 ?o1 .
      ?s2 ?p2 vc:MEAL-010 .
      ?f ?p3 ?o3 .
  }
  WHERE {
      GRAPH ?g 
      {
          {
              vc:MEAL-test010 ?p1 ?o1 .
          }
          UNION
          {
              ?s2 ?p2 vc:MEAL-test010 .
          }
          UNION
          {
              vc:MEAL-test010 vc:hasConsumedFood ?f .
              ?f ?p3 ?o3 .
          }
      }
      FILTER (contains(str(?g), "vc:demohelis-CONSUMED-MEALS"))
  }
  */
  
  
  
  /*
  private QuadModel loadRDF(final String location) {
    final long ts = System.currentTimeMillis();
    final QuadModel model = QuadModel.create();
    final RDFSource source = RDFSources.read(true, true, null, null, null, true, location);
    source.emit(RDFHandlers.wrap(model), 1);
    logger.info("Loaded {} statements from {} in {} ms", model.size(), location, System.currentTimeMillis() - ts);
    return model;
  }
  */

}
