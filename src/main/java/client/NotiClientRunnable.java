package client;

import model.Result;
import model.User;
import server.ServerInterface;

import java.util.ArrayList;
import java.util.List;

public class NotiClientRunnable implements Runnable {

  private List<String> notifications;
  private boolean isAlive = true;
  private ServerInterface serverInterface;
  private User user;

  public NotiClientRunnable(ServerInterface serverInterface) {
    this.serverInterface = serverInterface;
  }

  @Override
  public void run() {
    while (isAlive && user != null) {
      try {
        Result result = serverInterface.getNotifications(user);
        List<String> unreadNotifications = result.getUnreadNotifications();
        if (null != unreadNotifications && !unreadNotifications.isEmpty()) {
          System.out.println("You have a new notification.");
          notifications = unreadNotifications;
          user.setNotifications(unreadNotifications);
        }
        Thread.sleep(3000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void stop() {
    isAlive = false;
  }

  /**
   * Gets all the notifications received since the last method invocation.
   *
   * @return the notification strings array
   */
  public List<String> getAllNotifications() {
    List<String> res = new ArrayList<>();
    synchronized (notifications) {
      if (!notifications.isEmpty()) {
        res.addAll(notifications);
        notifications.clear();
      }
    }
    return res;
  }

  /**
   * Clears the notifications.
   */
  public void clearNotificationList() {
    synchronized (notifications) {
      notifications.clear();
    }
  }

    public List<String> getNotifications() {
        return this.notifications;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public ServerInterface getServerInterface() {
        return this.serverInterface;
    }

    public User getUser() {
        return this.user;
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public void setServerInterface(ServerInterface serverInterface) {
        this.serverInterface = serverInterface;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
