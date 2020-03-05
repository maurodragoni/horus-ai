package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.VC.EngineMode;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Violation;
import eu.fbk.ict.ehealth.virtualcoach.queue.Publisher;
import eu.fbk.rdfpro.util.QuadModel;

/**
 * Servlet implementation class Reasoner
 */
public class Reasoner extends HttpServlet {
  
  private static final long serialVersionUID = 1L;

  private static Logger logger;
  private Properties prp;
  
  /*
  private Repository r;
  private RepositoryConnection c;
  private String nsVC;
  private String nsVCPrefix;
  */
  
  private QuadModel ontology;
  private QuadModel rules;
  private QuadModel users;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public Reasoner() {
    super();
    VC.init();
  }

  
  
  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(Reasoner.class);
    
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream prpFile = classLoader.getResourceAsStream("helis.properties");
      this.prp = new Properties();
      this.prp.load(prpFile);

      /*
      String rdfServer = this.prp.getProperty("virtualcoach.repository.url");
      String repositoryId = this.prp.getProperty("virtualcoach.repository.id");
      this.r = new HTTPRepository(rdfServer, repositoryId);
      this.r.initialize();
      this.c = this.r.getConnection();

      this.nsVC = this.prp.getProperty("virtualcoach.namespace.vc");
      this.nsVCPrefix = this.prp.getProperty("virtualcoach.namespace.vc.prefix");

      VC.init();
      */
      this.ontology = QuadModel.create();
      this.initializeReasoner();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    logger.info("Request received by the Reasoner");
    String mode = request.getParameter("mode");
    boolean force = false;
    if (request.getParameter("force") != null) {
      force = Boolean.valueOf(request.getParameter("force"));
    }

    if (mode.compareTo("init") == 0) {
      this.initializeReasoner();
    } else if (mode.compareTo("singlemeal") == 0) {
      String userId = request.getParameter("userId");
      String mealId = request.getParameter("mealId");
      this.runSingleMealReasoning(userId, mealId);
      this.sendNotifications(userId, mealId);
    } else if (mode.compareTo("daily") == 0) {
      this.runDailyReasoning(request, force);
    } else if (mode.compareTo("weekly") == 0) {
      this.runWeeklyReasoning(request, force);
    }
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  private void initializeReasoner() {

    logger.info("Reasoner initialization...");
    int extractedStatements = 0;
    ValueFactory factory = SimpleValueFactory.getInstance();
    long ts = System.currentTimeMillis();

    IRI ontologyContext = factory.createIRI("vc:CXT-ONTOLOGY");
    RepositoryResult<Statement> ontologyStatements = VC.getConnection().getStatements(null, null, null, false, ontologyContext);
    logger.info("Extracting statements from the {} context.", ontologyContext.toString());
    while (ontologyStatements.hasNext()) {
      this.ontology.add((Statement) ontologyStatements.next());
      extractedStatements++;
    }
    ontologyStatements.close();
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        ontologyContext.toString(), System.currentTimeMillis() - ts);

  }

  // Cancellare violazioni esistenti (soprattutto quelle relative al giorno ed
  // alla settimana)
  private void runSingleMealReasoning(String userId, String mealId) {
    //Calendar c = new GregorianCalendar();

    /* Monitors potential violations associated with Meal upper limits */
    logger.info("Monitoring single meal.");
    //long ts = c.getTimeInMillis() - 36000000;
    //ArrayList<String> userMeals = this.getUserMeals(userId, ts);
    ArrayList<String> userMeals = new ArrayList<String>();
    userMeals.add("MEAL-" + mealId);
    //this.reasonOnData(userId, userMeals, EngineMode.QB, false);
    //this.reasonOnData(userId, userMeals, EngineMode.DAY, false);

    /* Monitors potential violations associated with Daily upper limits */
    /*
    logger.info("Monitoring current day meals.");
    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    ts = c.getTimeInMillis();
    userMeals = this.getUserMeals(userId, ts);
    this.reasonOnData(userId, userMeals, EngineMode.PORTION, true);
    */
    logger.info("***********************************************************");
  }

  
  private void runDailyReasoning(HttpServletRequest request, boolean force) {

    /* Gets all the active users of the system and analyze daily activities */

    Calendar c = new GregorianCalendar();
    int cY = c.get(Calendar.YEAR);
    int cM = c.get(Calendar.MONTH);
    int cD = c.get(Calendar.DAY_OF_MONTH);
    if (force) {
      if (request.getParameter("y") != null) {
        cY = Integer.valueOf(request.getParameter("y"));
      }
      if (request.getParameter("m") != null) {
        cM = Integer.valueOf(request.getParameter("m"));
      }
      if (request.getParameter("d") != null) {
        cD = Integer.valueOf(request.getParameter("d"));
      }
    }
    c.set(cY, cM, cD, 0, 0, 0);
    long ts = c.getTimeInMillis();
    ArrayList<String> users = this.getUsers();
    for (String userId : users) {
      logger.info("***********************************************************");
      ArrayList<String> userMeals = this.getUserMeals(userId, ts);
      //this.reasonOnData(userId, userMeals, EngineMode.DAY, true);
      logger.info("***********************************************************");
    }
  }

  
  
  private void runWeeklyReasoning(HttpServletRequest request, boolean force) {

    /* Gets all the active users of the system and analyze weekly activities */
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
      //this.reasonOnData(userId, userMeals, EngineMode.WEEK, true);
      logger.info("***********************************************************");
    }
  }

  
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
          VC.getConnection().add(s, rulesContext);
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
            VC.getConnection().add(s, rulesContext);
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
  
  
  private void sendNotifications(String userId, String mealId) {
    try (RepositoryConnection conn = VC.r.getConnection()) {

      String queryString = VC.SPARQL_PREFIX +
          "SELECT ?id ?user ?rule ?ruleId ?timestamp ?entityType ?entity ?timing ?quantity ?expectedQuantity ?priority " +
          "       ?level ?constraint ?history WHERE {" + 
          " ?id rdf:type " + VC.PREFIX + ":Violation ; " +
          VC.PREFIX + ":hasViolationUser " + VC.PREFIX + ":" + userId + " ; " + 
          VC.PREFIX + ":hasViolationRule ?rule ; " +
          VC.PREFIX + ":hasViolationRuleId ?ruleId ; " + 
          VC.PREFIX + ":hasTimestamp ?timestamp ; " +
          VC.PREFIX + ":hasViolationEntityType ?entityType ; " + 
          VC.PREFIX + ":hasViolationEntity ?entity ; " +
          VC.PREFIX + ":hasViolationQuantity ?quantity ; " + 
          VC.PREFIX + ":hasViolationExpectedQuantity ?expectedQuantity ; " +
          VC.PREFIX + ":hasViolationMeal " + VC.PREFIX + ":MEAL-" + mealId + " ; " + 
          VC.PREFIX + ":hasViolationPriority ?priority ; " +
          VC.PREFIX + ":hasViolationLevel ?level ; " + 
          VC.PREFIX + ":hasViolationConstraint ?constraint ; " +
          VC.PREFIX + ":hasViolationHistory ?history . } " + 
          "ORDER BY DESC(?timestamp) ?priority " + 
          "LIMIT 1";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      Violation v = new Violation();
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
          /*
           * if(timing == null) { timing =
           * bindingSet.getValue("timing").stringValue(); }
           */
          Value quantity = bindingSet.getValue("quantity");
          Value expectedQuantity = bindingSet.getValue("expectedQuantity");
          //Value meal = bindingSet.getValue("meal");
          Value priority = bindingSet.getValue("priority");
          Value level = bindingSet.getValue("level");
          Value constraint = bindingSet.getValue("constraint");
          Value history = bindingSet.getValue("history");

          
          v.setViolationId(id.stringValue().substring(id.stringValue().indexOf("#") + 1));
          v.setUser(userId.substring(userId.indexOf("#") + 1));
          v.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
          v.setRuleId(ruleId.stringValue());
          v.setTimestamp(timestamp.stringValue());
          v.setEntityType(entityType.stringValue());
          v.setEntity(entity.stringValue().substring(entity.stringValue().indexOf("#") + 1));
          // v.setTiming(timing.substring(timing.indexOf("#") + 1));
          v.setQuantity(quantity.stringValue());
          v.setExpectedQuantity(expectedQuantity.stringValue());
          //v.addMeal(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
          v.setPriority(priority.stringValue());
          v.setLevel(level.stringValue());
          v.setConstraint(constraint.stringValue());
          v.setHistory(history.stringValue());
          //this.violations.put(id.stringValue().substring(id.stringValue().indexOf("#") + 1), v);
          
        }
        result.close();
      }
      
      Gson gson = new Gson();
      try {
        logger.info(gson.toJson(v));
        Publisher.sendReasonerResult(gson.toJson(v));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  
  
  private void getRulesData() {

    ValueFactory factory = SimpleValueFactory.getInstance();

    int extractedStatements = 0;
    long ts = System.currentTimeMillis();
    IRI rulesContext = factory.createIRI("vc:CXT-RULES");
    RepositoryResult<Statement> rulesStatements = VC.getConnection().getStatements(null, null, null, false, rulesContext);
    logger.info("Extracting rules from the {} context.", rulesContext.toString());
    while (rulesStatements.hasNext()) {
      this.rules.add((Statement) rulesStatements.next());
      extractedStatements++;
    }
    rulesStatements.close();
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        rulesContext.toString(), System.currentTimeMillis() - ts);

    extractedStatements = 0;
    ts = System.currentTimeMillis();
    IRI intervalsContext = factory.createIRI("vc:CXT-INTERVALS");
    RepositoryResult<Statement> intervalsStatements = VC.getConnection().getStatements(null, null, null, false, intervalsContext);
    logger.info("Extracting intervals from the {} context.", intervalsContext.toString());
    while (intervalsStatements.hasNext()) {
      this.rules.add((Statement) intervalsStatements.next());
      extractedStatements++;
    }
    intervalsStatements.close();
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        intervalsContext.toString(), System.currentTimeMillis() - ts);

    extractedStatements = 0;
    ts = System.currentTimeMillis();
    IRI usersContext = factory.createIRI("vc:CXT-USERS");
    logger.info("Extracting users from the {} context.", usersContext.toString());
    RepositoryResult<Statement> usersStatements = VC.getConnection().getStatements(null, null, null, false, usersContext);
    while (usersStatements.hasNext()) {
      this.users.add((Statement) usersStatements.next());
      extractedStatements++;
    }
    usersStatements.close();
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        usersContext.toString(), System.currentTimeMillis() - ts);

  }

  
  // TODO: caricare le regole ogni volta

  private QuadModel getUserData(String userId, String mealId) {
    QuadModel userData = QuadModel.create();

    ValueFactory factory = SimpleValueFactory.getInstance();
    long ts = System.currentTimeMillis();
    int extractedStatements = 0;
    //IRI usersContext = factory.createIRI("vc:" + userId + "-" + mealId);
    IRI usersContext = factory.createIRI("vc:" + userId + "-CONSUMED-MEALS");
    logger.info("Extracting user data from the {} context.", usersContext.toString());
    RepositoryResult<Statement> usersStatements = VC.getConnection().getStatements(null, null, null, false, usersContext);
    while (usersStatements.hasNext()) {
      Statement s = (Statement) usersStatements.next();
      if(s.getSubject().stringValue().contains(mealId.substring(5)) || s.getObject().stringValue().contains(mealId)) {
        userData.add(s.getSubject(), s.getPredicate(), s.getObject());
        // userData.add((Statement) usersStatements.next());
        //logger.info("{} {} {}", s.getSubject(), s.getPredicate(), s.getObject());
        extractedStatements++;
      }
    }
    usersStatements.close();
    logger.info("Extracted {} statements from the {} context. Time {} ms.", extractedStatements,
        usersContext.toString(), System.currentTimeMillis() - ts);
    return userData;
  }

  
  
  
  private ArrayList<String> getUserMeals(String userId, long ts) {
    ArrayList<String> meals = new ArrayList<String>();

    String query = "PREFIX vc: <http://www.fbk.eu/ontologies/virtualcoach#> "
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + "SELECT ?meal " + "WHERE { " + " vc:" + userId
        + " vc:consumed ?meal . " + " ?meal vc:hasTimestamp ?timestamp . " + " FILTER(?timestamp > " + ts + ") " + "}";

    logger.info(query);
    TupleQuery tupleQuery = VC.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
    tupleQuery.setIncludeInferred(false);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value meal = bindingSet.getValue("meal");
        meals.add(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
      }
      result.close();
    }
    // this.c.close();

    return meals;
  }

  private ArrayList<String> getUsers() {
    ArrayList<String> users = new ArrayList<String>();
    String query = "PREFIX vc: <http://www.fbk.eu/ontologies/virtualcoach#> "
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + "SELECT ?user " + "WHERE { "
        + " ?user rdf:type vc:User . " + "}";

    logger.info(query);
    TupleQuery tupleQuery = VC.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);
    tupleQuery.setIncludeInferred(false);
    try (TupleQueryResult result = tupleQuery.evaluate()) {
      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value user = bindingSet.getValue("user");
        users.add(user.stringValue().substring(user.stringValue().indexOf("#") + 1));
      }
      result.close();
    }
    return users;
  }

  
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
