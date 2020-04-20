package com.distributed.chat;

import com.distributed.model.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sender {

  private DatagramSocket socket;
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
  public void sendMessage(Message message, InetAddress group) throws Exception {
    socket = new DatagramSocket();
    buffer = message.getBytes();

    DatagramPacket packet
            = new DatagramPacket(buffer, buffer.length, group, 4477);
    socket.send(packet);
  }

}
