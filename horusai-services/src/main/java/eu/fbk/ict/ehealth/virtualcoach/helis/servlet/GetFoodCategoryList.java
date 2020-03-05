package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.LanguagePair;

/**
 * Servlet implementation class GetNutrientList
 */
public class GetFoodCategoryList extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;
  private HashMap<String, ArrayList<LanguagePair>> categoryLabels;

  
  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetFoodCategoryList() {
    super();
    VC.init();
  }

  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(GetFoodCategoryList.class);
  }

  
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    this.categoryLabels = new HashMap<String, ArrayList<LanguagePair>>();

    String langCode = "";
    langCode = request.getParameter("lang");

    String languageFilter = "";
    if (langCode != null) {
      languageFilter = " FILTER(LANG(?label) = \"\" || LANGMATCHES(LANG(?label), \"" + langCode + "\"))";
    }

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?subj ?label ?langcode " + "WHERE " + "{ " +
          "?subj rdfs:label ?label ." + "?subj rdfs:subClassOf " + VC.PREFIX + ":Food " + languageFilter +
          "BIND (LANG(?label) AS ?langcode)}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodURI = bindingSet.getValue("subj");
          Value foodLabel = bindingSet.getValue("label");
          Value labelLang = bindingSet.getValue("langcode");

          String foodKey = foodURI.stringValue().substring(foodURI.stringValue().indexOf('#') + 1);
          ArrayList<LanguagePair> lp = this.categoryLabels.get(foodKey);
          if (lp == null) {
            lp = new ArrayList<LanguagePair>();
          }
          LanguagePair l = new LanguagePair(labelLang.stringValue(), foodLabel.stringValue());
          lp.add(l);
          this.categoryLabels.put(foodKey, lp);
          //System.out.println(foodLabel.stringValue());
        }
        result.close();
      }

      Gson gson = new Gson();
      String foodNamesListString = gson.toJson(this.categoryLabels);
      response.getWriter().println(foodNamesListString);
      conn.close();
    } catch (Exception e) {
      logger.info("Error retrieving food categories list. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
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
