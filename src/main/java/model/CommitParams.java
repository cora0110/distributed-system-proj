package model;

import java.io.FileInputStream;
import java.io.Serializable;

public class CommitParams implements Serializable {
    private User user;
    // 1 stands for put, 0 stands for delete, 2.1 stands for update occupant, 2.2 stands for update author
    int type;
    private FileInputStream fileInputStream;

    private String docNanme;

    private int sectionNum;

    // 0: userDB, 1:aliveUserDB, 2:docDB
    private int DBCode;

    public CommitParams(User user, int type, FileInputStream fileInputStream, String docNanme,
                        int sectionNum, int DBCode) {
        this.user = user;
        this.type = type;
        this.fileInputStream = fileInputStream;
        this.docNanme = docNanme;
        this.sectionNum = sectionNum;
        this.DBCode = DBCode;
    }
}
