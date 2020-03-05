package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Activity;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.IdBasedRequest;


/**
 * Servlet implementation class ActivityDetail
 */
public class ActivityDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;


  /**
   * @see HttpServlet#HttpServlet()
   */
  public ActivityDetail() {
    super();
    VC.init();
  }

  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(ProfileManager.class);
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    this.doPost(request, response);
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    String jsonPars = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Gson gson = new Gson();
    IdBasedRequest req = gson.fromJson(jsonPars, IdBasedRequest.class);
    logger.info(gson.toJson(req));
    
    Activity a = new Activity(req.getId());
    a.retrieve();
    response.getWriter().append(gson.toJson(a));
  }
}
