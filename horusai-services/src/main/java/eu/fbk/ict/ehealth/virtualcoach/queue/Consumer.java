package eu.fbk.ict.ehealth.virtualcoach.queue;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.HttpCURLClient;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Meal;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Profile;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.User;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.ClinicalDataNotification;
import eu.fbk.ict.ehealth.virtualcoach.reasoner.ReasonerManager;
import eu.fbk.ict.ehealth.virtualcoach.trec.SessionManager;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMeal;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealData;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealDataFoods;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.UserProfileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//import eu.fbk.conf.ClientParameters;

public class Consumer {

  private static Channel channel = null;
  private static Connection connection = null;
  private static DefaultConsumer consumer;
  private static String host;
  private static String exchange;
  private static String vhost;
  private static String username;
  private static String password;
  private static String routingKey;
  private static Logger logger;

  private Consumer() {}
  

  public static void init() {
    logger = LoggerFactory.getLogger(Consumer.class);
    logger.info("Initializing RabbitMQ.");
    try {
      
      /*
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream prpFile = classLoader.getResourceAsStream("helis.properties");
      Properties prp = new Properties();
      prp.load(prpFile);

      host = prp.getProperty("virtualcoach.mq.host");
      exchange = prp.getProperty("virtualcoach.mq.exchange.clinicaldata");
      vhost = prp.getProperty("virtualcoach.mq.vhost");
      username = prp.getProperty("virtualcoach.mq.user");
      password = prp.getProperty("virtualcoach.mq.pwd");
      routingKey = prp.getProperty("virtualcoach.mq.routingKey.clinicatdatanotifications");
      */
      
      HeLiSConfigurator prp = VC.config;
      
      host = prp.getVirtualCoachMqHost();
      exchange = prp.getVirtualCoachMqExchangeClinicalData();
      vhost = prp.getVirtualCoachMqVHost();
      username = prp.getVirtualCoachMqUser();
      password = prp.getVirtualCoachMqPwd();
      routingKey = prp.getVirtualCoachMqRoutingKeyClinicalDataNotifications();
      
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  
  public static void connect() throws Exception, IOException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setVirtualHost(vhost);
    factory.setUsername(username);
    factory.setPassword(password);
    connection = factory.newConnection();
    channel = connection.createChannel();

    // channel.exchangeDeclare(Parameters._MQEXCHANGE, "topic");
    String queueName = channel.queueDeclare().getQueue();

    channel.queueBind(queueName, exchange, routingKey);

    logger.info(" [*] Waiting for messages.");

    consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {

        String message = new String(body, "UTF-8");
        /*
         * Object message = null; ByteArrayInputStream bis = new
         * ByteArrayInputStream(body); ObjectInputStream in = null; try { in =
         * new ObjectInputStream(bis); message = in.readObject(); } catch
         * (ClassNotFoundException e) { e.printStackTrace(); } finally { try {
         * if (in != null) { in.close(); } } catch (IOException ex) { // ignore
         * close exception } }
         */
        onMessage(message);
      }
    };
    // channel.basicConsume(queueName, true, consumer);
    channel.basicConsume(queueName, true, consumer);
  }

  
  
  
  
  public static void onMessage(Object message) {
    logger.info(message.toString());
    Gson gson = new Gson();
    // Violation v = gson.fromJson(message.toString(), Violation.class);
    // logger.info(gson.toJson(v));

    /**
     * Get the stored meal from Clinical Data service
     */
    HashMap<String, String> header = new HashMap<String, String>();

    try {
      header = new HashMap<String, String>();
      header.put("accept", "application/json");
      header.put("content-type", "application/json");
      //header.put("X-TokenTreC", VC.trecToken);
      header.put("X-TokenTreC", SessionManager.getTokenTrec().getJWT());

      logger.info("Version CIRFood-Demo");
      
      ClinicalDataNotification v = gson.fromJson(message.toString(), ClinicalDataNotification.class);
      String url = new String("https://docker-ehealth.fbk.eu/staging/api/clinical-data/v1/clinicaldata/" + v.document_type
                              + "/" + v.get_id());
      String result = HttpCURLClient.get(url, header, "");
      logger.info(result);

      /**
       * Check user existance in HeLiS, otherwise create it
       */
      UserProfileRequest ureq = new UserProfileRequest();
      ureq.setUserId(v.getPatient_id());
      ureq.setUsername("demohelis");
      ureq.setGender("M");
      ureq.setHeight(180);
      ureq.setWeight(78);
      ureq.setAge(347963400000L);
      ArrayList<Profile> profiles = new ArrayList<Profile>();
      Profile p = new Profile("DEMOPROFILE");
      p.setStartDate(347963400000L);
      profiles.add(p);
      ureq.setProfiles(profiles);
      //User u = new User(VC.trecUserId);
      User u = new User(SessionManager.getTokenTrec().getTrecUserId());
      u.populate(ureq);
      boolean createFlag = u.store();
      System.out.println(createFlag);

      /**
       * Store the meal in HeliS and run the reasoner
       */
      CDSMeal req = gson.fromJson(result, CDSMeal.class);
      //CDSMetadata metadata = req.getMetadata();
      CDSMealData meal = req.getData();
      ArrayList<CDSMealDataFoods> foods = meal.getFoods();

      ArrayList<String> mealIds = new ArrayList<String>();
      Meal m = new Meal();
      m.setMealId(req.get_id());
      m.setMealType(meal.getMeal_type().substring(0, 1).toUpperCase() + meal.getMeal_type().substring(1));
      m.setUserId(req.getPatient_id());

      for (CDSMealDataFoods f : foods) {
        m.addConsumedFood(f.getFood_id(), "", f.getQuantity());
      }
      m.check();
      m.store(v.getPatient_id());
      mealIds.add(m.getMealId());
      Publisher.sendMeal(gson.toJson(m));
      
      ReasonerManager.threadPool.submit(new Runnable() {
        @Override
        public void run() {
          ReasonerManager.performLiveReasoning("MEAL", mealIds, v.getPatient_id());
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  
  public static void closeConnection() {
    try {
      channel.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}