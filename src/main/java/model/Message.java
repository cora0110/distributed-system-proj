package model;


import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = 1812908819029669642L;

  private String sender;
  private String content;
  private long timestamp;

  public Message(String sender, String content, long timestamp) {
    this.sender = sender;
    this.content = content;
    this.timestamp = timestamp;
  }

  public byte[] getBytes() {
    String s = timestamp + ":" + sender + " " + content;
    return s.getBytes();
  }

  public Message(String string) {
    String[] timeHead = string.split("/", 2);
    this.timestamp = Long.parseLong(timeHead[0]);
    String[] senderHead = timeHead[1].split(" ", 2);
    this.sender = senderHead[0];
    this.content = senderHead[1];
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
