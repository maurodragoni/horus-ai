package eu.fbk.ict.ehealth.virtualcoach.helis.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.helis.servlet.ServletManager;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.StandardServiceRequest;


public class ServletDataManager implements DataManager {

  private static Logger logger;
  
  public ServletDataManager() {
    logger = LoggerFactory.getLogger(ServletDataManager.class);
  }

  @Override
  public String manage(String jsonPars) {
    logger.info("Servlet data request received.");
    Gson gson = new Gson();
    StandardServiceRequest req = gson.fromJson(jsonPars, StandardServiceRequest.class);
    logger.info("Servlet data request parsed. Servlet: {}: ", req.getServiceRequestName());
    
    /**
     * Parse the PerseoNotification and run the related service
     */
    try {
      String servletRequestName = req.getServiceRequestName();
      String servletRequestData = String.valueOf(req.getServiceRequestData());
      
      String fullyQualifiedName = ServletManager.class.getName();
      String classPackage = "";
      int lastDot = fullyQualifiedName.lastIndexOf('.');
      if (lastDot != -1) {
        classPackage = fullyQualifiedName.substring(0, lastDot) + ".";
      }
      
      logger.info("Notification received for servlet: {}", (classPackage + servletRequestName));
      DataManager d;
      d = (DataManager) Class.forName(classPackage + servletRequestName).getDeclaredConstructor().newInstance();
      String response = d.manage(servletRequestData.toString());
      return response;
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return null;
  }
}
