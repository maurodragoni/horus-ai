package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import eu.fbk.ict.ehealth.virtualcoach.VC;

public class Activity {
  
  private static Logger logger = LoggerFactory.getLogger(Activity.class);
  @Expose private String activityId;
  @Expose private HashMap<String, String> labels;
  @Expose private ArrayList<String> parentCategories;
  @Expose private ArrayList<String> activeCategory;
  @Expose private double baseCalories;
  @Expose private double baseMET;
  
  
  public Activity(String id) {
    this.activityId = id;
    this.parentCategories = new ArrayList<String>();
    this.activeCategory = new ArrayList<String>();
    this.setBaseCalories(0.0);
    this.setBaseMET(0.0);
    this.labels = new HashMap<String, String>();    
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String id) {
    this.activityId = id;
  }
  
  public void setParentCategories(ArrayList<String> p) {
    this.parentCategories = p;
  }

  public void addParentCategory(String p) {
    this.parentCategories.add(p);
  }

  public ArrayList<String> getParentCategories() {
    return this.parentCategories;
  }

  public void addLabel(String langId, String l) {
    if(this.labels == null) {
      this.labels = new HashMap<String, String>();
    }
    this.labels.put(langId, l);
  }

  public ArrayList<String> getActiveCategory() {
    return activeCategory;
  }

  public void setActiveCategory(ArrayList<String> activeCategory) {
    this.activeCategory = activeCategory;
  }

  public HashMap<String, String> getLabels() {
    return labels;
  }

  public void setLabels(HashMap<String, String> labels) {
    this.labels = labels;
  }

  public double getBaseCalories() {
    return baseCalories;
  }

  public void setBaseCalories(double baseCalories) {
    this.baseCalories = baseCalories;
  }

  public double getBaseMET() {
    return baseMET;
  }

  public void setBaseMET(double baseMET) {
    this.baseMET = baseMET;
  }


  
  
  public String getJSONDataObject() {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    String jsonObjectString;
    jsonObjectString = gson.toJson(this);
    return jsonObjectString;
  }

  
  
  // TODO write the actual activity-save method 
  public boolean storeActivity(Activity ae, User u) {
    
    boolean storeFlag = false;
    RepositoryConnection conn = null;
    
    try {
      conn = VC.r.getConnection();
      //String ts = String.valueOf(System.currentTimeMillis());
      String context = "GRAPH <" + VC.PREFIX + ":" + "CXT-ACTIVITIES>";
  
      String query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ " + context + " " + "{ " +
                     this.activityId + " rdf:type " + VC.PREFIX + ":PerformedActivity ; " +
                     VC.PREFIX + ":hasActivity " + VC.PREFIX + ":" + ae.getActivityId() + " ; " +
                     "}}";
  
      logger.info(query);
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      logger.info("Query executed.");
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the save of the activity {}. {} {}", this.activityId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
      return storeFlag;
    }
    storeFlag = true;
    return storeFlag;
  }

  
  
  /**
   * Retrieves all activity's data.
   */
  public void retrieve() {
    this.retrieveLabels(this.activityId);
    this.retrieveBaseData(this.activityId);
    this.retrieveActivityCategory(this.activityId);
  }
  
  
 
  /**
   * Retrieves activity labels.
   */
  private void retrieveLabels(String activityId) {

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?label ?langcode WHERE { " +
          VC.PREFIX + ":" + activityId + " rdfs:label ?label . " + 
          "BIND (LANG(?label) AS ?langcode)" + "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value activityLabel = bindingSet.getValue("label");
          Value activityLang = bindingSet.getValue("langcode");

          this.addLabel(activityLang.stringValue(), activityLabel.stringValue());
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during retrieving the labels of activity {}. {} {}", this.activityId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  
  /**
   * Retrieves metabolic data.
   * @param activityId
   */
  private void retrieveBaseData(String activityId) {

    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      
      /* Get the list of nutrients */
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?met ?kcal WHERE { " +
          VC.PREFIX + ":" + activityId + " rdf:type ?type . " + 
          VC.PREFIX + ":" + activityId + " " + VC.PREFIX + ":hasMETValue ?met . " + 
          VC.PREFIX + ":" + activityId + " " + VC.PREFIX + ":hasUnitKCalValue ?kcal . " + 
          "filter(!strstarts(str(?type),str(owl:))) " +
          "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value met = bindingSet.getValue("met");
          Value kcal = bindingSet.getValue("kcal");
          this.setBaseMET(Double.valueOf(met.stringValue()));
          this.setBaseCalories(Double.valueOf(kcal.stringValue()));
          // System.out.println(type.stringValue());
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during retrieving the base data of activity {}. {} {}", this.activityId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }
  
  
  
  /**
   * Retrieves activity's categories.
   * @param activityId
   */
  private void retrieveActivityCategory(String activityId) {
    if(this.activeCategory == null) {
      this.activeCategory = new ArrayList<String>();
    }
    if(this.parentCategories == null) {
      this.parentCategories = new ArrayList<String>();
    }
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?activeFlag WHERE { " +
          VC.PREFIX + ":" + activityId + " rdf:type ?type . " + 
          "OPTIONAL {?type " + VC.PREFIX + ":activeCategory ?activeFlag} ." +
          "filter(!strstarts(str(?type),str(owl:))) " + "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value activityType = bindingSet.getValue("type");
          Value activeFlag = bindingSet.getValue("activeFlag");

          String activityStringId = activityType.stringValue().substring(activityType.stringValue().indexOf('#') + 1);

          String categoryPath = new String();
          String activeCategoryPath = new String();
          categoryPath = activityStringId;
          activeCategoryPath = new String("");
          if (activeFlag != null && activeFlag.toString().compareTo("1") == 0)
            activeCategoryPath = activityStringId;

          String[] nextCategoryPath = this.createCategoryPath(activityStringId);
          categoryPath = categoryPath.concat(";" + nextCategoryPath[0]);
          if (nextCategoryPath[1].compareTo("1") == 0) {
            activeCategoryPath = activeCategoryPath.concat(nextCategoryPath[0] + ";");
          }
          while (nextCategoryPath[0].length() != 0) {
            nextCategoryPath = this.createCategoryPath(nextCategoryPath[0]);
            categoryPath = categoryPath.concat(";" + nextCategoryPath[0]);
            if (nextCategoryPath[1].compareTo("1") == 0) {
              activeCategoryPath = activeCategoryPath.concat(nextCategoryPath[0] + ";");
            }
          }
          ArrayList<String> categories = new ArrayList<String>(Arrays.asList(categoryPath.split(";")));
          ArrayList<String> activeCategories = new ArrayList<String>(Arrays.asList(activeCategoryPath.split(";")));
          this.setParentCategories(categories);
          this.setActiveCategory(activeCategories);
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during retriving categories of activity {}. {} {}", this.activityId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  
  /**
   * Retrieves and caches the category taxonomy of the activity.
   * @param seedCategory
   * @return
   */
  private String[] createCategoryPath(String seedCategory) {
    
    String[] categoryPath = new String[2];
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?activeFlag WHERE { " +
          VC.PREFIX + ":" + seedCategory + " rdfs:subClassOf ?type . " + 
          "OPTIONAL {?type " + VC.PREFIX + ":activeCategory ?activeFlag} . " +
          "filter(!strstarts(str(?type),str(rdfs:))) " + "}";

      logger.info(queryString);
      categoryPath[0] = new String("");
      categoryPath[1] = new String("");
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value categoryType = bindingSet.getValue("type");
          Value activeFlag = bindingSet.getValue("activeFlag");

          String foodStringId = categoryType.stringValue().substring(categoryType.stringValue().indexOf('#') + 1);
          categoryPath[0] = foodStringId;
          if (activeFlag != null) {
            categoryPath[1] = activeFlag.stringValue();
          }
        }
        result.close();
      }
      conn.close();
      return categoryPath;
    } catch (Exception e) {
      logger.info("Error during retrieving category path of activity {}. {} {}", this.activityId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    return categoryPath;
  }
}
