package eu.fbk.ict.ehealth.virtualcoach.reasoner;

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

public class FunctionMintNutrient implements Function {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionMintNutrient.class);

    @Override
    public String getURI() {
        return VC.MINT_NUTRIENT.stringValue();
    }

    @Override
    public Value evaluate(final ValueFactory factory, final Value... args)
            throws ValueExprEvaluationException {

        final String sampleIri = ((IRI) args[0]).stringValue();
        final double amount = ((Literal) args[1]).doubleValue();

        int index = sampleIri.length();
        while (index > 0 && Character.isDigit(sampleIri.charAt(index - 1))) {
            --index;
        }
        final String prefix = sampleIri.substring(0, index);

        final int integralPart = (int) amount;
        final int decimalPart = (int) (amount * 100) % 100;
        final String suffix = String.format("%d%02d", integralPart, decimalPart);

        IRI iri = SimpleValueFactory.getInstance().createIRI(prefix + suffix);

        LOGGER.trace("Minted {} for sample {} and amount {}", iri, sampleIri, amount);

        return iri;
    }

}