package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.VC;


public class Meal {

  private static Logger logger;
  private String mealId;
  private String mealType;
  private long timestamp;
  private String userId;
  private double calories;
  private double carbs;
  private double lipids;
  private double proteins;
  private ArrayList<ConsumedFood> foods;

  public Meal() {
    logger = LoggerFactory.getLogger(Meal.class);
  }

  public String getMealId() {
    return mealId;
  }

  public void setMealId(String mealId) {
    this.mealId = mealId;
  }

  public String getMealType() {
    return mealType;
  }

  public void setMealType(String mealType) {
    this.mealType = mealType;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public double getCalories() {
    return calories;
  }

  public void setCalories(double calories) {
    this.calories = calories;
  }

  public double getCarbs() {
    return carbs;
  }

  public void setCarbs(double carbs) {
    this.carbs = carbs;
  }

  public double getLipids() {
    return lipids;
  }

  public void setLipids(double lipids) {
    this.lipids = lipids;
  }

  public double getProteins() {
    return proteins;
  }

  public void setProteins(double proteins) {
    this.proteins = proteins;
  }

  public ArrayList<ConsumedFood> getFoods() {
    return foods;
  }

  public void setFoods(ArrayList<ConsumedFood> foods) {
    this.foods = foods;
  }

  public void addConsumedFood(String foodId, String foodLabel, double foodQuantity) {
    if (this.foods == null) {
      this.foods = new ArrayList<ConsumedFood>();
    }
    ConsumedFood c = new ConsumedFood();
    // c.setMealId(this.mealId);
    c.setTimestamp(this.timestamp);
    c.setFoodId(foodId);
    c.setFoodLabel(foodLabel);
    c.setFoodQuantity(foodQuantity);
    this.foods.add(c);
  }
  
  
  
  
  
  public boolean store(String userId) {
    String mealType = this.getMealType();
    String mealId = this.getMealId();
    long timestamp = this.getTimestamp();
    if(timestamp == 0) timestamp = System.currentTimeMillis();
    ArrayList<ConsumedFood> foods = this.getFoods();

    /**
     * Normalizing foods' quantities by aggregating quantities referring to the same food
     */
    HashMap<String, Double> normalizedFoods = new HashMap<String, Double>();
    for (ConsumedFood mf : foods) {
      String foodId = mf.getFoodId();
      
      if(!foodId.startsWith("FOOD-")) {
        Food recipe = new Food(mf.getFoodId());
        double recipeQuantity = mf.getFoodQuantity();
        recipe.retrieveRecipeFoods();
        ArrayList<Pair> recipeFoods = recipe.getRecipeFoods();
        double baseRecipeQuantity = 0.0;
        for(Pair p : recipeFoods) {
          if(p.getK().compareTo("FOOD-999999") == 0) continue;
          baseRecipeQuantity += Double.valueOf(p.getV());
        }
        double recipeMultiplier = recipeQuantity / baseRecipeQuantity;
        for(Pair p : recipeFoods) {        
          Double currentQuantity = normalizedFoods.get(p.getK());
          if (currentQuantity == null) {
            currentQuantity = Double.valueOf(p.getV()) * recipeMultiplier;
          } else {
            currentQuantity += (Double.valueOf(p.getV()) * recipeMultiplier);
          }
          normalizedFoods.put(p.getK(), currentQuantity);
        }
      } else {
        double foodQuantity = mf.getFoodQuantity();
        Double currentQuantity = normalizedFoods.get(foodId);
        if (currentQuantity == null) {
          currentQuantity = Double.valueOf(foodQuantity);
        } else {
          currentQuantity += foodQuantity;
        }
        normalizedFoods.put(foodId, currentQuantity);
      }
    }

    String user = "vc:" + userId;
    //String context = "GRAPH <" + user + "-MEAL-" + mealId + ">";
    String context = "GRAPH <" + user + "-CONSUMED-MEALS>";
    //if (this.checkUserExist(user, context, userId, response)) {

    // Delete previous information
    /*
    for (ConsumedFood mf : foods) {
      this.deleteConsumedFood(mf.getFoodId(), mealId, mealType, timestamp, userId);
    }
    this.deleteMeal(mealId);
    */
    
    String query = VC.SPARQL_PREFIX + "INSERT DATA " + "{ " + context + " " + "{ " +
                   VC.PREFIX + ":MEAL-" + mealId + " rdf:type " + VC.PREFIX + ":" + mealType + " ; ";

    for (Map.Entry<String, Double> mf : normalizedFoods.entrySet()) {
      String foodId = "vc:" + mealType + "-" + userId + "-" + mealId + "-" + mf.getKey();
      query += "  " + VC.PREFIX + ":hasConsumedFood " + foodId + " ; ";
    }
    query += "  " + VC.PREFIX + ":hasTimestamp " + timestamp + " . ";
    query += user + " " + VC.PREFIX + ":consumed " + VC.PREFIX + ":MEAL-" + mealId + " . ";

   
    for (Map.Entry<String, Double> mf : normalizedFoods.entrySet()) {
      String foodId = mf.getKey();
      String foodEntity = "vc:" + mealType + "-" + userId + "-" + mealId + "-" + foodId;
      double foodQuantity = mf.getValue();
      query += foodEntity + " rdf:type vc:ConsumedFood ; " + "  " + VC.PREFIX + ":amountFood " + foodQuantity
          + " ; " + "  " + VC.PREFIX + ":hasFood " + VC.PREFIX + ":" + foodId + " . ";
    }

    query += "}}";

    logger.info(query);
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      conn.begin();
      UpdateUtil.executeUpdate(conn, query);
      conn.commit();
      conn.close();
    } catch (Exception e) {
      logger.info("Error in saving Meal. {} {}", e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    

    /*
    try {
      response.getWriter().append(query);
    } catch (IOException e) {
      e.printStackTrace();
    }
    //}
    */

    return true;
  }
  
  
  
  public void check() {
    Gson gson = new Gson();
    ArrayList<ConsumedFood> foods = this.getFoods();
    Food mealAggregator = new Food("CurrentMeal");
    for (ConsumedFood mf : foods) {
      String foodId = mf.getFoodId();
      Food f = new Food(foodId);
      f.retrieve();
      f.adaptFoodQuantity(mf.getFoodQuantity());
      logger.info(gson.toJson(f));
      mealAggregator.aggregateFoodEntity(f);
    }
    this.setCalories(mealAggregator.getAmountCalories());
    this.setCarbs(mealAggregator.getNutrient("Carbs"));
    this.setProteins(mealAggregator.getNutrient("Protein"));
    this.setLipids(mealAggregator.getNutrient("Lipid"));
  }
}
