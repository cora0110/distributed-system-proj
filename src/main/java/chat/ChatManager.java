package chat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import model.Document;

public class ChatManager {

  /**
   * Full range is from 233.0.0.0 to 233.255.255.254;
   */
  private static final long START_ADDR = 3909091329L;
  private static final long END_ADDR = 3925868542L;

  private ConcurrentHashMap<Document, Long> chatDatabase;

  public ChatManager() {
    chatDatabase = new ConcurrentHashMap<>();
  }

  /**
   * Get available multicast address.
   *
   * @param document document being edited
   * @return IPv4 address
   */
  public long getChatAddress(Document document) {
    long address;
    if (chatDatabase.contains(document)) {
      return chatDatabase.get(document);
    }
    for (address = START_ADDR; address <= END_ADDR; address++)
      if (!chatDatabase.contains(address)) {
        chatDatabase.put(document, address);
        return address;
      }
    return -1L;
  }

  /**
   * Remove from chat database.
   *
   * @param document document need removal
   */
  public void remove(Document document) {
    if (!chatDatabase.contains(document)) {
      if (document.getOccupiedSections().size() == 0) {
        chatDatabase.remove(document);
      }
    }
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

}
