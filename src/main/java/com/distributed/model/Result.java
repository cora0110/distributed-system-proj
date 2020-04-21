package com.distributed.model;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteOutputStream;

import java.io.Serializable;
import java.util.List;

public class Result implements Serializable {
  List<String> unreadNotifications;
  private int status;
  private String message;
  private RemoteInputStream remoteInputStream;
  private RemoteOutputStream remoteOutputStream;

  public Result(int status, String message) {
    this.status = status;
    this.message = message;
  }

  public Result(int status, String message, RemoteInputStream remoteInputStream) {
    this.status = status;
    this.message = message;
    this.remoteInputStream = remoteInputStream;
  }

  public Result() {
  }

  public List<String> getUnreadNotifications() {
    return this.unreadNotifications;
  }

  public void setUnreadNotifications(List<String> unreadNotifications) {
    this.unreadNotifications = unreadNotifications;
  }

  public int getStatus() {
    return this.status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public RemoteInputStream getRemoteInputStream() {
    return this.remoteInputStream;
  }

  public void setRemoteInputStream(RemoteInputStream remoteInputStream) {
    this.remoteInputStream = remoteInputStream;
  }

  public RemoteOutputStream getRemoteOutputStream() {
    return this.remoteOutputStream;
  }

  public void setRemoteOutputStream(RemoteOutputStream remoteOutputStream) {
    this.remoteOutputStream = remoteOutputStream;
  }
}
