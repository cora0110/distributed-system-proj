package com.distributed.model;

import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.Serializable;

public class Request implements Serializable {
  private String docName;
  private int sectionNum;
  private String token;
  private User targetUser;
  private RemoteInputStream remoteInputStream;

  public String getDocName() {
    return this.docName;
  }

  public void setDocName(String docName) {
    this.docName = docName;
  }

  public int getSectionNum() {
    return this.sectionNum;
  }

  public void setSectionNum(int sectionNum) {
    this.sectionNum = sectionNum;
  }

  public String getToken() {
    return this.token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public User getTargetUser() {
    return this.targetUser;
  }

  public void setTargetUser(User targetUser) {
    this.targetUser = targetUser;
  }

  public RemoteInputStream getRemoteInputStream() {
    return this.remoteInputStream;
  }

  public void setRemoteInputStream(RemoteInputStream remoteInputStream) {
    this.remoteInputStream = remoteInputStream;
  }
}
