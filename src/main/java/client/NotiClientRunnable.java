package client;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import model.Result;
import model.User;
import server.ServerInterface;

@Getter
@Setter
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
        notifications = unreadNotifications;
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
    List<String> notifications = new ArrayList<>();
    synchronized (notifications) {
      if (!notifications.isEmpty()) {
        notifications.addAll(notifications);
        notifications.clear();
      }
    }
    return notifications;
  }

  /**
   * Clears the notifications.
   */
  public void clearNotificationList() {
    synchronized (notifications) {
      notifications.clear();
    }
  }

}
