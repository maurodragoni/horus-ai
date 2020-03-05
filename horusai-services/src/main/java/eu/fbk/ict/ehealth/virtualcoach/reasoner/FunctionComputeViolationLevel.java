package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC;

public class FunctionComputeViolationLevel implements Function {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionComputeViolationLevel.class);

  @Override
  public String getURI() {
    return VC.COMPUTE_VIOLATIONLEVEL.stringValue();
  }

  @Override
  public Value evaluate(final ValueFactory factory, final Value... args) throws ValueExprEvaluationException {

    int violationLevel = 0;
    double expectedQuantity = ((Literal) args[0]).longValue();
    double actualQuantity = ((Literal) args[1]).longValue();

    int violationAbsolute = (int) ((((actualQuantity - expectedQuantity) / expectedQuantity)) * 100.0);

    LOGGER.info("Evaluating violation level: {}", violationAbsolute);

    if (violationAbsolute < -50)
      violationLevel = -4;
    else if (violationAbsolute >= -50 && violationAbsolute <= -21)
      violationLevel = -3;
    else if (violationAbsolute >= -20 && violationAbsolute <= -11)
      violationLevel = -2;
    else if (violationAbsolute >= -10 && violationAbsolute <= -5)
      violationLevel = -1;
    else if (violationAbsolute >= -4 && violationAbsolute <= 4)
      violationLevel = 0;
    else if (violationAbsolute >= 5 && violationAbsolute <= 10)
      violationLevel = 1;
    else if (violationAbsolute >= 11 && violationAbsolute <= 20)
      violationLevel = 2;
    else if (violationAbsolute >= 21 && violationAbsolute <= 50)
      violationLevel = 3;
    else if (violationAbsolute > 50)
      violationLevel = 4;

    LOGGER.info("Computed violation level of {} for actualQuantity {} and expectedQuantity {}", violationLevel,
        actualQuantity, expectedQuantity);

    return SimpleValueFactory.getInstance().createLiteral(violationLevel);
  }

}