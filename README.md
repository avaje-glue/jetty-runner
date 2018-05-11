jetty-runner
=================

Runs Jetty with a docker friendly /lib/* based classpath rather than war or uber jar packaging.

Expects
----------
 - web.xml in src/main/resources/web-inf/web.xml
 - static resources in src/main/resource/web-assets


What it does
-------------
 - Configures the Jetty WebAppContext (port, context path, tmp directory, cookie handler etc)
 - Detects if WebSocket support is in the classpath and if so sets up WebSocket support
 - Sets up a shutdown hook
 - Expects docker friendly /lib/* style classpath (rather than war or uber jar)


Classpath
-------------
Typically used with https://github.com/avaje-pom/tile-lib-classpath ... or equivalent plugins to build
META-INF classpath entries and Docker file that adds `target/lib/*`.


```java
package example.main;

import org.avaje.jettyrunner.JettyRun;

/**
 * Run the webapp using Jetty. 
 */
public class Application {

  public static void main(String[] args) {
    
    JettyRun jettyRun = new JettyRun();
    jettyRun.setHttpPort(8090);                    // default to 8080
    jettyRun.setContextPath("/hello");             // defaults to "/"
    jettyRun.run();
  }
}
```

History
--------
This is a fork from https://github.com/avaje-common/avaje-jetty-runner ... with
a focus on using a lib/* META.INF based classpath (and java main method) as opposed
to war packaging or uber jar packaging.

That is, forked with a view that this is a better approach when targeting docker.

