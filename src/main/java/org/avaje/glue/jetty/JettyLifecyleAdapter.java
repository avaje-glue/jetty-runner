package org.avaje.glue.jetty;

import org.eclipse.jetty.util.component.LifeCycle;

class JettyLifecyleAdapter implements LifeCycle.Listener {

  private final ContainerLifecycleListener listener;

  JettyLifecyleAdapter(ContainerLifecycleListener listener) {
    this.listener = listener;
  }

  @Override
  public void lifeCycleStarting(LifeCycle lifeCycle) {
    listener.starting();
  }

  @Override
  public void lifeCycleStarted(LifeCycle lifeCycle) {
    listener.started();
  }

  @Override
  public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
    listener.failure();
  }

  @Override
  public void lifeCycleStopping(LifeCycle lifeCycle) {
    listener.stopping();
  }

  @Override
  public void lifeCycleStopped(LifeCycle lifeCycle) {
    listener.stopped();
  }
}
