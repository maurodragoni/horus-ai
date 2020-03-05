package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC.EngineMode;
import eu.fbk.rdfpro.RuleEngine;
import eu.fbk.rdfpro.Ruleset;
import eu.fbk.rdfpro.util.QuadModel;

public final class Engine {

  private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);

  private Ruleset PRE_RULESET;
  private Ruleset RDFS_RULESET;
  private Ruleset VC1_RULESET;
  private Ruleset FOOD_QUANTITY_RULESET;
  private Ruleset FOOD_MONITORING_RULESET;
  private Ruleset FOOD_DAY_RULESET;
  private Ruleset FOOD_WEEK_RULESET;
  private Ruleset ACTIVITY_QUANTITY_RULESET;
  private Ruleset ACTIVITY_MONITORING_RULESET;
  private Ruleset ACTIVITY_DAY_RULESET;
  private Ruleset ACTIVITY_WEEK_RULESET;
  private Ruleset TEST_RULESET;

  private QuadModel ontology;
  private Ruleset ruleset;
  private RuleEngine engine;

  
  
  public Engine(final QuadModel ontology) {
    this.PRE_RULESET = null;
    this.RDFS_RULESET = null;
    this.VC1_RULESET = null;
    this.FOOD_QUANTITY_RULESET = null;
    this.FOOD_MONITORING_RULESET = null;
    this.FOOD_DAY_RULESET = null;
    this.FOOD_WEEK_RULESET = null;
    this.ACTIVITY_QUANTITY_RULESET = null;
    this.ACTIVITY_MONITORING_RULESET = null;
    this.ACTIVITY_DAY_RULESET = null;
    this.ACTIVITY_WEEK_RULESET = null;
    this.TEST_RULESET = null;

    this.ontology = ontology;
    this.ruleset = null;
    this.engine = null;
  }

  
  /*
  // public Engine(final QuadModel ontology, final QuadModel monitoringRules, final boolean explode) {
  public Engine(final QuadModel ontology, final boolean explode, EngineMode m) {

    this.PRE_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.pre.ttl").toString());
    this.RDFS_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.rdfs.ttl").toString());
    this.VC1_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.vc1.ttl").toString());
    
    this.FOOD_QUANTITY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.quantity.ttl").toString());
    this.FOOD_MONITORING_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.monitoring.ttl").toString());
    this.FOOD_DAY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.day.ttl").toString());
    this.FOOD_WEEK_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.week.ttl").toString());
    
    this.ACTIVITY_QUANTITY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.quantity.ttl").toString());
    this.ACTIVITY_MONITORING_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.monitoring.ttl").toString());
    this.ACTIVITY_DAY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.day.ttl").toString());
    this.ACTIVITY_WEEK_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.week.ttl").toString());
    
    this.TEST_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.test.ttl").toString());
        

    // Materialize RDFS inferences in input ontology
    long ts = System.currentTimeMillis();
    final QuadModel onto = QuadModel.create(ontology);
    // onto.addAll(monitoringRules);
    final int sizeBefore = onto.size();
    RuleEngine.create(Ruleset.RDFS).eval(onto);
    RuleEngine.create(PRE_RULESET).eval(onto);
    LOGGER.info("RDFS closure of ontology and monitoring rules ({} -> {} statements) computed in {} ms", sizeBefore,
        onto.size(), System.currentTimeMillis() - ts);

    /*
     * // Prepare ruleset ts = System.currentTimeMillis(); final Ruleset
     * ruleset; if (explode) { final Ruleset rdfsRulesetExploded =
     * RDFS_RULESET.getABoxRuleset(onto); final Ruleset vc1RulesetExploded =
     * VC1_RULESET.getABoxRuleset(onto); //ruleset =
     * Ruleset.merge(rdfsRulesetExploded, vc1RulesetExploded,
     * MANDATORY_QUANTITY_RULESET); ruleset = Ruleset.merge(rdfsRulesetExploded,
     * vc1RulesetExploded, MANDATORY_PORTION_RULESET); } else { //ruleset =
     * Ruleset.merge(RDFS_RULESET, VC1_RULESET, MANDATORY_QUANTITY_RULESET);
     * ruleset = Ruleset.merge(RDFS_RULESET, VC1_RULESET,
     * MANDATORY_PORTION_RULESET); }
     

    Ruleset ruleset = Ruleset.merge(RDFS_RULESET, VC1_RULESET);

    /*
     * Ruleset rdfsRuleset = this.RDFS_RULESET; Ruleset vc1Ruleset =
     * this.VC1_RULESET; if(explode) { rdfsRuleset =
     * RDFS_RULESET.getABoxRuleset(onto); vc1Ruleset =
     * VC1_RULESET.getABoxRuleset(onto); }
     

    if (m == EngineMode.QB) {
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              this.FOOD_QUANTITY_RULESET, this.ACTIVITY_QUANTITY_RULESET,
                              this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET);
    } else if (m == EngineMode.PORTION) {
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET);
    } else if (m == EngineMode.DAY) {
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET,
                              this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET,
                              this.FOOD_DAY_RULESET, this.ACTIVITY_DAY_RULESET);
    } else if (m == EngineMode.WEEK) {
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET,
                              this.FOOD_WEEK_RULESET, this.ACTIVITY_WEEK_RULESET);
    } else if (m == EngineMode.CHECK) {
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, this.TEST_RULESET);
    }

    LOGGER.info("Ruleset of {} rules prepared in {} ms", ruleset.getRules().size(), System.currentTimeMillis() - ts);
    LOGGER.debug("\n{}", ruleset);

    // Instantiate a rule engine using the exploded ruleset
    final RuleEngine engine = RuleEngine.create(ruleset);

    // Initialize engine
    this.ontology = onto;
    this.ruleset = ruleset;
    this.engine = engine;
  }
  */
  
  
  
  public void preInit() {
    this.PRE_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.pre.ttl").toString());
    this.RDFS_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.rdfs.ttl").toString());
    this.VC1_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.vc1.ttl").toString());
    
    this.FOOD_QUANTITY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.quantity.ttl").toString());
    this.FOOD_MONITORING_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.monitoring.ttl").toString());
    this.FOOD_DAY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.day.ttl").toString());
    this.FOOD_WEEK_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.food.week.ttl").toString());
    
    this.ACTIVITY_QUANTITY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.quantity.ttl").toString());
    this.ACTIVITY_MONITORING_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.monitoring.ttl").toString());
    this.ACTIVITY_DAY_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.day.ttl").toString());
    this.ACTIVITY_WEEK_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.activity.week.ttl").toString());
    
    this.TEST_RULESET = Ruleset.fromRDF(Engine.class.getResource("Engine.test.ttl").toString());

    // Materialize RDFS inferences in input ontology
    long ts = System.currentTimeMillis();
    final QuadModel onto = QuadModel.create(ontology);
    // onto.addAll(monitoringRules);
    final int sizeBefore = onto.size();
    RuleEngine.create(Ruleset.RDFS).eval(onto);
    RuleEngine.create(PRE_RULESET).eval(onto);
    LOGGER.info("RDFS closure of ontology and monitoring rules ({} -> {} statements) computed in {} ms", sizeBefore,
        onto.size(), System.currentTimeMillis() - ts);

    //this.ruleset = Ruleset.merge(RDFS_RULESET, VC1_RULESET);
    this.ontology = onto;
  }

  
  
  
  public void setEngineMode(EngineMode m) {
    long ts = System.currentTimeMillis();
    Ruleset ruleset = this.ruleset;
    
    LOGGER.info("Merging reasoner rule.");
    
    if (m == EngineMode.QB) {
      LOGGER.info("Merging QB rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              this.FOOD_QUANTITY_RULESET, this.ACTIVITY_QUANTITY_RULESET,
                              this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET);
    } else if (m == EngineMode.PORTION) {
      LOGGER.info("Merging PORTION rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET);
    } else if (m == EngineMode.DAY) {
      LOGGER.info("Merging DAY rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET,
                              this.FOOD_DAY_RULESET, this.ACTIVITY_DAY_RULESET);
    } else if (m == EngineMode.DAYFOOD) {
      LOGGER.info("Merging DAYFOOD rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.FOOD_MONITORING_RULESET, 
                              this.FOOD_DAY_RULESET);
    } else if (m == EngineMode.DAYACTIVITY) {
      LOGGER.info("Merging DAYACTIVITY rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.ACTIVITY_MONITORING_RULESET, 
                              this.ACTIVITY_DAY_RULESET);
    } else if (m == EngineMode.WEEK) {
      LOGGER.info("Merging WEEK rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.FOOD_MONITORING_RULESET, this.ACTIVITY_MONITORING_RULESET,
                              this.FOOD_WEEK_RULESET, this.ACTIVITY_WEEK_RULESET);
    } else if (m == EngineMode.WEEKFOOD) {
      LOGGER.info("Merging WEEKFOOD rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.FOOD_MONITORING_RULESET, 
                              this.FOOD_WEEK_RULESET);
    } else if (m == EngineMode.WEEKACTIVITY) {
      LOGGER.info("Merging WEEKACTIVITY rules.");
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, 
                              //this.ACTIVITY_MONITORING_RULESET, 
                              this.ACTIVITY_WEEK_RULESET);
    } else if (m == EngineMode.CHECK) {
      LOGGER.info("Merging CHECK rules.");
      //ruleset = Ruleset.merge(this.RDFS_RULESET, this.VC1_RULESET, this.TEST_RULESET);
      ruleset = Ruleset.merge(this.RDFS_RULESET, this.TEST_RULESET, this.VC1_RULESET);
    }

    LOGGER.info("Ruleset of {} rules prepared in {} ms", ruleset.getRules().size(), System.currentTimeMillis() - ts);
    LOGGER.debug("\n{}", ruleset);

    // Instantiate a rule engine using the exploded ruleset
    final RuleEngine engine = RuleEngine.create(ruleset);

    // Initialize engine
    this.ruleset = ruleset;
    this.engine = engine;
  }

  
  
  
  public QuadModel getOntology() {
    return this.ontology;
  }

  
  
  public Ruleset getRuleset() {
    return this.ruleset;
  }

  
  
  public Engine clone() {
    QuadModel o = QuadModel.create(this.ontology);
    //o.addAll(this.ontology);
    Engine e = new Engine(o);
    e.PRE_RULESET = this.PRE_RULESET;
    e.RDFS_RULESET = this.RDFS_RULESET;
    e.VC1_RULESET = this.VC1_RULESET;
    e.FOOD_QUANTITY_RULESET = this.FOOD_QUANTITY_RULESET;
    e.FOOD_MONITORING_RULESET = this.FOOD_MONITORING_RULESET;
    e.FOOD_DAY_RULESET = this.FOOD_DAY_RULESET;
    e.FOOD_WEEK_RULESET = this.FOOD_WEEK_RULESET;
    e.ACTIVITY_QUANTITY_RULESET = this.ACTIVITY_QUANTITY_RULESET;
    e.ACTIVITY_MONITORING_RULESET = this.ACTIVITY_MONITORING_RULESET;
    e.ACTIVITY_DAY_RULESET = this.ACTIVITY_DAY_RULESET;
    e.ACTIVITY_WEEK_RULESET = this.ACTIVITY_WEEK_RULESET;
    e.TEST_RULESET = this.TEST_RULESET;

    e.ruleset = null;
    e.engine = null;
    return e;
  }
  
  
  
  public QuadModel process(final QuadModel data) {

    // Take initial timestamp
    final long ts = System.currentTimeMillis();

    // Merge ontology and supplied ABox data, and take a timestamp
    final QuadModel model = QuadModel.create(this.ontology);
    for (final Statement s : data) {
      model.add(s.getSubject(), s.getPredicate(), s.getObject());
    }
    // model.addAll(data);

    final int sizeBefore = model.size();
    final long tsMerge = System.currentTimeMillis();

    // Apply exploded rules and take a timestamp
    this.engine.eval(model);
    final long tsClose = System.currentTimeMillis();

    // Isolate inferred statements and take a timestamp
    final QuadModel result = QuadModel.create();
    for (final Statement stmt : model) {

      /*
       * if(stmt.getSubject().toString().contains(
       * "Dinner-fb794275-589b25185ff22828e5000008-FOOD-059")) {
       * LOGGER.info("Statement: {} {} {}", stmt.getSubject().toString(),
       * stmt.getPredicate().toString(), stmt.getObject().toString()); }
       */

      if (!this.ontology.contains(stmt) && !data.contains(stmt)) {
        result.add(stmt);
        // LOGGER.info("Inferred Statement: {} {} {}",
        // stmt.getSubject().toString(), stmt.getPredicate().toString(),
        // stmt.getObject().toString());
      }
    }
    final long tsDiff = System.currentTimeMillis();

    // Log statistics
    LOGGER.info(
        "Inferred {} statements ({} -> {}) from {} data and {} ontology statements in {} ms "
            + "({} merge, {} close, {} diff)",
        result.size(), sizeBefore, model.size(), data.size(), this.ontology.size(), tsDiff - ts, tsMerge - ts,
        tsClose - tsMerge, tsDiff - tsClose);

    // Return inferred statements
    return result;
  }
}