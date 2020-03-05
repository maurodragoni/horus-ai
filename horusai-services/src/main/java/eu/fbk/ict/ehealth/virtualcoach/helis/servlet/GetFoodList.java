package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

import eu.fbk.ict.ehealth.virtualcoach.GsonFactory;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Food;


/**
 * Servlet implementation class GetFoodList
 */
public class GetFoodList extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger;

  
  /**
   * @see HttpServlet#HttpServlet()
   */
  public GetFoodList() {
    super();
  }

  
  /**
   * @see Servlet#init(ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    logger = LoggerFactory.getLogger(GetFoodList.class);
  }

  
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HashMap<String, Food> foods = new HashMap<String, Food>();

    boolean flagDetail = true;
    boolean flagCalories = false;
    String langCode = "";
    String format = "json";
    
    String flagDetailCheck = request.getParameter("detail");
    if(flagDetailCheck != null) {
      flagDetail = Boolean.valueOf(flagDetailCheck);
    }
    
    String flagCaloriesCheck = request.getParameter("calories");
    if(flagCaloriesCheck != null) {
      flagCalories = Boolean.valueOf(flagCaloriesCheck);
    }
    
    langCode = request.getParameter("lang");
    String languageFilter = "";
    if (langCode != null) {
      languageFilter = " FILTER(LANG(?label) = \"\" || LANGMATCHES(LANG(?label), \"" + langCode + "\"))";
    }
    
    String formatCheck = request.getParameter("format");
    if(formatCheck != null) {
      format = formatCheck;
    }

    
    
    logger.info("Request parameter: Lang: {} - Detail: {} - Format: {}", langCode, flagDetail, format);

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?subj ?label ?langcode " + "WHERE " + "{ " +
          "?subj rdfs:label ?label ." + 
          "?subj a " + VC.PREFIX + ":Food . " +
          //"?subj vc:id \"RECIPE-23710\" . " +
          languageFilter + "BIND (LANG(?label) AS ?langcode)}";
      
      queryString = VC.SPARQL_PREFIX + 
          "SELECT ?subj ?label ?langcode ?caloriesC ?ediblePartC WHERE { " +  
            "{" +
             "   ?subj a vc:BasicFood ;" +
              "      rdfs:label ?label ." +
              //"      vc:id \"RECIPE-23710\" ; " +
              "OPTIONAL { " +
              " ?subj vc:ediblePart ?ediblePart ; " + 
               "      vc:amountCalories ?calories . " + 
               "} " +
               "BIND(100.0 as ?defaultEdible) " +
               "BIND(0.0 as ?defaultCalories) " +
               "BIND (LANG(?label) AS ?langcode) " +
               "BIND(COALESCE(?ediblePart, ?defaultEdible) as ?ediblePartC) " +
               "BIND(COALESCE(?calories, ?defaultCalories) as ?caloriesC) " +
            "} " +
            "UNION" +
            "{" +
             "   SELECT ?subj ?label ?langcode (SUM(?c) as ?caloriesC) (SUM(?amountFood) as ?ediblePartC) WHERE {" +
              "      ?subj a vc:Recipe ; " +
               "             rdfs:label ?label ; " +
                //"            vc:id \"RECIPE-23710\" ; " +
                "            vc:hasRecipeFood ?recipeFood . " + 
                 "   ?recipeFood vc:amountFood ?amountFood ; " +
                  "          vc:hasFood ?food." +
                   " ?food vc:amountCalories ?fc ; " +
                    "      vc:ediblePart ?e . " +
                    "BIND((?fc * (?amountFood / ?e)) as ?c) " +
                    "BIND (LANG(?label) AS ?langcode) " +
                "} " +
                "GROUP BY ?subj ?label ?langcode " +
            "}" +
        "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodURI = bindingSet.getValue("subj");
          Value foodLabel = bindingSet.getValue("label");
          Value labelLang = bindingSet.getValue("langcode");
          Value calories = bindingSet.getValue("caloriesC");
          Value ediblePart = bindingSet.getValue("ediblePartC");

          String foodKey = foodURI.stringValue().substring(foodURI.stringValue().indexOf('#') + 1);
          Food f = foods.get(foodKey);
          if(f == null) {
            f = new Food(foodKey);
            
            /*
            if(flagDetail || flagCalories) {
              f.retrieveLight();
            }
            */
            
            if(flagCalories) {
              f.setAmountCalories(Double.valueOf(calories.stringValue()) / Double.valueOf(ediblePart.stringValue()));
            }
          }
          f.addLabel(labelLang.stringValue(), foodLabel.stringValue());

          foods.put(foodKey, f);
        }
        result.close();
      }
      
      
      if(flagDetail) {
        queryString = VC.SPARQL_PREFIX + 
            "SELECT ?subj ?label ?langcode WHERE { " +  
                "      ?subj a vc:Recipe ;" +
                  "            vc:hasRecipeFood ?recipeFood ." + 
                   "   ?recipeFood vc:hasFood ?food." +
                     " ?food rdfs:label ?label ." +
                      "BIND (LANG(?label) AS ?langcode)" +
          "}";

        logger.info(queryString);
        tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
          while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value foodURI = bindingSet.getValue("subj");
            Value foodLabel = bindingSet.getValue("label");
            Value labelLang = bindingSet.getValue("langcode");

            String foodKey = foodURI.stringValue().substring(foodURI.stringValue().indexOf('#') + 1);
            Food f = foods.get(foodKey);
            if(f == null) {
              f = new Food(foodKey);              
            }
            f.addRecipeLabel(labelLang.stringValue(), foodLabel.stringValue());

            foods.put(foodKey, f);
          }
          result.close();
        }  
      }
      
      HashMap<String, Food> expandedFoods = this.expandPastaRecipes(foods);
      foods.putAll(expandedFoods);
      
      
      String foodNamesListString = new String("");
      if(format.compareTo("json") == 0) {
        
        List<String> fieldExclusions = new ArrayList<String>();
        fieldExclusions.add("logger");
        fieldExclusions.add("nutrients");
        fieldExclusions.add("parentCategories");
        fieldExclusions.add("activeCategory");
        fieldExclusions.add("ediblePart");
        fieldExclusions.add("amountEnergy");
        fieldExclusions.add("amountWater");
        fieldExclusions.add("recipeFoods");

        if(!flagCalories) {
          fieldExclusions.add("amountCalories");
        }
        
        if(!flagDetail) {
          fieldExclusions.add("recipeLabels");
        }
        
        Gson gson = GsonFactory.build(fieldExclusions, null);
        foodNamesListString = gson.toJson(foods);
        response.getWriter().println(foodNamesListString);
        
      } else if(format.compareTo("csv") == 0) {
        /*
        Iterator<String> it = this.foodLabels.keySet().iterator();
        while(it.hasNext()) {
          String currentFoodId = it.next();
          ArrayList<LanguagePair> lp = this.foodLabels.get(currentFoodId);
          response.getWriter().print(currentFoodId);
          for(LanguagePair l : lp) {
            response.getWriter().print("\t" + l.label);
            ArrayList<String> als = l.getAlternateLabels();
            response.getWriter().print(" (");
            for(int j = 0; j < als.size(); j++) {
              response.getWriter().print(als.get(j));
              if(j < (als.size() - 1)) {
                response.getWriter().print(", ");
              }
            }
            response.getWriter().print(")");
          }
          response.getWriter().println();
        }
        */
      }
     
      conn.close();
    } catch (Exception e) {
      logger.info("Error retrieving foods list. {} {}", e.getMessage(), e);
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
  
  
  
  /**
   * Enriches food list by cloning all pasta's recipes with all possible type of pasta contained in the ontology
   */
  private HashMap<String, Food> expandPastaRecipes(HashMap<String, Food> foods) {
    
    HashMap<String, Food> expandedFoods = new HashMap<String, Food>();
    
    String pastaQueryString = VC.SPARQL_PREFIX + 
        "SELECT ?subj ?label ?langcode " + "WHERE " + "{ " +
        "?subj rdfs:label ?label . " + 
        "?subj a " + VC.PREFIX + ":PastaTypesClassification . " +
        "FILTER(LANG(?label) = \"\" || LANGMATCHES(LANG(?label), \"it\")) " + 
        "BIND (LANG(?label) AS ?langcode)}";
    
    HashMap<String, String> pastaTypes = new HashMap<String, String>();
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      logger.info(pastaQueryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, pastaQueryString);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodURI = bindingSet.getValue("subj");
          Value foodLabel = bindingSet.getValue("label");
          String foodKey = foodURI.stringValue().substring(foodURI.stringValue().indexOf('#') + 1);
          pastaTypes.put(foodKey, foodLabel.stringValue());
        }
        result.close();
      }
      
      
      Iterator<String> itF = foods.keySet().iterator();
      while(itF.hasNext()) {
        String foodId = itF.next();
        Food f = foods.get(foodId);
        HashMap<String, String> labels = f.getLabels();
        Iterator<String> itL = labels.keySet().iterator();
        HashMap<String, String> expandedFoodLabels = null;
        while(itL.hasNext()) {
          String langId = itL.next();
          if(langId.compareTo("it") == 0) {
            String currentLabel = new String(" ").concat(labels.get(langId));
            if(currentLabel.contains(" Pasta ")) {
              //expandedFoodLabels = this.replaceAndExpandLabels(currentLabel, " Pasta ", pastaTypes);
            } else if(currentLabel.contains(" pasta ")) {
              //expandedFoodLabels = this.replaceAndExpandLabels(currentLabel, " pasta ", pastaTypes);
            } else {
              Iterator<String> itP = pastaTypes.keySet().iterator();
              while(itP.hasNext()) {
                String pastaId = itP.next();
                String pastaType = pastaTypes.get(pastaId);
                if(currentLabel.contains(" " + pastaType + " ")) {
                  //expandedFoodLabels = this.replaceAndExpandLabels(currentLabel, " " + pastaType + " ", pastaTypes);
                  expandedFoodLabels = this.replaceAndExpandLabelsPasta(currentLabel, " " + pastaType + " ", "Pasta", foodId);
                  break;
                } else if(currentLabel.contains(" " + pastaType.toLowerCase() + " ")) {
                  //expandedFoodLabels = this.replaceAndExpandLabels(currentLabel, " " + pastaType.toLowerCase() + " ", pastaTypes);
                  expandedFoodLabels = this.replaceAndExpandLabelsPasta(currentLabel, " " + pastaType.toLowerCase() + " ", "pasta", foodId);
                  break;
                }
              }
            }
          }
        }
        
        if(expandedFoodLabels != null) {
          Iterator<String> itExpandedFoods = expandedFoodLabels.keySet().iterator();
          while(itExpandedFoods.hasNext()) {
            String expandedId = itExpandedFoods.next();
            String expandedLabel = expandedFoodLabels.get(expandedId);
            
            //System.out.print(expandedId + "---" + expandedLabel);
            Food c = f.clone();
            String newFoodId = f.getId().concat("-PASTA-" + expandedId);
            c.setId(newFoodId);
            c.addLabel("it", expandedLabel);
            expandedFoods.put(newFoodId, c);
          }
        }
      }
      
      conn.close();
    } catch (Exception e) {
      logger.info("Error retrieving foods list. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }

    return expandedFoods;
  }
  
  
  
  private HashMap<String, String> replaceAndExpandLabels(String originalLabel, String substringToReplace, HashMap<String, String> labelsForExpansion) {
    HashMap<String, String> expandedFoodLabels = new HashMap<String, String>();
    
    Iterator<String> itE = labelsForExpansion.keySet().iterator();
    while(itE.hasNext()) {
      String nextLabelId = itE.next();
      String nextLabelForExpansion = labelsForExpansion.get(nextLabelId);
      
      if(substringToReplace.contains(nextLabelForExpansion.toLowerCase()) == false) {
        String expandedLabel = originalLabel.replace(substringToReplace, " " + nextLabelForExpansion + " ");
        expandedFoodLabels.put(nextLabelId, expandedLabel.trim());
      }
    }
    //System.out.println(originalLabel);
    //System.out.println(substringToReplace);
    //System.out.println(expandedFoodLabels);
    return expandedFoodLabels;
  }
  
  
  private HashMap<String, String> replaceAndExpandLabelsPasta(String originalLabel, String substringToReplace, String pasta, String foodId) {
    HashMap<String, String> expandedFoodLabels = new HashMap<String, String>();
    
    String expandedLabel = originalLabel.replace(substringToReplace, " " + pasta + " ");
    expandedFoodLabels.put(foodId + "-PASTA", expandedLabel.trim());
    //System.out.println(originalLabel);
    //System.out.println(substringToReplace);
    //System.out.println(expandedFoodLabels);
    return expandedFoodLabels;
  }
}
