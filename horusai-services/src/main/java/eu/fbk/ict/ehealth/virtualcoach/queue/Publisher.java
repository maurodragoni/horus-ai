package eu.fbk.ict.ehealth.virtualcoach.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;


//import org.json.simple.JSONObject;


public class Publisher {
  
  private static Channel channel = null;
  private static Connection connection = null;
  private static String host;
  private static String exchange;
  private static String vhost;
  private static String username;
  private static String password;
  //private static String routingKey;
  private static String routingKeyViolations;
  private static String routingKeyGoals;
  private static String routingKeyMeals;
  private static String routingKeyUsers;
  private static Logger logger;
  
  private Publisher() {}
  
  
  public static void init() {
    logger = LoggerFactory.getLogger(Publisher.class);
    logger.info("Initializing RabbitMQ.");
    try {
      /*
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream prpFile = classLoader.getResourceAsStream("helis.properties");
      Properties prp = new Properties();
      prp.load(prpFile);
  
      host = prp.getProperty("virtualcoach.mq.host");
      exchange = prp.getProperty("virtualcoach.mq.exchange");
      vhost = prp.getProperty("virtualcoach.mq.vhost");
      username = prp.getProperty("virtualcoach.mq.user");
      password = prp.getProperty("virtualcoach.mq.pwd");
      routingKey = prp.getProperty("virtualcoach.mq.routingKey");
      routingKeyViolations = prp.getProperty("virtualcoach.mq.routingKey.violations");
      routingKeyGoals = prp.getProperty("virtualcoach.mq.routingKey.goals");
      routingKeyMeals = prp.getProperty("virtualcoach.mq.routingKey.meals");
      */
      
      HeLiSConfigurator prp = VC.config;
  
      host = prp.getVirtualCoachMqHost();
      exchange = prp.getVirtualCoachMqExchange();
      vhost = prp.getVirtualCoachMqVHost();
      username = prp.getVirtualCoachMqUser();
      password = prp.getVirtualCoachMqPwd();
      //routingKey = prp.getVirtualCoachMqRoutingKey();
      routingKeyViolations = prp.getVirtualCoachMqRoutingKeyViolations();
      routingKeyGoals = prp.getVirtualCoachMqRoutingKeyGoals();
      routingKeyMeals = prp.getVirtualCoachMqRoutingKeyMeals();
      routingKeyUsers = prp.getVirtualCoachMqRoutingKeyUsers();
      
      
      logger.info("Initializing Publisher on {}, {}, {} ", host, exchange, vhost);
      logger.info("Initializing Publisher routing keys {}, {}, {}, {}", 
                   routingKeyViolations, routingKeyGoals, routingKeyMeals, routingKeyUsers);
      
      
      
    } catch (Exception e) {
      logger.info("Problems during queue publisher initialization.");
      logger.info(Arrays.toString(e.getStackTrace()));
    }
  }
  
  
  
  public static void connect() throws Exception {
    logger.info("Connecting to the Queue Manager.");
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setVirtualHost(vhost);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setRequestedHeartbeat(30);
    factory.setConnectionTimeout(30000);
    Connection connection = factory.newConnection();
    channel = connection.createChannel();
    
    
    logger.info("Connecting Publisher on {}, {}, {} ", host, exchange, vhost);
    logger.info("Connecting Publisher routing keys {}, {}, {}, {}", 
                 routingKeyViolations, routingKeyGoals, routingKeyMeals, routingKeyUsers);
    
    
    //channel.exchangeDeclare(exchange, "topic", false);
  }

  
  
  public static void closeConnection() {
    try {
      channel.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  
  
  /*
  private static void send(String routingKey, String msg, AMQP.BasicProperties theProps) throws Exception {
    String message = msg;
    // channel.basicPublish(QUEUE_NAME,"", null, message.getBytes());
    channel.basicPublish(exchange, routingKey, theProps, message.getBytes());
    logger.info(" [x] Sent '" + routingKey + "':'" + message + "'");
  }
  */
  
  
  
  private static void send(String routingKey, String msg, AMQP.BasicProperties theProps) {
    try {
      String message = msg;
      // channel.basicPublish(QUEUE_NAME,"", null, message.getBytes());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(message);
      //channel.basicPublish(exchange, routingKey, theProps, message.getBytes());
      channel.basicPublish(exchange, routingKey, theProps, bos.toByteArray());
      logger.info(" [x] Sent '" + routingKey + "':'" + message + "'");
    } catch (Exception e) {
      logger.info("Problems during message publishing.");
      logger.info(Arrays.toString(e.getStackTrace()));
    }
  }
  
  
  private static void sendUser(String routingKey, String msg, AMQP.BasicProperties theProps) {
    try {
      String message = msg;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(message);
      //channel.basicPublish(exchange, routingKey, theProps, bos.toByteArray());
      logger.info(" [x] Sent '" + routingKey + "':'" + message + "'");
    } catch (Exception e) {
      logger.info("Problems during message publishing.");
      logger.info(Arrays.toString(e.getStackTrace()));
    }
  }

  
  public static void sendMeal(String msg) {
    send(routingKeyMeals, msg, null);
  }
  
  public static void sendReasonerResult(String msg) {
    send(routingKeyViolations, msg, null);
  }
  
  public static void sendGoal(String msg) {
    send(routingKeyGoals, msg, null);
  }
  
  public static void sendUserActivation(String msg) {
    sendUser(routingKeyUsers, msg, null);
  }
  
  public static void sendUserData(String msg) {
    send(routingKeyUsers, msg, null);
  }
  
  public static void sendTestNotifications(String msg) {
    send("helis.saluteplus.notifications", msg, null);
  }
  
  
  
  
  public static void send(String routingKey, byte[] msg, AMQP.BasicProperties theProps) throws Exception {
    // channel.basicPublish(QUEUE_NAME,"", null, message.getBytes());
    channel.basicPublish(exchange, routingKey, theProps, msg);
    logger.info(" [x] Sent '" + routingKey + "':'" + msg + "'");
  }

  
  
  public static void main(String[] args) {
    try {
      // ClientParameters.load();
      //connect();
      //send("test.fbk.eu", "pippo", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
