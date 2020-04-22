package com.distributed.client;

import com.distributed.model.Result;
import com.distributed.model.User;
import com.distributed.server.ServerInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * NotiClientRunnable.java
 *
 * A thread that runs separately with the client that receives notifications
 * from server every 3 seconds and manage notifications from other clients.
 *
 * @version 2020-4-21
 */
public class NotiClientRunnable implements Runnable {

  private List<String> notifications;
  private boolean isAlive = true;
  private ServerInterface serverInterface;
  private User user;

  public NotiClientRunnable(ServerInterface serverInterface) {
    this.serverInterface = serverInterface;
  }

  /**
   * Fetch notifications from server every 3 seconds
   */
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

  void stop() {
    isAlive = false;
  }

  /**
   * Get all new notifications received.
   */
  List<String> getAllNotifications() {
    List<String> res = new ArrayList<>();
    if (null != notifications) {
      synchronized (notifications) {
        if (!notifications.isEmpty()) {
          res.addAll(notifications);
          notifications.clear();
        }
      }
    }
    return res;
  }

  /**
   * Clear notifications.
   */
  void clearNotificationList() {
    if (null != notifications) {
      synchronized (notifications) {
        notifications.clear();
      }
    }
  }

  public List<String> getNotifications() {
    return this.notifications;
  }

  public void setNotifications(List<String> notifications) {
    this.notifications = notifications;
  }

  public boolean isAlive() {
    return this.isAlive;
  }

  public void setAlive(boolean isAlive) {
    this.isAlive = isAlive;
  }

  public ServerInterface getServerInterface() {
    return this.serverInterface;
  }

  public void setServerInterface(ServerInterface serverInterface) {
    this.serverInterface = serverInterface;
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
