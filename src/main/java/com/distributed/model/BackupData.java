package com.distributed.model;

import com.distributed.chat.ChatManager;
import com.distributed.server.AliveUserDatabase;
import com.distributed.server.DocumentDatabase;
import com.distributed.server.UserDatabase;
import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.Serializable;
import java.util.Map;

public class BackupData implements Serializable {
  private static final long serialVersionUID = 1L;
  DocumentDatabase documentDatabase;
  UserDatabase userDatabase;
  AliveUserDatabase aliveUserDatabase;
  ChatManager chatManager;
  Map<String, byte[]> fileStreamMap;

  public BackupData(DocumentDatabase documentDatabase, UserDatabase userDatabase, AliveUserDatabase aliveUserDatabase, ChatManager chatManager, Map<String, byte[]> fileStreamMap) {
    this.documentDatabase = documentDatabase;
    this.userDatabase = userDatabase;
    this.aliveUserDatabase = aliveUserDatabase;
    this.chatManager = chatManager;
    this.fileStreamMap = fileStreamMap;
  }

  public DocumentDatabase getDocumentDatabase() {
    return this.documentDatabase;
  }

  public void setDocumentDatabase(DocumentDatabase documentDatabase) {
    this.documentDatabase = documentDatabase;
  }

  public UserDatabase getUserDatabase() {
    return this.userDatabase;
  }

  public void setUserDatabase(UserDatabase userDatabase) {
    this.userDatabase = userDatabase;
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

  public Map<String, byte[]> getFileStreamMap() {
    return fileStreamMap;
  }

  public void setFileStreamMap(Map<String, byte[]> fileStreamMap) {
    this.fileStreamMap = fileStreamMap;
  }
}
