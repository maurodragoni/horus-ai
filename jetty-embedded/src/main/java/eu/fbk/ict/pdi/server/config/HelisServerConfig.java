package eu.fbk.ict.pdi.server.config;

import org.aeonbits.owner.Config;

/**
 * Configuration class used to setup some low-level properties of the Jetty server
 * Created by Carlo Mion on 05/05/17.
 */
public interface HelisServerConfig extends Config {

	/**
	 * The complete base path at which the application is reachable from the internet.
	 * (Used by Swagger UI for example, to test the microservice from the web interface)
	 * Defaults to /.
	 *
	 * @return The base path
	 */
	@Key("EXTERNAL_BASE_PATH")
	@DefaultValue("/")
	String getBasePath();

  /**
   * The amount of time that Jetty will wait for Helis to finish initialization
   * Default to 1 minute
   */
  @Key("JETTY_STARTUP_TIMEOUT_SECONDS")
  @DefaultValue("60")
  String getJettyStartupTimeoutSeconds();

	/**
	 * The base path at which the application listen for requests.
	 * Defaults to /
	 *
	 * @return The base path
	 */
	@Key("INTERNAL_BASE_PATH")
	@DefaultValue("/")
	String getInternalBasePath();

	/**
	 * The address at which the Jetty server will listen to requests.
	 * Default to 0.0.0.0
	 *
	 * @return The base path
	 */
	@Key("HTTP_LISTEN_ADDRESS")
	@DefaultValue("0.0.0.0")
	String getListenAddress();

	/** The port in which the main Jersey application is listening
	 * @return The port in which listens Jersey
	 */
	@Key("HTTP_PORT")
	@DefaultValue("8080")
	int getJerseyPort();

	@Key("METRICS_PORT")
	@DefaultValue("6666")
	int getMetricsServerPort();

	/**
	 * Enable logs in JSON format (for use in ELK stack).
	 */
	@Key("JSON_LOGGING")
	@DefaultValue("false")
	boolean enableJsonLogging();

	/**
	 * Path to the war containing the Helis application. By default it is the path to the Maven output directory
	 */
	@Key("WAR_PATH")
	//@DefaultValue("./helis-servlet/target/helis-service.war")
	@DefaultValue("./helis-service.war")
	String getWarPath();
}
