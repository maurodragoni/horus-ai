package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.helis.manager.MealDataManager;



/**
 * 
 * @author drago
 */
// TODO: in caso di alimenti doppi, fare l'update delle quantita'
// TODO: gestire la cancellazione dei pasti



/**
 * Servlet implementation class MealManager
 */
public class MealManager extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public MealManager() {
    super();
  }

  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(MealManager.class);
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

    logger.info("***********************************************************");

    String jsonPars = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    MealDataManager mdm = new MealDataManager();
    String strResponse = mdm.manage(jsonPars);
    PrintWriter out = response.getWriter();
    out.println(strResponse);
  }  
}
