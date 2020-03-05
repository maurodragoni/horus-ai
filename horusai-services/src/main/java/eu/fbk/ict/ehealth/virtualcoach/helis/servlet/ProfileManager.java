package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.helis.manager.ProfileDataManager;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;


/**
 * Servlet implementation class ProfileManager
 */
public class ProfileManager implements DataManager {

  private static final long serialVersionUID = 1L;
  private static Logger logger;


  public ProfileManager() {
    logger = LoggerFactory.getLogger(ProfileManager.class);
  }
  
  
  @Override
  public String manage(String jsonPars) {
    logger.info("User profile request received.");
    ProfileDataManager pdm = new ProfileDataManager();
    String strResponse = pdm.manage(jsonPars);
    return strResponse;
  }    
}
