package model;

import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.Serializable;

public class CommitParams implements Serializable {
  private User user;
  // 1 stands for put, 0 stands for delete,
  // 3 stands for update occupant, 4 stands for update author
  // 5 stands for update section
  private int type;
  private RemoteInputStream inputStream;

  private String docNanme;

  private int sectionNum;

  // 0: userDB, 1:aliveUserDB, 2:docDB
  private int DBCode;

  public CommitParams(User user, int type, RemoteInputStream inputStream, String docNanme, int sectionNum, int DBCode) {
    this.user = user;
    this.type = type;
    this.inputStream = inputStream;
    this.docNanme = docNanme;
    this.sectionNum = sectionNum;
    this.DBCode = DBCode;
  }
}
