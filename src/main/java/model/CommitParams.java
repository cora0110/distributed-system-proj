package model;

import com.healthmarketscience.rmiio.RemoteInputStream;

import java.io.Serializable;

import chat.ChatManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import server.AliveUserDatabase;
import server.DocumentDatabase;
import server.UserDatabase;

@Getter
@Setter
@NoArgsConstructor
public class CommitParams implements Serializable {
  private User user;
  private CommitEnum commitEnum;
  private RemoteInputStream inputStream;

  private String docName;
  private int sectionNum;

  // 0: userDB, 1:aliveUserDB, 2:docDB
//  private int DBCode;

  // in memory database
  private DocumentDatabase documentDatabase;
  private AliveUserDatabase aliveUserDatabase;
  private ChatManager chatManager;
  private UserDatabase userDatabase;

//  public CommitParams(User user, int type, RemoteInputStream inputStream, String docNanme, int sectionNum, int DBCode) {
//    this.user = user;
//    this.type = type;
//    this.inputStream = inputStream;
//    this.docNanme = docNanme;
//    this.sectionNum = sectionNum;
//    this.DBCode = DBCode;
//  }

}
