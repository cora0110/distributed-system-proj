package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import model.User;

public class UserDatabase {
  private static final long serialVersionUID = 1L;
  private List<User> users;
  private ReentrantReadWriteLock mutex;

  /**
   * Initializes the environment.
   */
  UserDatabase() {
    users = new ArrayList<>();
    mutex = new ReentrantReadWriteLock();
  }

  /**
   * Logs the user into system through its username and password credentials, retrieving its object
   * reference.
   *
   * @param username user's username
   * @param password user's password
   * @return the user object if exists and the credentials are valid, false otherwise
   */
  User doLogin(String username, String password) {
    try {
      User user = getUserByUsername(username);
      if (user != null) {
        return user.checkPassword(password) ? user : null;
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Registers a new {@code User} storing its data into the {@code UsersDB} if the input credentials
   * do not exist yet.
   *
   * @param username user's username
   * @param password user's password
   * @return new user reference or null if that username is not available
   */
  User addNewUser(String username, String password) {
    if (!isUsernameAvailable(username)) return null;
    User newUser = new User(username, password);
    mutex.writeLock().lock();
    users.add(newUser);
    mutex.writeLock().unlock();
    return newUser;
  }

  /**
   * Checks if the input username is available or not.
   *
   * @param username user's username
   * @return true if does not exist any user with that username, false otherwise
   */
  public boolean isUsernameAvailable(String username) {
    return getUserByUsername(username) == null;
  }

  /**
   * Gets the {@code User} object from its username {@code String}.
   *
   * @param username user's username
   * @return related user's object if exists, null otherwise
   */
  User getUserByUsername(String username) {
    mutex.readLock().lock();
    for (User user : users) {
      if (user.getUsername().compareTo(username) == 0) {
        mutex.readLock().unlock();
        return user;
      }
    }
    mutex.readLock().unlock();
    return null;
  }
}
