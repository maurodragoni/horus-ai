package eu.fbk.ict.ehealth.virtualcoach.helis.manager;

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

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Violation;
import eu.fbk.ict.ehealth.virtualcoach.interfaces.DataManager;
import eu.fbk.ict.ehealth.virtualcoach.webrequest.ViolationRequest;


public class ViolationDataManager implements DataManager {

  private static Logger logger;
  private HashMap<String, Violation> violations;
  
  
  public ViolationDataManager() {
    logger = LoggerFactory.getLogger(ViolationDataManager.class);
  }
  
  
  public String manage(String jsonPars) {
    this.violations = new HashMap<String, Violation>();
    Gson gson = new Gson();
    ViolationRequest req = gson.fromJson(jsonPars, ViolationRequest.class);
    this.getViolations(req);
    return gson.toJson(this.violations.values());
  }
  
  
  public String manage(ViolationRequest req) {
    this.violations = new HashMap<String, Violation>();
    Gson gson = new Gson();
    this.getViolations(req);
    return gson.toJson(this.violations.values());
  }
  
  

  private void getViolations(ViolationRequest req) {

    try (RepositoryConnection conn = VC.r.getConnection()) {

      String userFilter = new String("");
      if (req.getUserId() != null) {
        userFilter = " {FILTER EXISTS {?id " + VC.PREFIX + ":hasViolationUser " + VC.PREFIX + ":" + req.getUserId() + "}} ";
      }

      String timingFilter = new String("");
      if (req.getTiming() != null) {
        timingFilter = " ?meal rdf:type vc:" + req.getTiming() + " . ";
      }

      String startDateFilter = new String(" FILTER (?timestamp > 0)");
      if (req.getStartDate() != null) {
        startDateFilter = " FILTER (?timestamp > " + req.getStartDate() + ") ";
      }
      
      String ruleIdFilter = new String("?ruleId");
      if(req.getRuleId() != null) {
        ruleIdFilter = " FILTER (?ruleId = \"" + req.getRuleId() + "\") ";
      }

      String queryString = VC.SPARQL_PREFIX +
          "SELECT ?id ?user ?rule ?ruleId ?timestamp ?endTime ?startTime ?entityType ?entity ?goal ?goalconstraint ?timing ?quantity " +
          "       ?expectedQuantity ?meal ?priority ?level ?constraint ?history WHERE {" + 
          " ?id rdf:type " + VC.PREFIX + ":Violation ; " +
          VC.PREFIX + ":hasViolationUser ?user ; " + 
          VC.PREFIX + ":hasViolationRule ?rule ; " +
          VC.PREFIX + ":hasViolationRuleId ?ruleId ; " + 
          VC.PREFIX + ":hasTimestamp ?timestamp ; " +
          VC.PREFIX + ":hasViolationStartTime ?startTime ; " +
          VC.PREFIX + ":hasViolationEndTime ?endTime ; " +
          VC.PREFIX + ":hasViolationEntityType ?entityType ; " + 
          VC.PREFIX + ":hasViolationEntity ?entity ; " +
          VC.PREFIX + ":hasViolationGoal ?goal ; " +
          VC.PREFIX + ":hasViolationGoalConstraint ?goalconstraint ; " +
          VC.PREFIX + ":hasViolationTiming ?timing ; " +
          // " vc:hasViolationTiming vc:Meal ; " +
          VC.PREFIX + ":hasViolationQuantity ?quantity ; " + 
          VC.PREFIX + ":hasViolationExpectedQuantity ?expectedQuantity ; " +
          VC.PREFIX + ":hasViolationMeal ?meal ; " + 
          VC.PREFIX + ":hasViolationPriority ?priority ; " +
          VC.PREFIX + ":hasViolationLevel ?level ; " + 
          VC.PREFIX + ":hasViolationConstraint ?constraint ; " +
          VC.PREFIX + ":hasViolationHistory ?history . " +
          ruleIdFilter + 
          userFilter + 
          timingFilter + 
          startDateFilter + " }";

      logger.info(queryString);
      TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {
          BindingSet bindingSet = result.next();

          String uid = null;
          
          if (req.getUserId() == null) {
            uid = bindingSet.getValue("user").stringValue();
          }
          Value id = bindingSet.getValue("id");
          Value user = bindingSet.getValue("user");
          Value rule = bindingSet.getValue("rule");
          Value ruleId = bindingSet.getValue("ruleId");
          Value timestamp = bindingSet.getValue("timestamp");
          Value startTime = bindingSet.getValue("startTime");
          Value endTime = bindingSet.getValue("endTime");
          Value entityType = bindingSet.getValue("entityType");
          Value entity = bindingSet.getValue("entity");
          Value goal = bindingSet.getValue("goal");
          Value goalConstraint = bindingSet.getValue("goalconstraint");
          Value timing = bindingSet.getValue("timing");
          /*
           * if(timing == null) { timing =
           * bindingSet.getValue("timing").stringValue(); }
           */
          Value quantity = bindingSet.getValue("quantity");
          Value expectedQuantity = bindingSet.getValue("expectedQuantity");
          Value meal = bindingSet.getValue("meal");
          Value priority = bindingSet.getValue("priority");
          Value level = bindingSet.getValue("level");
          Value constraint = bindingSet.getValue("constraint");
          Value history = bindingSet.getValue("history");

          Violation v = this.violations.get(id.stringValue().substring(id.stringValue().indexOf("#") + 1));
          if (v == null) {
            v = new Violation();
          }
          v.setViolationId(id.stringValue().substring(id.stringValue().indexOf("#") + 1));
          v.setUser(uid.substring(uid.indexOf("#") + 1));
          v.setRule(rule.stringValue().substring(rule.stringValue().indexOf("#") + 1));
          v.setRuleId(ruleId.stringValue());
          v.setTimestamp(timestamp.stringValue());
          v.setStartTime(startTime.stringValue());
          v.setEndTime(endTime.stringValue());
          v.setEntityType(entityType.stringValue());
          v.setEntity(entity.stringValue().substring(entity.stringValue().indexOf("#") + 1));
          v.setTiming(timing.stringValue().substring(timing.stringValue().indexOf("#") + 1));
          v.setQuantity(quantity.stringValue());
          v.setExpectedQuantity(expectedQuantity.stringValue());
          v.addMeal(meal.stringValue().substring(meal.stringValue().indexOf("#") + 1));
          v.addGoal(goal.stringValue().substring(goal.stringValue().indexOf("#") + 1), goalConstraint.stringValue());
          v.setPriority(priority.stringValue());
          v.setLevel(level.stringValue());
          v.setConstraint(constraint.stringValue());
          v.setHistory(history.stringValue());
          this.violations.put(id.stringValue().substring(id.stringValue().indexOf("#") + 1), v);
        }
        result.close();
      }
    }
  }
  
}
