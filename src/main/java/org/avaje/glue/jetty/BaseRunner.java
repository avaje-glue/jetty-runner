package org.avaje.glue.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Base class for runnable war (RunWar) and IDE jetty runner (JettyRun).
 */
abstract class BaseRunner {

  private static final String WEBAPP_HTTP_PORT = "webapp.http.port";
  private static final String WEBAPP_CONTEXT_PATH = "webapp.context.path";
  private static final String WEBAPP_SECURE_COOKIES = "webapp.secure.cookies";

  private static final int DEFAULT_HTTP_PORT = 8080;

  private static final String DEFAULT_CONTEXT_PATH = "/";

  /**
   * A modification from WebAppContext.__dftServerClasses that exposes JDT
   * so that jsp works.
   *
   * @see org.eclipse.jetty.webapp.WebAppContext
   */
  private final static String[] exposeJdt_dftServerClasses = {
      "-org.eclipse.jetty.continuation.", // don't hide continuation classes
      "-org.eclipse.jetty.jndi.",         // don't hide naming classes
      "-org.eclipse.jetty.jaas.",         // don't hide jaas classes
      "-org.eclipse.jetty.servlet.",     // don't hide jetty servlets
      "-org.eclipse.jetty.servlets.",     // don't hide jetty servlets
      "-org.eclipse.jetty.servlet.DefaultServlet", // don't hide default servlet
      "-org.eclipse.jetty.servlet.listener.", // don't hide useful listeners
      "-org.eclipse.jetty.websocket.",    // don't hide websocket classes from webapps (allow webapp to use ones from system classloader)
      "-org.eclipse.jetty.apache.",       // don't hide jetty apache impls
      "-org.eclipse.jetty.util.log.",     // don't hide server log
      "org.objectweb.asm.",               // hide asm used by jetty
      //"org.eclipse.jdt.",                 // hide jdt used by jetty
      "org.eclipse.jetty."                // hide other jetty classes
    } ;

  /**
   * Set this on for IDE JettyRun use (for shutdown in IDE console).
   */
  boolean useStdInShutdown;

  int httpPort;

  String contextPath;

  boolean secureCookies;

  WebAppContext webapp;

  Server server;

  ServerContainer serverContainer;

  /**
   * Construct reading appropriate system properties.
   */
  BaseRunner() {
    this.httpPort = Integer.getInteger(WEBAPP_HTTP_PORT, DEFAULT_HTTP_PORT);
    this.contextPath = System.getProperty(WEBAPP_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
    this.secureCookies = Boolean.parseBoolean(System.getProperty(WEBAPP_SECURE_COOKIES, "true"));
    this.webapp = new WebAppContext();
    webapp.setThrowUnavailableOnStartupException(true);
  }

  /**
   * If logback.configurationFile is not set then setup to look for logback.xml in the current working directory.
   */
  void setDefaultLogbackConfig() {

    String logbackFile = System.getProperty("logback.configurationFile");
    if (logbackFile == null) {
      // set default behaviour to look in current working directory for logback.xml
      System.setProperty("logback.configurationFile", "logback.xml");
    }
  }

  /**
   * Create the WebAppContext with basic configurations set like context path
   * etc.
   */
  void createWebAppContext() {
    webapp.setServerClasses(getServerClasses());
    webapp.setContextPath(contextPath);
    webapp.setTempDirectory(createTempDir("jetty-app-"));
    webapp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    setSecureCookies();
  }

  File createTempDir(String prefix) {
    try {
      File tempDir = File.createTempFile(prefix + ".", "." + httpPort);
      tempDir.delete();
      tempDir.mkdir();
      tempDir.deleteOnExit();
      return tempDir;
    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to create tempDir. java.io.tmpdir is set to "+ System.getProperty("java.io.tmpdir"), ex);
    }
  }

  /**
   * Return true if WebSocket support is found in the classpath.
   */
  private boolean isWebSocketInClassPath() {
    try {
      Class.forName("org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Register WebSocket endpoints via ServletContextListener.
   */
  private void setupForWebSocket() {
    try {
      serverContainer = WebSocketServerContainerInitializer.configureContext(webapp);
      // you can manually register endpoints to this serverContainer
      // or register them via a ServletContextListener
      //serverContainer.addEndpoint(MyWebSocketServerEndpoint.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialise WebSocketServerContainer", e);
    }
  }

  /**
   * Refer to WebAppContext __dftServerClasses. This exposes JDT to the webapp for jsp use.
   */
  String[] getServerClasses() {
    return exposeJdt_dftServerClasses;
  }

  /**
   * Wrap handlers as you need with statistics collection or proxy request handling.
   */
  private Handler wrapHandlers() {
    return webapp;
  }

  /**
   * Start the Jetty server.
   */
  void startServer() {

    server = new Server(httpPort);
    server.setHandler(wrapHandlers());
    //webapp.setClassLoader(this.getClass().getClassLoader());

    if (isWebSocketInClassPath()) {
      setupForWebSocket();
    }
    try {
      server.start();
      log().info("server started");

      Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));

      if (useStdInShutdown) {
        // generally for use in IDE via JettyRun, Use CTRL-D in IDE console to shutdown
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        while((systemIn.readLine()) != null) {
          // ignore anything except CTRL-D by itself
        }
        System.out.println("Shutdown via CTRL-D");
        System.exit(0);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  void shutdownNicely(boolean normalShutdown) {
    try {
      server.stop();
      server.join();
      if (normalShutdown) {
        // only want to log this once
        log().info("server stopped");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  protected class ShutdownRunnable implements Runnable {

    @Override
    public void run() {
      log().info("server shutting down");
      shutdownNicely(true);
    }
  }


  /**
   * Set the secure cookies setting on the jetty session manager.
   */
  void setSecureCookies() {
    webapp.getSessionHandler().setHttpOnly(true);
    webapp.getSessionHandler().getSessionCookieConfig().setSecure(true);
  }

  /**
   * Set if stand input should be read to determine shutdown.
   *
   * This should really only be true for use when running in an IDE and
   * CTRL-D in the IDE console can be used to trigger shutdown.
   */
  public void setUseStdInShutdown(boolean useStdInShutdown) {
    this.useStdInShutdown = useStdInShutdown;
  }

  Logger log() {
    return Log.getLogger("org.avaje.glue.jetty");
  }

}
