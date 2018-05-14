package org.avaje.glue.jetty;


import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.ServiceLoader;

/**
 * Provides a Jetty Runner that can be used to run as a main method.
 * <p>
 * Does not expect "war" packaging and instead we put:
 * <ul>
 * <li>web.xml in resources/web-inf/web.xml</li>
 * <li>static assets in resources/web-assets</li>
 * </ul>
 */
public class JettyRun extends BaseRunner {

  private String resourceBase;

  private Resource webXml;

  /**
   * Construct reading system properties for http port etc.
   */
  public JettyRun() {
    this.useStdInShutdown = isIdeRun();
  }

  private boolean isIdeRun() {
    return new File("pom.xml").exists() || new File("build.gradle").exists();
  }

  public WebAppContext getContext() {
    return webapp;
  }

  /**
   * Configure and run the webapp using jetty.
   */
  public void run() {

    setDefaultLogbackConfig();
    createWebAppContext();
    setupForExpandedWar();
    startServer();
  }

  /**
   * Setup for an expanded webapp with resource base as a relative path.
   */
  protected void setupForExpandedWar() {

    if (resourceBase != null) {
      webapp.setResourceBase(resourceBase);

    } else {
      Resource base = Resource.newClassPathResource("/web-assets");
      if (base != null && base.exists()) {
        webapp.setResourceBase(base.toString());
      } else {
        webapp.setBaseResource(new NoResource());//EmptyResource.INSTANCE);
        log().warn("Missing resources/web-assets");
      }
    }

    try {
      if (webXml == null) {
        webXml = Resource.newClassPathResource("/web-inf/web.xml");
      }
      if (webXml != null && webXml.exists())  {
        webapp.getMetaData().setWebXml(webXml);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    webapp.setParentLoaderPriority(false);
  }

  public JettyRun setWebXml(Resource webXml) {
    this.webXml = webXml;
    return this;
  }

  /**
   * Set the resource base.
   */
  public JettyRun setResourceBase(String resourceBase) {
    this.resourceBase = resourceBase;
    return this;
  }

  /**
   * Set the http port to use.
   */
  public JettyRun setHttpPort(int httpPort) {
    this.httpPort = httpPort;
    return this;
  }

  /**
   * Set the context path to use.
   */
  public JettyRun setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  /**
   * Set the secure cookies setting.
   */
  public JettyRun setSecureCookies(boolean secureCookies) {
    this.secureCookies = secureCookies;
    return this;
  }
}
