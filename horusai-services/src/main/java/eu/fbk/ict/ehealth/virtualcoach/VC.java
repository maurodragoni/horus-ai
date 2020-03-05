package eu.fbk.ict.ehealth.virtualcoach;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;



public class VC {

  private VC() {}
  
  
  /** Initialization flag **/
  private static boolean initialized = false;
  
  public static Repository r;
  private static RepositoryConnection c;  
  
  /** Recommended prefix for the vocabulary namespace */
  public static String PREFIX;

  /** Vocabulary namespace */
  public static String NAMESPACE = "http://www.fbk.eu/ontologies/virtualcoach#";
  
  /** SPARQL queries namespaces prefixes */
  public static String SPARQL_PREFIX;

  /** Immutable {@link Namespace} constant for the vocabulary namespace. */
  public static final Namespace NS = new SimpleNamespace(VC.PREFIX, VC.NAMESPACE);

  public static Calendar clock = new GregorianCalendar();
  
  public static HeLiSConfigurator config;
  
  
  //CLASSES

  
  /** Class vc:Meal. */
  public static final IRI MEAL = VC.createIRI("Meal");

  /** Class vc:Breakfast. */
  public static final IRI BREAKFAST = VC.createIRI("Breakfast");

  /** Class vc:Lunch. */
  public static final IRI LUNCH = VC.createIRI("Lunch");

  /** Class vc:Dinner. */
  public static final IRI DINNER = VC.createIRI("Dinner");

  /** Class vc:Snack. */
  public static final IRI SNACK = VC.createIRI("Snack");

  /** Class vc:ConsumedFood. */
  public static final IRI CONSUMED_FOOD = VC.createIRI("ConsumedFood");

  /** Class vc:Food. */
  public static final IRI FOOD = VC.createIRI("Food");

  /** Class vc:Nutrient. */
  public static final IRI NUTRIENT = VC.createIRI("Nutrient");

  /** Class vc:Profile. */
  public static final IRI PROFILE = VC.createIRI("Profile");

  /** Class vc:User. */
  public static final IRI USER = VC.createIRI("User");

  /** Class vc:MonitoringRule. */
  public static final IRI MONITORING_RULE = VC.createIRI("MonitoringRule");

  /** Class vc:Violation. */
  public static final IRI VIOLATION = VC.createIRI("Violation");

  /** Class vc:ValueInterval. */
  public static final IRI VALUE_INTERVAL = VC.createIRI("ValueInterval");

  /** Class vc:ViolationInterval. */
  public static final IRI VIOLATION_INTERVAL = VC.createIRI("ViolationInterval");

  /** Class vc:Timespan. */
  public static final IRI TIMESPAN = VC.createIRI("Timespan");

  /** Class vc:Now. */
  public static final IRI NOW = VC.createIRI("Now");
  
  /** Class vc:Day. */
  public static final IRI DAY = VC.createIRI("Day");

  /** Class vc:Week. */
  public static final IRI WEEK = VC.createIRI("Week");


  // PROPERTIES

  /** Property vc:hasUser. */
  public static final IRI HAS_USER = VC.createIRI("HAS_USER");


  // FUNCTIONS

  /** Function vc:mintViolation. */
  public static final IRI MINT_VIOLATION = VC.createIRI("mintViolation");

  /** Function vc:mintNutrient. */
  public static final IRI MINT_NUTRIENT = VC.createIRI("mintNutrient");

  /** Function vc:mintEntityType. */
  public static final IRI MINT_ENTITYTYPE = VC.createIRI("mintEntityType");
  
  /** Function vc:computeTimestamp. */
  public static final IRI COMPUTE_TIMESTAMP = VC.createIRI("computeTimestamp");
  
  /** Function vc:computeTimestamp. */
  public static final IRI COMPUTE_VIOLATIONLEVEL = VC.createIRI("computeViolationLevel");

  /** Function vc:seq. */
  public static final IRI SEQ = VC.createIRI("seq");
  
  
  
  public enum EngineMode {
    CHECK, QB, PORTION, DAY, WEEK, DAYFOOD, DAYACTIVITY, WEEKFOOD, WEEKACTIVITY    
  };
  
  
  
  
  /** Login Tokens */
  public static String trecToken;
  public static String trecUserId;
  
  
  
  private static IRI createIRI(final String localName) {
    //return null;
    return SimpleValueFactory.getInstance().createIRI(VC.NAMESPACE, localName);
  }

  
  public static void init() {
    if(initialized == true) return;
    
    try {
      /*
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream prpFile = classLoader.getResourceAsStream("helis.properties");
      Properties prp = new Properties();
      prp.load(prpFile);

      String rdfServer = prp.getProperty("virtualcoach.repository.url");
      String repositoryId = prp.getProperty("virtualcoach.repository.id");
      r = new HTTPRepository(rdfServer, repositoryId);
      r.initialize();
      c = r.getConnection();

      NAMESPACE = new String(prp.getProperty("virtualcoach.namespace.vc") + "#");
      PREFIX = new String(prp.getProperty("virtualcoach.namespace.vc.prefix"));
      
      SPARQL_PREFIX = new String("PREFIX " + PREFIX + ": <" + NAMESPACE + "> " +
                                 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                                 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
      */
      
      config = ConfigFactory.create(HeLiSConfigurator.class, System.getenv());
      
      String rdfServer = config.getVirtualCoachRepositoryUrl();
      String repositoryId = config.getVirtualCoachRepositoryId();
      r = new HTTPRepository(rdfServer, repositoryId);
      r.initialize();
      //c = r.getConnection();

      NAMESPACE = new String(config.getVirtualCoachNamespaceVC() + "#");
      PREFIX = config.getVirtualCoachNamespacePrefix();
      
      SPARQL_PREFIX = new String("PREFIX " + PREFIX + ": <" + NAMESPACE + "> " +
                                 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                                 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
      
      
      //getTreCCredentials();
      
      initialized = true;
          
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public static RepositoryConnection getConnection() {
    return c;
  }
  
  /*
  public static void getTreCCredentials() {
    TreCToken tt = null;
    Gson gson = new Gson();
    HashMap<String, String> header = new HashMap<String, String>();
    header.put("cache-control", "no-cache");
    header.put("content", "application/json");
    header.put("content-type", "application/json");

    String jsonPars = "{\"username\":\"DragoTest\",\"password\":\"DragoTest\"}";
    String url = "https://docker-ehealth.fbk.eu/development/api/password-authenticator/v1/login";
    try {
      
      String credentials = HttpCURLClient.post(url, header, jsonPars);
      tt = gson.fromJson(credentials, TreCToken.class);
      
      trecToken = tt.getJWT();
      trecUserId = tt.getTrecUserId();
      
      
      //trecToken = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJON1BudG9adVBMcDhEWTJPaEhrUDc4YlAyWDgxalVYOHE3ZDMyYmVldzNFejRjODQ0UHRNVzU2cmMzNDJPNnZRV2w1dHlsNEtTWG4zUDQweTBSNTdvS1RPSW9jbUJHZDEybTMyIiwiaWF0IjoxNTA3OTAxNDIwLCJleHAiOjE1MDc5MDIzMjAsInN1YiI6IjdtQ0MyQTBtbUlabTBrQjJ6NUM5enZCTFJnQTY4cE1Jb2FZeHM0cEJ5YkNYVTZodFRpIiwiaXNzIjoiQVBTUyIsImF1ZCI6IlRyZUMiLCJUcmVjVXNlclR5cGUiOiJ1c2VyIiwibG9hIjoxfQ.b8J41UtBWuve70azkY3Pq2No3ZmZztSkjtZvdXTjG2fKIDWhExmzlND2QkVJRSUZf9sEIHg3b57ITog7Ya_gOZWEGhOFQHViu6Zr8T6-JRI8aX7wwTrc0eTj65yUuTl7ozSsIPRMm7Hgs7NqcQSVDi64V9s6qEH1AEQh7ioqyuDqzdSBADCw0_nnv0Busl028V98enXfCgR1bJurgd-SvO0pqHXx1MSf52-G_InVHTbtLMHc_CELBtXR7Fq4FyIYTv_JGIcCJkRyb8Qci7fq5yasNMhBMkl57FuAJbqNlLO9wnii9vyXaZdSlvCiA0Df__drZorxZYODsC0XbekcRY3MT7HolDWhCvepCTsc4Pdl4qsWTbpVlLMWq7gE62Ezi-stoLuEP6AOvB8JTDwnax3zfN9VqE2cfO24xP_w6eN6lL8u_6w6OVzxzZry9SQr5LK7BfBGck7-s4PlsooD_ElqN66XHjMCRZX1-UwxnX3EeGGrf_96m7rLn1G2qDsqTD56MhkHhWULsoIY9cJjRAek5Mu5oQdUK4FfLJtU_S2IjYB5_Ggzq1T8zw_KTB9Y2uNTyfjMNV0e78_7QONc6SZFzo8ce8fYGNi65xRYf3GrxOZbM6IGtBetChIzWIdEiq7Cc4pwuI8hjj7y0VyahXMiv6Sxd_WNXz3KZV4L4wk";
      //trecUserId = "7mCC2A0mmIZm0kB2z5C9zvBLRgA68pMIoaYxs4pBybCXU6htTi";
     
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  */
}
