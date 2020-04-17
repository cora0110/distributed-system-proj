package notification;

import java.util.List;

public class NotiServerRunnable implements Runnable {
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

}
