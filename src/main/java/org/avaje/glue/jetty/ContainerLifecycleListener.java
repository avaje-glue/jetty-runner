package org.avaje.glue.jetty;

public interface ContainerLifecycleListener {

  void starting();

  void started();

  void failure();

  void stopping();

  void stopped();
}
