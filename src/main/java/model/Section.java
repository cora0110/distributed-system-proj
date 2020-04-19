package model;

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

  /**
   * get the inputStream to read the section content
   * @return
   * @throws IOException
   */
  public InputStream getFileInputStream() throws IOException {
    FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);
    return Channels.newInputStream(fileChannel);
  }

  /**
   * get the outputStream to fill the section with a new content
   * @return
   * @throws IOException
   */
  public OutputStream getWriteStream() throws IOException {
    FileChannel fileChannel = FileChannel.open(Paths.get(path), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    return Channels.newOutputStream(fileChannel);
  }



}
