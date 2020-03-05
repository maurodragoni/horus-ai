package eu.fbk.ict.pdi.server;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.ict.pdi.server.app.JettyServer;
import eu.fbk.ict.pdi.server.config.HelisServerConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    final HelisServerConfig helisServerConfig = ConfigFactory.create(HelisServerConfig.class, System.getenv());
    final JettyServer jettyServer = new JettyServer(helisServerConfig);
    final Server server = jettyServer.getServer();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    try {
      // Calculate Jetty timeout via configuration interface
      final String startupTimeoutSeconds = helisServerConfig.getJettyStartupTimeoutSeconds();
      final Long timeoutLong = Long.valueOf(startupTimeoutSeconds);

      // Execute a new thread with a timer that will kill the process if Jetty
      // timeouts while starting
      Executors.newSingleThreadExecutor().submit(() -> {
        try {
          // Await max the given amount of time before giving up
          final boolean jettyStarted = countDownLatch.await(timeoutLong, TimeUnit.SECONDS);
          if (!jettyStarted) {
            // The server timed out, we kill the process
            logger.error("The Jetty server initialization timed out, the process will be killed");
            System.exit(1);
          }
        } catch (Exception e) {
          // no-op
        }
      });

      // Start the Jetty server
      server.start();
      // When the initialization is done, signal the timeout thread using the
      // countDown latch
      countDownLatch.countDown();

      logger.info("Jetty server listening on {}:{}{}", helisServerConfig.getListenAddress(),
        helisServerConfig.getJerseyPort(), helisServerConfig.getInternalBasePath());
      logger.info("Metrics servlet listening on {}:{}{}", helisServerConfig.getListenAddress(),
        helisServerConfig.getMetricsServerPort(), "/");
      server.join();
    } catch (Exception e) {
      logger.error("Error during Jetty server startup.", e);
      System.exit(1);
      throw e;
    } finally {
      server.stop();
    }
  }
}
