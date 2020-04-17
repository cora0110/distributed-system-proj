package chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import model.Message;

public class Sender {

  private DatagramSocket socket;
  private InetSocketAddress group;
  private byte[] buffer;

  public Sender() {
    try {
      this.socket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  /**
   * Send a multicast message to a group.
   */
  public void sendMessage(Message message, InetSocketAddress group) throws Exception {
    byte[] buffer = message.getBytes();
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group);
    socket.send(packet);
    socket.close();
  }

}
