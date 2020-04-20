package model;

import chat.ChatManager;
import com.healthmarketscience.rmiio.RemoteInputStream;
import server.AliveUserDatabase;
import server.DocumentDatabase;
import server.UserDatabase;

import java.io.Serializable;

public class CommitParams implements Serializable {
  private User user;
  private CommitEnum commitEnum;
  private RemoteInputStream inputStream;

  private String docName;
  private int sectionNum;
  private String targetUser;

  // 0: userDB, 1:aliveUserDB, 2:docDB
//  private int DBCode;

  // in memory database
  private DocumentDatabase documentDatabase;
  private AliveUserDatabase aliveUserDatabase;
  private ChatManager chatManager;
  private UserDatabase userDatabase;

    public CommitParams() {
    }

    public User getUser() {
        return this.user;
    }

    public CommitEnum getCommitEnum() {
        return this.commitEnum;
    }

    public RemoteInputStream getInputStream() {
        return this.inputStream;
    }

    public String getDocName() {
        return this.docName;
    }

    public int getSectionNum() {
        return this.sectionNum;
    }

    public DocumentDatabase getDocumentDatabase() {
        return this.documentDatabase;
    }

    public AliveUserDatabase getAliveUserDatabase() {
        return this.aliveUserDatabase;
    }

    public ChatManager getChatManager() {
        return this.chatManager;
    }

    public UserDatabase getUserDatabase() {
        return this.userDatabase;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCommitEnum(CommitEnum commitEnum) {
        this.commitEnum = commitEnum;
    }

    public void setInputStream(RemoteInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public void setSectionNum(int sectionNum) {
        this.sectionNum = sectionNum;
    }

    public void setDocumentDatabase(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public void setAliveUserDatabase(AliveUserDatabase aliveUserDatabase) {
        this.aliveUserDatabase = aliveUserDatabase;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
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

}
