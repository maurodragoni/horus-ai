package eu.fbk.ict.ehealth.virtualcoach;

import com.google.gson.Gson;

import eu.fbk.ict.ehealth.virtualcoach.queue.Publisher;
import eu.fbk.ict.ehealth.virtualcoach.trec.SessionManager;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMeal;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealData;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMealDataFoods;
import eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema.CDSMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class TestClass {

  public static void main(String[] args) {

    Logger logger;
    logger = LoggerFactory.getLogger(TestClass.class);
    Gson gson = new Gson();

    /*
    String t = "{\"_id\":\"5aeaee89c399b00001433659\",\"type\":\"observation/meal/1\",\"patient_id\":\"1SuRMYoAVS4iedb0t9k699KySKPGjFUsT7ygz0uRbNHYnN0t8y\",\"metadata\":{\"start_date\":1525345905325,\"end_date\":1525345905325,\"entry_date\":1525345929572,\"provenance\":\"personal\",\"source\":\"lifestyle\",\"user_id\":\"1SuRMYoAVS4iedb0t9k699KySKPGjFUsT7ygz0uRbNHYnN0t8y\",\"server_date\":1525345929514},\"data\":{\"meal_type\":\"snack\",\"foods\":[{\"coding_system\":\"helis\",\"food_id\":\"RECIPE-13263\",\"quantity\":100,\"unit\":\"g\"}]},\"version\":1}";
    CDSMeal req = gson.fromJson(t, CDSMeal.class);
    System.out.println(gson.toJson(req));
    */
    
    try {
      VC.init();
      Publisher.init();
      Publisher.connect();
      // Consumer.init();
      // Consumer.connect();
      //ReasonerManager.init();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // ReasonerManager.getUserData("", "", "");
    // ReasonerManager.performLiveReasoning("MEAL", "test010", "demohelis");
    // ReasonerManager.performLiveReasoning("MEAL", "test002", "demohelis");
    // ReasonerManager.performLiveReasoning("MEAL", "test004", "demohelis");
    // ReasonerManager.performLiveReasoning("MEAL", "test008", "demohelis");

    /*
     * ReasonerManager.performLiveReasoning("ACTIVITY",
     * "demohelis-ACTIVITY-7024-1509114773167", "demohelis"); System.exit(0);
     */

    
    //Publisher.sendTestNotifications("{\"messageType\":\"ActivityDataManager\",\"message\":{\"mode\":\"save\",\"activities\":[{\"duration\":\"50\",\"activityId\":\"ACTIVITY-7024\"}],\"userId\":\"98e476W8p0GN7oYzgR88Qt4qGWRqj2w2wp2slcXAG18XdcjPK9\",\"timestamp\":1522233800000}}");
    Publisher.sendTestNotifications("{\"foods\":[{\"food\":\"TURCONI-332-100\"},{\"food\":\"CIRFOOD-809\"},{\"food\":\"FOOD-100230\"},{\"food\":\"TURCONI-220-160\"}],\"userId\":101209892}");
    System.exit(0);
    
    
    /**
     * SalusPlus Package Test
     */
    HashMap<String, String> header = new HashMap<String, String>();
    long ts = System.currentTimeMillis();
    int mealId = 1;
    //String jsonPars = new String();
    String result = new String();
    String url = new String();
    /*
     * try { for (int i = 0; i < 4; i++) { ts += 86400000; mealId += 1;
     * 
     * String quantity = "100.0"; //if(i == 1) quantity = "10.0"; jsonPars =
     * "{\"meals\":[{\"mealType\":\"Snack\",\"mealId\":\"test-" + ts +
     * "\",\"timestamp\":" + ts + "," // + "\"foods\":[{\"mealId\":\"test-" + ts
     * + "\",\"foodId\":\"FOOD-419\",\"foodQuantity\":" + quantity + "}]}]," +
     * "\"foods\":[{\"foodId\":\"FOOD-419\",\"foodQuantity\":" + quantity +
     * "}]}]," + "\"userId\":\"demohelis\",\"mode\":\"save\",\"timestamp\":" +
     * ts + "}"; url = new
     * String("http://localhost:8080/helis-service/MealManager"); result =
     * HttpCURLClient.post(url, header, jsonPars); logger.info(result);
     * 
     * Thread.sleep(5000);
     * 
     * jsonPars =
     * "{\"mode\": \"save\", \"userId\": \"demohelis\", \"timestamp\":" + ts +
     * ", " +
     * "\"activities\": [{ \"activityId\": \"Running\", \"duration\": 90 }]}";
     * url = new String("http://localhost:8080/helis-service/ActivityManager");
     * //if((i % 2) == 1) { result = HttpCURLClient.post(url, header, jsonPars);
     * logger.info(result); //}
     * 
     * Thread.sleep(5000);
     * 
     * jsonPars =
     * "{\"mode\": \"save\", \"userId\": \"demohelis\", \"timestamp\":" + ts +
     * ", " +
     * "\"activities\": [{ \"activityId\": \"ACTIVITY-7024\", \"duration\": 0 }]}"
     * ; url = new
     * String("http://localhost:8080/helis-service/ActivityManager"); result =
     * HttpCURLClient.post(url, header, jsonPars); logger.info(result);
     * 
     * Thread.sleep(5000);
     * 
     * } } catch(Exception e) { e.printStackTrace(); } System.exit(0);
     */

    /**
     * Test MealManager service
     */
    // ExecutorService threadPool = Executors.newFixedThreadPool(2);
    ts = System.currentTimeMillis();
    mealId = 1;
    for (int i = 0; i < 1; i++) {
      ts += i;
      mealId = mealId + i;

      /*
       * jsonPars = "{\"meals\":[{\"mealType\":\"Dinner\",\"mealId\":\"test" +
       * mealId + "\",\"timeStamp\":" + ts + "000," +
       * "\"foods\":[{\"mealId\":\"test" + mealId +
       * "\",\"foodId\":\"RECIPE-21189\",\"foodQuantity\":300.0}]}]," +
       * "\"userId\":\"demohelis\",\"mode\":\"save\",\"timeStamp\":" + ts +
       * "000}";
       */

      try {

        /**
         * Create test meal
         */
        CDSMealDataFoods food = new CDSMealDataFoods();
        food.setCoding_system("cir");
        food.setFood_id("CIRFOOD-959");
        food.setQuantity(0.0);
        food.setUnit("g");

        ArrayList<CDSMealDataFoods> foods = new ArrayList<CDSMealDataFoods>();
        foods.add(food);
        CDSMealData meal = new CDSMealData();
        meal.setMeal_type("dinner");
        meal.setFoods(foods);

        CDSMetadata metadata = new CDSMetadata();
        metadata.setEntry_date(ts + (1000000 * i));
        metadata.setStart_date(ts + (1000000 * i));
        metadata.setEnd_date(ts + (1000000 * i));
        metadata.setSource("LifeStyle");
        metadata.setProvenance("personal");

        CDSMeal mealPost = new CDSMeal();
        // long tsid = ts + (1000000 * i);
        // mealPost.set_id("dragotest-meal-" + tsid);
        mealPost.set_id("");
        mealPost.setType("observation/meal/1");
        mealPost.setPatient_id(SessionManager.getTokenTrec().getJWT());
        mealPost.setPatient_id(SessionManager.getTokenTrec().getTrecUserId());
        // mealPost.setPatient_id("7mCC2A0mmIZm0kB2z5C9zvBLRgA68pMIoaYxs4pBybCXU6htTi");
        // mealPost.setPatient_id(VC.trecUserId);
        mealPost.setMetadata(metadata);
        mealPost.setData(meal);

        /**
         * Send meal to Clinical Data service
         */
        header = new HashMap<String, String>();
        header.put("accept", "application/json");
        header.put("content-type", "application/json");
        header.put("X-TokenTreC", SessionManager.getTokenTrec().getJWT());
        // header.put("X-TokenTreC",
        // "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJON1BudG9adVBMcDhEWTJPaEhrUDc4YlAyWDgxalVYOHE3ZDMyYmVldzNFejRjODQ0UHRNVzU2cmMzNDJPNnZRV2w1dHlsNEtTWG4zUDQweTBSNTdvS1RPSW9jbUJHZDEybTMyIiwiaWF0IjoxNTA3OTAxNDIwLCJleHAiOjE1MDc5MDIzMjAsInN1YiI6IjdtQ0MyQTBtbUlabTBrQjJ6NUM5enZCTFJnQTY4cE1Jb2FZeHM0cEJ5YkNYVTZodFRpIiwiaXNzIjoiQVBTUyIsImF1ZCI6IlRyZUMiLCJUcmVjVXNlclR5cGUiOiJ1c2VyIiwibG9hIjoxfQ.b8J41UtBWuve70azkY3Pq2No3ZmZztSkjtZvdXTjG2fKIDWhExmzlND2QkVJRSUZf9sEIHg3b57ITog7Ya_gOZWEGhOFQHViu6Zr8T6-JRI8aX7wwTrc0eTj65yUuTl7ozSsIPRMm7Hgs7NqcQSVDi64V9s6qEH1AEQh7ioqyuDqzdSBADCw0_nnv0Busl028V98enXfCgR1bJurgd-SvO0pqHXx1MSf52-G_InVHTbtLMHc_CELBtXR7Fq4FyIYTv_JGIcCJkRyb8Qci7fq5yasNMhBMkl57FuAJbqNlLO9wnii9vyXaZdSlvCiA0Df__drZorxZYODsC0XbekcRY3MT7HolDWhCvepCTsc4Pdl4qsWTbpVlLMWq7gE62Ezi-stoLuEP6AOvB8JTDwnax3zfN9VqE2cfO24xP_w6eN6lL8u_6w6OVzxzZry9SQr5LK7BfBGck7-s4PlsooD_ElqN66XHjMCRZX1-UwxnX3EeGGrf_96m7rLn1G2qDsqTD56MhkHhWULsoIY9cJjRAek5Mu5oQdUK4FfLJtU_S2IjYB5_Ggzq1T8zw_KTB9Y2uNTyfjMNV0e78_7QONc6SZFzo8ce8fYGNi65xRYf3GrxOZbM6IGtBetChIzWIdEiq7Cc4pwuI8hjj7y0VyahXMiv6Sxd_WNXz3KZV4L4wk");
        // header.put("X-TokenTreC", VC.trecToken);

        // url = new String("http://localhost:8080/helis-service/MealManager");
        url = new String("https://docker-ehealth.fbk.eu/staging/api/clinical-data/v1/clinicaldata/observation/meal/1");

        // String meals = HttpCURLClient.post(url, header, jsonPars);
        logger.info(gson.toJson(mealPost));

        result = HttpCURLClient.post(url, header, gson.toJson(mealPost));
        logger.info(result);
        // IdBasedRequest id = gson.fromJson(result, IdBasedRequest.class);

        /**
         * Get the stored meal from Clinical Data service
         */
        /*
         * url = new String(
         * "https://docker-ehealth.fbk.eu/development/api/clinical-data/v1/clinicaldata/observation/meal/1/"
         * + id.get_id()); result = HttpCURLClient.get(url, header, "");
         * logger.info(result);
         */

        /**
         * Check user existance in HeLiS, otherwise create it
         */
        /*
         * UserProfileRequest ureq = new UserProfileRequest();
         * ureq.setUserId(tt.getTrecUserId()); ureq.setUsername("demohelis");
         * ureq.setGender("M"); ureq.setHeight(180); ureq.setWeight(78);
         * ureq.setAge(347963400000L); ArrayList<Profile> profiles = new
         * ArrayList<Profile>(); Profile p = new Profile("DEMOPROFILE");
         * p.setStartDate(347963400000L); profiles.add(p);
         * ureq.setProfiles(profiles); User u = new User(tt.getTrecUserId());
         * u.populate(ureq); boolean createFlag = u.store();
         */

        /**
         * Store the meal in HeliS and run the reasoner
         */
        /*
         * CDSMeal req = gson.fromJson(result, CDSMeal.class); metadata =
         * req.getMetadata(); meal = req.getData(); foods = meal.getFoods();
         * 
         * Meal m = new Meal(); m.setMealId(req.get_id());
         * m.setMealType(meal.getMeal_type().substring(0, 1).toUpperCase() +
         * meal.getMeal_type().substring(1)); m.setUserId(req.getPatient_id());
         * 
         * for(CDSMealDataFoods f : foods) { m.addConsumedFood(f.getFood_id(),
         * "", f.getQuantity()); } m.store(req.getPatient_id(), null);
         * 
         * ReasonerManager.threadPool.submit(new Runnable() {
         * 
         * @Override public void run() {
         * ReasonerManager.performLiveReasoning("MEAL", m.getMealId(),
         * req.getPatient_id()); } });
         */
      } catch (Exception e) {
        e.printStackTrace();
      }

      /*
       * MealRequest req = gson.fromJson(jsonPars, MealRequest.class);
       * ArrayList<Meal> meals = req.getMeals(); Meal m = meals.get(0);
       * //Publisher.send(gson.toJson(m), null); m.store(req.getUserId(), null);
       * 
       * //final ExecutorService executor = Executors.newSingleThreadExecutor();
       * ReasonerManager.threadPool.submit(new Runnable() {
       * 
       * @Override public void run() {
       * ReasonerManager.performLiveReasoning("MEAL", m.getMealId(),
       * req.getUserId()); } });
       */
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    logger.info("Thread queue loaded.");
  }
}