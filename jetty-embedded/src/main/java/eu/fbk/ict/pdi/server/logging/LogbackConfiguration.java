package eu.fbk.ict.pdi.server.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import eu.fbk.ict.pdi.server.config.HelisServerConfig;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Logback Configuration. This class is used by Logback to configure logging
 * instead of use the logback.xml. Reads the environment to check whether to use
 * JSON or normal text as an output. NOTE: this class is not final so that the
 * projects using this library can further customize their logs
 * 
 * @author Andrea Zorzi (azorzi@fbk.eu)
 */

// @SuppressWarnings({"unused", "unchecked"})
public class LogbackConfiguration extends ContextAwareBase implements Configurator {

  /**
   * Loggers to disable to keep the logging clean.
   */
  private static List<String> LOGGERS_BLACK_LIST = Arrays.asList("org.reflections.Reflections", "ch.qos.logback",
      "io.swagger", "org.flywaydb", "org.eclipse.jetty", "com.zaxxer.hikari", "org.mongodb.driver.cluster",
      "org.mongodb.driver.connection", "org.mongodb.driver.protocol.command");

  private static List<String> LOGGERS_ERROR_LIST = Arrays.asList("org.apache.http");

  private static List<String> LOGGERS_INFO_LIST = Arrays.asList("eu.fbk.ict.ehealth.virtualcoach.helis");

  @Override
  public void configure(LoggerContext loggerContext) {

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
    levelChangePropagator.setContext(loggerContext);

    // log to the console
    final ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<ILoggingEvent>();
    logConsoleAppender.setContext(loggerContext);

    // read from the environment whether to use JSON or String format
    boolean enableJsonLogging = ConfigFactory.create(HelisServerConfig.class, System.getenv()).enableJsonLogging();

    // set Json Output for Logstash
    if (enableJsonLogging) {

      // correct order for Java Exceptions
      final ShortenedThrowableConverter shortenedThrowableConverter = new ShortenedThrowableConverter();
      shortenedThrowableConverter.setRootCauseFirst(true);

      final LogstashEncoder logstashEncoder = new LogstashEncoder();

      // customize the fields logged in JSON
      LogstashFieldNames logstashFieldNames = new LogstashFieldNames();
      // disable logs of the level value
      logstashFieldNames.setLevelValue(null);

      logstashEncoder.setFieldNames(logstashFieldNames);
      logstashEncoder.setThrowableConverter(shortenedThrowableConverter);
      logstashEncoder.start();
      logConsoleAppender.setEncoder(logstashEncoder);
    }

    // set the common encoder
    else {
      PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
      logEncoder.setContext(loggerContext);
      // logEncoder.setPattern("%-23(%d{HH:mm:ss.SSS} [%thread])
      // %highlight(%-5level) %-40(%logger{36} %-3line) %highlight(%msg) %n");
      logEncoder.setPattern("%-4r [%t] %-5p %c - %m%n");
      logEncoder.start();
      logConsoleAppender.setEncoder(logEncoder);
    }

    // start to log
    logConsoleAppender.start();

    // root Logger -> apply to every logger
    final Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    root.addAppender(logConsoleAppender);
    root.setLevel(Level.INFO);

    // set fine-grained logging levels for each logger
    LOGGERS_BLACK_LIST.forEach(name -> loggerContext.getLogger(name).setLevel(Level.WARN));
    LOGGERS_INFO_LIST.forEach(name -> loggerContext.getLogger(name).setLevel(Level.INFO));
    LOGGERS_ERROR_LIST.forEach(name -> loggerContext.getLogger(name).setLevel(Level.ERROR));
  }
}
