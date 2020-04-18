package model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User implements Serializable {
  private static final long serialVersionUID = -7351729135012380019L;

  private String username;
  private String password;
  private List<String> notifications;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public User(String username) {
    this.username = username;
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
    synchronized (notifications) {
      List<String> unreadNotifications = new ArrayList<>(notifications);
      this.notifications.clear();
      return unreadNotifications;
    }
  }

  /**
   * Add a new notification value to the unread ones.
   *
   * @param doc new document which user has access to
   */
  void pushNewNotification(String doc) {
    synchronized (notifications) {
      notifications.add(doc);
    }
  }

}
