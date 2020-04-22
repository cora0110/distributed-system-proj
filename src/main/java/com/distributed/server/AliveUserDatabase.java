package com.distributed.server;

import com.distributed.model.User;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AliveUserDatabase.java
 *
 * Implements data structure and methods for alive users.
 * Uses ReadWriteLock when updating database.
 *
 * @version 2020-4-21
 */
public class AliveUserDatabase implements Serializable {
  ConcurrentHashMap<String, AliveUserRecord> aliveUsers;
  private ReentrantReadWriteLock mutex;

  /**
   * Constructor
   */
  AliveUserDatabase() {
    aliveUsers = new ConcurrentHashMap<>();
    mutex = new ReentrantReadWriteLock();
  }


  /**
   * Logs a User instance into the AliveUsersDB letting it pass to the online
   * state.
   *
   * @param user user reference
   * @return the session token or null if error occurs
   */
  String login(User user) {
    if (user == null) return null;
    AliveUserRecord record = new AliveUserRecord(user);
    mutex.writeLock().lock();
    aliveUsers.put(user.getUsername(), record);
    mutex.writeLock().unlock();
    return record.getToken();
  }


  /**
   * When logout, remove the user from the aliveUserDatabase.
   *
   * @param user
   * @return false if user not exist or is already logged out. otherwise return true.
   */
  boolean logout(User user) {
    if (user == null) return false;
    if (!isLoggedIn(user.getUsername())) return false;

    mutex.writeLock().lock();
    aliveUsers.remove(user.getUsername());
    mutex.writeLock().unlock();
    return true;
  }

  /**
   * Gets a user object reference from a String token.
   *
   * @param token session token
   * @return user reference or null if error occurs
   */
  User getUserByToken(String token) {
    for (AliveUserRecord record : aliveUsers.values())
      if (record.verifyToken(token)) return record.getUser();
    return null;
  }

  /**
   * Gets a token from a user object
   * @param username
   * @return token
   */
  String getTokenByUser(String username) {
    return aliveUsers.get(username).getToken();
  }

  /**
   * Check whether a user is logged in
   * @param username
   * @return true if logged in, false if user not exist or logged out.
   */
  boolean isLoggedIn(String username) {
    return aliveUsers.containsKey(username) && aliveUsers.get(username).getToken() != null;
  }

  /**
   * Get user record from user name
   */
  AliveUserRecord getOnlineUserRecord(String username) {
    return aliveUsers.get(username);
  }

  /**Helper Class:
   *
   * The AliveUserRecord class represents a aliveUsersDB single record and is used
   * to relate a User object to its token.
   */
  public class AliveUserRecord implements Serializable {
    private User user;
    private String token;

    /**
     * Constructor
     * @param user user reference
     */
    AliveUserRecord(User user) {
      try {
        this.user = user;
        this.token = user.generateToken();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Gets the user.
     * @return user reference
     */
    public User getUser() {
      return user;
    }

    /**
     * Gets the session token.
     * @return session token
     */
    String getToken() {
      return token;
    }

    /**
     * set token
     * @param token token
     */
    void setToken(String token) {
      this.token = token;
    }

    /**
     * Verifies if the session token is this record related or not.
     *
     * @param token session token
     * @return true if token is the actual one, false otherwise
     */
    boolean verifyToken(String token) {
      if (token == null) {
        return this.token == null;
      }
      return (this.token.compareTo(token) == 0);
    }
  }
}
