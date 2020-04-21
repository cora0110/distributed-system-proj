package com.distributed.chat;

import com.distributed.model.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Sender {

  private DatagramChannel channel;
  private ByteBuffer buffer;


  private Sender(DatagramChannel channel) {
    this.channel = channel;
    buffer = ByteBuffer.allocate(2048);
  }

  /**
   * Initialize UDP channel and sets multicast interface.
   */
  public static Sender create() {
    try {
      DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
      NetworkInterface interf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
      channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
      return new Sender(channel);
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Put data into the buffer that will be streamed in a way easy to interpret. Every object in the
   * buffer is stored by its length first, then a byte raw format.
   */
  public void sendMessage(Message message, InetSocketAddress group) throws IOException {
    buffer.clear();
    buffer.putInt(message.getSender().length());
    buffer.put(message.getSender().getBytes());
    buffer.putInt(message.getContent().length());
    buffer.put(message.getContent().getBytes());
    buffer.putLong(message.getTimestamp());
    buffer.flip();
    while (buffer.hasRemaining())
      channel.send(buffer, group);
  }

}
