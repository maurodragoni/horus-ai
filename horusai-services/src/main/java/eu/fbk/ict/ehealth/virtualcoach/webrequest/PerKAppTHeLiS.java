package eu.fbk.ict.ehealth.virtualcoach.webrequest;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Servlet implementation class PerKAppTHeLiS
 */
public class PerKAppTHeLiS extends HttpServlet {

  
  private static final long serialVersionUID = 1L;
  private final String baseUrl = new String("https://shellvm1.fbk.eu:8080/helis-service/");
  private static Logger logger;


  public PerKAppTHeLiS() {
    super();
  }

  public void init(ServletConfig config) throws ServletException {
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    this.doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String jsonPars = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    Gson gson = new Gson();
    ServiceResolveRequest req = gson.fromJson(jsonPars, ServiceResolveRequest.class);

    logger.info(jsonPars);
    String resolvedUrl = this.baseUrl.concat(req.service);
    String serviceData = req.data.toString();
    logger.info(resolvedUrl);
    logger.info(serviceData);
  }

  class ServiceResolveRequest {
    @SerializedName("type")
    public String service;

    @SerializedName("data")
    public JsonObject data;
  }
  
}
