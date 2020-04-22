package com.distributed.chat;

import com.distributed.client.Client;
import com.distributed.model.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Receiver.java
 *
 * Implements a receiver class of the chat group
 *
 * @version 2020-4-21
 */
public class Receiver implements Runnable {

  private List<Message> messages;
  private MembershipKey activeGroup;
  private DatagramChannel channel;
  private NetworkInterface interf;

  public Receiver() {
    messages = new ArrayList<>();
    activeGroup = null;
    channel = null;
    interf = null;
  }

  @Override
  public void run() {
    try {
      Selector selector = Selector.open();
      channel = DatagramChannel.open(StandardProtocolFamily.INET);
      interf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
      channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
      channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
      channel.bind(new InetSocketAddress(Client.UDP_PORT));
      channel.configureBlocking(false);
      channel.register(selector, SelectionKey.OP_READ);
      ByteBuffer buffer = ByteBuffer.allocate(2048);
      while (!Thread.currentThread().isInterrupted()) {
        selector.select();
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();
          if (key.isReadable()) {
            buffer.clear();
            InetSocketAddress sa = (InetSocketAddress) channel.receive(buffer);
            if (sa != null) {
              buffer.flip();
              try {
                String sender = getString(buffer);
                String content = getString(buffer);
                long timestamp = buffer.getLong();

                Message message = new Message(sender, content, timestamp);
                synchronized (messages) {
                  messages.add(message);
                }
              } catch (BufferUnderflowException ignore) {
              }
            }
          }
        }
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    } finally {
      if (channel != null)
        try {
          channel.close();
        } catch (IOException ignore) {
        }
    }
  }

  private String getString(ByteBuffer buffer) {
    int size = buffer.getInt();
    byte[] res = new byte[size];
    buffer.get(res, 0, size);
    return new String(res);
  }

  public List<Message> retrieve() {
    List<Message> current = new ArrayList<>(messages);
    messages.clear();
    return current;
  }

  /**
   * Setting a group represented by the IPv4 address to receive messages
   */
  public void setNewGroup(long group) throws IOException, UnknownHostException {
    if (channel != null) {
      if (group > 0) {
        byte[] rawAddress = ChatManager.longToAddress(group).getAddress();
        if (activeGroup != null && activeGroup.isValid()) activeGroup.drop();
        activeGroup = channel.join(InetAddress.getByAddress(rawAddress), interf);
      } else {
        activeGroup.drop();
        activeGroup = null;
      }
    }
  }

  /**
   * Get the active group.
   */
  public InetAddress getActiveGroup() {
    return activeGroup.group();
  }

}
