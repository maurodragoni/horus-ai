package eu.fbk.ict.ehealth.virtualcoach.helis.old;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.UpdateUtil;

/**
 * Servlet implementation class ValueIntervalManager
 */
public class ValueIntervalManager extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private Properties prp;
  private Repository r;
  private RepositoryConnection c;
  private String nsVC;
  private String nsVCPrefix;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ValueIntervalManager() {
    super();
  }

  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream prpFile = classLoader.getResourceAsStream("virtualcoach.properties");
      this.prp = new Properties();
      this.prp.load(prpFile);

      String rdfServer = this.prp.getProperty("virtualcoach.repository.url");
      String repositoryId = this.prp.getProperty("virtualcoach.repository.id");
      this.r = new HTTPRepository(rdfServer, repositoryId);
      this.r.initialize();
      this.c = this.r.getConnection();

      this.nsVC = this.prp.getProperty("virtualcoach.namespace.vc");
      this.nsVCPrefix = this.prp.getProperty("virtualcoach.namespace.vc.prefix");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String mode = request.getParameter("mode");
    boolean result = true;
    if (mode.compareTo("add") == 0) {
      result = this.add(request);
    } else if (mode.compareTo("update") == 0) {
      result = this.update(request);
    } else if (mode.compareTo("delete") == 0) {
      result = this.delete(request);
    }
    response.getWriter().println(String.valueOf(result));
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  private boolean add(HttpServletRequest request) {
    String idInterval = request.getParameter("idInterval");
    String jsonPars = request.getParameter("timeInterval");
    Gson gson = new Gson();
    ValueInterval vi = gson.fromJson(jsonPars, ValueInterval.class);

    String query = "PREFIX " + this.nsVCPrefix + ": <" + this.nsVC + "#> "
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "INSERT DATA " + "{ GRAPH " + this.nsVCPrefix
        + ":INTERVAL-" + idInterval + " " + "{ " + this.nsVCPrefix + ":INTERVAL-" + idInterval + "  rdf:type "
        + this.nsVCPrefix + ":ValueInterval ; "
        + "                                                         rdfs:label  \"INTERVAL-" + idInterval + "\" ; "
        + "                                                         " + this.nsVCPrefix + ":id \"INTERVAL-" + idInterval
        + "\" ; " + "                                                         " + this.nsVCPrefix + ":lowerBound "
        + vi.getLowerBound() + " ; " + "                                                         " + this.nsVCPrefix
        + ":upperBound " + vi.getUpperBound() + ". " + "}}";

    this.c.begin();
    UpdateUtil.executeUpdate(this.r.getConnection(), query);
    this.c.commit();

    return true;
  }

  private boolean update(HttpServletRequest request) {
    return true;
  }

  private boolean delete(HttpServletRequest request) {
    String idInterval = request.getParameter("idInterval");
    String jsonPars = request.getParameter("timeInterval");
    Gson gson = new Gson();
    ValueInterval vi = gson.fromJson(jsonPars, ValueInterval.class);

    String query = "PREFIX " + this.nsVCPrefix + ": <" + this.nsVC + "#> " + "CLEAR GRAPH <" + this.nsVCPrefix
        + ":INTERVAL-" + idInterval + "> ";

    this.c.begin();
    UpdateUtil.executeUpdate(this.r.getConnection(), query);
    this.c.commit();

    return true;
  }

  private class ValueInterval {
    private double lowerBound;
    private double upperBound;

    public ValueInterval(double l, double u) {
      this.lowerBound = l;
      this.upperBound = u;
    }

    public void setLowerBound(double l) {
      this.lowerBound = l;
    }

    public void setUpperBound(double u) {
      this.upperBound = u;
    }

    public double getLowerBound() {
      return this.lowerBound;
    }

    public double getUpperBound() {
      return this.upperBound;
    }
  }

}
