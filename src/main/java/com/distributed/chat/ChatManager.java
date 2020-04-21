package com.distributed.chat;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager implements Serializable {
  private static final long serialVersionUID = -9086368079210319078L;

  /**
   * Full range is from 233.0.0.1 to 233.255.255.255;
   */
  private static final long START_ADDR = 3909091329L;
  private static final long END_ADDR = 3925868543L;

  private ConcurrentHashMap<String, Long> chatDatabase;

  public ChatManager() {
    chatDatabase = new ConcurrentHashMap<>();
  }

  /**
   * Convert IP address to long.
   */
  public static long addressToLong(InetAddress address) {
    int result = 0;
    for (byte b : address.getAddress()) {
      result = result << 8 | (b & 0xFF);
    }
    return result;
  }

  /**
   * Converts a long value to its InetAddress representation.
   */
  public static InetAddress longToAddress(long address) throws UnknownHostException {
    return InetAddress.getByName(String.valueOf(address));
  }

  /**
   * Get available multicast address.
   *
   * @return IPv4 address
   */
  public long getNextAvailableAddress() {
//    if (chatDatabase.contains(document)) {
//      return chatDatabase.get(document);
//    }
    for (long address = START_ADDR; address <= END_ADDR; address++) {
      return address;
    }
    return -1L;
  }

  public void putAddress(String documentName, long address) {
    if (!chatDatabase.contains(documentName)) {
      chatDatabase.put(documentName, address);
    }
  }

  public long getResultAddress(String document) {
    return chatDatabase.get(document);
  }

  /**
   * Remove from chat database.
   *
   * @param document document need removal
   */
  public void remove(String document) {
    chatDatabase.remove(document);
  }

  public ConcurrentHashMap<String, Long> getChatDatabase() {
    return chatDatabase;
  }

  public void setChatDatabase(ConcurrentHashMap<String, Long> chatDatabase) {
    this.chatDatabase = chatDatabase;
  }
}
