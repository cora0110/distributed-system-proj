package model;

import chat.ChatManager;
import com.healthmarketscience.rmiio.RemoteInputStream;
import server.AliveUserDatabase;
import server.DocumentDatabase;
import server.UserDatabase;

import java.util.Map;

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

    public DocumentDatabase getDocumentDatabase() {
        return this.documentDatabase;
    }

    public UserDatabase getUserDatabase() {
        return this.userDatabase;
    }

    public AliveUserDatabase getAliveUserDatabase() {
        return this.aliveUserDatabase;
    }

    public ChatManager getChatManager() {
        return this.chatManager;
    }

    public Map<String, RemoteInputStream> getFileStreamMap() {
        return this.fileStreamMap;
    }

    public void setDocumentDatabase(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public void setUserDatabase(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public void setAliveUserDatabase(AliveUserDatabase aliveUserDatabase) {
        this.aliveUserDatabase = aliveUserDatabase;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    public void setFileStreamMap(Map<String, RemoteInputStream> fileStreamMap) {
        this.fileStreamMap = fileStreamMap;
    }
}
