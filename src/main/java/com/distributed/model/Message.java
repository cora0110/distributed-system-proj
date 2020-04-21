package com.distributed.model;


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

  @Override
  public String toString() {
    return "[" + sender + "] - " + content;
  }

  public String getSender() {
    return this.sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
