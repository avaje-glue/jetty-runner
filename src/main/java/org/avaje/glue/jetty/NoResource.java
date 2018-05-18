package org.avaje.glue.jetty;

import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

class NoResource extends Resource {

  @Override
  public boolean isContainedIn(Resource resource) {
    return false;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public long lastModified() {
    return 0;
  }

  @Override
  public long length() {
    return 0;
  }

  @Override
  public URL getURL() {
    return null;
  }

  @Override
  public File getFile() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public InputStream getInputStream() {
    return null;
  }

  @Override
  public ReadableByteChannel getReadableByteChannel() {
    return null;
  }

  @Override
  public boolean delete() throws SecurityException {
    return false;
  }

  @Override
  public boolean renameTo(Resource resource) throws SecurityException {
    return false;
  }

  @Override
  public String[] list() {
    return new String[0];
  }

  @Override
  public Resource addPath(String s) {
    return this;
  }
}
