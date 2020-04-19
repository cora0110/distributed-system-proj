package chat;

import model.Message;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

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

    public List<Message> getMessages() {
        return this.messages;
    }

    public MulticastSocket getSocket() {
        return this.socket;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
