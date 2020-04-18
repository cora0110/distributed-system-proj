package chat;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import model.Message;

@Getter
@Setter
public class Receiver implements Runnable {

  private List<Message> messages;
  private MulticastSocket socket;
  private byte[] buffer;
  private boolean isAlive;
  private InetAddress address;

  public Receiver() throws Exception {
    this.messages = new ArrayList<>();
    this.buffer = new byte[2048];
    this.isAlive = true;
    this.socket = new MulticastSocket();
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

  public void setNewGroup(long address) throws Exception {
    if (socket != null) {
      InetAddress inetAddress = ChatManager.longToAddress(address);
      this.address = inetAddress;
      this.socket.joinGroup(inetAddress);
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
