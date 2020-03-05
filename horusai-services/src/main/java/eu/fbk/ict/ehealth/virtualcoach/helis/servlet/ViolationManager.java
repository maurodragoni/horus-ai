package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.helis.manager.ViolationDataManager;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.ViolationRequest;

/**
 * Servlet implementation class ViolationManager
 */
public class ViolationManager extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;
  

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ViolationManager() {
    super();
  }

  
  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(ViolationManager.class);
  }

  
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String mode = request.getParameter("mode");
    String userId = request.getParameter("userId");
    String timing = request.getParameter("timing");
    String startDate = request.getParameter("startDate");
    String ruleId = request.getParameter("ruleId");

    logger.info("ViolationRequest Parameters: {}, {}, {}, {}, {}", mode, userId, timing, startDate, ruleId);

    ViolationRequest v = new ViolationRequest();
    if (mode != null && mode.compareTo("get") == 0) {
      v.setMode(mode);
      v.setUserId(userId);
      v.setTiming(timing);
      v.setStartDate(startDate);
      v.setRuleId(ruleId);
      ViolationDataManager vdm = new ViolationDataManager();
      response.getWriter().append(vdm.manage(v));
    }
  }

  
  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

}
