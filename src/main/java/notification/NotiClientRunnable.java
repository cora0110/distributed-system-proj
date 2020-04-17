package notification;

import java.util.ArrayList;
import java.util.List;

public class NotiClientRunnable implements Runnable {

  private List<String> notifications;
  private boolean isAlive = true;
  private int port;

  @Override
  public void run() {
    while (isAlive) {

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

}
