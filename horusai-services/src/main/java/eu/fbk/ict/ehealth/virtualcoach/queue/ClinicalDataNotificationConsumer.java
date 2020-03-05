package eu.fbk.ict.ehealth.virtualcoach.queue;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Meal;
import eu.fbk.ict.ehealth.virtualcoach.helis.manager.MealDataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.ClinicalDataNotification;
import eu.fbk.ict.ehealth.virtualcoach.trec.ClinicalData;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMeal;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealData;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealDataFoods;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMetadata;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.MealRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

//import eu.fbk.conf.ClientParameters;

public class ClinicalDataNotificationConsumer {

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

  private ClinicalDataNotificationConsumer() {}
  

  public static void init() {
    logger = LoggerFactory.getLogger(ClinicalDataNotificationConsumer.class);
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
      
      logger.info("Initializing Consumer on {}, {}, {}, {}", host, exchange, vhost, routingKey);
      
      
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

    logger.info("Connecting Consumer on {}, {}, {}, {}", host, exchange, vhost, routingKey);    
    
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
    
    /**
     * Get the stored meal from Clinical Data service
     */
    
    try {
      
      ClinicalDataNotification v = gson.fromJson(message.toString(), ClinicalDataNotification.class);
      //logger.info("Notification received: " + gson.toJson(v));

      String mealData = ClinicalData.getMeal(v.getPatient_id(), v.get_id());
      logger.info("CDSMeal: " + mealData);
      CDSMeal req = gson.fromJson(mealData, CDSMeal.class);
      //logger.info("CDSMeal cast: " + gson.toJson(req));
      
      /**
       * Store the meal in HeliS and run the reasoner
       */
      
      /*
      {
        "userId":"Blu4L3D8J1buZ4eI8ig4W9DU89zGDq1P6I0TeSo5efCBf90Dvn",
        "mode":"save",
        "timestamp":1510412785980,
        "meals":[{
          "mealType":"Snack",
          "mealId":"test-1510412785980",
          "timestamp":1510412785980,
          "foods":[
            {"mealId":"test-1510412785980","timestamp":1510412785980,"foodId":"FOOD-419","foodQuantity":10.0}]}
        ]}
       */
      
      ArrayList<Meal> meals = new ArrayList<Meal>();
      CDSMetadata metadata = req.getMetadata();
      MealRequest mr = new MealRequest();
      mr.setMode("save");
      mr.setUserId(v.getPatient_id());
      
      if(String.valueOf(metadata.getStart_date()).compareTo("0") != 0) {
        mr.setTimestamp(String.valueOf(metadata.getStart_date()));
      } else if(String.valueOf(metadata.getEntry_date()).compareTo("0") != 0) {
        mr.setTimestamp(String.valueOf(metadata.getEntry_date()));
      } else {
        mr.setTimestamp(String.valueOf(System.currentTimeMillis()));
      }
      
      
      CDSMealData meal = req.getData();
      ArrayList<CDSMealDataFoods> foods = meal.getFoods();

      //ArrayList<String> mealIds = new ArrayList<String>();
      Meal m = new Meal();
      m.setMealId(req.get_id());
      m.setMealType(meal.getMeal_type().substring(0, 1).toUpperCase() + meal.getMeal_type().substring(1));
      m.setUserId(req.getPatient_id());

      for (CDSMealDataFoods f : foods) {
        m.addConsumedFood(f.getFood_id(), "", f.getQuantity());
      }
      meals.add(m);
      mr.setMeals(meals);
      MealDataManager mdm = new MealDataManager();
      mdm.manage(mr);
      
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