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
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.messages.receive.StandardServiceRequest;

/**
 * Servlet implementation class GetFoodList
 */
public class GetActivityList implements DataManager {
  private static final long serialVersionUID = 1L;
  private static Logger logger;
  private HashMap<String, ArrayList<LanguagePair>> foodLabels;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetActivityList() {
    logger = LoggerFactory.getLogger(GetActivityList.class);
  }

  
  
  @Override
  public String manage(String jsonPars) {
    
    String response = null;
    Gson gson = new Gson();
    Request request = gson.fromJson(jsonPars, Request.class);
    
    this.foodLabels = new HashMap<String, ArrayList<LanguagePair>>();

    String langCode = "";
    String filter = "";
    
    langCode = request.getLang();
    filter = request.getType();
    
    String languageFilter = "";
    if(langCode != null) {
      languageFilter = " FILTER(LANG(?label) = \"\" || LANGMATCHES(LANG(?label), \"" + langCode + "\"))";
    }
    String typeFilter = "";
    if(filter != null) {
      typeFilter = "?subj a " + VC.PREFIX + ":" + filter + " . ";
    }

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?subj ?label ?langcode " + "WHERE " + "{ " +
          "?subj rdfs:label ?label . " + " ?subj a " + VC.PREFIX + ":Activity . " + typeFilter + languageFilter + " " +
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
          ArrayList<LanguagePair> lp = this.foodLabels.get(foodKey);
          if (lp == null) {
            lp = new ArrayList<LanguagePair>();
          }
          LanguagePair l = new LanguagePair(labelLang.stringValue(), foodLabel.stringValue());
          lp.add(l);
          this.foodLabels.put(foodKey, lp);
        }
        result.close();
      }

      String foodNamesListString = gson.toJson(this.foodLabels);
      response = new String(foodNamesListString);
      conn.close();
    } catch (Exception e) {
      logger.info("Error retrieving activities list. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    return response;
  }
  
  
  
  
  private class Request {
    
    private String mode;
    private String lang;
    private String type;
    
    public Request() {}

    
    public String getMode() {
      return mode;
    }

    public void setMode(String mode) {
      this.mode = mode;
    }

    public String getLang() {
      return lang;
    }

    public void setLang(String lang) {
      this.lang = lang;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
