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

  public Message(String string) {
    String[] timeHead = string.split("/", 2);
    this.timestamp = Long.parseLong(timeHead[0]);
    String[] senderHead = timeHead[1].split(" ", 2);
    this.sender = senderHead[0];
    this.content = senderHead[1];
  }

  public byte[] getBytes() {
    String s = timestamp + ":" + sender + " " + content;
    return s.getBytes();
  }

    public String getSender() {
        return this.sender;
    }

    public String getContent() {
        return this.content;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
