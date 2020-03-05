package eu.fbk.ict.ehealth.virtualcoach;

import eu.fbk.ict.ehealth.virtualcoach.queue.ClinicalDataActivityNotificationConsumer;
import eu.fbk.ict.ehealth.virtualcoach.queue.ClinicalDataNotificationConsumer;
import eu.fbk.ict.ehealth.virtualcoach.queue.Consumer;
import eu.fbk.ict.ehealth.virtualcoach.queue.PerseoConsumer;
import eu.fbk.ict.ehealth.virtualcoach.queue.PersonalDataNotificationConsumer;
import eu.fbk.ict.ehealth.virtualcoach.queue.Publisher;
import eu.fbk.ict.ehealth.virtualcoach.reasoner.ReasonerManager;
import eu.fbk.ict.ehealth.virtualcoach.trec.SessionManager;

import java.util.Arrays;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class HeLiSStarter implements ServletContextListener {

  private static Logger logger = LoggerFactory.getLogger(HeLiSStarter.class);
  
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    logger.info("HeLiS Component is starting up!");
    
    try {
      
      try {
        VC.init();
      } catch (Exception e) {
        VC.r.getConnection().close();
        logger.info("There was an issue during the initialization of the semantic environment. {}", e.getMessage(), e);
        logger.info(Arrays.toString(e.getStackTrace()));
      }
      
      HeLiSConfigurator prp = VC.config;
      
      /* Loads TreC token only if the PHR is used by the current instance. */
      if(prp.getVirtualCoachUseTrec().compareTo("1") == 0)
      {
        try {
          SessionManager.getTokenTrec();
        } catch (Exception e) {
          VC.r.getConnection().close();
          logger.info("There was an issue in getting the TreC Token. {}", e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
        }
      }
      
      
      /* Connects to RabbitMQ only if it is used within the current instance. */
      if(prp.getVirtualCoachUseRabbit().compareTo("1") == 0)
      {
        try {
          PerseoConsumer.init();
          PerseoConsumer.connect();
          PersonalDataNotificationConsumer.init();
          PersonalDataNotificationConsumer.connect();
          Publisher.init();
          Publisher.connect();
          
          String instanceId = prp.getVirtualCoachRepositoryId();
          if(instanceId.compareTo("lifestyle") == 0 || instanceId.compareTo("inmp") == 0) {
            ClinicalDataNotificationConsumer.init();
            ClinicalDataNotificationConsumer.connect();
            ClinicalDataActivityNotificationConsumer.init();
            ClinicalDataActivityNotificationConsumer.connect();
          }
        } catch (Exception e) {
          logger.info("Rabbit not connected. Queue requests and responses will not work. {}", e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
        }
      }
      
      
      /* Starts the reasoner if it is required by the current instance. */
      if(prp.getVirtualCoachUseReasoner().compareTo("1") == 0)
      {
        try {
          ReasonerManager.init();
        } catch (Exception e) {
          VC.r.getConnection().close();
          logger.info("There was an issue during the initialization of the reasoner. {}", e.getMessage(), e);
          logger.info(Arrays.toString(e.getStackTrace()));
        }
      }
      
      
    
    } catch (Exception e) {
      //throw new RuntimeException("Helis startup failed", e);
      logger.info("General problems during HeLiS start. {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
    }
  }

  
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    System.out.println("HeLiS Component is shutting down!");
    logger.info("HeLiS Component is shutting down!");
    HeLiSConfigurator prp = VC.config;
    try {
      
      /* Closes the connections with RabbitMQ only if it was used by the current instance. */
      if(prp.getVirtualCoachUseRabbit().compareTo("1") == 0)
      {
        Publisher.closeConnection();
        Consumer.closeConnection();
      }
      
      if(prp.getVirtualCoachUseReasonerBatch().compareTo("1") == 0) {
        logger.info("Reasoner schedule is shutting down...");
        ReasonerManager.batchDailyHandler.cancel(true);
        ReasonerManager.batchWeeklyHandler.cancel(true);
        logger.info("Reasoner schedule shutted down.");
      }
      
      System.out.println("Closing connections...");
      logger.info("Closing connections...");
      VC.r.getConnection().close();
      VC.r.shutDown();
      System.out.println("Connections closed and repository shutdown.");
      logger.info("Connections closed and repository shutdown.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
