package com.distributed.chat;

import com.distributed.model.Message;

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
  private InetAddress group;

  public Receiver() throws Exception {
    this.messages = new ArrayList<>();
    this.buffer = new byte[2048];
    this.isAlive = true;
    this.socket = new MulticastSocket(4567);
  }

  @Override
  public void run() {
    while (isAlive) {
      if (group != null) {
        try {
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);
//        socket.receive(packet);
//        String received = new String(packet.getData(), 0, packet.getLength());
//        Message message = new Message(received);
//        messages.add(message);


          socket.joinGroup(group);
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
          socket.receive(packet);
          String received = new String(
                  packet.getData(), 0, packet.getLength());
          Message message = new Message(received);
          messages.add(message);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Unable receiving messages.");
        }
      }
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void setNewGroup(long address) throws Exception {
    if (socket != null) {
      group = ChatManager.longToAddress(address);
      this.socket.joinGroup(group);
    }
  }

  public List<Message> retrieve() {
    List<Message> current = new ArrayList<>(messages);
    messages.clear();
    return current;
  }

  public void leave() throws Exception {
    this.socket.leaveGroup(group);
    this.isAlive = false;
  }

  public List<Message> getMessages() {
    return this.messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  public MulticastSocket getSocket() {
    return this.socket;
  }

  public void setSocket(MulticastSocket socket) {
    this.socket = socket;
  }

  public byte[] getBuffer() {
    return this.buffer;
  }

  public void setBuffer(byte[] buffer) {
    this.buffer = buffer;
  }

  public boolean isAlive() {
    return this.isAlive;
  }

  public void setAlive(boolean isAlive) {
    this.isAlive = isAlive;
  }

  public InetAddress getGroup() {
    return group;
  }

  public void setGroup(InetAddress group) {
    this.group = group;
  }


}
