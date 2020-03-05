package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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


public class Food {

  private static Logger logger;
  private String id;
  private HashMap<String, String> labels;
  private HashMap<String, Double> nutrients;
  private ArrayList<String> parentCategories;
  private ArrayList<String> activeCategory;
  private double ediblePart;
  private double amountEnergy;
  private double amountWater;
  private double amountCalories;
  private ArrayList<Pair> recipeFoods;
  private HashMap<String, String> recipeLabels;

  
  public Food(String id) {
    logger = LoggerFactory.getLogger(Food.class);
    this.id = id;
    this.parentCategories = new ArrayList<String>();
    this.activeCategory = new ArrayList<String>();
    this.ediblePart = 0.0;
    this.amountCalories = 0.0;
    this.amountEnergy = 0.0;
    this.amountWater = 0.0;
    this.labels = new HashMap<String, String>();
    this.nutrients = new HashMap<String, Double>();
    this.recipeFoods = new ArrayList<Pair>();
    this.recipeLabels = new HashMap<String, String>();
  }
  
  
  public Food clone() {
    Food c = new Food(this.id);
    c.setParentCategories(this.getParentCategories());
    c.setActiveCategory(this.getActiveCategory());
    c.setEdiblePart(this.getEdiblePart());
    c.setAmountCalories(this.getAmountCalories());
    c.setAmountEnergy(this.getAmountEnergy());
    c.setAmountWater(this.getAmountWater());
    //c.setLabels(this.getLabels());
    c.setNutrients(this.getNutrients());
    c.setRecipeLabels(this.getRecipeLabels());
    c.setRecipeFoods(this.getRecipeFoods());
    return c;
  }

  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public HashMap<String, String> getLabels() {
    return labels;
  }

  public void setLabels(HashMap<String, String> labels) {
    this.labels = labels;
  }

  public HashMap<String, Double> getNutrients() {
    return nutrients;
  }

  public void setNutrients(HashMap<String, Double> nutrients) {
    this.nutrients = nutrients;
  }

  public ArrayList<String> getParentCategories() {
    return parentCategories;
  }

  public void setParentCategories(ArrayList<String> parentCategories) {
    this.parentCategories = parentCategories;
  }

  public double getEdiblePart() {
    return ediblePart;
  }

  public void setEdiblePart(double ediblePart) {
    this.ediblePart = ediblePart;
  }

  public double getAmountEnergy() {
    return amountEnergy;
  }

  public void setAmountEnergy(double amountEnergy) {
    this.amountEnergy = amountEnergy;
  }

  public double getAmountWater() {
    return amountWater;
  }

  public void setAmountWater(double amountWater) {
    this.amountWater = amountWater;
  }

  public double getAmountCalories() {
    return amountCalories;
  }

  public void setAmountCalories(double amountCalories) {
    this.amountCalories = amountCalories;
  }

  public ArrayList<String> getActiveCategory() {
    return activeCategory;
  }
  
  public ArrayList<Pair> getRecipeFoods() {
    return recipeFoods;
  }
  
  public HashMap<String, String> getRecipeLabels() {
    return recipeLabels;
  }

  public void setRecipeLabels(HashMap<String, String> recipeLabels) {
    this.recipeLabels = recipeLabels;
  }

  
  

  public void addParentCategory(String p) {
    this.parentCategories.add(p);
  }

  public void addLabel(String langId, String l) {
    this.labels.put(langId, l);
  }
  
  public void addRecipeLabels(HashMap<String, String> labels) {
    Iterator<String> it = labels.keySet().iterator();
    while(it.hasNext()) {
      String langCode = it.next();
      String label = labels.get(langCode);
      this.addRecipeLabel(langCode, label);
    }
  }  
  
  public void addRecipeLabel(String langId, String l) {
    String currentRecipeLabels = this.recipeLabels.get(langId);
    if(currentRecipeLabels == null) {
      currentRecipeLabels = l;
    } else {
      currentRecipeLabels = currentRecipeLabels + ", " + l;
    }
    this.recipeLabels.put(langId, currentRecipeLabels);
  }

  public void setRecipeFoods(ArrayList<Pair> recipeFoods) {
    this.recipeFoods = recipeFoods;
  }


  public void addNutrient(String n, Double v) {
    this.nutrients.put(n, v);
  }
  
  public Double getNutrient(String n) {
    Double nutrientQuantity = this.nutrients.get(n);
    if(nutrientQuantity == null) {
      return 0.0;
    } else {
      return nutrientQuantity;
    }
  }
  
  public void setActiveCategory(ArrayList<String> activeCategory) {
    this.activeCategory = activeCategory;
  }

  
  
  
  
  public String getJSONDataObject(String foodQuantity) {
    Gson gson = new Gson();
    double fq = Double.valueOf(foodQuantity);
    String jsonObjectString;
    if (fq == 100.0) {
      jsonObjectString = gson.toJson(this);
    } else {
      this.adaptFoodQuantity(fq);
      jsonObjectString = gson.toJson(this);
    }
    return jsonObjectString;
  }

  
  // TODO: Split data retrieval. Put the complete recipe retrieval management in a separated method.
  
  /**
   * Retrieves all food data.
   */
  public void retrieve() {
    this.retrieveLabels();
    this.retrieveFoodCategory();
    
    if(this.getId().startsWith("FOOD-")) {
      this.retrieveBaseData();
      this.retrieveNutrients();
      this.setEdiblePart(100.0);
    } else {
      this.retrieveRecipeFoods();
      for(Pair p : this.recipeFoods) {
        //logger.info("{} {} {} {}", this.id, this.getEdiblePart(), p.getK(), p.getV());
        Food f = new Food(p.getK());
        f.retrieve();
        f.retrieveLabels();
        f.adaptFoodQuantity(Double.valueOf(p.getV())); 
        //logger.info("{} {} {}", f.id, f.getEdiblePart(), f.getAmountCalories());
        if(p.getK().compareTo("FOOD-999999") != 0 && !this.getId().startsWith("TURCONI-")) {
          this.ediblePart += Double.valueOf(p.getV());
        }
        this.aggregateFoodEntity(f);
        //logger.info("{} {} {}", this.id, this.getEdiblePart(), this.getAmountCalories());
        this.addRecipeLabels(f.getLabels());
      }
    }
  }
  
  
  /**
   * Retrieves all food data.
   */
  public void retrieveLight() {
       
    if(this.getId().startsWith("FOOD-")) {
      this.retrieveBaseData();
    } else {
      this.retrieveRecipeFoods();
      for(Pair p : this.recipeFoods) {
        Food f = new Food(p.getK());
        f.retrieveLight();
        f.adaptFoodQuantity(Double.valueOf(p.getV()));
        if(p.getK().compareTo("FOOD-999999") != 0) this.ediblePart += Double.valueOf(p.getV());
        this.aggregateFoodEntity(f);
      }
    }
  }
  
  
  /**
   * Resets quantity data
   */
  public void reset() {
    this.adaptFoodQuantity(100.0);
  }
  
  
  
  /**
   * Methods used for populating the Food object
   */
  public void retrieveLabels() {
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?label ?langcode WHERE { " +
          VC.PREFIX + ":" + this.getId() + " rdfs:label ?label . " + 
          "BIND (LANG(?label) AS ?langcode)}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodLabel = bindingSet.getValue("label");
          Value labelLang = bindingSet.getValue("langcode");
          this.addLabel(labelLang.stringValue(), foodLabel.stringValue());
        }
        result.close();
      } 
      conn.close();
    } catch (Exception e) {
      logger.info("Error in retrieving labels of food {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  
  private void retrieveBaseData() {
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      
      /* Get the list of nutrients */
      String queryString = VC.SPARQL_PREFIX +
          "SELECT ?type ?ediblePart ?amountWater ?amountEnergy ?amountCalories WHERE { " + 
          VC.PREFIX + ":" + this.getId() + " rdf:type ?type ; " + 
          VC.PREFIX + ":ediblePart ?ediblePart ; " +
          VC.PREFIX + ":amountCalories ?amountCalories ; " + 
          VC.PREFIX + ":amountEnergy ?amountEnergy . " +
          "OPTIONAL {" + VC.PREFIX + ":" + this.getId() + " " + VC.PREFIX + ":amountWater ?amountWater .} " + 
          "filter(!strstarts(str(?type),str(owl:))) " +
          // "filter(!strstarts(str(?nutrientType),str(owl:))) " +
          "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value ediblePart = bindingSet.getValue("ediblePart");
          Value amountWater = bindingSet.getValue("amountWater");
          Value amountCalories = bindingSet.getValue("amountCalories");
          Value amountEnergy = bindingSet.getValue("amountEnergy");
          this.setEdiblePart(Double.valueOf(ediblePart.stringValue()));
          
          if(amountWater != null) this.setAmountWater(Double.valueOf(amountWater.stringValue()));
          else this.setAmountWater(0.0);
          
          this.setAmountCalories(Double.valueOf(amountCalories.stringValue()));
          this.setAmountEnergy(Double.valueOf(amountEnergy.stringValue()));
          // System.out.println(type.stringValue());
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error in retrieving base data of food {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  private void retrieveNutrients() {
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      /* Get the list of nutrients */
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?nutrientType ?amount WHERE { " + 
          VC.PREFIX + ":" + this.getId() + " rdf:type ?type ; " + 
          VC.PREFIX + ":hasNutrient ?nutrient . " +
          "?nutrient " + VC.PREFIX + ":amountNutrient ?amount ; " + 
          " rdf:type ?nutrientType . " + 
          "filter(!strstarts(str(?type),str(owl:))) " + 
          "filter(!strstarts(str(?nutrientType),str(owl:))) " + "}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value nutrientType = bindingSet.getValue("nutrientType");
          Value nutrientAmount = bindingSet.getValue("amount");

          String nutrientString = nutrientType.stringValue().substring(nutrientType.stringValue().indexOf('#') + 1);
          this.addNutrient(nutrientString, Double.valueOf(nutrientAmount.stringValue()));
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error in retrieving nutrients of food {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  private void retrieveFoodCategory() {
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?activeFlag WHERE { " +
          VC.PREFIX + ":" + this.getId() + " rdf:type ?type . " + 
          "OPTIONAL {?type " + VC.PREFIX + ":activeCategory ?activeFlag} . " +
          "filter(!strstarts(str(?type),str(owl:)))}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodType = bindingSet.getValue("type");
          Value activeFlag = bindingSet.getValue("activeFlag");

          String foodStringId = foodType.stringValue().substring(foodType.stringValue().indexOf('#') + 1);

          /*
          String categoryPath = this.parentCategories.get(foodStringId);
          String activeCategoryPath = this.activeCategoryPaths.get(foodStringId);
          if (categoryPath == null) {
            categoryPath = foodStringId;
            activeCategoryPath = new String("");
            if (activeFlag != null && activeFlag.toString().compareTo("1") == 0)
              activeCategoryPath = foodStringId;

            String[] nextCategoryPath = this.createCategoryPath(foodStringId);
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
            this.parentCategories.put(foodStringId, categoryPath);
            this.activeCategoryPaths.put(foodStringId, activeCategoryPath);
          }
          categoryPath = this.parentCategories.get(foodStringId);
          activeCategoryPath = this.activeCategoryPaths.get(foodStringId);
          ArrayList<String> categories = new ArrayList<String>(Arrays.asList(categoryPath.split(";")));
          ArrayList<String> activeCategories = new ArrayList<String>(Arrays.asList(activeCategoryPath.split(";")));
          */
          
          String categoryPath = new String();
          String activeCategoryPath = new String();
          categoryPath = foodStringId;
          activeCategoryPath = new String("");
          if (activeFlag != null && activeFlag.toString().compareTo("1") == 0)
            activeCategoryPath = foodStringId;

          String[] nextCategoryPath = this.createCategoryPath(foodStringId);
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
      logger.info("Error in retrieving categories of food {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
  }

  
  private String[] createCategoryPath(String seedCategory) {
    
    String[] categoryPath = new String[2];
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?type ?activeFlag WHERE { " +
          VC.PREFIX + ":" + seedCategory + " rdfs:subClassOf ?type . " + 
          "OPTIONAL {?type " + VC.PREFIX + ":activeCategory ?activeFlag} . " +
          "filter(!strstarts(str(?type),str(rdfs:)))}";

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

          // System.out.println(categoryPath);
          // categoryPath = categoryPath.concat(";" +
          // this.createCategoryPath(foodStringId));
        }
        result.close();
      }
      conn.close();
      return categoryPath;
    } catch (Exception e) {
      logger.info("Error in retrieving category path of food {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    return categoryPath;
  }
  
  
  
  public void retrieveRecipeFoods() {
   
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      String queryString = VC.SPARQL_PREFIX + 
          "SELECT ?edibleRecipe ?food ?quantity WHERE { " +
          "?recipe rdf:type " + VC.PREFIX + ":Recipe ; " +  
          " " + VC.PREFIX + ":id \"" + this.id + "\" ; " +
          " " + VC.PREFIX + ":hasRecipeFood ?recipeFood . " +
          "?recipeFood rdf:type " + VC.PREFIX + ":RecipeFood ; " + 
          " " + VC.PREFIX + ":hasFood ?food ; " +  
          " " + VC.PREFIX + ":amountFood ?quantity . " +
          "OPTIONAL {?recipe " + VC.PREFIX + ":amountFood ?edibleRecipe .}}";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(true);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value foodId = bindingSet.getValue("food");
          Value quantity = bindingSet.getValue("quantity");
          Value edibleRecipe = bindingSet.getValue("edibleRecipe");
          
          if(edibleRecipe != null) this.setEdiblePart(Double.valueOf(edibleRecipe.stringValue()));
          //logger.info(foodId.stringValue().substring(foodId.stringValue().indexOf('#') + 1) + " - " + quantity.stringValue());
          Pair p = new Pair(foodId.stringValue().substring(foodId.stringValue().indexOf('#') + 1), quantity.stringValue());
          this.recipeFoods.add(p);
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error in retrieving recipe foods for recipe {}. {} {}", this.id, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }  
  }
  
  
  
  
  
  
  
  
  public void aggregateFoodEntity(Food f) {
    this.amountCalories += f.getAmountCalories();
    this.amountEnergy += f.getAmountEnergy();
    this.amountWater += f.getAmountWater();
    //this.ediblePart += f.getEdiblePart();
    HashMap<String, Double> nutrientsToAggregate = f.getNutrients();
    Iterator<String> it = nutrientsToAggregate.keySet().iterator();
    while (it.hasNext()) {
      String n = it.next();
      if (this.nutrients.get(n) == null) {
        this.addNutrient(n, 0.0);
      }
      Double a = this.nutrients.get(n) + nutrientsToAggregate.get(n);
      this.addNutrient(n, a);
    }
  }

  
  public void adaptFoodQuantity(double foodQuantity) {
    if(foodQuantity == 0.0) return;
    double q = Double.valueOf(foodQuantity) / this.getEdiblePart();
    DecimalFormat formatter = new DecimalFormat("#0.00");
    this.setParentCategories(this.parentCategories);
    this.setEdiblePart(Double.valueOf(formatter.format(this.ediblePart * q)));
    this.setAmountCalories(Double.valueOf(formatter.format(this.amountCalories * q)));
    this.setAmountWater(Double.valueOf(formatter.format(this.amountWater * q)));
    this.setAmountEnergy(Double.valueOf(formatter.format(this.amountEnergy * q)));
    Iterator<String> it = this.nutrients.keySet().iterator();
    while (it.hasNext()) {
      String n = it.next();
      Double a = this.nutrients.get(n) * q;
      this.addNutrient(n, Double.valueOf(formatter.format(a)));
    }
    it = this.labels.keySet().iterator();
    while (it.hasNext()) {
      String n = it.next();
      String l = this.labels.get(n);
      this.addLabel(n, l);
    }
  }
}
