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
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Food;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.FoodDetailRequest;


/**
 * Servlet implementation class FoodDetail
 */
public class FoodDetail extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;


  /**
   * @see HttpServlet#HttpServlet()
   */
  public FoodDetail() {
    super();
    VC.init();
  }

  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(FoodDetail.class);
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //this.doPost(request, response);
    
    Gson gson = new Gson();
    String foodId = request.getParameter("id");
    String quantity = request.getParameter("quantity");
    double q = -1.0;
    if(quantity != null) {
      q = Double.valueOf(quantity);
    }
    
    Food f = new Food(foodId);
    f.retrieve();
    if(q < 0.0) {
      q = f.getEdiblePart();
    }
    f.adaptFoodQuantity(q);
    response.getWriter().append(gson.toJson(f));
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String jsonPars = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Gson gson = new Gson();
    FoodDetailRequest req = gson.fromJson(jsonPars, FoodDetailRequest.class);
    logger.info(gson.toJson(req));
    
    System.out.println(System.getenv("enableJsonLogging"));
    
    Food f = new Food(req.getId());
    f.retrieve();
    f.adaptFoodQuantity(req.getQuantity());
    response.getWriter().append(gson.toJson(f));
  }
}
