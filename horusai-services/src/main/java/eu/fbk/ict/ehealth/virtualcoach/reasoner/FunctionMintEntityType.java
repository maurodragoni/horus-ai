package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC;

public class FunctionMintEntityType implements Function {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionMintEntityType.class);

  @Override
  public String getURI() {
    return VC.MINT_ENTITYTYPE.stringValue();
  }

  @Override
  public Value evaluate(final ValueFactory factory, final Value... args) throws ValueExprEvaluationException {

    String entityType = args[0].stringValue();
    String computedEntityType = new String("FOODCATEGORY");
    if (entityType.contains("#FOOD-")) {
      computedEntityType = new String("FOOD");
    }
    LOGGER.info("mintEntityType function called - {}.", entityType);
    return SimpleValueFactory.getInstance().createLiteral(computedEntityType);
  }

}
