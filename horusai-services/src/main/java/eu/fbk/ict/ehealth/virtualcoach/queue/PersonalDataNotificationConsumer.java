package eu.fbk.ict.ehealth.virtualcoach.queue;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Profile;
import eu.fbk.ict.ehealth.virtualcoach.helis.manager.ProfileDataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.PersonalDataNotification;
import eu.fbk.ict.ehealth.virtualcoach.trec.ClinicalData;
import eu.fbk.ict.ehealth.virtualcoach.trec.PersonalData;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.UserProfileRequest;
import eu.fbk.trec.personal_data.model.ExtendedUserData;
import eu.fbk.trec.session_manager.model.enums.Genotype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;


public class PersonalDataNotificationConsumer {

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

  private PersonalDataNotificationConsumer() {
  }

  public static void init() {

    logger = LoggerFactory.getLogger(PersonalDataNotificationConsumer.class);
    logger.info("Initializing RabbitMQ.");

    try {

      HeLiSConfigurator prp = VC.config;

      host = prp.getVirtualCoachMqHost();
      exchange = prp.getVirtualCoachMqExchangePersonalData();
      vhost = prp.getVirtualCoachMqVHost();
      username = prp.getVirtualCoachMqUser();
      password = prp.getVirtualCoachMqPwd();
      routingKey = "#";
      
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

    channel.basicConsume(queueName, true, consumer);
  }

  public static void onMessage(Object message) {

    logger.info(message.toString());
    Gson gson = new Gson();

    /**
     * Parse the PerseoNotification and run the related service
     */
    try {

      PersonalDataNotification p = gson.fromJson(message.toString(), PersonalDataNotification.class);
      logger.info(gson.toJson(p));

      //Calendar c = new GregorianCalendar();

      String patientId = p.getTrec_user_id();
      String operation = p.getOperation();

      if (operation.compareTo("CREATE") == 0) {
        ExtendedUserData eud = PersonalData.getUserData(patientId);
        double userWeight = ClinicalData.getWeight(patientId);
        int userHeight = ClinicalData.getHeight(patientId);

        logger.info(gson.toJson(eud));

        UserProfileRequest upr = new UserProfileRequest();
        upr.setMode("new");
        upr.setUserId(eud.getTrecUserId());
        upr.setUsername(eud.getTrecUserId());

        if (eud.getGenotype() == Genotype.Y) {
          upr.setGender("M");
        } else if (eud.getGenotype() == Genotype.X) {
          upr.setGender("F");
        }
        upr.setHeight(userHeight);
        upr.setWeight((int) userWeight);

        Date date = Date.from(eud.getBirthday().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        upr.setAge(date.getTime());

        upr.setProfiles(new ArrayList<Profile>());

        logger.info(gson.toJson(upr));

        ProfileDataManager pdm = new ProfileDataManager();
        pdm.manage(upr);

        Publisher.sendUserActivation("{\"userId\":\"" + eud.getTrecUserId() + "\"}");

      } else if (operation.compareTo("UPDATE") == 0) {
        
        ExtendedUserData eud = PersonalData.getUserData(patientId);
        double userWeight = ClinicalData.getWeight(patientId);
        int userHeight = ClinicalData.getHeight(patientId);

        logger.info(gson.toJson(eud));

        UserProfileRequest upr = new UserProfileRequest();
        upr.setMode("update");
        upr.setUserId(eud.getTrecUserId());
        upr.setUsername(eud.getTrecUserId());

        if (eud.getGenotype() == Genotype.Y) {
          upr.setGender("M");
        } else if (eud.getGenotype() == Genotype.X) {
          upr.setGender("F");
        }
        upr.setHeight(userHeight);
        upr.setWeight((int) userWeight);

        Date date = Date.from(eud.getBirthday().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        upr.setAge(date.getTime());

        upr.setProfiles(new ArrayList<Profile>());

        logger.info(gson.toJson(upr));

        ProfileDataManager pdm = new ProfileDataManager();
        pdm.manage(upr);

        Publisher.sendUserActivation("{\"userId\":\"" + eud.getTrecUserId() + "\"}");

      } else if (operation.compareTo("DELETE") == 0) {

      } else {

      }

      logger.info("Notification received for User: {}", patientId);

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