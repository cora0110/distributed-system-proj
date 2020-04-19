package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import model.User;

public class AliveUserDatabase {
  ConcurrentHashMap<String, OnlineUserRecord> aliveUsers;
  private ReentrantReadWriteLock mutex;

  /**
   * Initializes the underlining {@code ConcurrentHashMap}.
   */
  AliveUserDatabase() {
    aliveUsers = new ConcurrentHashMap<>();
    mutex = new ReentrantReadWriteLock();
  }



  /**
   * Logs a {@code User} instance into the {@code AliveUsersDB} letting it pass to the online
   * state.
   * <p>
   * Only one valid token at a time can exist but it would result senseless to look for a
   * pre-existing {@code User} because the assignment as a key of the {@code ConcurrentHashMap} is a
   * destructive operation anyway.
   *
   * @param user user reference
   * @return the session token or null if error occurs
   */
  String login(User user) {
    if (user == null) return null;
    OnlineUserRecord record = new OnlineUserRecord(user);
    mutex.writeLock().lock();
    aliveUsers.put(user.getUsername(), record);
    mutex.writeLock().unlock();
    return record.getToken();
  }


  /**
   * When logout, remove the user from the aliveUserDatabase.
   * @param user
   * @return
   */
  boolean logout(User user){
    if (user==null) return false;
    if (!isLoggedIn(user.getUsername())) return false;

    mutex.writeLock().lock();
    aliveUsers.remove(user.getUsername());
    mutex.writeLock().unlock();
    return true;
  }

  /**
   * Gets a {@code User} object reference from a {@code String} token.
   *
   * @param token session token
   * @return user reference or null if error occurs
   */
  User getUserByToken(String token) {
    for (OnlineUserRecord record : aliveUsers.values())
      if (record.verifyToken(token)) return record.getUser();
    return null;
  }

  String getTokenByUser(String username) {
    return aliveUsers.get(username).getToken();
  }

  public boolean isLoggedIn(String username) {
    return aliveUsers.containsKey(username);
  }

  OnlineUserRecord getOnlineUserRecord(String username) {
    return aliveUsers.get(username);
  }

  /**
   * The {@code OnlineUserRecord} class represents a {@code OnlineUsersDB} single record and is used
   * to relate a {@code User} object to its {@code String} session token.
   *
   * @author Federico Gerardi
   * @author https://azraelsec.github.io/
   */
  public class OnlineUserRecord {
    private User user;
    private String token;

    /**
     * Initializes {@code OnlineUserRecord}.
     *
     * @param user user reference
     */
    OnlineUserRecord(User user) {
      try {
        this.user = user;
        this.token = user.generateToken();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /**
     * Gets the {@code User}.
     *
     * @return user reference
     */
    public User getUser() {
      return user;
    }

    /**
     * Gets the {@code String} session token.
     *
     * @return session token
     */
    String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    /**
     * Verifies if the {@code String} session token is this record related or not.
     *
     * @param token session token
     * @return true if {@code token} is the actual one, false otherwise
     */
    boolean verifyToken(String token) {
      return (this.token.compareTo(token) == 0);
    }
  }
}
