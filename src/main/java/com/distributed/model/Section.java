package com.distributed.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

public class Section implements Serializable {
  private static final long serialVersionUID = 4529055276637295352L;

  private String path;
  private User occupant;
  private ReentrantLock lock;

  public Section(String directory, String name) {
    this.path = directory + "/" + name + ".section";
    lock = new ReentrantLock();
  }

  // TODO: 4/14/20  
  public boolean occupy(User user) {
    if (lock.tryLock()) {
      if (this.occupant == null) {
        this.occupant = user;
      } else {
        if (user == null) {
          this.occupant = null;
        } else {
          lock.unlock();
          return false;
        }
      }
      lock.unlock();
      return true;
    }
    return false;
  }

  public User getOccupant() {
    lock.lock();
    User ocp = this.occupant;
    lock.unlock();
    return ocp;
  }

  public String getPath() {
    return path;
  }

  // a new string replace port in current path
  public String getPathByPort(int curPort) {
    String[] split = path.split("/", 3);
    String[] server = split[1].split("_");
    String originalPort = server[2];
    split[1] = split[1].replace(originalPort, String.valueOf(curPort));
    StringBuilder sb = new StringBuilder();
    sb.append(split[0]).append("/").append(split[1]).append("/").append(split[2]);
    return sb.toString();
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * get the inputStream to read the section content
   *
   * @return
   * @throws IOException
   */
  public InputStream getFileInputStream() throws IOException {
    FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);
    return Channels.newInputStream(fileChannel);
  }

  /**
   * get the outputStream to fill the section with a new content
   *
   * @return
   * @throws IOException
   */
  public OutputStream getWriteStream() throws IOException {
    FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    return Channels.newOutputStream(fileChannel);
  }


}
