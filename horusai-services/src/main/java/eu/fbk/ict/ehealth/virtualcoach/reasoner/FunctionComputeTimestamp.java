package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC;

public class FunctionComputeTimestamp implements Function {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionComputeTimestamp.class);

  @Override
  public String getURI() {
    return VC.COMPUTE_TIMESTAMP.stringValue();
  }

  @Override
  public Value evaluate(final ValueFactory factory, final Value... args) throws ValueExprEvaluationException {

    Calendar c = new GregorianCalendar();
    
    final IRI timing = (IRI) args[0];
    final long ts = ((Literal) args[1]).longValue();

    final long timestamp;
    if (timing.equals(VC.MEAL) || timing.equals(VC.BREAKFAST) || timing.equals(VC.LUNCH) || timing.equals(VC.DINNER)
        || timing.equals(VC.SNACK)) {
      timestamp = ts;
    } else if (timing.equals(VC.DAY)) {
      //timestamp = Instant.ofEpochMilli(ts).truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS).toEpochMilli();
      int cY = c.get(Calendar.YEAR);
      int cM = c.get(Calendar.MONTH);
      int cD = c.get(Calendar.DAY_OF_MONTH);
      c.set(cY, cM, cD, 0, 0, 0);
      timestamp = c.getTimeInMillis();
    } else if (timing.equals(VC.WEEK)) {
      // timestamp =
      // Instant.ofEpochMilli(ts).truncatedTo(ChronoUnit.WEEKS).plus(1,
      // ChronoUnit.WEEKS).toEpochMilli();
      timestamp = ts - (1000 * 3600 * 24 * 7);
    } else if (timing.equals(VC.NOW)) {
      timestamp = System.currentTimeMillis();
    } else {
      throw new IllegalArgumentException("Unsupported timing: " + timing);
    }

    LOGGER.info("Computed timestamp {} for timing {} and ts {}", timestamp, timing, ts);

    return SimpleValueFactory.getInstance().createLiteral(timestamp);
  }

}