package com.distributed.model;

import com.distributed.chat.ChatManager;
import com.distributed.server.AliveUserDatabase;
import com.distributed.server.DocumentDatabase;
import com.distributed.server.UserDatabase;
import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.Serializable;

/**
 * CommitParams.java
 * Commit parameters for distributed transactions.
 *
 * @version 2020-4-21
 */
public class CommitParams implements Serializable {
  private User user;
  private CommitEnum commitEnum;
  private RemoteInputStream inputStream;
  private byte[] bytes;

  private String docName;
  private int sectionNum;
  private String targetUser;
  private long multicastAddress;

  // 0: userDB, 1:aliveUserDB, 2:docDB
  //  private int DBCode;

  // in memory database
  private DocumentDatabase documentDatabase;
  private AliveUserDatabase aliveUserDatabase;
  private ChatManager chatManager;
  private UserDatabase userDatabase;

  public CommitParams() {
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public CommitEnum getCommitEnum() {
    return this.commitEnum;
  }

  public void setCommitEnum(CommitEnum commitEnum) {
    this.commitEnum = commitEnum;
  }

  public RemoteInputStream getInputStream() {
    return this.inputStream;
  }

  public void setInputStream(RemoteInputStream inputStream) {
    this.inputStream = inputStream;
  }

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

  public DocumentDatabase getDocumentDatabase() {
    return this.documentDatabase;
  }

  public void setDocumentDatabase(DocumentDatabase documentDatabase) {
    this.documentDatabase = documentDatabase;
  }

  public AliveUserDatabase getAliveUserDatabase() {
    return this.aliveUserDatabase;
  }

  public void setAliveUserDatabase(AliveUserDatabase aliveUserDatabase) {
    this.aliveUserDatabase = aliveUserDatabase;
  }

  public ChatManager getChatManager() {
    return this.chatManager;
  }

  public void setChatManager(ChatManager chatManager) {
    this.chatManager = chatManager;
  }

  public UserDatabase getUserDatabase() {
    return this.userDatabase;
  }

  public void setUserDatabase(UserDatabase userDatabase) {
    this.userDatabase = userDatabase;
  }

  public String getTargetUser() {
    return targetUser;
  }

  public void setTargetUser(String targetUser) {
    this.targetUser = targetUser;
  }

  public long getMulticastAddress() {
    return multicastAddress;
  }

  public void setMulticastAddress(long multicastAddress) {
    this.multicastAddress = multicastAddress;
  }
}
