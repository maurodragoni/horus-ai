package eu.fbk.ict.pdi.server.app;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import eu.fbk.ict.pdi.server.config.HelisServerConfig;

/**
 * Base Jetty server to host a Jersey application. It supports HTTP/2.
 *
 * @author Carlo Mion
 */
@SuppressWarnings("WeakerAccess")
public class JettyServer {

  private final int metricsPort;
  private final String address;
  private final int port;
  private final String warPath;

  private Server server;

  /**
   * Create a Jetty server configured to expose a Jersey application
   *
   */
  public JettyServer(HelisServerConfig serverConfig) {
    this.address = serverConfig.getListenAddress();
    this.port = serverConfig.getJerseyPort();
    this.warPath = serverConfig.getWarPath();

    this.metricsPort = serverConfig.getMetricsServerPort();

    this.server = createServer();
  }

  /**
   * Create a HTTP server using Jetty embedded, that contains a Jersey
   * application It supports HTTP/1 and HTTP/2.
   *
   * @return HTTP server.
   */
  private Server createServer() {
    final Server server = new Server();

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //// JETTY SERVLET CONFIGURATION
    /////////////////////////////////////////////////////////////////////////////////////////////////
    final WebAppContext helisWebApp = new WebAppContext("helis", "/");

    // Set the location of the war containing Helis, as configured using the
    // HelisServerConfig interface
    helisWebApp.setWar(warPath);

    helisWebApp.setVirtualHosts(new String[] { "@helisExternalConnector" });

    // Disable jar scanning, otherwise the war will not load properly
    helisWebApp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "^$");
    helisWebApp.setParentLoaderPriority(true);

    // Launch exception if the war fails to deploy
    helisWebApp.setThrowUnavailableOnStartupException(true);
    // Launch exception if servlet fails to initialize
    helisWebApp.getServletHandler().setStartWithUnavailable(false);

    //Initialize some default JVM metrics from Prometheus Java library
    DefaultExports.initialize();

    // Create a servlet handler for Prometheus Metrics
    final ServletContextHandler metricsContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    metricsContextHandler.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
    metricsContextHandler.setVirtualHosts(new String[] { "@metricsConnector" });

    // Attach handlers to server
    server.setHandler(new HandlerList(helisWebApp, metricsContextHandler));

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //// JETTY HTTP CONNECTOR CONFIGURATION
    /////////////////////////////////////////////////////////////////////////////////////////////////

    // suppress the "Server" header
    final HttpConfiguration config = new HttpConfiguration();
    config.setSendServerVersion(false);
    config.setSendXPoweredBy(false);

    // support both HTTP/1 and HTTP/2
    final HttpConnectionFactory http1 = new HttpConnectionFactory(config);
    final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(config);

    // Connector that will process requests from clients
    final ServerConnector jerseyExternalConnector = new ServerConnector(server, http1, http2c);
    jerseyExternalConnector.setPort(port);
    jerseyExternalConnector.setHost(address);
    jerseyExternalConnector.setName("helisExternalConnector");

    // Connector that will process requests from internal monitoring system
    final ServerConnector metricsInternalConnector = new ServerConnector(server, http1, http2c);
    metricsInternalConnector.setPort(metricsPort);
    metricsInternalConnector.setHost(address);
    metricsInternalConnector.setName("metricsConnector");

    server.addConnector(jerseyExternalConnector);
    server.addConnector(metricsInternalConnector);
    // Stop the server with the JVM
    // Handle Ctrl + C to stop the server
    server.setStopAtShutdown(true);

    // server instance
    return server;
  }

  public Server getServer() {
    return server;
  }
}
