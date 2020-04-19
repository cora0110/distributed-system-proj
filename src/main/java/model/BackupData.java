package model;

import chat.ChatManager;
import server.AliveUserDatabase;
import server.DocumentDatabase;
import server.UserDatabase;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackupData {
    DocumentDatabase documentDatabase;
    UserDatabase userDatabase;
    AliveUserDatabase aliveUserDatabase;
    ChatManager chatManager;
    Map<String, RemoteInputStream> fileStreamMap;

    public BackupData(DocumentDatabase documentDatabase, UserDatabase userDatabase, AliveUserDatabase aliveUserDatabase, ChatManager chatManager, Map<String, RemoteInputStream> fileStreamMap) {
        this.documentDatabase = documentDatabase;
        this.userDatabase = userDatabase;
        this.aliveUserDatabase = aliveUserDatabase;
        this.chatManager = chatManager;
        this.fileStreamMap = fileStreamMap;
    }
}
