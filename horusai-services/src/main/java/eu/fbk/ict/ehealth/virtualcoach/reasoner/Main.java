package eu.fbk.ict.ehealth.virtualcoach.reasoner;

import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.rdfpro.RDFHandlers;
import eu.fbk.rdfpro.RDFSource;
import eu.fbk.rdfpro.RDFSources;
import eu.fbk.rdfpro.util.IO;
import eu.fbk.rdfpro.util.QuadModel;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.Environment;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /*
    public static void main(final String... args) {
        try {
            // Parse command line
            final CommandLine cmd = CommandLine.parser()
                    .withOption("o", "ontology", "specifies the PATH of the ontology file", "PATH",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("m", "monitoring-rules",
                            "specifies the PATH of the monitoring rules file", "PATH...",
                            CommandLine.Type.FILE_EXISTING, false, true, true)
                    .withOption("d", "data", "specifies the PATH of the data files to process",
                            "PATH...", CommandLine.Type.FILE_EXISTING, false, true, true)
                    .withOption("r", "report",
                            "specifies the PATH of the optional TSV report to generate", "PATH",
                            CommandLine.Type.FILE, true, false, false)
                    .withOption("e", "explode", "specifies whether to explode the ruleset")
                    .withOption("t", "throughput-test",
                            "perform a test of throughput (users/h) for data files with 1 user")
                    .withLogger(LoggerFactory.getLogger("eu.fbk.virtualcoach")).parse(args);

            // Read options
            final Path ontologyPath = cmd.getOptionValue("o", Path.class);
            final List<Path> monitoringRulesPaths = cmd.getOptionValues("m", Path.class);
            final List<Path> dataPaths = cmd.getOptionValues("d", Path.class);
            final Path reportPath = cmd.getOptionValue("r", Path.class);
            final boolean explode = cmd.hasOption("e");
            final boolean testThroughput = cmd.hasOption("t");

            // Load ontology
            final QuadModel ontology = loadRDF(ontologyPath.toString());

            // Open the report file, if specified
            Writer writer = null;
            if (reportPath != null) {
                writer = IO.utf8Writer(IO.buffer(IO.write(reportPath.toString())));
                writer.write("rule_file\trule_triples\trule_count\t"
                        + "data_file\tdata_triples\tdata_users\tdata_meals\t"
                        + "inf_file\tinf_triples\tinf_violations\ttime\tthroughput\n");
                writer.flush();
            }

            // Iterate over all monitoring rules files
            for (final Path monitoringRulesPath : monitoringRulesPaths) {

                // Load monitoring rules
                final QuadModel monitoringRules = loadRDF(monitoringRulesPath.toString());
                final int ruleTriples = monitoringRules.size();
                final int ruleCount = monitoringRules.filter(null, RDF.TYPE, VC.MONITORING_RULE)
                        .size();

                // Create the engine
                //final Engine engine = new Engine(ontology, monitoringRules, explode);
                final Engine engine = new Engine(ontology, explode, null);

                // Apply the engine to each input file
                for (final Path dataPath : dataPaths) {

                    // Log operation
                    LOGGER.info("Processing {} with rules {}", dataPath.getFileName(),
                            monitoringRulesPath.getFileName());

                    // Compute output path
                    final Path parentPath = dataPath.toAbsolutePath().getParent();
                    final Path outputPath = parentPath
                            .resolve("output." + dataPath.getFileName().toString());

                    // Load input data
                    final QuadModel data = loadRDF(dataPath.toString());
                    final int dataTriples = data.size();
                    final int dataUsers = data.filter(null, RDF.TYPE, VC.USER).size();
                    final int dataMeals = data.filter(null, RDF.TYPE, VC.BREAKFAST).size()
                            + data.filter(null, RDF.TYPE, VC.LUNCH).size()
                            + data.filter(null, RDF.TYPE, VC.DINNER).size()
                            + data.filter(null, RDF.TYPE, VC.SNACK).size();

                    // Evaluate rules
                    final long ts = System.currentTimeMillis();
                    final QuadModel inf = engine.process(data);
                    final long time = System.currentTimeMillis() - ts;
                    final int infTriples = inf.size();
                    final int infViolations = inf.filter(null, RDF.TYPE, VC.VIOLATION).size();

                    // Evaluate throughput, if possible
                    double throughput = Double.NaN;
                    if (testThroughput && dataUsers == 1) {
                        final int reps = Math.max(10, (int) (60000L / time));
                        final List<Runnable> tasks = Lists.newArrayList();
                        final AtomicLong fakeCounter = new AtomicLong();
                        for (int i = 0; i < reps; ++i) {
                            tasks.add(() -> {
                                fakeCounter.addAndGet(engine.process(data).size());
                            });
                        }
                        final long ts2 = System.currentTimeMillis();
                        LOGGER.info("Evaluating throughput by repeating test {} times", reps);
                        Environment.run(tasks);
                        final long time2 = System.currentTimeMillis() - ts2;
                        throughput = 60000.0 / time2 * reps;
                    }

                    // Write output
                    writeRDF(outputPath.toString(), inf);

                    // Write report
                    if (writer != null) {
                        writer.write(monitoringRulesPath.getFileName().toString());
                        writer.write("\t");
                        writer.write(Integer.toString(ruleTriples));
                        writer.write("\t");
                        writer.write(Integer.toString(ruleCount));
                        writer.write("\t");
                        writer.write(dataPath.getFileName().toString());
                        writer.write("\t");
                        writer.write(Integer.toString(dataTriples));
                        writer.write("\t");
                        writer.write(Integer.toString(dataUsers));
                        writer.write("\t");
                        writer.write(Integer.toString(dataMeals));
                        writer.write("\t");
                        writer.write(outputPath.getFileName().toString());
                        writer.write("\t");
                        writer.write(Integer.toString(infTriples));
                        writer.write("\t");
                        writer.write(Integer.toString(infViolations));
                        writer.write("\t");
                        writer.write(Long.toString(time));
                        writer.write("\t");
                        writer.write(Double.toString(throughput));
                        writer.write("\n");
                        writer.flush();
                    }
                }
            }

        } catch (final Throwable ex) {
            // Log exception and terminate
            CommandLine.fail(ex);
        }
    }
    */

    private static QuadModel loadRDF(final String location) {
        final long ts = System.currentTimeMillis();
        final QuadModel model = QuadModel.create();
        final RDFSource source = RDFSources.read(true, true, null, null, null, true, location);
        source.emit(RDFHandlers.wrap(model), 1);
        LOGGER.info("Loaded {} statements from {} in {} ms", model.size(), location,
                System.currentTimeMillis() - ts);
        return model;
    }

    private static void writeRDF(final String location, final QuadModel model) {
        final long ts = System.currentTimeMillis();
        final RDFSource source = RDFSources.wrap(model);
        final RDFHandler handler = RDFHandlers.write(null, 1000, location);
        try {
            source.emit(handler, 1);
        } finally {
            IO.closeQuietly(handler);
        }
        LOGGER.info("Written {} statements to {} in {} ms", model.size(), location,
                System.currentTimeMillis() - ts);
    }

}
