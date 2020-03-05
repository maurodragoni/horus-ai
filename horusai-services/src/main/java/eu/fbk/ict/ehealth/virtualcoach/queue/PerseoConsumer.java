package eu.fbk.ict.ehealth.virtualcoach.queue;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.manager.ProfileDataManager;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.StandardServiceRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class PerseoConsumer {

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

  private PerseoConsumer() {}
  

  public static void init() {
    
    logger = LoggerFactory.getLogger(PerseoConsumer.class);
    logger.info("Initializing RabbitMQ.");
    
    try {
      
      HeLiSConfigurator prp = VC.config;
      
      host = prp.getVirtualCoachMqHost();
      exchange = prp.getVirtualCoachMqExchange();
      vhost = prp.getVirtualCoachMqVHost();
      username = prp.getVirtualCoachMqUser();
      password = prp.getVirtualCoachMqPwd();
      routingKey = prp.getVirtualCoachMqRoutingKeyPerseoNotifications();
      
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
    // String queueName = channel.queueDeclare().getQueue();
    String queueName = channel.queueDeclare("helis-" + VC.config.getVirtualCoachRepositoryId(), true, false, false, null).getQueue();
    
    channel.queueBind(queueName, exchange, routingKey);

    logger.info(" [*] Waiting for messages.");

    consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {

        //logger.info(body.toString());
        //String message = new String(body, "UTF-8");
        
        Object message = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        ObjectInputStream in = null;
        try {
          in = new ObjectInputStream(bis);
          message = in.readObject();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } finally {
          try {
            if (in != null) {
              in.close();
            }
          } catch (IOException ex) { 
            // ignore close exception
          }
        }
         
        onMessage(message);
      }
    };

    channel.basicConsume(queueName, true, consumer);
  }

  
  
  
  
  public static void onMessage(Object message) {
    
    logger.info(message.toString());
    Gson gson = new Gson();
    StandardServiceRequest p = gson.fromJson(message.toString(), StandardServiceRequest.class);
    logger.info(gson.toJson(p));

    
    /**
     * Parse the PerseoNotification and run the related service
     */
    try {
      String notificationType = p.getServiceRequestName();
      String notificationData = String.valueOf(p.getServiceRequestData());
      
      String fullyQualifiedName = ProfileDataManager.class.getName();
      String classPackage = "";
      int lastDot = fullyQualifiedName.lastIndexOf('.');
      if (lastDot != -1) {
        classPackage = fullyQualifiedName.substring(0, lastDot) + ".";
      }
      
      logger.info("Notification received for service: {}", (classPackage + notificationType));
      DataManager d;
      d = (DataManager) Class.forName(classPackage + notificationType).getDeclaredConstructor().newInstance();
      d.manage(notificationData.toString());
      
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