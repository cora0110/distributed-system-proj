package chat;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import model.Message;

public class Receiver implements Runnable {

  private List<Message> messages;
  private MulticastSocket socket;
  private byte[] buffer;
  private boolean isAlive;
  private InetAddress address;

  public Receiver() {
    this.messages = new ArrayList<>();
    this.buffer = new byte[2048];
    this.isAlive = true;
  }

  @Override
  public void run() {
    while (isAlive) {
      try {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        Message message = new Message(received);
        messages.add(message);
      } catch (Exception e) {
        System.out.println("Unable receiving messages.");
      }
    }
  }

  public void setNewGroup(InetAddress address) throws Exception {
    if (socket != null) {
      this.address = address;
      this.socket.joinGroup(address);
      this.socket = new MulticastSocket();
    }
  }

  public List<Message> retrieve() {
    List<Message> current = new ArrayList<>(messages);
    messages.clear();
    return current;
  }

  public void leave() throws Exception {
    this.socket.leaveGroup(address);
    this.socket.close();
    this.isAlive = false;
  }

}
