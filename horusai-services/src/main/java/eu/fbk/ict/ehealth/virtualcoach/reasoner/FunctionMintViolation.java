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

public class FunctionMintViolation implements Function {

	private static final Logger LOGGER = LoggerFactory.getLogger(FunctionMintViolation.class);

	@Override
	public String getURI() {
		return VC.MINT_VIOLATION.stringValue();
	}

	@Override
	public Value evaluate(final ValueFactory factory, final Value... args) throws ValueExprEvaluationException {

		final String ruleId = ((Literal) args[0]).stringValue();
		final String userId = ((Literal) args[1]).stringValue();
		final long mealTs = ((Literal) args[2]).longValue();

		final String iriString = String.format("%sviolation_%s_%s_%d", VC.NAMESPACE, userId.replaceAll("\\s+", "_"),
				ruleId.replaceAll("\\s+", "_"), mealTs);

		LOGGER.info("Minted {} for ruleId {}, userId {}, ts {}", iriString, ruleId, userId, mealTs);

		return SimpleValueFactory.getInstance().createIRI(iriString);
	}

}
