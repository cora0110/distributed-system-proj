package model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
  private static final long serialVersionUID = -7351729135012380019L;

  private String username;
  private String password;
  private List<String> notifications;

  public User(String username, String password) {
    try {
      this.username = username;
      this.password = getEncrypted(password);
      notifications = new ArrayList<>();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public User(String username) {
    this.username = username;
    notifications = new ArrayList<>();
  }

  /**
   * Check password validity.
   */
  public boolean checkPassword(String pwd) throws Exception {
    return this.password.equals(getEncrypted(pwd));
  }

  /**
   * Generate token for alive user.
   */
  public String generateToken() throws Exception {
    String tokenString = password + System.currentTimeMillis();
    return getEncrypted(tokenString);
  }


  public String generateToken(long timestamp) throws Exception {
    String tokenString = password + timestamp;
    return getEncrypted(tokenString);
  }


  /**
   * Get MD5 encrypted result.
   */
  public String getEncrypted(String str) throws Exception {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] bytes = md5.digest(str.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /**
   * Gets all the unread notifications to this user associated.
   *
   * @return notifications strings array
   */
  public List<String> getUnreadNotifications() {
    if (null != notifications) {
      synchronized (notifications) {
        List<String> unreadNotifications = new ArrayList<>(notifications);
        this.notifications.clear();
        return unreadNotifications;
      }
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Add a new notification value to the unread ones.
   *
   * @param doc new document which user has access to
   */
  public void pushNewNotification(String doc) {
    if (notifications == null) {
      notifications = new ArrayList<>();
    }
    synchronized (notifications) {
      notifications.add(doc);
    }
  }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public List<String> getNotifications() {
        return this.notifications;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }
}
