package eu.fbk.ict.ehealth.virtualcoach;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

/**
 * Extra environment variable used by this microservice.
 *
 * @author Mauro Dragoni (dragoni@fbk.eu).
 */
@Config.Sources({
  //Read configuration from Docker secrets
  "file:/run/secrets/rabbitmq_configuration"
})
public interface HeLiSConfigurator extends Config {

  // Basic settings
  String VIRTUALCOACH_USE_TREC = "USE_TREC";
  String VIRTUALCOACH_USE_RABBIT = "USE_RABBIT";
  String VIRTUALCOACH_USE_REASONER = "USE_REASONER";
  String VIRTUALCOACH_USE_REASONER_LIVE = "USE_REASONER_LIVE";
  String VIRTUALCOACH_USE_REASONER_BATCH = "USE_REASONER_BATCH";
  String VIRTUALCOACH_USE_GOALCHECK_LIVE = "USE_GOALCHECK_LIVE";
  String VIRTUALCOACH_USE_GOALCHECK_BATCH = "USE_GOALCHECK_BATCH";
  
  // GraphDB settings
  String VIRTUALCOACH_REPOSITORY_URL = "GRAPHDB_URL";
  String VIRTUALCOACH_REPOSITORY_ID = "REPOSITORY_ID";
  String VIRTUALCOACH_REPOSITORY_VC = "REPOSITORY_VC";
  String VIRTUALCOACH_NAMESPACE_VC_PREFIX = "NAMESPACE_VC_PREFIX";
  String VIRTUALCOACH_ONTOLOGY_VERSION = "ONTOLOGY_VERSION";
  
  // User Profile settings
  String VIRTUALCOACH_USER_DEFAULT_PROFILES = "USER_DEFAULT_PROFILES";
  
  // TreC Environment settings
  String VIRTUALCOACH_HELIS_TREC_USER = "TREC_USER";

  // RabbitMQ connection settings
  String VIRTUALCOACH_MQ_HOST = "RABBITMQ_HOST";
  String VIRTUALCOACH_MQ_VHOST = "RABBITMQ_VIRTUALHOST";
  String VIRTUALCOACH_MQ_USER = "RABBITMQ_USERNAME";
  String VIRTUALCOACH_MQ_PASSWORD = "RABBITMQ_PASSWORD";

  // RabbitMQ exchanges and routing keys
  String VIRTUALCOACH_MQ_EXCHANGE = "MQ_EXCHANGE";
  String VIRTUALCOACH_MQ_ROUTING_KEY = "MQ_ROUTING_KEY";
  String VIRTUALCOACH_MQ_ROUTING_KEY_VIOLATIONS = "MQ_ROUTING_KEY_VIOLATIONS";
  String VIRTUALCOACH_MQ_ROUTING_KEY_GOALS = "MQ_ROUTING_KEY_GOALS";
  String VIRTUALCOACH_MQ_ROUTING_KEY_MEALS = "MQ_ROUTING_KEY_MEALS";
  String VIRTUALCOACH_MQ_ROUTING_KEY_USERS = "MQ_ROUTING_KEY_USERS";
  String VIRTUALCOACH_MQ_ROUTING_KEY_PERSEO_NOTIFICATIONS = "MQ_ROUTING_KEY_PERSEO_NOTIFICATIONS";

  String VIRTUALCOACH_MQ_EXCHANGE_CLINICALDATA = "MQ_EXCHANGE_CLINICALDATA";
  String VIRTUALCOACH_MQ_EXCHANGE_PERSONALDATA = "MQ_EXCHANGE_PERSONALDATA";
  String VIRTUALCOACH_MQ_ROUTING_KEY_CLINICATDATA_NOTIFICATIONS = "MQ_ROUTING_KEY_CLINICATDATA_NOTIFICATIONS";
  String VIRTUALCOACH_MQ_ROUTING_KEY_CLINICATDATA_ACTIVITY_NOTIFICATIONS = "MQ_ROUTING_KEY_CLINICATDATA_ACTIVITY_NOTIFICATIONS";

  
  
  
  /* Basic settings */
  @Key(VIRTUALCOACH_USE_TREC)
  @DefaultValue("0")
  String getVirtualCoachUseTrec();
  
  @Key(VIRTUALCOACH_USE_RABBIT)
  @DefaultValue("0")
  String getVirtualCoachUseRabbit();
  
  @Key(VIRTUALCOACH_USE_REASONER)
  @DefaultValue("1")
  String getVirtualCoachUseReasoner();
  
  @Key(VIRTUALCOACH_USE_REASONER_LIVE)
  @DefaultValue("0")
  String getVirtualCoachUseReasonerLive();
  
  @Key(VIRTUALCOACH_USE_REASONER_BATCH)
  @DefaultValue("0")
  String getVirtualCoachUseReasonerBatch();
  
  @Key(VIRTUALCOACH_USE_GOALCHECK_LIVE)
  @DefaultValue("0")
  String getVirtualCoachUseGoalCheckLive();
  
  @Key(VIRTUALCOACH_USE_GOALCHECK_BATCH)
  @DefaultValue("0")
  String getVirtualCoachUseGoalCheckBatch();
  
  
  
  
  /* GraphDB settings */
  @Key(VIRTUALCOACH_REPOSITORY_URL)
  //@DefaultValue("http://localhost:7200")
  @DefaultValue("https://graphdb.fbk.eu")
  String getVirtualCoachRepositoryUrl();

  /*
   * 1: all-in-one HeLiS ontology
   * 2: HeLiS v1.50 - modularized ontology
   */
  @Key(VIRTUALCOACH_ONTOLOGY_VERSION)
  @DefaultValue("2")
  String getVirtualCoachOntologyVersion();
  
  @Key(VIRTUALCOACH_REPOSITORY_ID)
  //* Repositories uses the triplestore loader v1 (all-in-one HeLiS ontology) */
  //@DefaultValue("key-to-health")
  //@DefaultValue("salute-plus-development")
  //@DefaultValue("demo-helis")
  //@DefaultValue("test")
  //@DefaultValue("inmp")
  //@DefaultValue("helis")
  /* Repositories uses the triplestore loader v2 (HeLiS v1.50 - modularized ontology) */
  @DefaultValue("helis-puffbot")
  String getVirtualCoachRepositoryId();

  @Key(VIRTUALCOACH_REPOSITORY_VC)
  @DefaultValue("http://www.fbk.eu/ontologies/virtualcoach")
  String getVirtualCoachNamespaceVC();

  @Key(VIRTUALCOACH_NAMESPACE_VC_PREFIX)
  @DefaultValue("vc")
  String getVirtualCoachNamespacePrefix();
  
  
  
  
  
  /* User Profile settings */
  @Key(VIRTUALCOACH_USER_DEFAULT_PROFILES)
  @DefaultValue("SALUSPLUS")
  String getVirtualCoachUserDefaultProfiles();
  
  
  
  
  
  /* TreC Environment settings */
  @Key(VIRTUALCOACH_HELIS_TREC_USER)
  @DefaultValue("Helis")
  String getVirtualCoachTreCUser();

  
  
  
  
  /* RabbitMQ connection settings */
  @Key(VIRTUALCOACH_MQ_HOST)
  @DefaultValue("localhost")
  //@DefaultValue("mobsmq.fbk.eu")
  String getVirtualCoachMqHost();

  @Key(VIRTUALCOACH_MQ_VHOST)
  //@DefaultValue("trec")
  @DefaultValue("perseo")
  String getVirtualCoachMqVHost();

  @Key(VIRTUALCOACH_MQ_USER)
  //@DefaultValue("developer")
  @DefaultValue("perkapp")
  String getVirtualCoachMqUser();

  @Key(VIRTUALCOACH_MQ_PASSWORD)
  //@DefaultValue("rabbitTRECdeveloper!")
  @DefaultValue("p3rk4pp")
  String getVirtualCoachMqPwd();

  
  
  
  
  /* RabbitMQ exchanges and routing keys */
  @Key(VIRTUALCOACH_MQ_EXCHANGE)
  //@DefaultValue("salusplus")
  @DefaultValue("perseo")
  String getVirtualCoachMqExchange();
  
  @Key(VIRTUALCOACH_MQ_ROUTING_KEY)
  @DefaultValue("entry.perseo.violations.demo")
  String getVirtualCoachMqRoutingKey();

  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_VIOLATIONS)
  //@DefaultValue("entry.perseo.lifestyle.violations")
  @DefaultValue("perseo.saluteplus.violations")
  String getVirtualCoachMqRoutingKeyViolations();

  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_GOALS)
  @DefaultValue("entry.perseo.lifestyle.goals")
  String getVirtualCoachMqRoutingKeyGoals();

  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_MEALS)
  @DefaultValue("entry.perseo.lifestyle.meals")
  String getVirtualCoachMqRoutingKeyMeals();
  
  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_USERS)
  @DefaultValue("helis.saluteplus.users")
  String getVirtualCoachMqRoutingKeyUsers();
  
  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_PERSEO_NOTIFICATIONS)
  @DefaultValue("helis.saluteplus.notifications")
  String getVirtualCoachMqRoutingKeyPerseoNotifications();

  @Key(VIRTUALCOACH_MQ_EXCHANGE_CLINICALDATA)
  @DefaultValue("clinical_events")
  String getVirtualCoachMqExchangeClinicalData();
  
  @Key(VIRTUALCOACH_MQ_EXCHANGE_PERSONALDATA)
  @DefaultValue("personal_data_events")
  String getVirtualCoachMqExchangePersonalData();

  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_CLINICATDATA_NOTIFICATIONS)
  @DefaultValue("observation/meal/1.#")
  String getVirtualCoachMqRoutingKeyClinicalDataNotifications();
  
  @Key(VIRTUALCOACH_MQ_ROUTING_KEY_CLINICATDATA_ACTIVITY_NOTIFICATIONS)
  @DefaultValue("observation/activity/1.#")
  String getVirtualCoachMqRoutingKeyClinicalDataActivityNotifications();

  
  

  /*
  #virtualcoach.repository.url=http://localhost:8180/rdf4j-server/
  virtualcoach.repository.url=http://localhost:7200
  virtualcoach.repository.id=demo-helis
  virtualcoach.namespace.vc=http://www.fbk.eu/ontologies/virtualcoach
  virtualcoach.namespace.vc.prefix=vc

  virtualcoach.mq.host=mobsmq.fbk.eu
  virtualcoach.mq.exchange=salusplus
  #virtualcoach.mq.exchange=lifestyle
  virtualcoach.mq.vhost=perseodev
  virtualcoach.mq.user=helis
  virtualcoach.mq.pwd=h3l1s
  virtualcoach.mq.routingKey=entry.perseo.violations.demo
  #virtualcoach.mq.routingKey.violations=entry.perseo.lifestyle.violations
  #virtualcoach.mq.routingKey.meals=entry.perseo.lifestyle.meals
  virtualcoach.mq.routingKey.violations=entry.perseo.salusplus.violations
  virtualcoach.mq.routingKey.goals=entry.perseo.salusplus.goals
  virtualcoach.mq.routingKey.meals=entry.perseo.salusplus.meals

  virtualcoach.mq.exchange.clinicaldata=clinical_events
  virtualcoach.mq.routingKey.clinicatdatanotifications=observation/meal/1.#
  */






  default void validate() throws Exception {

    /*
    if (getMailBasePath() == null || getMailBasePath().equals("")) {
      throw new ValidationException(MAIL_BASE_PATH + " env variable must be set.");
    }
    if (getMailBasePath().endsWith("/")) {
      throw new ValidationException(MAIL_BASE_PATH + " must not end with \"/\".");
    }

    if (getMailServerUrl() == null || getMailServerUrl().equals("")) {
      throw new ValidationException(MAIL_SERVER_URL + " env variable must be set.");
    }

    if (getMailServerPort() < 0) {
      throw new ValidationException(MAIL_SERVER_PORT + " value must be greater than zero.");
    }

    if (getMailFromAddress() == null || getMailFromAddress().equals("")) {
      throw new ValidationException(MAIL_FROM_NAME + " value must be set.");
    }
    */
  }


  /**
   * Factory for this configuration.
   *
   * @return Factory.
   */
  static HeLiSConfigurator factory() {
    // Create a new configuration from ENV variables
    HeLiSConfigurator config = ConfigFactory.create(HeLiSConfigurator.class, System.getenv());

    // validate the configuration and return it
    //config.validate();

    return config;
  }

}
