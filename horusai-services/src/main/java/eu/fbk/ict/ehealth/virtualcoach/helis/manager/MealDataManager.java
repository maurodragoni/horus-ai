package eu.fbk.ict.ehealth.virtualcoach.helis.manager;

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
import eu.fbk.ict.ehealth.virtualcoach.helis.core.ConsumedFood;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Food;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Meal;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.reasoner.ReasonerManager;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.MealRequest;


public class MealDataManager implements DataManager {

  private static Logger logger;
  
  public MealDataManager() {
    logger = LoggerFactory.getLogger(MealDataManager.class);
  }
  
  
  
  public String manage(String jsonPars) {
    logger.info("Meal management request received.");
    Gson gson = new Gson();
    MealRequest req = gson.fromJson(jsonPars, MealRequest.class);
    logger.info("Meal management request parsed. Mode: " + req.getMode());
    return this.manage(req);
  }
  
  
  public String manage(MealRequest req) {
    
    Gson gson = new Gson();
    ArrayList<Meal> meals = req.getMeals();

    /* Checks the composition of a single meal */
    if(req.getMode().compareTo("check") == 0) {
      
      Meal m = meals.get(0);
      /*
      ArrayList<ConsumedFood> foods = m.getFoods();
      Food mealAggregator = new Food("CurrentMeal");
      for (ConsumedFood mf : foods) {
        String foodId = mf.getFoodId();
        Food f = new Food(foodId);
        f.retrieve();
        f.adaptFoodQuantity(mf.getFoodQuantity());
        logger.info(gson.toJson(f));
        mealAggregator.aggregateFoodEntity(f);
      }
      m.setCalories(mealAggregator.getAmountCalories());
      m.setCarbs(mealAggregator.getNutrient("Carbs"));
      m.setProteins(mealAggregator.getNutrient("Protein"));
      m.setLipids(mealAggregator.getNutrient("Lipid"));
      */
      m.check();
      return gson.toJson(m);
      
      
    /* Saves the request into the knowledge repository */
    } else if(req.getMode().compareTo("save") == 0) {
      
      ArrayList<String> mealIds = new ArrayList<String>();
      //Meal m = meals.get(0);
      for(Meal m : meals) {
        m.check();
        m.setUserId(req.getUserId());
        m.store(req.getUserId());
        mealIds.add(m.getMealId());
      }
      
      //Publisher.sendMeal(gson.toJson(m));
      /*
      final ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.execute(new Runnable() {
        @Override
        public void run() {
          runReasoner(request, response, req.getUserId(), m.getMealId());
        }
      });
      */
      
      ReasonerManager.threadPool.submit(new Runnable() {
        @Override
        public void run() {
          ReasonerManager.performLiveReasoning("MEAL", mealIds, req.getUserId());
        }
      });
      
      return gson.toJson(mealIds);
      
    /* Retrieves a set of meals from the knowledge repository */
    } else if (req.getMode().compareTo("get") == 0) {
      
      HashMap<String, Meal> m = this.getUserMeals(req);
      Iterator<String> itMeals = m.keySet().iterator();
      while(itMeals.hasNext()) {
        String mealId = (String) itMeals.next();
        Meal cm = m.get(mealId);
        ArrayList<ConsumedFood> foods = cm.getFoods();
        Food mealAggregator = new Food("CurrentMeal");
        for (ConsumedFood mf : foods) {
          String foodId = mf.getFoodId();
          Food f = new Food(foodId);
          f.retrieve();
          f.adaptFoodQuantity(mf.getFoodQuantity());
          logger.info(gson.toJson(f));
          mealAggregator.aggregateFoodEntity(f);
        }
        cm.setCalories(mealAggregator.getAmountCalories());
        cm.setCarbs(mealAggregator.getNutrient("Carbs"));
        cm.setProteins(mealAggregator.getNutrient("Protein"));
        cm.setLipids(mealAggregator.getNutrient("Lipid"));
        m.put(mealId, cm);
      }
      return gson.toJson(m);
    }

    return null;
  }
  
  
  
  
  private HashMap<String, Meal> getUserMeals(MealRequest request) {

    HashMap<String, Meal> meals = new HashMap<String, Meal>();
    String userId = request.getUserId();
    long timestamp = 0;
    if (request.getTimestamp() != null) {
      timestamp = Long.parseLong(request.getTimestamp());
    }

    String query = VC.SPARQL_PREFIX +
        "SELECT ?meal ?mealType ?mealTime ?foodMeal ?foodEntity ?foodQuantity ?foodLabel " + 
        "WHERE { " + 
        " vc:" + userId + " vc:consumed ?meal . " + 
        " ?meal rdf:type ?mealType . " + 
        " ?meal vc:hasTimestamp ?mealTime . " +
        " ?meal vc:hasConsumedFood ?foodMeal . " + 
        " ?foodMeal vc:amountFood ?foodQuantity . " +
        " ?foodMeal vc:hasFood ?foodEntity . " + 
        " ?foodEntity rdfs:label ?foodLabel . " + 
        " FILTER(?mealTime > " + timestamp + ") " + " FILTER(LANG(?foodLabel) = \"\" || LANGMATCHES(LANG(?foodLabel), \"it\"))}";

    logger.debug(query);
    
    RepositoryConnection conn = null;
    try {
      conn = VC.r.getConnection();
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value meal = bindingSet.getValue("meal");
          Value mealType = bindingSet.getValue("mealType");
          Value mealTime = bindingSet.getValue("mealTime");
          Value foodMeal = bindingSet.getValue("foodMeal");
          Value foodEntity = bindingSet.getValue("foodEntity");
          Value foodQuantity = bindingSet.getValue("foodQuantity");
          Value foodLabel = bindingSet.getValue("foodLabel");
  
          logger.debug(meal + " - " + mealType + " - " + foodMeal + " - " + foodLabel + " - " + foodQuantity);
          Meal m = meals.get(meal.stringValue());
          if (m == null) {
            m = new Meal();
            m.setMealId(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
            m.setMealType(mealType.stringValue().substring(mealType.stringValue().indexOf("#") + 1));
            m.setTimestamp(Long.valueOf(mealTime.stringValue()));
          }
          m.addConsumedFood(foodEntity.stringValue().substring(foodMeal.stringValue().indexOf("#") + 1),
                            foodLabel.stringValue(), Double.valueOf(foodQuantity.stringValue()));
          meals.put(meal.stringValue(), m);
        }
        result.close();
      }
      conn.close();
    } catch (Exception e) {
      logger.info("Error during the retrieval of the meals for the user {}. {} {}", userId, e.getMessage(), e);
      logger.info(Arrays.toString(e.getStackTrace()));
      if(conn != null) {
        conn.close();
      }
    }
    
    return meals;
  }
  
  
  
  /*
  private boolean deleteMeal(String mealId) {
    String query = VC.SPARQL_PREFIX + 
        "DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . " + 
        "FILTER (?s = " + VC.PREFIX + ":MEAL-" + mealId + " || ?v = " + VC.PREFIX + ":MEAL-" + mealId + ")}";

    logger.info(query);
    VC.c.begin();
    UpdateUtil.executeUpdate(VC.r.getConnection(), query);
    VC.c.commit();
    return true;
  }

  
  
  private boolean deleteConsumedFood(String foodId, String mealId, String mealType, long timestamp, String userId) {
    String foodToDelete = VC.PREFIX + ":" + mealType + "-" + userId + "-" + mealId + "-" + foodId;
    String query = VC.SPARQL_PREFIX + 
        "DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . " +
        "FILTER (?s = " + foodToDelete + ")}";

    logger.info(query);
    VC.c.begin();
    UpdateUtil.executeUpdate(VC.r.getConnection(), query);
    VC.c.commit();
    return true;
  }
  */
  
}
