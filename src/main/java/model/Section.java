package model;

import java.io.Serializable;
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

  public void setOccupant(User occupant) {
    this.occupant = occupant;
  }
}
