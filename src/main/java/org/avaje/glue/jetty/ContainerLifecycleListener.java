package org.avaje.glue.jetty;

/**
 * The container lifecycle that plugins can register for.
 */
public interface ContainerLifecycleListener {

  /**
   * The container is starting.
   */
  void starting();

  /**
   * The container has started.
   */
  void started();

  /**
   * The container failed.
   */
  void failure();

  /**
   * The container is stopping.
   */
  void stopping();

  /**
   * The container has stopped.
   */
  void stopped();
}
