package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.rdfpro.util.Statements;

public class FunctionSeq implements Function {

    // This piece of code is really a bad hack :-(

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionSeq.class);

    private static final ThreadLocal<AtomicLong> COUNTER = new ThreadLocal<AtomicLong>();

    private static final ThreadLocal<Long> HASHES = new ThreadLocal<Long>();

    @Override
    public String getURI() {
        return VC.SEQ.stringValue();
    }

    @Override
    public Value evaluate(final ValueFactory factory, final Value... args)
            throws ValueExprEvaluationException {

        AtomicLong counter = COUNTER.get();
        if (counter == null) {
            counter = new AtomicLong();
            COUNTER.set(counter);
        }

        final Hasher hasher = Hashing.sipHash24().newHasher();
        for (final Value arg : args) {
            hasher.putByte((byte) 0);
            hasher.putUnencodedChars(Statements.formatValue(arg));
        }
        final Long hash = hasher.hash().asLong();

        final Long oldHash = HASHES.get();
        if (oldHash == null || !oldHash.equals(hash)) {
            counter.set(0);
            HASHES.set(hash);
        }

        final long value = counter.incrementAndGet();

        LOGGER.trace("Generated {} for {}", counter, Arrays.asList(args));

        return SimpleValueFactory.getInstance().createLiteral(value);
    }

}
